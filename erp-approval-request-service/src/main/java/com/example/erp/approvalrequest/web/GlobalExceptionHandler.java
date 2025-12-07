package com.example.erp.approvalrequest.web;

import com.example.erp.approvalrequest.exception.ApprovalNotFoundException;
import com.example.erp.approvalrequest.exception.InvalidApprovalRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApprovalNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ApprovalNotFoundException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "NOT_FOUND");
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidApprovalRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(InvalidApprovalRequestException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "INVALID_APPROVAL_REQUEST");
        body.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, Object> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("error", "VALIDATION_FAILED");
        body.put("details", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOthers(Exception e) {
        e.printStackTrace();
        Map<String, Object> body = new HashMap<>();
        body.put("error", "INTERNAL_SERVER_ERROR");
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}