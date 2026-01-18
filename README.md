
### 1. 설계 핵심 요약
- 비동기 AI 처리: 외부 API(Gemini) 호출 시 DB 커넥션 점유를 방지하기 위해 CompletableFuture 사용 및 트랜잭션 분리(ChatTxService).
- 세션 관리: 마지막 대화 기준 30분 초과 시 새로운 스레드를 자동 생성하는 세션 캡슐화 로직 구현.
- 인프라 추상화: AiClient 인터페이스를 통한 모델 확장성 확보 (Gemini 2.5 Flash 기본 적용)
- 보안: 무상태(Stateless) JWT 기반 인증 및 도메인별 접근 권한 제어.

### 2. 프로젝트 상세 구조 및 클래스 역할

#### 도메인 계층 (domain)
- BaseEntity: 생성/수정 시간 공통 관리 (@CreatedDate, @LastModifiedDate)
- User: 회원 정보 및 권한(Role) 관리
- Thread: 1:N 대화 묶음 단위 (30분 세션 식별자)
- Chat: 사용자 질문 및 AI 답변 개별 데이터
- Feedback: 특정 답변에 대한 사용자 평가 (좋아요/싫어요 및 피드백 상태)

#### 서비스 계층 (service)
- auth.UserService: 회원가입, 로그인 처리 및 토큰 발급
- chat.ChatService: 대화 전체 흐름 제어 및 비동기 오케스트레이션
- chat.ChatTxService: 채팅 관련 데이터의 원자적 저장 및 데이터 이력 준비
- feedback.FeedbackService: 피드백 생성(본인 확인 로직 포함) 및 목록 조회, 상태 업데이트
- analysis.AnalysisService: 당일 지표 요약 및 텍스트 데이터의 CSV 가공

#### 인프라 및 설정 (infrastructure / common)
- ai.AiClient: AI 모델 연동을 위한 규격 정의
- ai.GeminiAiClient: Google Gemini API(v1beta) 연동 구현체
- security.SecurityConfig: 스프링 시큐리티 필터 체인 및 경로별 인가 설정
- security.JwtProvider: JWT 토큰 생성, 파싱, 검증 로직
- common.GlobalExceptionHandler: 비즈니스 예외(BusinessException) 및 전역 에러 핸들링

#### 컨트롤러 및 인터페이스 (controller / dto)
- UserController: 계정 관리 및 인증 엔드포인트
- ChatController: 채팅 생성, 목록 조회, 삭제 (Pageable 최적화 적용)
- FeedbackController: 피드백 남기기 및 관리 기능
- AnalysisController: 관리자용 요약 및 리포트 다운로드

### 3. 테스트 상세 가이드

#### 초기 환경 확인
- Database: H2 In-Memory (자동 생성)
- Swagger: http://localhost:8080/swagger-ui.html

#### 상세 테스트 프로세스 및 파라미터

1. 토큰 획득 (Auth)
   - POST /api/v1/users/signup
     - 파라미터: email, password, name, role(USER 또는 ADMIN)
     - 예시: {"email": "test@test.com", "password": "pass", "name": "홍길동", "role": "USER"}
   - POST /api/v1/users/login
     - 파라미터: email, password
     - 응답으로 받은 token 값을 복사

2. 인증 설정 (Swagger)
   - Swagger UI 상단 Authorize 버튼 클릭 후 복사한 토큰 값 입력

3. 채팅 테스트 (Chat)
   - POST /api/v1/chats
     - 파라미터: question(필수), isStreaming(선택), model(선택)
     - 예시: {"question": "스쿼트 자세 알려줘"}
   - 응답에서 chatId(피드백용)와 threadId 확인

4. 대화 목록 조회 (History)
   - GET /api/v1/chats
     - 파라미터: page(페이지), size(개수), sort(정렬필드,방향)
     - 입력팁: sort 칸에 updatedAt,desc 입력 시 최신 활동순 정렬

5. 피드백 남기기 (Feedback)
   - POST /api/v1/feedbacks
     - 파라미터: chatId, isPositive(true/false)
     - 예시: {"chatId": 1, "isPositive": true}
     - 본인 대화가 아닐 경우 403 에러 발생

6. 관리자 기능 (Analysis)
   - GET /api/v1/analysis/summary (오늘의 가입자, 대화수 집계 확인)
   - GET /api/v1/analysis/report/csv (전체 대화 CSV 다운로드)
   - PATCH /api/v1/feedbacks/{id}/status (어드민이 피드백 상태RESOLVED 등으로 변경)

### 4. 과제 수행

#### 과제 분석
- 단순히 AI API를 호출하는 기능을 넘어, 실무 환경에서 발생할 수 있는 '성능 병목'과 '운영 안정성'을 보완하는 데 초점을 맞추어 분석했습니다.
- AI 응답 지연이 DB 커넥션 풀 고갈로 이어지지 않도록 비동기 처리와 트랜잭션의 분리를 중요한 기술적 요구사항으로 정의했습니다.
- 세션(Thread)의 생명주기 관리와 관리자용 지표 요약(CSV 가공 포함)을 통해 운영 실효성을 확보하려 노력했습니다.

#### AI 활용 및 어려움
- AI 페어 프로그래밍을 통해 전체적인 도메인 설계와 리팩토링 시간을 대폭 단축했습니다. 다만, AI가 제안하는 표준적인 코드가 실제 환경(Kotlin, Spring)에서 일으키는 세밀한 오류들을 잡아내고 최적화하는 데 집중했습니다.
- 명세서 테스트 중 Swagger에서 Pageable 매핑이 꼬여 정렬 오류(`PropertyReferenceException`)가 난 지점이나, 코틀린 문자열 템플릿과 `@Value` 문법이 충돌하여 수동으로 이스케이프 처리를 해야 했던 부분 등 언어와 프레임워크 특성에 따른 예외 상황들을 직접 해결하며 완성도를 높였습니다.
- 특히 비동기 처리 시 트랜잭션 커밋 시점에 따라 데이터 가시성 문제가 생기는 부분(`ThreadNotFound`) 등을 잡아내며, AI의 제안을 그대로 수용하기보다 비판적으로 검토하고 기술적으로 보완하는 과정을 거쳤습니다.

#### 핵심 구현 기능 (어려웠던 점)
- AI 호출의 비동기 처리와 트랜잭션 분리가 가장 도전적이었습니다. 일반적인 @Transactional은 메서드 전체를 감싸므로, 수 초가 걸리는 외부 AI API 호출 시 DB 커넥션을 계속 점유하게 됩니다. 이를 해결하기 위해 비즈니스 로직(ChatService)과 DB 트랜잭션 로직(ChatTxService)을 분리했습니다. CompletableFuture를 사용해 AI 호출을 비동기로 실행하고, 결과가 나오면 다시 짧은 트랜잭션을 열어 데이터를 저장함으로써 리소스 효율을 극대화했습니다. 이 과정에서 비동기 스레드 간의 데이터 가시성(Visibility) 문제를 해결하기 위해 트랜잭션 커밋 시점을 정교하게 제어했습니다.
