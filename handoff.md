# Handoff Document — jusin-financial-predictor

**세션 날짜**: 2026-03-21
**프로젝트**: jusin (재무제표 기반 주가 예측 서비스)
**기술 스택**: Java 24 / Spring Boot 4.0.4 / Gradle 9.3.1 / MySQL / Spring Data JPA (Hibernate 7) / WebFlux / Quartz / Lombok

---

## 1. 이번 세션에서 완료한 작업

### TASK-01: Spring Boot 프로젝트 초기화 및 환경 설정 ✅

| 파일 | 경로 | 내용 |
|------|------|------|
| `build.gradle` | `/` | Spring Boot 의존성 설정 |
| `settings.gradle` | `/` | 프로젝트 이름: jusin |
| `JusinApplication.java` | `src/main/java/com/jusin/` | @SpringBootApplication + @EnableScheduling |
| `AppConfig.java` | `src/main/java/com/jusin/config/` | WebClient Bean (DART API) |
| `SchedulerConfig.java` | `src/main/java/com/jusin/config/` | 스케줄러 설정 |
| `application.properties` | `src/main/resources/` | DB/JPA/DART API/로깅 설정 |

패키지 구조 (14개 디렉터리):
```
src/main/java/com/jusin/
├── config/
├── controller/
├── service/
├── client/
├── parser/
├── domain/
│   ├── entity/
│   └── enums/
├── repository/
├── dto/
│   ├── request/
│   └── response/
├── exception/
└── scheduler/
```

### TASK-02: DB 스키마 및 JPA Entity 설계 ✅

| 파일 | 경로 | 내용 |
|------|------|------|
| `schema.sql` | `src/main/resources/` | 5개 테이블 DDL |
| `Signal.java`, `SignalLevel.java`, `SyncStatus.java`, `SyncType.java` | `domain/enums/` | Enum 클래스 4개 |
| `Company.java`, `FinancialStatement.java`, `FinancialIndicator.java`, `PredictionResult.java`, `DataSyncLog.java` | `domain/entity/` | Entity 5개 |
| `CompanyRepository.java` 외 3개 | `repository/` | JpaRepository + 커스텀 쿼리 |

테이블: `companies`, `financial_statements`, `financial_indicators`, `prediction_results`, `data_sync_log`

### HARNESS SETUP: 에이전트 협업 환경 구축 ✅

| 파일 | 경로 | 내용 |
|------|------|------|
| `AGENTS.md` | `/` | 에이전트 진입점 (빌드 명령, 패키지 구조, 코딩 컨벤션, 금지 사항) |
| `application.properties.example` | `src/main/resources/` | 환경 설정 템플릿 |
| `ci.yml` | `.github/workflows/` | Java 24 Gradle CI 파이프라인 |
| `docker-compose.yml` | `/` | MySQL 8.0 로컬 개발 환경 |
| `application.properties` | `src/test/resources/` | H2 in-memory 테스트 설정 |

- `.gitignore` 수정: `application.properties` → `src/main/resources/application.properties` (경로 한정)
- `build.gradle` 수정: `testRuntimeOnly 'com.h2database:h2'` 추가
- 커밋: `0143a6a` (feat/harness-setup 브랜치)
- Harness 점수 목표: L2(30.9) → L3(~51점)

---

## 2. 현재 프로젝트 상태

- **git 브랜치**: `feat/harness-setup` (PR 대기 중)
- **빌드 상태**: BUILD SUCCESSFUL (확인 중)
- **완료된 TASK**: TASK-01, TASK-02, HARNESS SETUP
- **미완료 TASK**: TASK-03 ~ TASK-12
- **Harness 수준**: L2 (30.9점) → L3 목표 (~51점)

### 환경 설정

- `application.properties`는 `src/main/resources/application.properties`로 gitignore됨
- 신규 환경 설정: `src/main/resources/application.properties.example` 복사 후 환경변수 입력
- 필수 환경변수: `DB_USERNAME`, `DB_PASSWORD`, `DART_API_KEY`
- `schema.sql`: gitignore됨, DB에 직접 실행 (`ddl-auto=validate`)

---

## 3. 코딩 컨벤션 및 절대 금지 사항

### 코딩 컨벤션

- **Builder 강제**: 모든 Entity는 `@Builder` 사용, `@Setter` 절대 금지
- **FK 관리**: FK는 ID(String)로 관리 (객체 참조 대신 ID 보유)
- **ddl-auto**: 반드시 `validate` 유지 (create/update 사용 금지)
- **Enum**: DB 저장 시 `@Enumerated(EnumType.STRING)` 사용
- **레이어 규칙**: controller → service → client/repository, parser는 service에서만 호출

### 절대 금지 사항

- `sourceCompatibility` 최상위 직접 설정 금지 → Java toolchain 방식 사용
- `MySQL8Dialect` 명시적 지정 금지 → Hibernate 자동 감지
- `ddl-auto=create` 또는 `ddl-auto=update` 금지
- Entity에 `@Setter` 사용 금지
- 서비스 레이어에서 직접 HTTP 호출 금지 → client 레이어 경유 필수

---

## 4. 다음 세션에서 진행해야 할 작업

### 즉시 처리

- [ ] feat/harness-setup PR 머지 후 main 브랜치로 복귀

### Sprint 2 작업 (TASK-03 ~ TASK-05, 병렬 가능)

- [ ] **TASK-03**: DART API 클라이언트 구현
  - `client/DartApiClient.java`
  - DART Open API 연동 (기업 목록, 재무제표 XML)
  - WebClient 기반 비동기 구현

- [ ] **TASK-04**: XML/XBRL 파싱 로직 (TASK-03 완료 후)
  - `parser/FinancialXmlParser.java`
  - Jsoup 기반 XML 파싱

- [ ] **TASK-05**: 기업 검색 서비스 (TASK-03과 병렬 가능)
  - `service/CompanyService.java`
  - 기업 정보 CRUD + DART 동기화

### Sprint 2 이후 (TASK-06 ~ TASK-12)

- TASK-06: 재무제표 수집 서비스
- TASK-07: 재무 지표 계산 서비스
- TASK-08: 예측 스코어링 엔진
- TASK-09: REST API 컨트롤러
- TASK-10: 스케줄러 구현
- TASK-11: 예외 처리 및 로깅
- TASK-12: 통합 테스트

---

## 5. 설계 결정 사항 (ADR)

### Entity 설계 원칙

- PK: `String` 타입 UUID (DART 고유 ID와 일관성 유지)
- FK: 객체 참조 대신 ID(String) 보유 → 레이어 간 결합도 최소화
- 모든 Entity: `@Builder`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
- `created_at`, `updated_at`: `@CreationTimestamp`, `@UpdateTimestamp`

### API 설계 원칙

- DART API Key: 환경변수 `DART_API_KEY`로 주입
- WebClient timeout: 30초 (AppConfig.java)
- 비동기 처리: `Mono<>` / `Flux<>` 반환

---

## 6. 참고 파일 경로

| 파일 | 설명 |
|------|------|
| `task/PRD.md` | 제품 요구사항 정의서 (v1.0) |
| `task/README.md` | 태스크 목록 + DAG |
| `task/TASK-01.md` ~ `task/TASK-12.md` | 개별 태스크 상세 |
| `result/REPORT-SPRINT1.md` | Sprint 1 결과보고서 |
| `result/HARNESS-AUDIT.md` | Harness Audit 결과 (L2 30.9점) |
| `result/HARNESS-SETUP.md` | Harness Setup 진단 보고서 |
| `result/REPORT-HARNESS-SETUP.md` | Harness Setup 결과보고서 |
| `AGENTS.md` | 에이전트 진입점 (신규) |
| `docker-compose.yml` | MySQL 로컬 환경 |
| `.github/workflows/ci.yml` | CI 파이프라인 |
| `src/main/resources/application.properties.example` | 환경 설정 템플릿 |
| `src/main/resources/schema.sql` | DB 스키마 (gitignore됨) |
