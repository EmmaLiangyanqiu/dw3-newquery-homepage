package com.bonc.dw3.common.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Candy on 2017/6/13.
 * @author gp
 */
public class MyThread extends Thread {

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

    /**
     * 从请求到返回耗时
     */
    long time;

    public MyThread(RestTemplate restTemplate, String url, String paramStr){
        this.restTemplate = restTemplate;
        this.url = url;
        this.paramStr = paramStr;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        this.result = restTemplate.postForObject(url, paramStr, Object.class);
        this.time = System.currentTimeMillis() - start;
        log.info( "\r\n" + "查询参数：" + paramStr + "\r\n"
                + "返回结果：" + this.result + "\r\n"
                + "耗时：" + this.time + "ms" + "\r\n");
    }
}
