package com.bonc.dw3.service;

import com.bonc.dw3.common.thread.MyThread;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guopeng
 * @date 2017/12/15
 * @description 首页service的辅助service
 */
@Service
@CrossOrigin(origins = "*")
public class HomepageSubclassService {
    /**
     * 日志对象
     */
    private static Logger log = LoggerFactory.getLogger(HomepageSubclassService.class);


    /**
     * 向es搜索引擎发请求
     * @param paramStr "userId,searchType,search,tabId,startNum,num"即"用户Id,搜索类型,搜索内容,日月标识,起始条数,记录条数"
     */
    public Map<String, Object> requestToES(String paramStr) {
        RestTemplate restTemplateTmp = new RestTemplate();
        //查询参数有可能有中文，需要转码
        Map<String, Object> resMap = new HashMap<>(10);
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType("text/html; charset=UTF-8");
        headers.setContentType(mediaType);
        HttpEntity<String> requestEntity = new HttpEntity<String>(paramStr, headers);
        //es支持维度搜索
        resMap = restTemplateTmp.postForObject("http://10.249.216.116:8998/es/explore", requestEntity, Map.class);
        return resMap;
    }


    /**
     * 汇总所有线程返回的数据
     * @param myThreads 线程数组
     */
    public List<Map<String, Object>> getMyThreadsData(MyThread[] myThreads) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i < myThreads.length; i++) {
            if (null == myThreads[i]) {
                log.error("thread is null and id is " + i);
            } else {
                Map<String, Object> map = (Map<String, Object>) myThreads[i].result;
                dataList.add(map);
            }
        }
        return dataList;
    }


    /**
     * join全部线程
     * @param myThreads 线程数组
     */
    public void joinAllThreads(MyThread[] myThreads) throws InterruptedException {
        for (int i = 0; i < myThreads.length; i++) {
            if (myThreads[i] != null){
                myThreads[i].join();
            }
        }
    }


    /**
     * 判断是否还有下一页
     * @param esCount es返回结果的条数
     * @param count   前端已经显示的数据条数
     */
    public String isNext(int esCount, int count) {
        String nextFlag = "";
        //如果现在前端显示的总条数小于es的总数，那么还有下一页，反之没有下一页了
        if (count < esCount) {
            nextFlag = SystemVariableService.hasNext;
        } else {
            nextFlag = SystemVariableService.noNext;
        }
        return nextFlag;
    }


    /**
     * 单位是否为null的判断，为null时处理为空字符串
     * 不为null时，取它本身即可
     */
    public String dealUnit(Object o) {
        String unit = "";
        if (o != null) {
            unit = o.toString();
        }
        return unit;
    }


    /**
     * 判断是否占比指标
     */
    public String isPercentageKpi(String unitNow) {
        String isPercentage = "";
        boolean flag = (!StringUtils.isBlank(unitNow)) && ("%".equals(unitNow) || "PP".equals(unitNow) || "pp".equals(unitNow));
        if (flag) {
            isPercentage = "1";
        } else {
            isPercentage = "0";
        }
        return isPercentage;
    }

}
