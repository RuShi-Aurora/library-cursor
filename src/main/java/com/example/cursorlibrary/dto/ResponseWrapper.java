package com.example.cursorlibrary.dto;

import lombok.Data;

@Data
public class ResponseWrapper<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ResponseWrapper<T> success(T data) {
        ResponseWrapper<T> wrapper = new ResponseWrapper<>();
        wrapper.setSuccess(true);
        wrapper.setData(data);
        return wrapper;
    }

    public static <T> ResponseWrapper<T> error(String message) {
        ResponseWrapper<T> wrapper = new ResponseWrapper<>();
        wrapper.setSuccess(false);
        wrapper.setMessage(message);
        return wrapper;
    }
} 