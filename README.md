# Seedit Backend

모의 투자 학습 서비스 **Seedit**의 백엔드 레포지토리입니다.

사용자가 가상 잔액으로 종목을 모의 매매하고, 투자할 때 세운 **가설을 기록하고 나중에 복기**하면서 투자 습관을 기르는 것을 목표로 합니다. 투자 일기·레벨·AI 리포트 등 학습을 돕는 기능을 함께 제공합니다.

## 주요 기능

- 모의 매수 / 매도 및 거래 내역, 포트폴리오 평가
- 투자 가설 작성과 복기
- 투자 일기 작성 및 AI 피드백
- 레벨 · 포인트 성장 시스템
- 관심 종목, 금융 학습 콘텐츠
- AI 포트폴리오 / 데일리 경제 리포트, 단타 경고 알림

## 기술 스택

| 구분 | 사용 기술                 |
| --- |-----------------------|
| Language | Java                  |
| Framework | Spring Boot           |
| Persistence | MyBatis               |
| Database | MySQL                 |
| Auth | Spring Security + JWT |
| Build | Maven                 |

## 빠른 시작

```bash
# 1) 클론
git clone <backend-repo-url>
cd <backend-repo>

# 2) 깃 훅 활성화 (클론마다 1회)
git config core.hooksPath .githooks

# 3) 설정
#   src/main/resources/application.yml 에 DB 접속 정보 설정
#   민감 정보는 환경변수 또는 application-local.yml 로 분리

# 4) 빌드 & 실행
./mvnw clean package      # 빌드
./mvnw spring-boot:run    # 실행

```

서버 기동 확인:

```
GET /api/health
```

## 프로젝트 구조

```
com.seedit
├── SeeditApplication.java
│
├── global                       # 전 도메인이 공유하는 공통 레이어
│   ├── auth                     # 인증, 인가, 토큰, 회원가입 통합 관리 영역
│   │   ├── config
│   │   │   └── SecurityConfig.java         # Security 설정 
│   │   ├── controller
│   │   │   └── AuthController.java         # 로그인, 회원가입 API 통합
│   │   ├── service
│   │   │   ├── AuthService.java            # 로그인, 로그아웃, 토큰 발급 로직
│   │   │   └── CustomUserDetailsService.java
│   │   ├── security
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── CustomUserDetails.java
│   │   └── dto
│   │       ├── request
│   │       │   ├── SignUpRequest.java      # 회원가입 요청 DTO
│   │       │   └── LoginRequest.java       # 로그인 요청 DTO
│   │       └── response
│   │           └── LoginResponse.java      # 토큰 반환 DTO
│   │
│   ├── config
│   │   ├── WebConfig.java
│   │   ├── CorsConfig.java
│   │   └── AiConfig.java
│   ├── error
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   └── ErrorCode.java
│   ├── response
│   │   ├── ApiResponse.java
│   │   └── PageResponse.java
│   └── util
│       ├── JWTUtil.java
│       ├── DateUtils.java
│       └── MoneyUtils.java
│
├── feature                       # 기능 도메인 (controller/service/repository/domain)
│   ├── user                     # 프로필, 내 정보, 활동 요약
│   │   ├── controller/UserController.java
│   │   ├── service/{UserService, UserServiceImpl}.java
│   │   ├── repository/UserAccountRepository.java
│   │   ├── domain/UserAccount.java
│   │   └── dto/{UserProfileResponse, UserSummaryResponse}.java
│   │
│   ├── level                    # 레벨, 포인트, 다음 레벨 조건
│   │   ├── controller/LevelController.java
│   │   ├── service/LevelService.java
│   │   ├── repository/{UserLevelRepository, LevelDefinitionRepository}.java
│   │   ├── domain/{UserLevel, LevelDefinition}.java
│   │   └── dto/LevelResponse.java
│   │
│   ├── balance                  # 잔액 변동 이력
│   │   ├── controller/BalanceHistoryController.java
│   │   ├── service/BalanceHistoryService.java
│   │   ├── repository/BalanceHistoryRepository.java
│   │   ├── domain/BalanceHistory.java
│   │   └── dto/BalanceHistoryResponse.java
│   │
│   ├── stock                    # 종목 리스트/상세, 일별 시세
│   │   ├── controller/StockController.java
│   │   ├── service/StockService.java
│   │   ├── repository/{StockRepository, StockDetailRepository}.java
│   │   ├── domain/{Stock, StockDetail}.java
│   │   └── dto/{StockListResponse, StockDetailResponse, StockPriceResponse}.java
│   │
│   ├── trade                    # 모의 매수/매도, 거래 내역
│   │   ├── controller/TradeController.java
│   │   ├── service/TradeService.java
│   │   ├── repository/TradeRepository.java
│   │   ├── domain/Trade.java
│   │   └── dto/{BuyRequest, SellRequest, TradeResponse, TradeHistoryResponse}.java
│   │
│   ├── reason                   # 투자 가설 작성/목록, 복기
│   │   ├── controller/ReasonController.java
│   │   ├── service/ReasonService.java
│   │   ├── repository/ReasonRepository.java
│   │   ├── domain/Reason.java
│   │   └── dto/{ReasonCreateRequest, ReasonResponse, ReasonVerifyRequest}.java
│   │
│   ├── portfolio                # 보유 종목, 포트폴리오 평가
│   │   ├── controller/PortfolioController.java
│   │   ├── service/PortfolioService.java
│   │   ├── repository/PortfolioRepository.java
│   │   ├── domain/Portfolio.java
│   │   └── dto/{PortfolioResponse, HoldingResponse}.java
│   │
│   ├── watchlist                # 관심 종목 등록/조회/삭제
│   │   ├── controller/WatchlistController.java
│   │   ├── service/WatchlistService.java
│   │   ├── repository/WatchlistRepository.java
│   │   ├── domain/Watchlist.java
│   │   └── dto/WatchlistResponse.java
│   │
│   ├── diary                    # 투자 일기 + AI 피드백
│   │   ├── controller/DiaryController.java
│   │   ├── service/DiaryService.java
│   │   ├── repository/DiaryRepository.java
│   │   ├── domain/Diary.java
│   │   └── dto/{DiaryCreateRequest, DiaryUpdateRequest, DiaryResponse, DiaryAiFeedbackResponse}.java
│   │
│   ├── study                    # 금융 콘텐츠, 즐겨찾기
│   │   ├── controller/StudyController.java
│   │   ├── service/StudyService.java
│   │   ├── repository/{InvestmentStudyRepository, StudyBookmarkRepository}.java
│   │   ├── domain/{InvestmentStudy, StudyBookmark}.java
│   │   └── dto/{StudyListResponse, StudyDetailResponse, StudyBookmarkResponse}.java
│   │
│   ├── alert                    # 단타 경고 알림 (조회 전용, 저장 없음)
│   │   ├── controller/AlertController.java
│   │   ├── service/AlertService.java
│   │   └── dto/ShortTermTradingAlertResponse.java
│   │
│   └── report                   # AI 포트폴리오/데일리 리포트
│       ├── controller/ReportController.java
│       ├── service/{PortfolioReportService, DailyEconomicReportService}.java
│       ├── repository/AiDailyReportRepository.java
│       ├── domain/AiDailyReport.java
│       └── dto/{PortfolioReportResponse, DailyReportResponse, DailyReportRegenerateRequest}.java
│
└── external                     # 외부 연동 클라이언트
    ├── ai/{AiClient, AiPromptFactory, AiResponseParser}.java
    ├── rss/{HankyungRssClient, RssArticle, RssParser}.java
    └── stockapi/{KoreaInvestmentClient, StockApiResponse}.java
```
