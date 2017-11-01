package com.bonc.dw3.common.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Created by Candy on 2017/6/13.
 */
public class TestThread extends Thread {

    private static Logger log = LoggerFactory.getLogger(TestThread.class);

    //发送请求的rest对象
    RestTemplate restTemplate;
    //请求的url地址
    String url;
    //String paramStr;
    //请求的参数
    Map<String, Object> paramMap;
    //从请求到返回耗时
    //long time;
    public Object result = null; //请求返回的结果


    public TestThread(RestTemplate restTemplate, String url, Map<String, Object> paramMap){
        this.restTemplate = restTemplate;
        this.url = url;
        this.paramMap = paramMap;
    }

    @Override
    public void run() {
        log.info("T-----查询服务的参数是：" + paramMap);
        this.result = restTemplate.postForObject(url, paramMap, Object.class);
        log.info("T-----返回的结果是：" + this.result);
    }
}
