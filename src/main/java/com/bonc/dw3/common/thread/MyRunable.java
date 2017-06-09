package com.bonc.dw3.common.thread;

import com.bonc.dw3.service.HomepageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Created by GuoPeng on 2017/5/18.
 */
public class MyRunable implements Runnable {

    private static Logger log = LoggerFactory.getLogger(MyRunable.class);

    //请求的url地址
    String url;
    //请求的参数
    String paramStr;
    //RestTemplate对象
    RestTemplate restTemplate;
    //请求的结果
    public Object result = null;

    public MyRunable(RestTemplate restTemplate, String url, String paramStr){
        this.restTemplate = restTemplate;
        this.url = url;
        this.paramStr = paramStr;
    }

    @Override
    public void run() {
        this.result = restTemplate.postForObject(url, paramStr, Object.class);
        //log.info(this.result.toString());
    }
}
