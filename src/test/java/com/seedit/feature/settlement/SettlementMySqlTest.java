package com.seedit.feature.settlement;

import com.seedit.feature.balance.repository.BalanceHistoryRepository;
import com.seedit.feature.settlement.domain.Settlement;
import com.seedit.feature.settlement.domain.SettlementStatus;
import com.seedit.feature.settlement.repository.SettlementRepository;
import com.seedit.feature.settlement.service.*;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.feature.trade.service.TradeService;
import com.seedit.feature.trade.service.TradeServiceImpl;
import com.seedit.feature.trade.dto.request.TradeRequest;
import com.seedit.feature.trade.domain.TradeType;
import com.seedit.feature.trade.repository.TradeRepository;
import com.seedit.feature.stock.repository.StockRepository;
import com.seedit.feature.stock.domain.StockDetail;
import com.seedit.feature.level.service.LevelService;
import com.seedit.feature.level.repository.UserLevelRepository;
import com.seedit.feature.level.repository.LevelDefinitionRepository;
import com.seedit.feature.portfolio.repository.PortfolioRepository;
import com.seedit.feature.portfolio.service.PortfolioService;
import com.seedit.feature.reason.repository.ReasonRepository;
import com.seedit.feature.reason.service.ReasonService;
import com.seedit.feature.diary.repository.DiaryRepository;
import com.seedit.feature.watchlist.repository.WatchlistRepository;
import com.seedit.feature.study.repository.StudyRepository;
import com.seedit.feature.user.service.UserService;
import com.seedit.feature.user.service.UserServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 전용 폐기 가능한 MySQL DB만 사용한다. docs/settlement-operations.md 참고. */
@EnabledIfEnvironmentVariable(named = "SETTLEMENT_TEST_JDBC_URL", matches = ".+")
class SettlementMySqlTest {
    private AnnotationConfigApplicationContext context;
    private JdbcTemplate jdbc;
    private SettlementService service;
    private SettlementProcessor processor;
    private SettlementRepository settlements;
    private UserAccountRepository accounts;
    private MutableClock clock;
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 11);

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(Config.class);
        DataSource ds = context.getBean(DataSource.class);
        new ResourceDatabasePopulator(new ClassPathResource("settlement-schema.sql"),
                new ClassPathResource("db/migrations/20261111_settlement_retry.sql")).execute(ds);
        jdbc = new JdbcTemplate(ds);
        service = context.getBean(SettlementService.class);
        processor = context.getBean(SettlementProcessor.class);
        settlements = context.getBean(SettlementRepository.class);
        accounts = context.getBean(UserAccountRepository.class);
        clock = context.getBean(MutableClock.class);
        for (long id = 1; id <= 3; id++) {
            jdbc.update("INSERT INTO user_account(user_id,email,balance) VALUES (?,?,1000)", id, "user" + id);
        }
    }

    @AfterEach
    void close() { if (context != null) context.close(); }

    @Test
    void failureRollsBackOnlyOneItemAndKeepsUnpaidTotal() {
        long a = due(1, 100), b = due(2, 13), c = due(3, 200);
        service.settleDue(TODAY);
        assertThat(status(a)).isEqualTo("SETTLED");
        assertThat(status(b)).isEqualTo("FAILED");
        assertThat(status(c)).isEqualTo("SETTLED");
        assertThat(balance(1)).isEqualTo(1100);
        assertThat(balance(2)).isEqualTo(1000);
        assertThat(balance(3)).isEqualTo(1200);
        assertThat(countHistory()).isEqualTo(2);
        assertThat(settlements.sumPendingByUser(2L)).isEqualTo(13);
    }

    @Test
    void transientSqlFailureIsAutomaticallyRetriedInNewTransaction() {
        long id = due(1, 100);
        jdbc.execute("CREATE TRIGGER transient_test_failure BEFORE INSERT ON balance_history FOR EACH ROW "
                + "SIGNAL SQLSTATE '40001' SET MYSQL_ERRNO=1213, MESSAGE_TEXT='test transient failure'");
        service.settleDue(TODAY);
        assertThat(status(id)).isEqualTo("PENDING");
        assertThat(balance(1)).isEqualTo(1000);
        assertThat(countHistory()).isZero();
        jdbc.execute("DROP TRIGGER transient_test_failure");
        service.retryDue(TODAY);
        assertThat(attempts(id)).isEqualTo(1);
        clock.advance(Duration.ofMinutes(1));
        service.retryDue(TODAY);
        assertThat(status(id)).isEqualTo("SETTLED");
        assertThat(attempts(id)).isEqualTo(2);
        assertThat(balance(1)).isEqualTo(1100);
        assertThat(countHistory()).isEqualTo(1);
    }

    @Test
    void concurrentBatchesCreditExactlyOnce() throws Exception {
        long id = due(1, 100);
        concurrently(() -> service.settleDue(TODAY), () -> service.settleDue(TODAY));
        assertThat(balance(1)).isEqualTo(1100);
        assertThat(countHistory()).isEqualTo(1);
        assertThat(attempts(id)).isEqualTo(1);
    }

    @Test
    void multipleSettlementsForSameAccountPreserveBalanceHistory() throws Exception {
        long a = due(1, 100), b = due(1, 200);
        var first = processor.claim(a, 1L, TODAY);
        var second = processor.claim(b, 1L, TODAY);
        concurrently(() -> processor.settleOne(first), () -> processor.settleOne(second));
        assertThat(balance(1)).isEqualTo(1300);
        assertThat(countHistory()).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT MAX(current_balance) FROM balance_history", Long.class)).isEqualTo(1300);
    }

    @Test
    void retriesRespectBackoffAndStopAfterFourAttempts() {
        long id = due(1, 100);
        int[] delays = {1, 5, 15};
        for (int i = 0; i < 4; i++) {
            var attempt = processor.claim(id, 1L, TODAY);
            assertThat(attempt).isNotNull();
            processor.recordFailure(attempt, true, "TRANSIENT_LOCK_FAILURE");
            assertThat(attempts(id)).isEqualTo(i + 1);
            assertThat(processor.claim(id, 1L, TODAY)).isNull();
            if (i < 3) {
                assertThat(status(id)).isEqualTo("PENDING");
                assertThat(jdbc.queryForObject("SELECT next_retry_at FROM settlement WHERE settlement_id=?",
                        LocalDateTime.class, id)).isEqualTo(LocalDateTime.now(clock).plusMinutes(delays[i]));
                clock.advance(Duration.ofMinutes(delays[i]));
            }
        }
        assertThat(status(id)).isEqualTo("FAILED");
        clock.advance(Duration.ofDays(1));
        service.settleDue(TODAY.plusDays(1));
        assertThat(attempts(id)).isEqualTo(4);
        assertThat(balance(1)).isEqualTo(1000);
    }

    @Test
    void retryWorkerDoesNotRunInitialItemsOrFutureDatedItems() {
        long id = due(1, 100);
        clock.advance(Duration.ofMinutes(-1));
        service.retryDue(TODAY);
        assertThat(attempts(id)).isZero();
        assertThat(processor.claim(id, 1L, TODAY.minusDays(1))).isNull();
        clock.advance(Duration.ofMinutes(1));
        service.retryDue(TODAY); // 00:10 이후 미선점 건도 장애 복구 대상으로 회수
        assertThat(status(id)).isEqualTo("SETTLED");
    }

    @Test
    void expiredClaimRecoversWithoutReusingOldToken() {
        long id = due(1, 100);
        var old = processor.claim(id, 1L, TODAY);
        assertThat(processor.claim(id, 1L, TODAY)).isNull();
        clock.advance(Duration.ofMinutes(5));
        service.retryDue(TODAY); // 만료 복구, 1분 후 재시도 예약
        assertThat(attempts(id)).isEqualTo(1);
        assertThat(processor.settleOne(old)).isFalse();
        clock.advance(Duration.ofMinutes(1));
        service.retryDue(TODAY);
        assertThat(attempts(id)).isEqualTo(2);
        assertThat(balance(1)).isEqualTo(1100);
        processor.recordFailure(old, false, "STALE_FAILURE");
        assertThat(status(id)).isEqualTo("SETTLED");
    }

    @Test
    void expiredFourthAttemptBecomesFailed() {
        long id = due(1, 100);
        jdbc.update("UPDATE settlement SET attempt_count=3 WHERE settlement_id=?", id);
        processor.claim(id, 1L, TODAY);
        clock.advance(Duration.ofMinutes(5));
        service.retryDue(TODAY);
        assertThat(status(id)).isEqualTo("FAILED");
        assertThat(attempts(id)).isEqualTo(4);
    }

    @Test
    void staleFailureCannotOverwriteNewAttempt() {
        long id = due(1, 100);
        var old = processor.claim(id, 1L, TODAY);
        processor.recordFailure(old, true, "TRANSIENT_LOCK_FAILURE");
        clock.advance(Duration.ofMinutes(1));
        var current = processor.claim(id, 1L, TODAY);
        processor.recordFailure(old, false, "STALE_FAILURE");
        assertThat(processor.settleOne(current)).isTrue();
        assertThat(processor.settleOne(current)).isFalse();
        assertThat(countHistory()).isEqualTo(1);
    }

    @Test
    void nullBalanceAndOverflowNeverCredit() {
        long a = due(1, 100), b = due(2, 100);
        jdbc.update("UPDATE user_account SET balance=NULL WHERE user_id=1");
        jdbc.update("UPDATE user_account SET balance=? WHERE user_id=2", Long.MAX_VALUE);
        service.settleDue(TODAY);
        assertThat(status(a)).isEqualTo("FAILED");
        assertThat(status(b)).isEqualTo("FAILED");
        assertThat(countHistory()).isZero();
    }

    @Test
    void deletedReservationCannotBePaid() {
        long id = due(1, 100);
        var attempt = processor.claim(id, 1L, TODAY);
        transaction().executeWithoutResult(tx -> {
            accounts.findUserByIdForUpdate(1L);
            settlements.deleteByUserId(1L);
            accounts.updateBalance(1L, 5000000L);
        });
        assertThat(processor.settleOne(attempt)).isFalse();
        assertThat(balance(1)).isEqualTo(5000000);
    }

    @Test
    void debitWaitingOnAccountLockUsesCommittedBalance() throws Exception {
        long id = due(1, 100);
        var attempt = processor.claim(id, 1L, TODAY);
        concurrently(() -> processor.settleOne(attempt), () -> context.getBean(TradeService.class)
                .processOrder("user1", new TradeRequest("TEST", 1, TradeType.BUY, "tag", "reason", null)));
        assertThat(balance(1)).isEqualTo(800);
    }

    @Test
    void actualAccountResetAndSettlementUseSameLockOrder() throws Exception {
        long id = due(1, 100);
        var attempt = processor.claim(id, 1L, TODAY);
        concurrently(() -> processor.settleOne(attempt), () -> context.getBean(UserService.class).resetUser("user1"));
        assertThat(balance(1)).isEqualTo(5000000);
        assertThat(processor.settleOne(attempt)).isFalse();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM settlement", Integer.class)).isZero();
    }

    @Test
    void restartedContextKeepsRetryCountAndSchedule() {
        long id = due(1, 100);
        var attempt = processor.claim(id, 1L, TODAY);
        processor.recordFailure(attempt, true, "TRANSIENT_LOCK_FAILURE");
        context.close();
        context = new AnnotationConfigApplicationContext(Config.class);
        jdbc = new JdbcTemplate(context.getBean(DataSource.class));
        var restarted = context.getBean(SettlementService.class);
        restarted.retryDue(TODAY);
        assertThat(attempts(id)).isEqualTo(1);
        assertThat(status(id)).isEqualTo("PENDING");
        context.getBean(MutableClock.class).advance(Duration.ofMinutes(1));
        restarted.retryDue(TODAY);
        assertThat(attempts(id)).isEqualTo(2);
        assertThat(balance(1)).isEqualTo(1100);
    }

    @Test
    void reservationParticipatesInCallerTransaction() {
        transaction().executeWithoutResult(tx -> {
            service.reserve(1L, 1L, 100, TODAY);
            tx.setRollbackOnly();
        });
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM settlement", Integer.class)).isZero();
    }

    @Test
    void keysetPaginationDoesNotSkipItemsAfterStatusChanges() {
        for (int i = 0; i < 205; i++) due(1, 1);
        service.settleDue(TODAY);
        assertThat(balance(1)).isEqualTo(1205);
        assertThat(countHistory()).isEqualTo(205);
    }

    private TransactionTemplate transaction() {
        return new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
    }

    private long due(long userId, long amount) {
        Settlement s = Settlement.builder().userId(userId).amount(amount).tradeDate(TODAY.minusDays(2))
                .settleDate(TODAY).status(SettlementStatus.PENDING).createdAt(LocalDateTime.now(clock)).build();
        settlements.save(s);
        return s.getSettlementId();
    }

    private long balance(long id) { return jdbc.queryForObject("SELECT balance FROM user_account WHERE user_id=?", Long.class, id); }
    private int attempts(long id) { return jdbc.queryForObject("SELECT attempt_count FROM settlement WHERE settlement_id=?", Integer.class, id); }
    private String status(long id) { return jdbc.queryForObject("SELECT status FROM settlement WHERE settlement_id=?", String.class, id); }
    private int countHistory() { return jdbc.queryForObject("SELECT COUNT(*) FROM balance_history", Integer.class); }

    private void concurrently(Runnable first, Runnable second) throws Exception {
        try (var pool = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            Future<?> a = pool.submit(() -> { await(start); first.run(); });
            Future<?> b = pool.submit(() -> { await(start); second.run(); });
            start.countDown();
            a.get(15, TimeUnit.SECONDS);
            b.get(15, TimeUnit.SECONDS);
        }
    }

    private static void await(CountDownLatch latch) {
        try { if (!latch.await(10, TimeUnit.SECONDS)) throw new AssertionError("Latch timed out"); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
    }

    static class MutableClock extends Clock {
        private final AtomicReference<Instant> time = new AtomicReference<>(Instant.parse("2026-11-10T15:10:00Z"));
        void advance(Duration duration) { time.updateAndGet(t -> t.plus(duration)); }
        @Override public ZoneId getZone() { return ZoneId.of("Asia/Seoul"); }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(instant(), zone); }
        @Override public Instant instant() { return time.get(); }
    }

    @Configuration
    @EnableTransactionManagement
    @Import({SettlementProcessor.class, SettlementServiceImpl.class})
    static class Config {
        @Bean TradeService tradeService(UserAccountRepository accounts, SettlementService settlements,
                                       BalanceHistoryRepository histories) {
            StockDetail stock = mock(StockDetail.class);
            when(stock.getSid()).thenReturn(1L);
            when(stock.getSdid()).thenReturn(1L);
            when(stock.getCurrentPrice()).thenReturn(300L);
            StockRepository stocks = mock(StockRepository.class);
            when(stocks.findDetailByTicker("TEST")).thenReturn(stock);
            return new TradeServiceImpl(mock(TradeRepository.class), stocks, accounts, settlements,
                    mock(LevelService.class), histories, mock(PortfolioRepository.class),
                    mock(ReasonRepository.class), mock(ReasonService.class));
        }
        @Bean UserService userService(UserAccountRepository accounts, SettlementRepository settlements,
                                     BalanceHistoryRepository histories) {
            return new UserServiceImpl(accounts, mock(PasswordEncoder.class), mock(UserLevelRepository.class),
                    mock(LevelDefinitionRepository.class), histories, mock(TradeRepository.class),
                    mock(DiaryRepository.class), mock(WatchlistRepository.class), mock(StudyRepository.class),
                    mock(LevelService.class), mock(PortfolioService.class), mock(PortfolioRepository.class),
                    settlements, mock(ReasonRepository.class));
        }
        @Bean MutableClock settlementClock() { return new MutableClock(); }
        @Bean(destroyMethod = "close") HikariDataSource dataSource() {
            String url = System.getenv("SETTLEMENT_TEST_JDBC_URL");
            if (!url.matches("jdbc:mysql://[^/]+/seedit_settlement_test(?:\\?.*)?")) {
                throw new IllegalArgumentException("Only disposable seedit_settlement_test database is allowed");
            }
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(System.getenv().getOrDefault("SETTLEMENT_TEST_DB_USER", "root"));
            config.setPassword(System.getenv().getOrDefault("SETTLEMENT_TEST_DB_PASSWORD", ""));
            config.setMaximumPoolSize(4);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(5000);
            return new HikariDataSource(config);
        }
        @Bean PlatformTransactionManager transactionManager(DataSource ds) { return new DataSourceTransactionManager(ds); }
        @Bean SqlSessionFactory sqlSessionFactory(DataSource ds) throws Exception {
            SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
            factory.setDataSource(ds);
            factory.setTypeAliasesPackage("com.seedit.feature");
            var resolver = new PathMatchingResourcePatternResolver();
            factory.setMapperLocations(resolver.getResources("classpath:mapper/feature/**/*.xml"));
            var config = new org.apache.ibatis.session.Configuration();
            config.setMapUnderscoreToCamelCase(true);
            factory.setConfiguration(config);
            return factory.getObject();
        }
        @Bean MapperFactoryBean<SettlementRepository> settlements(SqlSessionFactory factory) {
            return mapper(SettlementRepository.class, factory);
        }
        @Bean MapperFactoryBean<UserAccountRepository> accounts(SqlSessionFactory factory) {
            return mapper(UserAccountRepository.class, factory);
        }
        @Bean MapperFactoryBean<BalanceHistoryRepository> histories(SqlSessionFactory factory) {
            return mapper(BalanceHistoryRepository.class, factory);
        }
        private <T> MapperFactoryBean<T> mapper(Class<T> type, SqlSessionFactory factory) {
            MapperFactoryBean<T> bean = new MapperFactoryBean<>(type);
            bean.setSqlSessionFactory(factory);
            return bean;
        }
    }
}
