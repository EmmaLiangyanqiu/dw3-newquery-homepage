package com.bonc.dw3.common.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author guopeng
 * @date 2017/12/19
 * @description
 */
public class MyCallable implements Callable{

    private static Logger log = LoggerFactory.getLogger(MyThread.class);

    /**
     * 发送请求的rest对象
     */
    RestTemplate restTemplate;

    /**
     * 请求的url地址
     */
    String url;

    /**
     * 请求的参数
     */
    String paramStr;

    /**
     * 请求返回的结果
     */
    public Object result = null;

    public MyCallable(RestTemplate restTemplate, String url, String paramStr){
        this.restTemplate = restTemplate;
        this.url = url;
        this.paramStr = paramStr;
    }

    @Override
    public Object call() throws Exception {
        long start = System.currentTimeMillis();
        this.result = restTemplate.postForObject(url, paramStr, Object.class);
        log.info( "\r\n" + "查询参数：" + paramStr + "\r\n"
                + "返回结果：" + this.result + "\r\n"
                + "耗时：" + (System.currentTimeMillis() - start) + "ms" + "\r\n");
        return this.result;
    }
}
