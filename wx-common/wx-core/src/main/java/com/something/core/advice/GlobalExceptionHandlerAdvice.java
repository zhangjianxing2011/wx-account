package com.something.core.advice;

import com.something.core.constant.ResultMsg;
import com.something.core.exception.MyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @Date 2023/5/18
 * @Version 1.0
 */

@RestControllerAdvice
public class GlobalExceptionHandlerAdvice {

    @ExceptionHandler(MyException.class)
    public ResponseEntity<ResultMsg<Object>> handleBusinessException(MyException e) {
        return new ResponseEntity<>(new ResultMsg<>(e.getCode(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResultMsg<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return new ResponseEntity<>(new ResultMsg<>(HttpStatus.BAD_REQUEST.value(), "Required request body is missing"), HttpStatus.BAD_REQUEST);
    }

    /**
     * http请求的方法不正确
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<ResultMsg<Object>> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e) {
        return new ResponseEntity<>(new ResultMsg<>(HttpStatus.METHOD_NOT_ALLOWED.value(), e.getMessage()), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultMsg<Object>> handleTestServiceException(Exception e) {
        if (e instanceof DataIntegrityViolationException) {
            DataIntegrityViolationException err = (DataIntegrityViolationException) e;
            return new ResponseEntity<>(new ResultMsg<>(HttpStatus.SERVICE_UNAVAILABLE.value(), "error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        System.out.println(e.getMessage());
        return new ResponseEntity<>(new ResultMsg<>(HttpStatus.SERVICE_UNAVAILABLE.value(), "error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultMsg<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().sorted(Comparator.comparing(FieldError::getField)).collect(Collectors.toList()).get(0);
        String errorMessage = fieldError != null ? fieldError.getField() + fieldError.getDefaultMessage() : "Request parameter validation failed";
        return new ResponseEntity<>(new ResultMsg<>(HttpStatus.BAD_REQUEST.value(), errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ResultMsg<?>> handleBindException(BindException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().sorted(Comparator.comparing(FieldError::getField)).collect(Collectors.toList()).get(0);
        String errorMessage = fieldError != null ? fieldError.getField() + " can`t be null" : "Request parameter validation failed";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResultMsg(HttpStatus.BAD_REQUEST.value(), errorMessage));
    }
}
