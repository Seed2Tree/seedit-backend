package com.seedit.feature.stock;

import com.seedit.feature.stock.dto.StockSyncResponse;
import com.seedit.feature.stock.external.DailyCandle;
import com.seedit.feature.stock.external.StockPriceProvider;
import com.seedit.feature.stock.repository.StockRepository;
import com.seedit.feature.stock.service.StockHistoryReplacer;
import com.seedit.feature.stock.service.StockPriceSyncService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** SEED-111: 백필의 종목별 교체가 실제 MySQL에서 원자적인지 확인. 폐기 가능한 seedit_settlement_test DB만 사용한다. */
@EnabledIfEnvironmentVariable(named = "SETTLEMENT_TEST_JDBC_URL", matches = ".+")
class StockBackfillMySqlTest {
    private static final LocalDate START = LocalDate.of(2020, 1, 1);
    private static final Long FAIL_SID = 1L;

    private AnnotationConfigApplicationContext context;
    private JdbcTemplate jdbc;
    private StockPriceProvider provider;
    private StockPriceSyncService service;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(Config.class);
        DataSource ds = context.getBean(DataSource.class);
        new ResourceDatabasePopulator(new ClassPathResource("stock-schema.sql")).execute(ds);
        jdbc = new JdbcTemplate(ds);
        provider = context.getBean(StockPriceProvider.class);
        service = context.getBean(StockPriceSyncService.class);
        insertStockWithHistory(1L, "AAA", 3);
        insertStockWithHistory(2L, "BBB", 3);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void secondChunkFailureKeepsExistingHistory() {
        when(provider.fetchDailyHistory(eq("AAA"), anyInt())).thenReturn(candles(501));
        when(provider.fetchDailyHistory(eq("BBB"), anyInt())).thenReturn(candles(3));

        StockSyncResponse result = service.backfillAll(1300);

        // AAA의 두 번째 청크(501번째 캔들) INSERT 실패 → 삭제·첫 청크 삽입이 모두 롤백돼야 한다
        assertThat(openPrices(1L)).containsExactly(1000L, 1001L, 1002L);
        assertThat(result.getFailedTickers()).singleElement().asString().startsWith("AAA");
    }

    @Test
    void failureOfOneStockDoesNotAffectNext() {
        when(provider.fetchDailyHistory(eq("AAA"), anyInt())).thenReturn(candles(501));
        when(provider.fetchDailyHistory(eq("BBB"), anyInt())).thenReturn(candles(3));

        StockSyncResponse result = service.backfillAll(1300);

        assertThat(openPrices(1L)).containsExactly(1000L, 1001L, 1002L);
        assertThat(openPrices(2L)).containsExactly(5000L, 5001L, 5002L);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getSuccess()).isEqualTo(1);
        assertThat(result.getFailed()).isEqualTo(1);
    }

    /** count개의 정상 과거 캔들 (500건 청크 기준 count>500이면 두 번째 청크가 생긴다) */
    private static List<DailyCandle> candles(int count) {
        List<DailyCandle> candles = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            long price = 5000L + i - 1;
            candles.add(DailyCandle.builder()
                    .tradeDate(START.plusDays(i - 1))
                    .open(price)
                    .close(price).high(price).low(price).volume(1L).tradingValue(price)
                    .build());
        }
        return candles;
    }

    private void insertStockWithHistory(long sid, String ticker, int rows) {
        jdbc.update("INSERT INTO stock(sid, company_name, ticker) VALUES (?, ?, ?)", sid, ticker, ticker);
        for (int i = 0; i < rows; i++) {
            long price = sid * 1000 + i;
            jdbc.update("INSERT INTO stock_detail(sid, open_price, current_price, trade_date) VALUES (?, ?, ?, ?)",
                    sid, price, price, START.minusYears(1).plusDays(i));
        }
    }

    private List<Long> openPrices(long sid) {
        return jdbc.queryForList("SELECT open_price FROM stock_detail WHERE sid = ? ORDER BY trade_date",
                Long.class, sid);
    }

    @Configuration
    @EnableTransactionManagement
    @Import({StockPriceSyncService.class, StockHistoryReplacer.class})
    static class Config {
        @Bean StockPriceProvider stockPriceProvider() { return mock(StockPriceProvider.class); }
        @Bean(destroyMethod = "close") HikariDataSource dataSource() {
            String url = System.getenv("SETTLEMENT_TEST_JDBC_URL");
            if (!url.matches("jdbc:mysql://[^/]+/seedit_settlement_test(?:\\?.*)?")) {
                throw new IllegalArgumentException("Only disposable seedit_settlement_test database is allowed");
            }
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(System.getenv().getOrDefault("SETTLEMENT_TEST_DB_USER", "root"));
            config.setPassword(System.getenv().getOrDefault("SETTLEMENT_TEST_DB_PASSWORD", ""));
            config.setMaximumPoolSize(2);
            return new HikariDataSource(config);
        }
        @Bean PlatformTransactionManager transactionManager(DataSource ds) { return new DataSourceTransactionManager(ds); }
        @Bean SqlSessionFactory sqlSessionFactory(DataSource ds) throws Exception {
            SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
            factory.setDataSource(ds);
            factory.setTypeAliasesPackage("com.seedit.feature");
            factory.setMapperLocations(new PathMatchingResourcePatternResolver()
                    .getResources("classpath:mapper/feature/**/*.xml"));
            var config = new org.apache.ibatis.session.Configuration();
            config.setMapUnderscoreToCamelCase(true);
            factory.setConfiguration(config);
            return factory.getObject();
        }
        /**
         * 실제 MyBatis 매퍼(Spring 트랜잭션 참여)를 감싸, FAIL_SID 종목의 두 번째 청크 INSERT에서만 실패를 주입한다.
         * (매퍼가 INSERT IGNORE라 제약 위반 데이터로는 실패가 나지 않는다)
         */
        @Bean StockRepository stockRepository(SqlSessionFactory factory) {
            StockRepository real = new SqlSessionTemplate(factory).getMapper(StockRepository.class);
            AtomicInteger chunks = new AtomicInteger();
            return (StockRepository) Proxy.newProxyInstance(StockRepository.class.getClassLoader(),
                    new Class<?>[]{StockRepository.class}, (proxy, method, args) -> {
                        if (method.getName().equals("insertHistoryBatch") && FAIL_SID.equals(args[0])
                                && chunks.incrementAndGet() == 2) {
                            throw new IllegalStateException("주입된 두 번째 청크 실패");
                        }
                        try {
                            return method.invoke(real, args);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    });
        }
    }
}
