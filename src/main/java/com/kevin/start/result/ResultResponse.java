package com.kevin.start.result;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author huang jiahui
 * @date 2021/11/11 9:24
 */
@Data
public class ResultResponse<T> implements Serializable {
    private static Integer SUCCESS_CODE = 200;
    private static Integer FAIL_CODE = 500;

    /**
     * 状态码
     */
    private Integer status;

    /**
     * 信息
     */
    private String  message;

    /**
     * 结果数据
     */
    private T data;


    public ResultResponse(Integer status, String msg,T data){
        this.status = status;
        this.message = msg;
        this.data = data;
    }

    public static<T> ResultResponse<T> success(T data){
        return new ResultResponse<>(SUCCESS_CODE,"success",data);
    }

    public static<T> ResultResponse<T> fail(String msg,T data){
        return new ResultResponse<>(FAIL_CODE,msg,data);
    }

    public static<T> ResultResponse<T> error(Throwable t){
        return new ResultResponse<>(FAIL_CODE,t.getMessage(),null);
    }


}
