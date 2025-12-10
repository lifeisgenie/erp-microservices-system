# ERP Microservices System

본 프로젝트는 마이크로서비스 아키텍처(MSA)를 기반으로 구현된 ERP 결재 승인 시스템이다.
각 서비스는 REST, gRPC, WebSocket 등 서로 다른 통신 방식을 사용하며,
저장소 또한 MySQL, MongoDB, In-Memory 로 분리하여 실제 기업 ERP의 구조적 특징을 학습하도록 설계되었다.

## 1. 아키텍처 개요



### 서비스 목록

| 서비스명 | 주요 기능 | 통신 방식 | 저장소 |
|----------|-----------|-----------|--------|
| Employee Service | 직원 CRUD | REST | MySQL |
| Approval Request Service | 결재 요청 생성 및 단계 관리 | REST, gRPC Client | MongoDB |
| Approval Processing Service | 결재 승인/반려 처리, 대기열 관리 | REST, gRPC Server | In-Memory |
| Notification Service | 실시간 알림 전송 | WebSocket | 없음 |

## 2. 전체 처리 흐름

1. Requester → Approval Request Service: POST `/approvals`
2. Request Service는 MongoDB에 저장
3. Request Service → Processing Service(gRPC): `RequestApproval()`
4. Approver → Processing Service: 승인/반려 요청
5. Processing Service → Request Service(gRPC): 결과 전달
6. 단계가 남아 있으면 다음 Approver로 전달
7. 모든 단계 승인 시 Notification Service 호출
8. Notification Service → Requester(WebSocket) 알림 전송

## 3. 서비스 상세 설명

### 3.1 Employee Service

#### 테이블 스키마

```sql
CREATE TABLE employees (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  department VARCHAR(100) NOT NULL,
  position VARCHAR(100) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

#### REST API

| Method | URI | 설명 |
|--------|------|------|
| POST | /employees | 직원 생성 |
| GET | /employees | 목록 조회 |
| GET | /employees/{id} | 상세 조회 |
| PUT | /employees/{id} | 수정 |
| DELETE | /employees/{id} | 삭제 |

### 3.2 Approval Request Service

```json
{
  "requestId": 1,
  "requesterId": 1,
  "title": "출장비 결재 요청",
  "content": "부산 학회 참석 비용",
  "steps": [
    { "step": 1, "approverId": 2, "status": "approved" },
    { "step": 2, "approverId": 3, "status": "pending" }
  ],
  "finalStatus": "in_progress"
}
```

### 3.3 Approval Processing Service

```json
{
  "2": [
    {
      "requestId": 1,
      "steps": [
        { "step": 1, "approverId": 2, "status": "approved" },
        { "step": 2, "approverId": 3, "status": "pending" }
      ]
    }
  ]
}
```

### 3.4 Notification Service

WebSocket 접속:

```
ws://{host}:8084/ws?id={employeeId}
```

승인 메시지:

```json
{
  "requestId": 1,
  "finalStatus": "approved"
}
```

반려 메시지:

```json
{
  "requestId": 1,
  "finalStatus": "rejected",
  "rejectedBy": 3
}
```

## 4. gRPC 정의 (Proto)

```proto
syntax = "proto3";
package approval;

service Approval {
  rpc RequestApproval(ApprovalRequest) returns (ApprovalResponse);
  rpc ReturnApprovalResult(ApprovalResultRequest) returns (ApprovalResultResponse);
}

message Step {
  int32 step = 1;
  int32 approverId = 2;
  string status = 3;
}

message ApprovalRequest {
  int32 requestId = 1;
  int32 requesterId = 2;
  string title = 3;
  string content = 4;
  repeated Step steps = 5;
}

message ApprovalResponse {
  string status = 1;
}

message ApprovalResultRequest {
  int32 requestId = 1;
  int32 step = 2;
  int32 approverId = 3;
  string status = 4;
}

message ApprovalResultResponse {
  string status = 1;
}
```

## 5. 실행 방법

### 빌드

```
./gradlew build
```

### 실행

```
./gradlew bootRun
```

## 6. E2E 테스트 시나리오

### 승인 시나리오

1. 직원 생성
2. 결재 요청 생성
3. 1단계 승인
4. 2단계 승인
5. WebSocket 알림 확인

### 반려 시나리오

요청 단계 중 하나라도 반려되면 즉시 종료 후 알림 발생

## 7. test-noti.html 실행 방법

Notification WebSocket 테스트를 위해 반드시 HTTP 서버로 실행해야 한다.

### 7.1 테스트 실행

```
cd test
python3 -m http.server 8000
```

### 7.2 브라우저 접속

```
http://localhost:8000/test-noti.html
```

### 7.3 사용 방법

1. Request ID 입력  
2. Connect 클릭  
3. 결재 승인/반려 시 Notification Service에서 전달되는 STOMP 메시지 수신
