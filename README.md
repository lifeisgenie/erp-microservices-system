# ERP Microservices Project

본 프로젝트는 다양한 통신 방식(REST, gRPC, WebSocket)과 이종 저장소(MySQL, MongoDB, In-Memory)를 활용해  
하나의 ERP 시스템처럼 동작하는 마이크로서비스 아키텍처 기반 결재 시스템을 구현하는 것을 목표로 한다.  
각 서비스는 독립적으로 배포되며, 서로 다른 프로토콜을 사용해 통신한다.

## 1. 프로젝트 아키텍처 개요

### 서비스 구성
| 서비스명 | 주요 기능 | 통신 방식 | 저장소 |
|----------|-----------|-----------|---------|
| Employee Service | 직원 CRUD | REST | MySQL |
| Approval Request Service | 결재 요청 생성, 단계 관리 | REST, gRPC Client | MongoDB |
| Approval Processing Service | 결재 승인/반려 처리, 대기열 관리 | REST, gRPC Server | In-Memory |
| Notification Service | 실시간 알림 | WebSocket | 없음 |

## 2. 전체 처리 흐름(승인 시나리오)

1. Requester → Approval Request Service: POST `/approvals`
2. Request Service는 요청 데이터를 MongoDB에 저장
3. Request Service → Processing Service(gRPC): `RequestApproval()` 호출
4. Approver → Processing Service: POST `/process/{approverId}/{requestId}`
5. Processing Service → Request Service(gRPC): `ReturnApprovalResult()`
6. 단계 업데이트 후 다음 결재자에게 gRPC 전달(남아 있을 경우)
7. 모든 단계 승인 시 Notification Service 호출
8. Notification Service → Requester WebSocket 메시지 전송

## 3. 서비스 상세 설명

### 3.1 Employee Service (REST + MySQL)

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
|--------|-----|------|
| POST | /employees | 직원 생성 |
| GET | /employees | 목록 조회 |
| GET | /employees/{id} | 상세 조회 |
| PUT | /employees/{id} | 부서/직책 수정 |
| DELETE | /employees/{id} | 삭제 |

### 3.2 Approval Request Service

#### MongoDB Document
```json
{
  "requestId": 1,
  "requesterId": 1,
  "title": "Expense Report",
  "content": "Travel expenses",
  "steps": [
    { "step": 1, "approverId": 3, "status": "approved" },
    { "step": 2, "approverId": 7, "status": "pending" }
  ],
  "finalStatus": "in_progress"
}
```

### 3.3 Approval Processing Service

#### In-Memory 구조
```json
{
  "7": [
    {
      "requestId": 1,
      "steps": [
        { "step": 1, "approverId": 3, "status": "approved" },
        { "step": 2, "approverId": 7, "status": "pending" }
      ]
    }
  ]
}
```

### 3.4 Notification Service

WebSocket 접속:
```
ws://{host}:8080/ws?id={employeeId}
```

승인 메시지:
```json
{
  "requestId": 1,
  "result": "approved",
  "finalResult": "approved"
}
```

반려 메시지:
```json
{
  "requestId": 1,
  "result": "rejected",
  "rejectedBy": 7,
  "finalResult": "rejected"
}
```

## 4. gRPC 정의(proto)

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
각 서비스 디렉토리에서:
```
./gradlew bootRun
```

## 6. 테스트 시나리오

### 승인 시나리오
1. 직원 생성
2. 결재 요청 생성
3. 1단계 승인
4. 2단계 승인
5. WebSocket 알림 확인

### 반려 시나리오
1단계 또는 2단계에서 반려 시 즉시 종료 및 알림 발생

## 7. 디렉토리 구조

```
학번/
  employee-service/
  approval-request-service/
  approval-processing-service/
  notification-service/
  proto/
    approval.proto
  scripts/
    init_mysql.sql
  k8s/
```
