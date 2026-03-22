# jusin — 재무제표 기반 주가 예측 REST API

DART(전자공시시스템) API에서 상장 기업의 재무제표를 수집하고,
7가지 재무 지표를 자동 계산하여 주가 방향성 신호를 REST API로 제공하는 백엔드 서비스입니다.

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [기술 스택](#2-기술-스택)
3. [아키텍처](#3-아키텍처)
4. [API 명세](#4-api-명세)
5. [스코어링 시스템](#5-스코어링-시스템)
6. [사전 준비](#6-사전-준비)
7. [로컬 개발 환경 실행](#7-로컬-개발-환경-실행)
8. [테스트 실행](#8-테스트-실행)
9. [프로젝트 구조](#9-프로젝트-구조)
10. [자동 스케줄러](#10-자동-스케줄러)
11. [환경변수 참조](#11-환경변수-참조)

---

## 1. 프로젝트 개요

**jusin**은 국내 상장 기업의 재무제표 데이터를 DART API로 수집하고, 정량적 재무 지표 분석을 통해 주가 방향성 신호를 산출하는 시스템입니다.

**핵심 흐름**
```
종목코드 입력
    → DART API에서 기업 정보 및 재무제표 수집 (WebFlux / WebClient)
    → XML/XBRL 파싱 (DOM 기반, XXE 방지)
    → 재무 지표 계산 (ROE, 부채비율, EPS, 영업이익률, 유동비율 등)
    → 100점 만점 스코어링
    → 5단계 신호 판정 (강한상승 🟢 / 약한상승 🟡 / 중립 ⚪ / 약한하락 🟠 / 강한하락 🔴)
    → REST API 응답
```

---

## 2. 기술 스택

| 구분 | 기술 |
|------|------|
| 언어 | Java 24 |
| 프레임워크 | Spring Boot 4.0.4 |
| 빌드 | Gradle 9.3.1 |
| DB | MySQL 8.0 |
| ORM | Spring Data JPA (Hibernate 7) |
| HTTP 클라이언트 | Spring WebFlux (WebClient) |
| 스케줄러 | Quartz + Spring Scheduling |
| XML 파싱 | Java DOM (`javax.xml.parsers`) + Jsoup 1.17.2 |
| 코드 생성 | Lombok |
| 테스트 | JUnit 5, Mockito, H2 (인메모리), JaCoCo |

---

## 3. 아키텍처

```
┌──────────────────────────────────────────────────────┐
│                  REST API Layer                      │
│  CompanyController  AnalysisController  FSController │
└─────────────────────────┬────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────┐
│                  Service Layer                       │
│  CompanyService  FinancialStatementService           │
│  IndicatorCalculationService  PredictionService      │
│  IndicatorScoreCalculator  DataSyncLogService        │
└──────────┬────────────────────┬────────────────────┘
           │                    │
┌──────────▼──────┐  ┌──────────▼──────────────────────┐
│  Client Layer   │  │  Parser Layer                   │
│  DartApiClient  │  │  FinancialXmlParser              │
│  RateLimiter    │  │  FinancialJsonParser             │
└──────────┬──────┘  │  ZipExtractor / DartXbrlTag     │
           │         └─────────────────────────────────┘
           │
    DART OpenAPI (외부)
           │
┌──────────▼──────────────────────────────────────────┐
│                Repository Layer                     │
│  CompanyRepo  FSRepo  IndicatorRepo  PredictionRepo │
│  DataSyncLogRepo                                    │
└──────────┬──────────────────────────────────────────┘
           │
        MySQL 8.0
```

**레이어 규칙**: controller → service → client/repository / parser는 service에서만 호출
**외부 HTTP 호출**: service에서 직접 호출 금지, 반드시 client 레이어 경유

---

## 4. API 명세

### 기업 검색

```
GET /api/v1/companies/search?q={검색어}&limit={개수}
```

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| q | String | Y | 기업명 검색어 |
| limit | int | N | 최대 결과 수 (기본값: 10) |

**응답 예시**
```json
{
  "success": true,
  "count": 2,
  "data": [
    {
      "stockCode": "005930",
      "companyName": "삼성전자",
      "corpCode": "00126380",
      "market": "KOSPI"
    }
  ]
}
```

---

### 기업 상세 조회

```
GET /api/v1/companies/{stockCode}
```

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| stockCode | String | 6자리 종목코드 (예: 005930) |

**응답 예시**
```json
{
  "success": true,
  "data": {
    "stockCode": "005930",
    "companyName": "삼성전자",
    "corpCode": "00126380",
    "representativeName": "한종희",
    "industry": "전자부품 제조업"
  }
}
```

---

### 재무제표 조회

```
GET /api/v1/companies/{stockCode}/financial-statements?period={기간}
```

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| stockCode | String | 6자리 종목코드 |
| period | String | 기간 (예: 2025-Q4) — 생략 시 최신 데이터 |

**응답 예시**
```json
{
  "success": true,
  "data": {
    "period": "2025-Q4",
    "revenue": "320000000000000",
    "operatingIncome": "32000000000000",
    "netIncome": "28000000000000",
    "totalAssets": "480000000000000",
    "equity": "320000000000000",
    "totalLiabilities": "160000000000000"
  }
}
```

---

### 재무 분석 및 예측

```
GET /api/v1/analysis/{stockCode}
```

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| stockCode | String | 6자리 종목코드 |

기업 조회 → 재무제표 수집 → 지표 계산 → 예측 점수 산출을 모두 자동으로 수행합니다.
이미 계산된 결과는 DB에서 캐시로 반환합니다.

**응답 예시**
```json
{
  "success": true,
  "data": {
    "companyName": "삼성전자",
    "stockCode": "005930",
    "analysisDate": "2026-03-22",
    "lastDataDate": "2025-Q4",
    "prediction": {
      "signal": "UP",
      "signalLevel": "STRONG",
      "score": 82,
      "emoji": "🟢",
      "scoreLevel": "강한 상승 신호"
    },
    "indicators": {
      "roe":            { "value": 18.5,  "score": 20, "maxScore": 20, "evaluation": "우수" },
      "debtRatio":      { "value": 42.3,  "score": 15, "maxScore": 15, "evaluation": "우수" },
      "eps":            { "value": 4800,  "score": 15, "maxScore": 15, "evaluation": "우수" },
      "operatingMargin":{ "value": 14.2,  "score": 8,  "maxScore": 10, "evaluation": "양호" },
      "currentRatio":   { "value": 1.85,  "score": 10, "maxScore": 10, "evaluation": "우수" }
    },
    "financialData": {
      "period": "2025-Q4",
      "revenue": "320000000000000",
      "operatingIncome": "32000000000000",
      "netIncome": "28000000000000",
      "totalAssets": "480000000000000",
      "equity": "320000000000000"
    },
    "notes": [
      "PER/PBR은 주가 데이터 미확보로 0점 처리됩니다.",
      "점수 기준: 80+ 강한상승 / 60+ 약한상승 / 40+ 중립 / 20+ 약한하락 / ~19 강한하락"
    ]
  }
}
```

---

### 오류 응답 형식

모든 오류는 일관된 형식으로 반환됩니다.

```json
{
  "status": 404,
  "code": "COMPANY_NOT_FOUND",
  "message": "기업을 찾을 수 없습니다: 999999",
  "timestamp": "2026-03-22T10:30:00"
}
```

| HTTP 상태 | 코드 | 설명 |
|----------|------|------|
| 400 | INSUFFICIENT_DATA | 재무 데이터 부족 |
| 400 | VALIDATION_ERROR | 요청 파라미터 검증 실패 |
| 404 | COMPANY_NOT_FOUND | 기업 없음 |
| 500 | DATA_PROCESSING_ERROR | 파싱/계산 실패 |
| 503 | EXTERNAL_API_ERROR | DART API 호출 실패 |

---

## 5. 스코어링 시스템

7개 지표를 100점 만점으로 평가하여 신호를 산출합니다.

| 지표 | 배점 | 세부 기준 |
|------|------|----------|
| PER | 20점 | ≤10: 20점 / ≤20: 15점 / ≤30: 10점 / 초과: 0점 |
| ROE | 20점 | ≥15%: 20점 / ≥10%: 15점 / ≥5%: 10점 / ≥0%: 5점 / 음수: 0점 |
| 부채비율 | 15점 | ≤50%: 15점 / ≤100%: 10점 / ≤150%: 5점 / 초과: 0점 |
| EPS 성장률 | 15점 | ≥10%: 15점 / ≥0%: 10점 / ≥-10%: 5점 / 그 이하: 0점 |
| PBR | 10점 | ≤1.0: 10점 / ≤2.0: 8점 / ≤3.0: 5점 / 초과: 0점 |
| 영업이익률 | 10점 | ≥15%: 10점 / ≥10%: 8점 / ≥5%: 5점 / ≥0%: 2점 / 음수: 0점 |
| 유동비율 | 10점 | 1.5~2.0: 10점 / 1.2~2.5: 8점 / 1.0~3.0: 5점 / 1.0미만: 0점 |

**신호 판정 기준**

| 점수 | 신호 | 등급 | 이모지 |
|------|------|------|------|
| 80점 이상 | UP | STRONG | 🟢 강한 상승 신호 |
| 60~79점 | UP | WEAK | 🟡 약한 상승 신호 |
| 40~59점 | NEUTRAL | NEUTRAL | ⚪ 중립 신호 |
| 20~39점 | DOWN | WEAK | 🟠 약한 하락 신호 |
| 0~19점 | DOWN | STRONG | 🔴 강한 하락 신호 |

> PER/PBR은 실시간 주가 데이터가 필요하므로 현재 버전에서는 0점 처리됩니다.

---

## 6. 사전 준비

### 필수 요건

- Java 24 이상
- Docker (MySQL 로컬 환경용)
- DART API 키 ([DART 개발자센터](https://opendart.fss.or.kr) 발급)

### DART API 키 발급

1. [DART 개발자센터](https://opendart.fss.or.kr/intro/main.do) 접속
2. 회원가입 후 API 인증키 신청
3. 발급받은 키를 환경변수 `DART_API_KEY`에 설정

---

## 7. 로컬 개발 환경 실행

### Step 1 — 저장소 클론

```bash
git clone https://github.com/your-org/jusin.git
cd jusin
```

### Step 2 — MySQL 컨테이너 시작

```bash
docker compose up -d
```

컨테이너가 기동되면 `localhost:3307`에 MySQL 8.0이 실행됩니다.

| 항목 | 값 |
|------|-----|
| 호스트 | localhost |
| 포트 | 3307 |
| 데이터베이스 | jusin |
| 사용자 | jusin |
| 비밀번호 | jusinpass |

### Step 3 — 환경변수 파일 설정

```bash
cp .env.example .env
```

`.env` 파일을 열어 값을 채워 넣습니다.

```dotenv
DB_URL=jdbc:mysql://localhost:3307/jusin?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
DB_USERNAME=jusin
DB_PASSWORD=jusinpass
DART_API_KEY=발급받은_키를_여기에_입력
```

### Step 4 — application.properties 설정

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

> `application.properties`는 `.gitignore`에 등록되어 있어 커밋되지 않습니다.

### Step 5 — DB 스키마 적용

MySQL에 스키마를 직접 실행합니다 (`ddl-auto=validate` 사용).

```bash
# docker exec으로 MySQL에 접속하여 스키마 적용
docker exec -i jusin_mysql mysql -ujusin -pjusinpass jusin < src/main/resources/schema.sql
```

또는 MySQL 클라이언트(DBeaver 등)에서 `src/main/resources/schema.sql` 파일을 직접 실행합니다.

### Step 6 — 서버 실행

```bash
./gradlew bootRun
```

서버가 기동되면 `http://localhost:8080`에서 API를 사용할 수 있습니다.

### 빠른 동작 확인

```bash
# 삼성전자 분석 요청 (최초 실행 시 DART API 호출 발생)
curl http://localhost:8080/api/v1/analysis/005930

# 기업 검색
curl "http://localhost:8080/api/v1/companies/search?q=삼성"

# 재무제표 조회
curl "http://localhost:8080/api/v1/companies/005930/financial-statements?period=2025-Q4"
```

---

## 8. 테스트 실행

### 단위/통합 테스트 실행

테스트는 H2 인메모리 DB를 사용하므로 MySQL 없이도 실행됩니다.

```bash
./gradlew test
```

### 커버리지 리포트 생성

```bash
./gradlew test jacocoTestReport
```

리포트 위치: `build/reports/jacoco/test/html/index.html`

### 테스트 구성

| 테스트 클래스 | 종류 | 테스트 수 |
|-------------|------|---------|
| `IndicatorCalculationServiceTest` | 단위 | 8개 |
| `IndicatorScoreCalculatorTest` | 파라미터화 단위 | 19개 |
| `FinancialXmlParserTest` | 단위 | 2개 |
| `AnalysisControllerIntegrationTest` | 통합 | 5개 |
| **합계** | | **34개** |

---

## 9. 프로젝트 구조

```
jusin/
├── src/
│   ├── main/
│   │   ├── java/com/jusin/
│   │   │   ├── JusinApplication.java          # 진입점 (@EnableScheduling, @EnableJpaAuditing)
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java             # WebClient Bean 설정 (timeout 30초)
│   │   │   │   ├── SchedulerConfig.java       # Quartz 스케줄러 설정
│   │   │   │   └── RequestLoggingFilter.java  # 요청/응답 로깅 필터 (MDC requestId)
│   │   │   ├── domain/
│   │   │   │   ├── entity/                    # JPA 엔티티 5개
│   │   │   │   │   ├── Company.java           # 기업 정보
│   │   │   │   │   ├── FinancialStatement.java # 재무제표
│   │   │   │   │   ├── FinancialIndicator.java # 재무 지표
│   │   │   │   │   ├── PredictionResult.java  # 예측 결과
│   │   │   │   │   └── DataSyncLog.java       # 동기화 이력
│   │   │   │   └── enums/
│   │   │   │       ├── Signal.java            # UP / DOWN / NEUTRAL
│   │   │   │       ├── SignalLevel.java        # STRONG / WEAK / NEUTRAL
│   │   │   │       ├── SyncStatus.java        # SUCCESS / FAILED / IN_PROGRESS
│   │   │   │       └── SyncType.java          # COMPANY / FINANCIAL / INDICATOR
│   │   │   ├── repository/                    # Spring Data JPA Repository
│   │   │   ├── client/
│   │   │   │   ├── DartApiClient.java         # DART API WebClient (비동기)
│   │   │   │   └── DartApiRateLimiter.java    # 일일 9,000건 호출 제한
│   │   │   ├── parser/
│   │   │   │   ├── FinancialXmlParser.java    # XBRL XML DOM 파싱
│   │   │   │   ├── FinancialJsonParser.java   # 재무 JSON 파싱 (Jackson 3.x)
│   │   │   │   ├── ZipExtractor.java          # DART ZIP 파일 EUC-KR 압축 해제
│   │   │   │   └── DartXbrlTag.java           # XBRL 태그 상수 (IFRS/DART/US-GAAP)
│   │   │   ├── service/
│   │   │   │   ├── CompanyService.java        # 기업 조회 (DB 우선, DART 폴백)
│   │   │   │   ├── FinancialStatementService.java # 재무제표 수집 및 저장
│   │   │   │   ├── IndicatorCalculationService.java # 재무 지표 계산
│   │   │   │   ├── IndicatorScoreCalculator.java   # 100점 스코어링
│   │   │   │   ├── IndicatorValidator.java    # 이상치 경고 로깅
│   │   │   │   ├── PredictionService.java     # 예측 결과 생성 (캐시 우선)
│   │   │   │   └── DataSyncLogService.java    # 동기화 이력 기록
│   │   │   ├── controller/
│   │   │   │   ├── CompanyController.java     # GET /api/v1/companies
│   │   │   │   ├── FinancialStatementController.java # GET /api/v1/companies/{code}/financial-statements
│   │   │   │   ├── AnalysisController.java    # GET /api/v1/analysis/{stockCode}
│   │   │   │   └── GlobalExceptionHandler.java # @RestControllerAdvice 전역 예외 처리
│   │   │   ├── exception/
│   │   │   │   ├── JusinException.java        # 추상 기반 예외 (errorCode + HttpStatus)
│   │   │   │   ├── CompanyNotFoundException.java  # 404
│   │   │   │   ├── InsufficientDataException.java # 400
│   │   │   │   ├── ExternalApiException.java  # 503
│   │   │   │   └── DataProcessingException.java   # 500
│   │   │   ├── scheduler/
│   │   │   │   ├── DartDataSyncScheduler.java      # 매일 06:00 재무제표 수집
│   │   │   │   └── IndicatorCalculationScheduler.java # 매일 07:00 지표 계산, 07:30 예측
│   │   │   └── dto/
│   │   │       ├── request/                   # 요청 DTO
│   │   │       └── response/                  # 응답 DTO
│   │   └── resources/
│   │       ├── application.properties.example # 설정 템플릿 (이것을 복사해 사용)
│   │       ├── schema.sql                     # DB DDL (gitignore됨, 직접 실행 필요)
│   │       └── logback-spring.xml             # 로그 설정 (콘솔+파일, 30일 보관)
│   └── test/
│       ├── java/com/jusin/
│       │   ├── fixture/                       # 테스트 픽스처 (Company, FS, DartApi)
│       │   ├── service/                       # 서비스 단위 테스트
│       │   ├── parser/                        # 파서 단위 테스트
│       │   └── integration/                   # 컨트롤러 통합 테스트
│       └── resources/
│           └── application.properties         # H2 인메모리 테스트 DB 설정
├── .env.example                               # 환경변수 템플릿
├── docker-compose.yml                         # MySQL 8.0 로컬 환경
├── build.gradle                               # Gradle 빌드 설정
└── AGENTS.md                                  # 에이전트 협업 가이드
```

---

## 10. 자동 스케줄러

서버가 실행 중일 때 아래 작업이 자동으로 수행됩니다.

| 시각 | 스케줄러 | 작업 |
|------|---------|------|
| 매일 06:00 | `DartDataSyncScheduler` | DART API에서 재무제표 수집 및 저장 (일일 9,000건 제한 체크) |
| 매일 07:00 | `IndicatorCalculationScheduler` | 신규 재무제표에 대한 지표 재계산 |
| 매일 07:30 | `IndicatorCalculationScheduler` | 지표 기반 예측 신호 업데이트 |

> `spring.quartz.auto-startup=false`로 설정 시 스케줄러가 자동 시작되지 않습니다.
> 로컬 개발 중 불필요한 DART API 호출을 방지하려면 이 값을 유지하세요.

---

## 11. 환경변수 참조

| 변수명 | 필수 | 기본값 | 설명 |
|--------|------|--------|------|
| `DB_URL` | N | `jdbc:mysql://localhost:3306/jusin?...` | MySQL JDBC URL |
| `DB_USERNAME` | Y | — | DB 사용자명 |
| `DB_PASSWORD` | Y | — | DB 비밀번호 |
| `DART_API_KEY` | Y | — | DART OpenAPI 인증키 |

> `application.properties`와 `schema.sql`은 `.gitignore`에 등록되어 있습니다. 절대 커밋하지 마십시오.

---

## 라이선스

이 프로젝트는 학습 및 연구 목적으로 작성되었습니다.
DART Open API 이용 시 [금융감독원 이용약관](https://opendart.fss.or.kr/intro/main.do)을 준수하십시오.
