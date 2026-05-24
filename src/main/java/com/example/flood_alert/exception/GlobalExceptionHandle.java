package com.example.flood_alert.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.flood_alert.dbo.response.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandle {
    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ApiResponse> handlingRuntimeExcetion(RuntimeException exception){
        ApiResponse apiResponse=new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse> handlingAppException(AppException ex){
        ErrorCode errorCode=ex.getErrorCode();

        ApiResponse apiResponse=new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception){
        String enumKey=exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode=ErrorCode.INVALID_KEY;

        try{
            errorCode=ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        ApiResponse apiResponse=new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }
}

