package com.bonc.dw3.controller;

import com.bonc.dw3.common.thread.TestThread;
import com.bonc.dw3.service.HomepageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "首页测试1102", description = "测试")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/gpTest")
public class TestController {


    /*@Autowired
    RestTemplate restTemplate;*/

    /**
     * 1.头部栏组件接口
     *
     * @Parameter paramMap 例：{"userId":"41","token":"2"}
     * @Author gp
     * @Date 2017/5/27
     */
    @ApiOperation("1.测试接口")
    @PostMapping("/test")
    public String headerSelect(@ApiParam("请求参数json串") @RequestBody String paramStr,
                               Model model) {
        Map<String, Object> paramMap = new HashMap<>();

        RestTemplate restTemplateTmp = new RestTemplate();
        //查询参数有可能有中文，需要转码
        Map<String, Object> resMap = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType("text/html; charset=UTF-8");
        headers.setContentType(mediaType);
        HttpEntity<String> requestEntity = new HttpEntity<String>(paramStr, headers);
        //resMap = restTemplateTmp.postForObject("http://10.249.216.108:8998/es/explore", requestEntity, Map.class);
        resMap = restTemplateTmp.postForObject("http://10.249.216.117:8998/es/explore", requestEntity, Map.class);
        Map<String, Object> dimensionMap = (Map<String, Object>) resMap.get("dimension");
        paramMap.put("selectType", dimensionMap.get("selectType"));
        paramMap.put("provId", dimensionMap.get("provId"));
        paramMap.put("cityId", dimensionMap.get("cityId"));
        TestThread testThread = new TestThread(new RestTemplate(), "http://10.249.216.57:9777/gpTest/test", paramMap);
        testThread.start();
        String result = testThread.result.toString();
        return result;
    }




}