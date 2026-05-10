package com.agrosphere.dto.response;
import lombok.*;
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    public static <T> ApiResponse<T> success(T data){return new ApiResponse<>(true,"OK",data);}
    public static <T> ApiResponse<T> success(T data,String message){return new ApiResponse<>(true,message,data);}
    public static ApiResponse<Void> error(String message){return new ApiResponse<>(false,message,null);}
}
