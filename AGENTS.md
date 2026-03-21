# AGENTS.md — jusin 프로젝트 에이전트 가이드

재무제표 기반 주가 예측 REST API 프로젝트입니다.
이 파일을 읽은 직후 별도 탐색 없이 작업을 시작할 수 있습니다.

---

## 프로젝트 개요

- **이름**: jusin
- **목적**: DART API에서 재무제표를 수집하고, 재무 지표를 계산하여 주가 예측 신호를 REST API로 제공
- **기술 스택**: Java 24, Spring Boot 4.0.4, Gradle 9.3.1, MySQL, Spring Data JPA (Hibernate 7), WebFlux, Quartz, Lombok, Jsoup

---

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 서버 실행
./gradlew bootRun
```

빌드 성공 기준: `BUILD SUCCESSFUL` 출력 확인.

---

## 환경 설정 (최초 1회)

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

`application.properties`에 아래 환경변수를 채워 넣습니다.

| 변수명 | 설명 |
|---|---|
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` | DB 사용자명 |
| `DB_PASSWORD` | DB 비밀번호 |
| `DART_API_KEY` | DART OpenAPI 키 |

> `application.properties`와 `schema.sql`은 `.gitignore`에 등록되어 있습니다.
> 절대 커밋하지 마십시오.

DB 스키마는 `schema.sql`을 DB에 직접 실행하십시오 (`ddl-auto=validate` 사용).

---

## 디렉터리 구조

```
src/main/java/com/jusin/
├── JusinApplication.java
├── config/           — Bean 설정 (AppConfig: WebClient, SchedulerConfig)
├── domain/
│   ├── entity/       — Company, FinancialStatement, FinancialIndicator, PredictionResult, DataSyncLog
│   └── enums/        — Signal, SignalLevel, SyncStatus, SyncType
├── repository/       — Spring Data JPA Repository
├── service/          — [미구현] 비즈니스 로직
├── client/           — [미구현] DART API WebClient
├── parser/           — [미구현] XML/XBRL 파싱
├── controller/       — [미구현] REST 컨트롤러
├── dto/
│   ├── request/      — [미구현]
│   └── response/     — [미구현]
├── exception/        — [미구현] 예외 처리
└── scheduler/        — [미구현] Quartz 스케줄러
```

---

## 태스크 진행 현황

| 태스크 | 상태 | 설명 |
|---|---|---|
| TASK-01 | 완료 | Spring Boot 초기화 |
| TASK-02 | 완료 | DB 스키마 및 Entity 설계 |
| TASK-03 | 미완료 | DART API 클라이언트 (`client/`) |
| TASK-04 | 미완료 | XML/XBRL 파싱 (`parser/`) |
| TASK-05 | 미완료 | 기업 검색 서비스 (`service/`) |
| TASK-06 | 미완료 | 재무제표 수집 서비스 (`service/`) |
| TASK-07 | 미완료 | 재무 지표 계산 서비스 (`service/`) |
| TASK-08 | 미완료 | 예측 스코어 서비스 (`service/`) |
| TASK-09 | 미완료 | REST API 컨트롤러 (`controller/`) |
| TASK-10 | 미완료 | 예외 처리 및 로깅 (`exception/`) |
| TASK-11 | 미완료 | 스케줄러 (`scheduler/`) |
| TASK-12 | 미완료 | 테스트 |

태스크 상세 내용은 `task/TASK-XX.md`를 참조하십시오.
전체 요구사항은 `task/PRD.md`, 태스크 DAG는 `task/README.md`를 참조하십시오.
세션 이관 정보는 `handoff.md`를 참조하십시오.

---

## 코딩 컨벤션

### Entity 작성 규칙

- `@Builder` 패턴 필수, **Setter 없음**
- 필드 수정은 반드시 `update()` 메서드를 통해서만 수행
- FK는 `@ManyToOne` 없이 ID 참조 방식 사용

```java
// 올바른 방식
private String companyId;  // @ManyToOne 사용 금지

// 수정 메서드
public void update(String newValue) {
    this.field = newValue;
}
```

### 설정 파일

- `application.properties` 방식 사용 (`application.yml`로 변경 금지)
- 민감 정보는 환경변수 주입 방식으로 관리

---

## 절대 금지 사항

| 항목 | 잘못된 방식 | 올바른 방식 |
|---|---|---|
| Gradle Java 버전 설정 | `sourceCompatibility = '24'` (최상위) | `java { toolchain { languageVersion = JavaLanguageVersion.of(24) } }` |
| Hibernate Dialect | `org.hibernate.dialect.MySQL8Dialect` | `org.hibernate.dialect.MySQLDialect` |
| 설정 파일 형식 | `application.yml` | `application.properties` |
| 민감 파일 커밋 | `application.properties` 커밋 | 커밋 금지 |
| 민감 파일 커밋 | `schema.sql` 커밋 | 커밋 금지 |

> `MySQL8Dialect`는 Spring Boot 4.x / Hibernate 7에서 삭제되었습니다.
> Gradle 9.x에서 최상위 `sourceCompatibility`는 오류를 유발합니다.

---

## 참고 문서

- `task/PRD.md` — 전체 요구사항 정의서
- `task/README.md` — 태스크 목록 및 의존성 DAG
- `task/TASK-XX.md` — 각 태스크 상세 명세
- `handoff.md` — 세션 간 이관 정보
