# 🚀 aeranghae-main-api (애랑해 메인 API 서버)

## 📌 프로젝트 소개

본 리포지토리는 **"애랑해 (AI 사랑해)"** 프로젝트의 **핵심 백엔드 서비스**입니다. 자연어 요구사항을 바탕으로 코드를 자동 생성하는 시스템에서 **사용자 데이터 관리, 시스템 오케스트레이션(조정), 그리고 최종 프로그램 파일 생성**을 담당합니다.

### 핵심 기능

1.  **사용자/계정 관리:** 사용자 등록, 인증, 권한 관리 등 핵심 백엔드 기능.
2.  **요청 오케스트레이션:** 클라이언트 요청을 받아 LLM 서버로 전달하고, LLM 서버의 응답을 받아 후속 처리를 진행하는 중앙 통제 역할.
3.  **코드 파일 생성:** LLM 서버로부터 받은 JSON 형식의 프로젝트 구조 정보를 실제 파일 시스템에 **프로젝트 형태로 변환 및 생성**하는 핵심 로직 수행.
4.  **클라이언트 API:** Electron/React 클라이언트와의 통신 (HTTP/REST).
5.  **LLM 통신:** FastAPI 기반의 LLM 서버와의 **WebSocket 통신**을 통한 실시간 코드 생성 요청 및 응답 처리.

## 🛠️ 기술 스택 (Tech Stack)

| 구분 | 기술 스택 | 설명 |
| :--- | :--- | :--- |
| **백엔드 프레임워크** | `Spring Boot` (Java) | 안정적인 API 구축 및 서비스 로직 처리. |
| **통신 프로토콜** | `HTTP/REST`, `WebSocket` | 클라이언트 통신 (REST) 및 LLM 서버 통신 (WebSocket) 담당. |
| **데이터베이스** | (추가 예정) | 사용자 정보, 프로젝트 이력 저장 등에 사용. |
| **빌드/의존성** | `Gradle` (또는 Maven) | 프로젝트 빌드 및 의존성 관리. |

## ⚙️ 개발 환경 설정

### 1\. 전제 조건

* Java Development Kit (JDK) 21 이상
* Gradle (또는 Maven)
* IDE (IntelliJ IDEA 또는 Eclipse 권장)
* Database (MySQL, PostgreSQL 등) 설치 및 설정

### 2\. 프로젝트 클론 및 실행

```bash
# 리포지토리 클론
git clone https://github.com/aeranghae/aeranghae-main-api.git
cd aeranghae-main-api

# 의존성 다운로드 및 빌드
./gradlew build

# Spring Boot 실행 (로컬 개발 환경)
./gradlew bootRun
```

### 3\. 주요 설정 파일

설정 정보는 주로 `src/main/resources/application.yml`에 위치하며, 다음 항목을 수정해야 합니다.

* **Database 연결 정보:**
  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/aeranghae_db
      username: [DB_USERNAME]
      password: [DB_PASSWORD]
  ```
* **LLM WebSocket Endpoint:**
  ```yaml
  llm:
    server:
      websocket-url: ws://localhost:8000/ws/generate
  ```

## 📐 주요 아키텍처 및 모듈

### 1\. `UserController` / `UserService`

사용자 등록 및 로그인(인증/인가) 로직을 처리하는 핵심 모듈입니다.

### 2\. `CodeGenerationController`

클라이언트의 코드 생성 요청을 받는 REST API 엔드포인트 (`/api/v1/generate`). 요청을 받아 LLM 서버로 전달하는 로직이 포함됩니다.

### 3\. `LlmWebSocketHandler`

LLM 서버와의 실시간 통신(WebSocket)을 관리합니다. 요청 전송 및 LLM으로부터 JSON 응답을 수신하는 역할을 합니다.

### 4\. `ProjectFileService`

LLM으로부터 받은 **JSON 데이터를 실제 파일 구조로 변환**하고, 서버의 임시 디렉토리에 **압축(.zip)하는 핵심 파일 I/O 로직**을 담당합니다.

## 💡 기여 방법 (Contribution)

이 프로젝트에 기여하는 것을 환영합니다\! 기여 절차는 다음과 같습니다.

1.  리포지토리를 Fork 합니다.
2.  새로운 기능 또는 버그 수정에 대한 브랜치를 생성합니다. (`feature/new-auth` 또는 `fix/db-connection`)
3.  커밋 메시지 규칙에 따라 변경 사항을 커밋합니다.
4.  Pull Request를 생성합니다. (변경 사항 설명 명시)