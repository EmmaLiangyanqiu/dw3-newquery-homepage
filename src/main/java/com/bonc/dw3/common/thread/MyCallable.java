package com.bonc.dw3.common.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * @author guopeng
 * @date 2017/12/19
 * @description
 */
public class MyCallable implements Callable {

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
     * 请求的参数(map)
     */
    HashMap<String, Object> paramMap;

    /**
     * 请求的参数(String)
     */
    String paramStr;
    /**
     * 请求的参数类型(String)
     */
    String paramType;
    /**
     * 请求返回的结果
     */
    public Object result = null;

    public MyCallable(RestTemplate restTemplate, String url, String paramStr) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.paramStr = paramStr;
    }

    public MyCallable(RestTemplate restTemplate, String url, HashMap<String, Object> paramMap, String paramType) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.paramMap = paramMap;
        this.paramType = paramType;
    }

    @Override
    public Object call() throws Exception {
        long start = System.currentTimeMillis();
        if (this.paramType.equals("map")) {
            this.result = restTemplate.postForObject(url, paramMap, Object.class);
        } else {
            this.result = restTemplate.postForObject(url, paramStr, Object.class);
        }
        log.info("\r\n" + "查询参数paramStr为：" + paramStr + "\r\n"
                + "查询参数paramMap为：" + paramMap.toString() + "\r\n"
                + "返回结果为：" + this.result + "\r\n"
                + "耗时为：" + (System.currentTimeMillis() - start) + "ms" + "\r\n");

        return this.result;
    }
}
