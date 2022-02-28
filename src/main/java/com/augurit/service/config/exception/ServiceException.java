package com.augurit.service.config.exception;

import lombok.NoArgsConstructor;

/**
 * 自定义service层 Exception
 * @author huang jiahui
 * @date 2021/11/20 11:21
 */
@NoArgsConstructor
public class ServiceException extends RuntimeException{

    public ServiceException(String message , Throwable cause) {
        super(message, cause);
    }

    public ServiceException(String message){
        super(message);
    }
}
