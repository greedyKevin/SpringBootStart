package com.augurit.service.utils.dingtalkrobot;


import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.taobao.api.ApiException;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * 钉钉机器人告警
 *
 * @author huang jiahui
 * @date 2022/2/14 17:19
 */
@Component
public class DingTalkRobotUtils {

    /**
     * @param url    钉钉机器人url
     * @param e      报错信息
     * @param people 提示人员
     * @throws ApiException
     */
    public void sendMsg(String url, Exception e, String[] people,String host) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(url);
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("text");

        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent("后台错误： \n" +
                 "来自服务器：" + host + "\n"
                + "错误信息" + e.getMessage() + "\n"
                + printStackTraceToString(e) +
                 "请检查日志。");
        request.setText(text);

        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setAtMobiles(Arrays.asList(people));
        request.setAt(at);
        client.execute(request);
    }

    private String printStackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return "\r\n" + sw + "\r\n";
    }
}
