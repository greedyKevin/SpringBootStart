package com.augurit.service.config.handler;

import com.augurit.service.config.exception.NoAuthException;
import com.augurit.service.utils.dingtalkrobot.DingTalkRobotUtils;
import com.augurit.service.utils.result.ResultResponse;
import com.taobao.api.ApiException;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.sqlite.SQLiteException;

import java.io.IOException;

/**
 * 切面处理异常
 * @author huang jiahui
 * @date 2021/11/20 10:46
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings("all")
public class GlobalExceptionHandler {
    private final DingTalkRobotUtils dingTalkRobotUtils;

    @Value("${dingtalk.url}")
    private String url;
    @Value("${dingtalk.people}")
    private String [] people;
    @Value("${host}")
    private String host;

    /**
     *  捕获所有异常
     * @return {@link ResultResponse}
     */
    @ExceptionHandler(value = Exception.class)
    public ResultResponse exceptionHandler(Exception e) throws ApiException {

        dingTalkRobotUtils.sendMsg(url,e,people,host);

        return new ResultResponse(500,e.getMessage(),null);
    }

    /**
     * minIO连接异常
     * @return {@link ResultResponse}
     */
    @ExceptionHandler(value = ServerException.class)
    public ResultResponse serverExceptionHandler(ServerException e) throws ApiException {
        dingTalkRobotUtils.sendMsg(url,e,people,host);
        return new ResultResponse(500,"minio连接错误",e.getMessage());
    }

    /**
     * minIO错误返回异常
     * @return {@link ResultResponse}
     */
    @ExceptionHandler(value = ErrorResponseException.class)
    public ResultResponse errorResponseExceptionHandler(ErrorResponseException e) throws ApiException {
        dingTalkRobotUtils.sendMsg(url,e,people,host);
        return new ResultResponse(500,"错误的返回",e.getMessage());
    }

    /**
     * 文件IO异常
     * @return {@link ResultResponse}
     */
    @ExceptionHandler(value = IOException.class)
    public ResultResponse ioExceptionHandler(IOException e) throws ApiException {
        dingTalkRobotUtils.sendMsg(url,e,people,host);
        return new ResultResponse(500,"文件IO错误",e.getMessage());
    }

    /**
     * 权限异常
     * @return {@link ResultResponse}
     */
    @ExceptionHandler(value = NoAuthException.class)
    public ResultResponse noAuthException(NoAuthException e) throws ApiException {
        return new ResultResponse(401,e.getMessage(),null);
    }

    @ExceptionHandler(value = SQLiteException.class)
    public ResultResponse sqlException(SQLiteException e) throws ApiException {
        dingTalkRobotUtils.sendMsg(url,e,people,host);
        return new ResultResponse(500,"sql错误",e.getMessage());
    }
}
