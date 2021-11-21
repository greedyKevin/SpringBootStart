package com.kevin.start.handler;

import com.kevin.start.result.ResultResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * 切面处理异常
 * @author huang jiahui
 * @date 2021/11/20 10:46
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     *  捕获所有异常
     * @return {@link ResultResponse}
     */
    @ExceptionHandler(value = Exception.class)
    public ResultResponse exceptionHandler(Exception e){
        return new ResultResponse(500,e.getMessage(),null);
    }

//    /**
//     * minIO连接异常
//     * @return {@link ResultResponse}
//     */
//    @ExceptionHandler(value = ServerException.class)
//    public ResultResponse serverExceptionHandler(){
//        return new ResultResponse(500,"Minio连接错误",null);
//    }
//
//    /**
//     * minIO错误返回异常
//     * @return {@link ResultResponse}
//     */
//    @ExceptionHandler(value = ErrorResponseException.class)
//    public ResultResponse errorResponseExceptionHandler(){
//        return new ResultResponse(500,"错误的返回",null);
//    }

    /**
     * 文件IO异常
     * @return {@link ResultResponse}
     */
    @ExceptionHandler(value = IOException.class)
    public ResultResponse ioExceptionHandler(IOException e){
        return new ResultResponse(500,"文件IO错误",null);
    }

}
