package com.wallet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse <T> {

    private String status;
    private String message;
    private T data;
    private LocalDateTime timeStamp;

    public ApiResponse() {
        this.timeStamp = LocalDateTime.now();
    }

    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
        this.timeStamp = LocalDateTime.now();
    }

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timeStamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>("SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "Operation completed successfully", data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>("SUCCESS", message, null);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>("ERROR", message);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timeStamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timeStamp = timestamp;
    }
}
