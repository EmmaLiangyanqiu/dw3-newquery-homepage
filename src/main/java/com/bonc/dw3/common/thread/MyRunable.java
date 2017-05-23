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

    @Autowired
    RestTemplate restTemplate;

    private static Logger log = LoggerFactory.getLogger(MyRunable.class);

    //请求的url地址
    String url;
    //请求的参数
    MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
    //请求的结果
    List<Map<String, Object>> resList = new ArrayList<>();

    public MyRunable(String url, MultiValueMap<String, Object> paramMap){
        this.url = url;
        this.paramMap = paramMap;
    }

    @Override
    public void run() {
        long current =System.currentTimeMillis();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Accept", "*/*");
        HttpEntity<MultiValueMap<String, Object>> formEntity = new HttpEntity<>(paramMap, httpHeaders);
        this.resList = restTemplate.postForObject(url, formEntity, List.class);
        log.info("当前线程拿到结果所用时间:" + (System.currentTimeMillis()-current) + "ms"
                + "线程号[" + Thread.currentThread().getId() + "]");
    }
}
