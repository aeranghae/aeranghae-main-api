
---

# 애랑해(aeranghae) 서비스 아키텍처 및 통신 설계서

## 1. 시스템 개요 및 역할 분담

애랑해 서비스는 메인 비즈니스 로직을 담당하는 Spring Boot 서버와 무거운 AI 연산을 담당하는 FastAPI 서버로 분리된 마이크로서비스 아키텍처(MSA)

- **메인 서버 (Spring Boot)**

    - **역할**: 프로젝트 매니저 (Manager)
    - **기능**: 사용자 인증/인가, 메인 DB(PostgreSQL) 관리, 물리적 파일 I/O 관리, 클라이언트와의 실시간 통신(SSE) 담당, FastAPI로의 작업 지시 및 결과물 수신.

- **AI 서버 (FastAPI + LangChain)**

    - **역할**: 무상태(Stateless) AI 작업자 (Worker)
    - **기능**: Spring Boot로부터 전달받은 프롬프트와 파일 컨텍스트를 기반으로 코드 생성. 응답 파싱 및 정제. 작업 완료 후 메모리를 초기화하여 서버 자원 최적화.

- **클라이언트 (React / Electron)**

    - **역할**: 사용자 인터페이스 (UI)
    - **기능**: 프롬프트 입력, SSE(Server-Sent Events) 파이프를 통한 실시간 파일 생성 현황 및 코드 뷰어 렌더링.


## 2. 작업 공간(Workspace) 격리 정책

서버 자원(CPU, 메모리)의 고갈을 막기 위해 물리적인 OS 프로세스를 나누는 대신, **논리적 작업 공간과 파일 디렉토리 격리** 방식을 사용합니다.

- **물리적 파일 격리**: Spring Boot 서버 내부(예: `/app/workspaces/{userId}/{projectId}/`)에 프로젝트별 고유 디렉토리를 생성하여 다른 사용자의 파일과 완벽하게 분리합니다.

- **논리적 연산 격리**: FastAPI는 비동기(Async) 백그라운드 작업을 통해 여러 프로젝트의 코드 생성 요청을 병렬로 처리하며, 각 작업은 독립적인 메모리 공간에서 실행됩니다.


## 3. 클라이언트-서버 및 서버 간 통신 규격 (End-to-End Flow)

LLM의 긴 응답 시간으로 인한 타임아웃을 방지하기 위해 **비동기 콜백(웹훅)과 실시간 스트리밍(SSE) 방식**을 결합하여 통신합니다.

- **Phase 1: 사용자 요청 및 SSE 대기 (Client ↔ Spring Boot)**

    1. 클라이언트가 Spring Boot로 새로운 기능 생성/수정 요청을 보냅니다.
    2. Spring Boot는 요청을 접수한 뒤, 클라이언트와 **SSE(Server-Sent Events)** 연결을 맺고 실시간 알림을 보낼 파이프를 엽니다.

- **Phase 2: 비동기 작업 지시 (Spring Boot ↔ FastAPI)**

    1. Spring Boot는 사용자의 프롬프트와 현재 프로젝트의 파일 상태를 JSON 형태로 묶어 FastAPI로 전송합니다.
    2. FastAPI는 "접수 완료(HTTP 202 ACCEPTED)" 응답만 즉시 반환하고 연결을 종료합니다.

- **Phase 3: 실시간 결과 푸시 (FastAPI ➡️ Spring Boot ➡️ Client)**

    1. FastAPI는 코드를 생성하며, **파일 단위로 작업이 완료될 때마다** Spring Boot의 내부 웹훅 API를 호출하여 결과물을 청크(Chunk) 형태로 던져줍니다.
    2. Spring Boot는 웹훅으로 받은 코드를 디스크에 저장(File I/O)한 뒤, 열려있는 SSE 파이프를 통해 클라이언트에게 이벤트를 실시간으로 푸시합니다.
    3. 클라이언트는 이벤트를 수신하는 즉시 화면의 코드 뷰어를 렌더링합니다.


## 4. LLM 컨텍스트 관리 (파일 기반 상태 갱신)

LLM의 환각(Hallucination) 현상을 방지하고 토큰 제한을 최적화하기 위해, 무의미한 과거 채팅 내역 대신 **'실제 완성된 최신 파일'을 Source of Truth(단일 진실 공급원)로 사용**

- **Context Consolidation 적용**:

    1. 사용자가 추가/수정 요청을 보냄.
    2. Spring Boot가 해당 프로젝트 디렉토리에서 현재 완성된 최신 코드 파일들의 내용을 읽어옴.
    3. 읽어온 최신 파일 내용과 사용자의 새로운 요청만 JSON으로 묶어 FastAPI로 전송.
    4. FastAPI는 전달받은 최신 베이스 코드를 바탕으로 수정/추가 작업을 수행.

- **기대 효과**: 무거운 채팅 이력 DB 테이블 불필요, AI 응답의 정확도 대폭 상승, 토큰 비용 절감.

## 5. 예외 상황 처리 및 시스템 안정성 설계

실제 서비스 운영 중 발생할 수 있는 데이터 손실 및 AI 오류를 방어하기 위한 3중 안전장치를 도입합니다.

- **5.1. 파일 버전 관리 및 롤백 (Internal Git 활용)**

    - **문제**: 파일이 잘못 수정되었을 때 과거 버전으로 되돌리는 기능이 필요함.
    - **해결**: Spring Boot 서버 내부 프로젝트 폴더에 깃(Git)을 도입. 파일의 생성/수정/삭제 이벤트가 발생하여 디스크에 써질 때마다 백그라운드에서 자동 커밋(`git commit`)을 생성. 사용자의 복구 요청 시 해당 시점의 커밋으로 롤백 처리.

- **5.2. LLM 응답 불량품 필터링 (순수 코드 추출)**

    - **문제**: LLM이 코드 블록 외에 불필요한 설명 텍스트를 포함하여 응답할 수 있음.
    - **해결**: 응답 파싱의 책임은 FastAPI(Worker)가 가짐. FastAPI 서버 내부에서 정규표현식을 활용해 마크다운 코드 블록(예: `java ...` ) 내부의 순수 텍스트만 정제한 뒤, Spring Boot에게는 완벽한 코드만 전송함.

- **5.3. 파일/폴더 삭제 및 구조 변경 규격화**

    - **문제**: 코드를 '생성/수정'하는 것 외에 특정 파일을 '삭제'해야 하는 상황 처리 필요.
    - **해결**: FastAPI와 Spring Boot가 주고받는 Webhook JSON에 `action` 필드를 도입.

    - **Webhook JSON Payload 예시**:
```JSON
{
  "project_id": "proj_12345",
  "files": [
    {
      "action": "CREATE", 
      "file_path": "src/main/java/.../BoardController.java",
      "content": "package cloud.aeranghae..."
    },
    {
      "action": "DELETE", 
      "file_path": "src/main/java/.../OldController.java",
      "content": ""
    }
  ]
}
```
Spring Boot는 위 JSON을 수신하면 `action` 값에 따라 파일 시스템 명령어(생성, 덮어쓰기, 삭제)를 분기하여 실행함.

---