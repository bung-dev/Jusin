package com.jusin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {

    private String status;
    private T data;
    private Integer count;
    private String message;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, int count) {
        return ApiResponse.<T>builder()
                .status("success")
                .data(data)
                .count(count)
                .build();
    }

    public static ApiResponse<Void> error(String code, String message) {
        return ApiResponse.<Void>builder()
                .status("error")
                .message(message)
                .build();
    }
}
