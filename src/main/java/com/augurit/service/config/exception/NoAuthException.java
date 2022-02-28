package com.augurit.service.config.exception;

/**
 * 无权限错误
 * @author huang jiahui
 * @date 2021/12/8 9:11
 */
public class NoAuthException extends RuntimeException{
    public NoAuthException(String message , Throwable cause) {
        super(message, cause);
    }

    public NoAuthException(String message){
        super(message);
    }
}
