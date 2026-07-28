package bnds.chinese.controller;

import bnds.chinese.exception.DataStorageException;
import bnds.chinese.exception.InvalidOperationException;
import bnds.chinese.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return Map.of("code", "VALIDATION_ERROR", "message", "请检查事件信息", "fields", fields);
    }

    @ExceptionHandler(InvalidOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidOperation(InvalidOperationException exception) {
        return Map.of("code", "INVALID_OPERATION", "message", exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NotFoundException exception) {
        return Map.of("code", "NOT_FOUND", "message", exception.getMessage());
    }

    @ExceptionHandler(DataStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> storage(DataStorageException exception) {
        return Map.of("code", "STORAGE_ERROR", "message", exception.getMessage());
    }
}
