# jusin — 재무제표 기반 주가 예측 시스템

DART(전자공시시스템) API에서 상장 기업의 재무제표를 수집하고,
7가지 재무 지표를 자동 계산하여 주가 방향성 신호를 REST API로 제공하는 풀스택 서비스입니다.

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [기술 스택](#2-기술-스택)
3. [아키텍처](#3-아키텍처)
4. [API 명세](#4-api-명세)
5. [스코어링 시스템](#5-스코어링-시스템)
6. [사전 준비](#6-사전-준비)
7. [백엔드 실행](#7-백엔드-실행)
8. [프론트엔드 실행](#8-프론트엔드-실행)
9. [모니터링](#9-모니터링)
10. [테스트 실행](#10-테스트-실행)
11. [프로젝트 구조](#11-프로젝트-구조)
12. [자동 스케줄러](#12-자동-스케줄러)
13. [환경변수 참조](#13-환경변수-참조)

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
    → REST API 응답 / Next.js 대시보드 시각화
```

---

## 2. 기술 스택

### 백엔드

| 구분 | 기술 |
|------|------|
| 언어 | Java 24 |
| 프레임워크 | Spring Boot 4.0.4 |
| 빌드 | Gradle 9.3.1 |
| DB | MySQL 8.0 |
| ORM | Spring Data JPA (Hibernate 7) |
| HTTP 클라이언트 | Spring WebFlux (WebClient) |
| 스케줄러 | Quartz + Spring Scheduling |
| XML 파싱 | Java DOM + Jsoup 1.17.2 |
| 인증 | Spring Security + JWT (jjwt 0.12.x) |
| 모니터링 | Actuator + Micrometer + Prometheus + Grafana |
| 코드 생성 | Lombok |
| 테스트 | JUnit 5, Mockito, H2, JaCoCo |

### 프론트엔드

| 구분 | 기술 |
|------|------|
| 프레임워크 | Next.js 14 App Router |
| 언어 | TypeScript (strict) |
| 스타일링 | Tailwind CSS 3.x |
| UI | shadcn/ui (Radix UI) |
| 서버 상태 | TanStack Query v5 |
| 클라이언트 상태 | Zustand |
| HTTP | Axios |
| 차트 | Recharts |

---

## 3. 아키텍처

```
┌──────────────────────────────────────────────────────┐
│             Next.js 14 프론트엔드 (:3000)              │
│  메인(검색) / 분석 / 비교 / 히스토리 / 관심종목          │
└─────────────────────────┬────────────────────────────┘
                          │ REST API
┌─────────────────────────▼────────────────────────────┐
│                  REST API Layer (:8080)               │
│  CompanyController  AnalysisController  AuthController│
└─────────────────────────┬────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────┐
│                  Service Layer                       │
│  CompanyService  FinancialStatementService           │
│  IndicatorCalculationService  PredictionService      │
└──────────┬────────────────────┬────────────────────┘
           │                    │
┌──────────▼──────┐  ┌──────────▼──────────────────────┐
│  Client Layer   │  │  Parser Layer                   │
│  DartApiClient  │  │  FinancialXmlParser              │
│  RateLimiter    │  │  FinancialJsonParser             │
│  StockPriceClient│  │  ZipExtractor / DartXbrlTag    │
└──────────┬──────┘  └─────────────────────────────────┘
           │
    DART OpenAPI + Naver Finance (외부)
           │
┌──────────▼──────────────────────────────────────────┐
│                Repository Layer                     │
│  CompanyRepo  FSRepo  IndicatorRepo  PredictionRepo │
└──────────┬──────────────────────────────────────────┘
           │
        MySQL 8.0 (:3307)

┌──────────────────────────────────────────────────────┐
│              모니터링 스택                             │
│  Prometheus (:9090)  →  Grafana (:3001)              │
└──────────────────────────────────────────────────────┘
```

---

## 4. API 명세

### 기업 검색

```
GET /api/v1/companies/search?q={검색어}&limit={개수}
```

### 기업 상세 조회

```
GET /api/v1/companies/{stockCode}
```

### 재무제표 조회

```
GET /api/v1/companies/{stockCode}/financial-statements?period={기간}
```

### 재무 분석 및 예측

```
GET /api/v1/analysis/{stockCode}
```

### 기업 비교 분석

```
GET /api/v1/analysis/compare?codes={code1},{code2}
```

### 신호 변화 히스토리

```
GET /api/v1/analysis/{stockCode}/history
```

### 인증

```
POST /api/v1/auth/login        — 로그인 (JWT 발급)
POST /api/v1/auth/refresh      — 토큰 갱신
```

### 관리자 (ROLE_ADMIN 필요)

```
GET /api/v1/admin/sync/corp-codes                — 기업코드 전체 동기화
GET /api/v1/admin/sync/financial/{stockCode}     — 재무제표 수동 수집
```

### 모니터링

```
GET /actuator/health       — 헬스 체크
GET /actuator/prometheus   — Prometheus 메트릭
```

**분석 응답 예시**:
```json
{
  "success": true,
  "data": {
    "companyName": "삼성전자",
    "stockCode": "005930",
    "prediction": {
      "signal": "UP",
      "signalLevel": "STRONG",
      "score": 82,
      "scoreLevel": "강한 상승 신호"
    },
    "indicators": {
      "roe":            { "value": 18.5, "score": 20, "evaluation": "우수" },
      "debtRatio":      { "value": 42.3, "score": 15, "evaluation": "우수" }
    }
  }
}
```

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

| 점수 | 신호 | 등급 | 표시 |
|------|------|------|------|
| 80점 이상 | UP | STRONG | 🟢 강한 상승 신호 |
| 60~79점 | UP | WEAK | 🟡 약한 상승 신호 |
| 40~59점 | NEUTRAL | NEUTRAL | ⚪ 중립 신호 |
| 20~39점 | DOWN | WEAK | 🟠 약한 하락 신호 |
| 0~19점 | DOWN | STRONG | 🔴 강한 하락 신호 |

---

## 6. 사전 준비

- Java 24 이상
- Node.js 18 이상 (프론트엔드)
- Docker (MySQL / Prometheus / Grafana)
- DART API 키 ([DART 개발자센터](https://opendart.fss.or.kr) 발급)

---

## 7. 백엔드 실행

### Step 1 — MySQL 컨테이너 시작

```bash
docker compose up -d mysql
```

### Step 2 — 환경변수 설정

`.env` 파일 생성 (`.env.example` 참고):

```dotenv
DB_URL=jdbc:mysql://localhost:3307/jusin?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
DB_USERNAME=jusin
DB_PASSWORD=jusinpass
DART_API_KEY=발급받은_키를_여기에_입력
```

### Step 3 — application.properties 설정

`src/main/resources/application.properties` 파일을 직접 생성합니다 (CLAUDE.md 참고).

### Step 4 — DB 스키마 적용

```bash
docker exec -i jusin_mysql mysql -ujusin -pjusinpass jusin < src/main/resources/schema.sql
```

### Step 5 — 서버 실행

```bash
./gradlew bootRun
```

http://localhost:8080 에서 API 사용 가능.

### 빠른 동작 확인

```bash
curl http://localhost:8080/actuator/health
curl "http://localhost:8080/api/v1/companies/search?q=삼성"
curl http://localhost:8080/api/v1/analysis/005930
```

---

## 8. 프론트엔드 실행

> 상세 내용: `docs/frontend-start.md`

```bash
cd C:/github/jusin-web
npm install      # 최초 1회
npm run dev      # http://localhost:3000
```

`.env.local`:
```dotenv
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

---

## 9. 모니터링

> 상세 내용: `docs/monitoring-guide.md`

```bash
# Prometheus + Grafana 기동
docker compose up -d prometheus grafana
```

| 서비스 | 주소 | 계정 |
|--------|------|------|
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3001 | admin / admin |

Grafana에서 대시보드 ID `4701` (JVM Micrometer) 또는 `6756` (Spring Boot Statistics)을 가져오면 바로 시각화됩니다.

---

## 10. 테스트 실행

```bash
# 전체 테스트 (H2 인메모리, MySQL 불필요)
./gradlew test

# 커버리지 리포트
./gradlew test jacocoTestReport
# → build/reports/jacoco/test/html/index.html
```

**테스트 현황**: 53개 통과

---

## 11. 프로젝트 구조

```
jusin/
├── src/main/java/com/jusin/
│   ├── config/            — AppConfig, SecurityConfig, SchedulerConfig
│   ├── domain/entity/     — Company, FinancialStatement, FinancialIndicator, PredictionResult, DataSyncLog, User
│   ├── domain/enums/      — Signal, SignalLevel, SyncStatus, SyncType, UserRole
│   ├── repository/        — Spring Data JPA Repositories
│   ├── client/            — DartApiClient, DartApiRateLimiter, StockPriceClient
│   ├── parser/            — FinancialXmlParser, FinancialJsonParser, ZipExtractor
│   ├── service/           — CompanyService, IndicatorCalculationService, PredictionService 등
│   ├── security/          — JwtTokenProvider, JwtAuthenticationFilter
│   ├── controller/        — CompanyController, AnalysisController, AuthController, AdminController
│   ├── scheduler/         — DartDataSyncScheduler, IndicatorCalculationScheduler
│   ├── exception/         — JusinException, CompanyNotFoundException 등
│   └── dto/               — 요청/응답 DTO
├── monitoring/
│   ├── prometheus.yml     — Prometheus 스크랩 설정
│   └── grafana/           — Grafana 자동 프로비저닝 설정
├── docs/
│   ├── monitoring-guide.md    — Prometheus/Grafana 사용법
│   └── frontend-start.md     — 프론트엔드 시작 가이드
├── task/
│   ├── backend/           — 백엔드 태스크 명세 및 결과 보고서
│   └── frontend/          — 프론트엔드 태스크 명세 및 결과 보고서
├── docker-compose.yml     — MySQL + Prometheus + Grafana
├── build.gradle
└── CLAUDE.md              — AI 에이전트 가이드
```

---

## 12. 자동 스케줄러

| 시각 | 스케줄러 | 작업 |
|------|---------|------|
| 매일 06:00 | `DartDataSyncScheduler` | DART API에서 재무제표 수집 (일일 9,000건 제한) |
| 매일 07:00 | `IndicatorCalculationScheduler` | 신규 재무제표 지표 재계산 |
| 매일 07:30 | `IndicatorCalculationScheduler` | 지표 기반 예측 신호 업데이트 |
| 매월 1일 02:00 | `DartDataSyncScheduler` | 전체 기업코드 재동기화 |

---

## 13. 환경변수 참조

| 변수명 | 필수 | 설명 |
|--------|------|------|
| `DB_URL` | Y | MySQL JDBC URL |
| `DB_USERNAME` | Y | DB 사용자명 |
| `DB_PASSWORD` | Y | DB 비밀번호 |
| `DART_API_KEY` | Y | DART OpenAPI 인증키 |

> `application.properties`와 `schema.sql`은 `.gitignore`에 등록되어 있습니다. 절대 커밋하지 마십시오.

---

## 라이선스

이 프로젝트는 학습 및 연구 목적으로 작성되었습니다.
DART Open API 이용 시 [금융감독원 이용약관](https://opendart.fss.or.kr/intro/main.do)을 준수하십시오.
