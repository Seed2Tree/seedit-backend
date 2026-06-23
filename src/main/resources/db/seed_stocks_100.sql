-- ================================================================
-- SEED-75: 투자종목 100개 확장 (KOSPI 시총 상위 기준)
-- 실행 방법: MySQL 클라이언트 또는 DBeaver에서 직접 실행
-- ================================================================

-- Step 1: search_keywords 컬럼 추가
ALTER TABLE stock
  ADD COLUMN search_keywords VARCHAR(200) NULL COMMENT '검색 키워드 (별칭, 한국어 표기)';

-- Step 2: 100개 종목 삽입/갱신
-- ticker 중복 시 company_name·sector·search_keywords만 갱신 (description·market_cap 등 기존 값 유지)
INSERT INTO stock (company_name, ticker, market, sector, search_keywords)
VALUES
  ('SK하이닉스',               '000660', 'KOSPI', '반도체',        '에스케이하이닉스'),
  ('삼성전자',                 '005930', 'KOSPI', '반도체',        '갤럭시'),
  ('SK스퀘어',                 '402340', 'KOSPI', '지주',          '에스케이스퀘어'),
  ('삼성전기',                 '009150', 'KOSPI', '전자부품',      NULL),
  ('현대차',                   '005380', 'KOSPI', '자동차',        NULL),
  ('삼성생명',                 '032830', 'KOSPI', '금융',          NULL),
  ('LG에너지솔루션',            '373220', 'KOSPI', '배터리',        '엘지에너지솔루션'),
  ('삼성물산',                 '028260', 'KOSPI', '건설',          NULL),
  ('HD현대중공업',              '329180', 'KOSPI', '조선',         '현대중공업'),
  ('두산에너빌리티',             '034020', 'KOSPI', '방산',         NULL),
  ('삼성바이오로직스',           '207940', 'KOSPI', '바이오',        '삼바'),
  ('한화에어로스페이스',          '012450', 'KOSPI', '방산',         NULL),
  ('KB금융',                   '105560', 'KOSPI', '금융',          NULL),
  ('기아',                     '000270', 'KOSPI', '자동차',        NULL),
  ('SK',                       '034730', 'KOSPI', '지주',          '에스케이'),
  ('현대모비스',                '012330', 'KOSPI', '자동차부품',    NULL),
  ('신한지주',                 '055550', 'KOSPI', '금융',          NULL),
  ('삼성SDI',                  '006400', 'KOSPI', '배터리',        NULL),
  ('LS ELECTRIC',              '010120', 'KOSPI', '전력장비',      'LS일렉트릭,엘에스일렉트릭'),
  ('HD현대일렉트릭',             '267260', 'KOSPI', '전력장비',     '현대일렉트릭'),
  ('셀트리온',                 '068270', 'KOSPI', '바이오',        NULL),
  ('효성중공업',                '298040', 'KOSPI', '전력장비',      NULL),
  ('LG전자',                   '066570', 'KOSPI', '전자',         '엘지전자'),
  ('한화오션',                 '042660', 'KOSPI', '조선',          NULL),
  ('하나금융지주',              '086790', 'KOSPI', '금융',          NULL),
  ('NAVER',                    '035420', 'KOSPI', 'IT',           '네이버'),
  ('삼성화재',                 '000810', 'KOSPI', '금융',          NULL),
  ('HD한국조선해양',             '009540', 'KOSPI', '조선',        '현대조선'),
  ('두산',                     '000150', 'KOSPI', '지주',          NULL),
  ('한미반도체',                '042700', 'KOSPI', '반도체장비',    NULL),
  ('POSCO홀딩스',              '005490', 'KOSPI', '철강',         '포스코'),
  ('미래에셋증권',              '006800', 'KOSPI', '금융',          NULL),
  ('LG이노텍',                 '011070', 'KOSPI', '전자부품',      '엘지이노텍'),
  ('한국전력',                 '015760', 'KOSPI', '전력',         '한전'),
  ('고려아연',                 '010130', 'KOSPI', '비철금속',      NULL),
  ('삼성중공업',                '010140', 'KOSPI', '조선',         NULL),
  ('우리금융지주',              '316140', 'KOSPI', '금융',          NULL),
  ('현대로템',                 '064350', 'KOSPI', '방산',          NULL),
  ('LG화학',                   '051910', 'KOSPI', '화학',          '엘지화학'),
  ('SK텔레콤',                 '017670', 'KOSPI', '통신',         'SKT,에스케이텔레콤'),
  ('LIG디펜스앤에어로스페이스',   '079550', 'KOSPI', '방산',        'LIG넥스원'),
  ('HMM',                      '011200', 'KOSPI', '해운',         '현대상선'),
  ('메리츠금융지주',             '138040', 'KOSPI', '금융',         NULL),
  ('KT&G',                     '033780', 'KOSPI', '소비재',       '케이티앤지'),
  ('HD현대',                   '267250', 'KOSPI', '지주',         '현대'),
  ('SK이노베이션',              '096770', 'KOSPI', '에너지',       '에스케이이노베이션'),
  ('포스코퓨처엠',              '003670', 'KOSPI', '배터리소재',   NULL),
  ('기업은행',                 '024110', 'KOSPI', '금융',          NULL),
  ('한화시스템',                '272210', 'KOSPI', '방산',          NULL),
  ('LG',                       '003550', 'KOSPI', '지주',         '엘지'),
  ('현대오토에버',              '307950', 'KOSPI', 'IT',           NULL),
  ('카카오',                   '035720', 'KOSPI', 'IT',           '다음,카카오톡'),
  ('삼성에스디에스',             '018260', 'KOSPI', 'IT',          '삼성SDS'),
  ('현대글로비스',              '086280', 'KOSPI', '물류',          NULL),
  ('한국항공우주',              '047810', 'KOSPI', '방산',         'KAI'),
  ('에이피알',                 '278470', 'KOSPI', '소비재',        'APR'),
  ('현대건설',                 '000720', 'KOSPI', '건설',          NULL),
  ('KT',                       '030200', 'KOSPI', '통신',         '케이티'),
  ('LS',                       '006260', 'KOSPI', '지주',         '엘에스'),
  ('한국금융지주',              '071050', 'KOSPI', '금융',          NULL),
  ('S-Oil',                    '010950', 'KOSPI', '정유',         '에스오일'),
  ('NH투자증권',                '005940', 'KOSPI', '금융',          '엔에이치투자증권'),
  ('크래프톤',                 '259960', 'KOSPI', '게임',         '배틀그라운드,PUBG'),
  ('삼성증권',                 '016360', 'KOSPI', '금융',          NULL),
  ('카카오뱅크',                '323410', 'KOSPI', '금융',          NULL),
  ('삼성에피스홀딩스',           '0126Z0', 'KOSPI', '바이오',       '에피스'),
  ('대한항공',                 '003490', 'KOSPI', '항공',          NULL),
  ('DB손해보험',                '005830', 'KOSPI', '금융',          '디비손해보험'),
  ('HD현대마린솔루션',           '443060', 'KOSPI', '조선',        '현대마린솔루션'),
  ('삼성E&A',                  '028050', 'KOSPI', '건설',          NULL),
  ('포스코인터내셔널',           '047050', 'KOSPI', '상사',          NULL),
  ('키움증권',                 '039490', 'KOSPI', '금융',           NULL),
  ('하이브',                   '352820', 'KOSPI', '엔터테인먼트',  'HYBE'),
  ('이수페타시스',              '007660', 'KOSPI', '반도체장비',    NULL),
  ('대우건설',                 '047040', 'KOSPI', '건설',           NULL),
  ('LG씨엔에스',                '064400', 'KOSPI', 'IT',           'LG CNS,엘지씨엔에스'),
  ('한화',                     '000880', 'KOSPI', '지주',           NULL),
  ('삼양식품',                 '003230', 'KOSPI', '식품',          '불닭'),
  ('한국타이어앤테크놀로지',      '161390', 'KOSPI', '자동차부품',  '한국타이어'),
  ('한진칼',                   '180640', 'KOSPI', '지주',           NULL),
  ('대한전선',                 '001440', 'KOSPI', '전선',           NULL),
  ('DB하이텍',                 '000990', 'KOSPI', '반도체',         '디비하이텍'),
  ('대덕전자',                 '353200', 'KOSPI', '전자부품',       NULL),
  ('산일전기',                 '062040', 'KOSPI', '전력장비',       NULL),
  ('신세계',                   '004170', 'KOSPI', '유통',           NULL),
  ('SK바이오팜',                '326030', 'KOSPI', '바이오',         '에스케이바이오팜'),
  ('LG디스플레이',              '034220', 'KOSPI', '디스플레이',    'LGD,엘지디스플레이'),
  ('한화솔루션',                '009830', 'KOSPI', '화학',           NULL),
  ('GS',                       '078930', 'KOSPI', '지주',           '지에스'),
  ('두산로보틱스',              '454910', 'KOSPI', '로봇',           NULL),
  ('LG유플러스',                '032640', 'KOSPI', '통신',          'LGU+,엘지유플러스'),
  ('HD건설기계',                '267270', 'KOSPI', '기계',          '현대건설기계'),
  ('두산밥캣',                 '241560', 'KOSPI', '기계',           NULL),
  ('코웨이',                   '021240', 'KOSPI', '소비재',          NULL),
  ('가온전선',                 '000500', 'KOSPI', '전선',            NULL),
  ('아모레퍼시픽',              '090430', 'KOSPI', '소비재',          NULL),
  ('유한양행',                 '000100', 'KOSPI', '제약',            NULL),
  ('SKC',                      '011790', 'KOSPI', '화학',            '에스케이씨'),
  ('카카오페이',                '377300', 'KOSPI', '금융',            NULL),
  ('엔씨소프트',                '036570', 'KOSPI', '게임',           'NC')
ON DUPLICATE KEY UPDATE
  company_name    = VALUES(company_name),
  sector          = VALUES(sector),
  search_keywords = VALUES(search_keywords);

-- Step 3: 90개 종목 description 채우기 (mock_data.sql의 10개 제외)
UPDATE stock SET description = 'SK그룹의 ICT 지주회사로 SK하이닉스·SK텔레콤 등 계열사 지분을 보유한 투자형 지주회사예요.' WHERE ticker = '402340';
UPDATE stock SET description = '스마트폰용 적층세라믹콘덴서(MLCC)와 카메라 모듈을 만드는 삼성그룹 전자부품 계열사예요.' WHERE ticker = '009150';
UPDATE stock SET description = '국내 1위 생명보험사로 삼성그룹 금융 계열의 핵심 기업이에요.' WHERE ticker = '032830';
UPDATE stock SET description = '건설·상사·패션·리조트 등 다양한 사업을 운영하는 삼성그룹 최상위 지주사 역할을 하는 복합기업이에요.' WHERE ticker = '028260';
UPDATE stock SET description = '세계 최대 규모의 조선소를 보유한 조선 기업으로 LNG선·컨테이너선을 주로 만들어요.' WHERE ticker = '329180';
UPDATE stock SET description = '원자력·가스터빈·풍력 등 에너지 설비를 만드는 종합 에너지 기업이에요.' WHERE ticker = '034020';
UPDATE stock SET description = '항공기 엔진·방산 장비를 만들고 한화그룹의 방산 사업을 이끄는 핵심 계열사예요.' WHERE ticker = '012450';
UPDATE stock SET description = 'KB국민은행·카드·증권·보험 등을 거느린 국내 1위 금융지주사예요.' WHERE ticker = '105560';
UPDATE stock SET description = 'SK그룹의 중간 지주사로 SK이노베이션·SK텔레콤 등 주요 계열사 지분을 보유해요.' WHERE ticker = '034730';
UPDATE stock SET description = '현대·기아차에 핵심 부품을 공급하고 AS 부품 사업을 운영하는 자동차 부품 기업이에요.' WHERE ticker = '012330';
UPDATE stock SET description = '신한은행·카드·증권 등을 거느린 대형 금융지주사로 리딩금융그룹을 지향해요.' WHERE ticker = '055550';
UPDATE stock SET description = '전기차·ESS용 배터리와 반도체 소재를 만드는 삼성그룹 에너지 소재 계열사예요.' WHERE ticker = '006400';
UPDATE stock SET description = '변압기·차단기 등 전력 인프라 장비를 만드는 국내 1위 전력기기 기업이에요.' WHERE ticker = '010120';
UPDATE stock SET description = '초고압 변압기·차단기 등 전력기기를 만들어 북미 시장에서 높은 점유율을 가지고 있어요.' WHERE ticker = '267260';
UPDATE stock SET description = '바이오시밀러(복제 바이오의약품)를 개발·생산하는 국내 대표 바이오 기업이에요.' WHERE ticker = '068270';
UPDATE stock SET description = '초고압 변압기·차단기를 만드는 전력기기 기업으로 미국 인프라 투자 수혜를 받고 있어요.' WHERE ticker = '298040';
UPDATE stock SET description = '가전·TV·전장(자동차 부품)·B2B 솔루션을 운영하는 LG그룹의 대표 전자 계열사예요.' WHERE ticker = '066570';
UPDATE stock SET description = 'LNG선·군함·잠수함을 건조하는 조선 기업으로 방산 수출에도 적극 나서고 있어요.' WHERE ticker = '042660';
UPDATE stock SET description = '하나은행·증권·카드 등을 거느린 대형 금융지주사예요.' WHERE ticker = '086790';
UPDATE stock SET description = '국내 1위 손해보험사로 자동차·화재·건강 보험을 운영해요.' WHERE ticker = '000810';
UPDATE stock SET description = 'HD현대중공업·현대미포조선 등을 거느린 HD현대그룹의 조선 중간 지주사예요.' WHERE ticker = '009540';
UPDATE stock SET description = '두산그룹의 지주사로 두산에너빌리티·두산밥캣 등 다양한 계열사를 보유해요.' WHERE ticker = '000150';
UPDATE stock SET description = '반도체 패키징 장비(TC본더)를 만드는 기업으로 HBM 시장 확대 최대 수혜주 중 하나예요.' WHERE ticker = '042700';
UPDATE stock SET description = '국내 1위 증권사로 자산운용·투자은행·해외 사업 등 다양한 금융 서비스를 제공해요.' WHERE ticker = '006800';
UPDATE stock SET description = '애플 아이폰용 카메라 모듈과 전장 부품을 만드는 LG그룹 전자부품 계열사예요.' WHERE ticker = '011070';
UPDATE stock SET description = '국내 전력 생산·송배전·판매를 독점하는 국영 에너지 기업이에요.' WHERE ticker = '015760';
UPDATE stock SET description = '아연·연·금·은 등 비철금속을 제련하는 세계 1위 아연 제련사예요.' WHERE ticker = '010130';
UPDATE stock SET description = 'LNG선·드릴십 등 고부가가치 선박을 만드는 삼성그룹 조선 계열사예요.' WHERE ticker = '010140';
UPDATE stock SET description = '우리은행·카드·증권 등을 거느린 금융지주사예요.' WHERE ticker = '316140';
UPDATE stock SET description = 'KTX·고속철도 등 철도차량과 K2전차 등 방산 장비를 만드는 기업이에요.' WHERE ticker = '064350';
UPDATE stock SET description = '국내 1위 이동통신사로 5G·AI·클라우드 서비스를 운영해요.' WHERE ticker = '017670';
UPDATE stock SET description = '천무·철매 등 유도무기 체계를 만드는 국내 대표 방위산업체예요.' WHERE ticker = '079550';
UPDATE stock SET description = '컨테이너 해운·벌크 운송을 운영하는 국내 최대 해운사예요.' WHERE ticker = '011200';
UPDATE stock SET description = '메리츠화재·증권을 거느린 금융지주사로 공격적 성장 전략으로 주목받고 있어요.' WHERE ticker = '138040';
UPDATE stock SET description = '담배·인삼(정관장)·부동산 사업을 운영하는 복합 소비재 기업이에요.' WHERE ticker = '033780';
UPDATE stock SET description = 'HD현대중공업·HD현대일렉트릭 등 조선·에너지 계열사를 거느린 HD현대그룹 지주사예요.' WHERE ticker = '267250';
UPDATE stock SET description = '정유·배터리(SK온)·화학 사업을 운영하는 SK그룹의 에너지·화학 계열사예요.' WHERE ticker = '096770';
UPDATE stock SET description = '2차전지 양극재·음극재를 만드는 배터리 소재 기업으로 POSCO홀딩스 계열사예요.' WHERE ticker = '003670';
UPDATE stock SET description = '중소기업 금융 지원을 전문으로 하는 국책은행이에요.' WHERE ticker = '024110';
UPDATE stock SET description = '방산 전자장비·위성통신·ICT 서비스를 운영하는 한화그룹 방산 계열사예요.' WHERE ticker = '272210';
UPDATE stock SET description = 'LG전자·LG화학·LG유플러스 등 LG그룹 계열사를 거느린 최상위 지주사예요.' WHERE ticker = '003550';
UPDATE stock SET description = '현대차그룹의 IT 서비스·소프트웨어·커넥티드카 플랫폼을 운영하는 IT 계열사예요.' WHERE ticker = '307950';
UPDATE stock SET description = '삼성그룹의 IT 서비스·물류 플랫폼(첼로)을 운영하는 IT 서비스 기업이에요.' WHERE ticker = '018260';
UPDATE stock SET description = '현대·기아차 물류와 해운·무역 사업을 운영하는 현대차그룹 물류 계열사예요.' WHERE ticker = '086280';
UPDATE stock SET description = 'FA-50·수리온 등 항공기를 개발·생산하는 국내 유일의 완성 항공기 제조사예요.' WHERE ticker = '047810';
UPDATE stock SET description = '에이프릴스킨 등 K뷰티 브랜드를 운영하는 화장품 기업이에요.' WHERE ticker = '278470';
UPDATE stock SET description = '국내 1위 건설사로 해외 플랜트·인프라·주택 사업을 운영해요.' WHERE ticker = '000720';
UPDATE stock SET description = '유무선 통신·IPTV·클라우드 서비스를 운영하는 국내 대표 통신사예요.' WHERE ticker = '030200';
UPDATE stock SET description = 'LS전선·LS ELECTRIC·LS엠트론 등을 거느린 LS그룹의 지주사예요.' WHERE ticker = '006260';
UPDATE stock SET description = '한국투자증권·한국투자신탁 등 금융 계열사를 거느린 금융지주사예요.' WHERE ticker = '071050';
UPDATE stock SET description = '사우디 아람코가 대주주인 정유사로 정유·석유화학 사업을 운영해요.' WHERE ticker = '010950';
UPDATE stock SET description = '농협금융지주 계열의 대형 증권사로 투자은행·리테일 서비스를 제공해요.' WHERE ticker = '005940';
UPDATE stock SET description = '배틀그라운드(PUBG)를 개발한 글로벌 게임사로 인도 등 신흥 시장에서 큰 인기를 끌고 있어요.' WHERE ticker = '259960';
UPDATE stock SET description = '삼성그룹 계열의 대형 증권사로 리테일·자산관리 서비스를 제공해요.' WHERE ticker = '016360';
UPDATE stock SET description = '모바일 기반 인터넷전문은행으로 편리한 금융 서비스로 젊은 층에게 인기예요.' WHERE ticker = '323410';
UPDATE stock SET description = '삼성바이오에피스의 지주사로 바이오시밀러 사업을 영위해요.' WHERE ticker = '0126Z0';
UPDATE stock SET description = '국내 1위 항공사로 여객·화물 운송과 항공우주 사업을 운영해요.' WHERE ticker = '003490';
UPDATE stock SET description = '자동차·화재·건강보험을 운영하는 대형 손해보험사예요.' WHERE ticker = '005830';
UPDATE stock SET description = '선박 유지보수·엔진 서비스 등 해양 애프터마켓 서비스를 제공하는 조선 서비스 기업이에요.' WHERE ticker = '443060';
UPDATE stock SET description = '가스처리·정유·석유화학 플랜트를 건설하는 삼성그룹의 엔지니어링 계열사예요.' WHERE ticker = '028050';
UPDATE stock SET description = '철강·에너지·식량 등을 거래하는 POSCO홀딩스 계열의 종합 상사예요.' WHERE ticker = '047050';
UPDATE stock SET description = '국내 주식 거래량 1위 온라인 증권사로 개인 투자자에게 인기예요.' WHERE ticker = '039490';
UPDATE stock SET description = 'BTS·세븐틴·뉴진스 등 K팝 아티스트를 보유한 세계 최대 규모의 음악 엔터테인먼트 기업이에요.' WHERE ticker = '352820';
UPDATE stock SET description = '반도체·통신 장비에 쓰이는 고다층 인쇄회로기판(MLB PCB)을 만드는 기업이에요.' WHERE ticker = '007660';
UPDATE stock SET description = '주택·토목·해외 플랜트 공사를 운영하는 대형 건설사예요.' WHERE ticker = '047040';
UPDATE stock SET description = 'LG그룹의 IT 서비스·디지털 전환·클라우드 사업을 운영하는 IT 계열사예요.' WHERE ticker = '064400';
UPDATE stock SET description = '방산·화학·금융·건설 등 한화그룹의 사업을 총괄하는 지주사예요.' WHERE ticker = '000880';
UPDATE stock SET description = '불닭볶음면으로 전 세계에서 인기를 끄는 K라면 브랜드를 운영하는 식품 기업이에요.' WHERE ticker = '003230';
UPDATE stock SET description = '국내 1위 타이어 제조사로 글로벌 완성차 업체에 타이어를 공급해요.' WHERE ticker = '161390';
UPDATE stock SET description = '대한항공·진에어 등을 거느린 한진그룹의 지주사예요.' WHERE ticker = '180640';
UPDATE stock SET description = '전력·통신·해저케이블을 만드는 전선 제조 기업이에요.' WHERE ticker = '001440';
UPDATE stock SET description = '전력반도체·디스플레이 구동칩 등을 위탁 생산하는 파운드리(반도체 수탁생산) 기업이에요.' WHERE ticker = '000990';
UPDATE stock SET description = '스마트폰·서버용 고밀도 인쇄회로기판(HDI PCB)을 만드는 전자부품 기업이에요.' WHERE ticker = '353200';
UPDATE stock SET description = '변압기·리액터 등 전력기기를 만드는 기업으로 미국 전력 인프라 수요 수혜를 받고 있어요.' WHERE ticker = '062040';
UPDATE stock SET description = '백화점·이마트·스타필드 등 유통 사업을 운영하는 신세계그룹의 핵심 기업이에요.' WHERE ticker = '004170';
UPDATE stock SET description = '뇌전증 치료제(세노바메이트) 등 중추신경계 신약을 개발하는 SK그룹의 바이오 계열사예요.' WHERE ticker = '326030';
UPDATE stock SET description = 'OLED·LCD 패널을 만드는 세계 1위 OLED 제조사예요.' WHERE ticker = '034220';
UPDATE stock SET description = '태양광 모듈·케미칼·유통 사업을 운영하는 한화그룹의 에너지·화학 계열사예요.' WHERE ticker = '009830';
UPDATE stock SET description = 'GS칼텍스·GS리테일·GS건설 등을 거느린 GS그룹의 지주사예요.' WHERE ticker = '078930';
UPDATE stock SET description = '협동로봇을 개발·생산하는 기업으로 제조·서비스 현장 자동화 수요를 공략하고 있어요.' WHERE ticker = '454910';
UPDATE stock SET description = 'LG그룹 계열의 이동통신·인터넷·IPTV 서비스를 운영하는 통신사예요.' WHERE ticker = '032640';
UPDATE stock SET description = '굴착기·지게차 등 건설기계를 만드는 HD현대그룹의 건설기계 계열사예요.' WHERE ticker = '267270';
UPDATE stock SET description = '소형 건설장비(스키드로더·소형굴착기)를 만들어 북미 시장에서 높은 점유율을 가진 글로벌 장비 기업이에요.' WHERE ticker = '241560';
UPDATE stock SET description = '정수기·공기청정기 렌탈 서비스 1위 기업으로 동남아 시장에서도 성장 중이에요.' WHERE ticker = '021240';
UPDATE stock SET description = '전력·통신·해저케이블을 만드는 LS그룹 계열의 전선 기업이에요.' WHERE ticker = '000500';
UPDATE stock SET description = '설화수·라네즈·이니스프리 등 K뷰티 브랜드를 운영하는 국내 1위 화장품 기업이에요.' WHERE ticker = '090430';
UPDATE stock SET description = '렉라자(폐암 신약) 등 혁신 의약품을 개발·판매하는 국내 대표 제약사예요.' WHERE ticker = '000100';
UPDATE stock SET description = '반도체 유리기판·동박·화학 소재를 만드는 SK그룹의 소재 계열사예요.' WHERE ticker = '011790';
UPDATE stock SET description = '카카오톡 기반의 간편결제·보험·투자 서비스를 운영하는 핀테크 기업이에요.' WHERE ticker = '377300';
UPDATE stock SET description = '리니지·블레이드앤소울 등 온라인 게임을 개발·운영하는 국내 대표 게임사예요.' WHERE ticker = '036570';
