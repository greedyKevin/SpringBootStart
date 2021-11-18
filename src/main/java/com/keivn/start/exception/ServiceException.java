package com.keivn.start.exception;

/**
 *
 * @author huang jiahui
 * @date 2021/11/18 14:26
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String msg,Throwable cause) {
        super(msg, cause);
    }
}
