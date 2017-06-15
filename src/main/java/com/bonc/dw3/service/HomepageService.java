package com.bonc.dw3.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import com.bonc.dw3.common.thread.MyRunable;
import com.bonc.dw3.common.thread.MyThread;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.bonc.dw3.mapper.HomepageMapper;
import org.springframework.web.client.RestTemplate;

@Service
@CrossOrigin(origins = "*")
public class HomepageService {

    //日志对象
    private static Logger log = LoggerFactory.getLogger(HomepageService.class);

    //通过zuul向其它服务发送请求的REST对象
    @Autowired
    private RestTemplate restTemplate;

    //mapper对象
    @Autowired
    HomepageMapper homepageMapper;


    /**
     * 1.头部栏组件接口
     *
     * @Author gp
     * @Date 2017/5/27
     */
    public Map<String, Object> headerSelect() {
        Map<String, Object> resMap = new HashMap<>();

        List<Map<String, String>> resList = homepageMapper.headerSelect();
        resMap.put("default", resList.get(0));
        resMap.put("selectList", resList);

        return resMap;
    }


    /**
     * 3.模块选项卡接口
     *
     * @Parameter markType 模块类型
     * @Author gp
     * @Date 2017/5/27
     */
    public List<Map<String, String>> moduleTab(String markType) {
        List<Map<String, String>> resList = homepageMapper.moduleTab(markType);
        return resList;
    }


    /**
     * 6.搜索：全部接口
     *
     * @Parameter searchStr 查询es的参数
     * @Parameter numStart 查询es的起始条数
     * @Parameter num 查询es的数据条数
     * @Author gp
     * @Date 2017/5/18
     */
    public Map<String, Object> allSearch(String searchStr, String numStart, String num) throws InterruptedException {
        //最后返回给前端的结果 {"nextFlag":"","data":""}
        Map<String, Object> resMap = new HashMap<>();
        //所有服务返回的数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        //es返回的所有指标
        List<String> kpiList = new ArrayList<>();
        //es返回的所有专题
        List<String> topicList = new ArrayList<>();
        //es返回的所有报告
        List<String> reportList = new ArrayList<>();

        //1.查询ES，ES中根据权重排序，支持分页，结果中携带排序序号
        log.info("查询es的参数------->" + searchStr);
        Map<String, Object> esMap = requestToES(searchStr);
        log.info("查询es的结果------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的数据的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        int queryNum = Integer.parseInt(num);
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + queryNum - 1;
        String nextFlag = isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        log.info("esCount is "+esCount);
        //根据es返回的数据条数控制线程数组的大小
        MyThread[] myThreads = new MyThread[queryNum];
        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");

        //3.查询类型是全部，需要遍历所有的数据，根据typeId将数据分类并开启子线程查询各个服务得到详细的数据
        startAllThreads(esList, myThreads, kpiList, topicList, reportList);

        //4.汇总所有服务返回的详细数据
        for (int i = 0; i < myThreads.length; i ++){
        	Map<String, Object> map = new HashMap<String, Object>();
        	if(null == myThreads[i]){
        		log.error("thread is null and id is " + i);
        	}else{
        		map = (Map<String, Object>) myThreads[i].result;
        		log.info(i+" thread result is "+map);
        		dataList.add(map);
        	}
        }

        //5.组合es数据和所有服务返回的详细数据
        List<Map<String, Object>> resList = combineAllTypeData(esList, dataList, kpiList, topicList, reportList);
        log.debug(""+resList);
        resMap.put("data", resList);

        return resMap;
    }

    /**
     * 判断是否还有下一页
     * @param esCount es返回结果的条数
     * @param count 前端已经显示的数据条数
     *
     * @Author gp
     * @Date 2017/6/13
     */
    private String isNext(int esCount, int count) {
        String nextFlag = "";
        //如果现在前端显示的总条数小于es的总数，那么还有下一页，反之没有下一页了
        if (count < esCount) {
            nextFlag = "1";
        } else {
            nextFlag = "0";
        }
        return nextFlag;
    }


    /**
     * 综合搜索接口：开启所有线程
     * @param esList es查询结果
     * @param myThreads 线程数组
     * @param kpiList es中的指标数据id集合
     * @param topicList es中的专题数据id集合
     * @param reportList es中的报告数据id集合
     *
     * @Author gp
     * @Date 2017/6/13
     */
    private void startAllThreads(List<Map<String, Object>> esList, MyThread[] myThreads, List<String> kpiList,
                                 List<String> topicList, List<String> reportList) throws InterruptedException {
        if (esList.size() != 0) {
            for (int i = 0; i < esList.size(); i ++) {
                Map<String, Object> map = esList.get(i);
                //数据类型id
                String typeId = map.get("typeId").toString();
                //数据id
                String id = map.get("id").toString();
                //typeId=1指标；2专题；3报告
                if (typeId.equals("1")) {
                    //指标
                    kpiList.add(id);
                    //查询指标服务的参数处理："-1,-1,"查询的是全国，最大账期条件下的数据
                    String paramStr = "-1,-1," + id;
                    //开子线程
                    myThreads[i] = new MyThread(restTemplate,"http://DW3-NEWQUERY-HOMEPAGE-ZUUL-TEST/index/indexForHomepage/dataOfAllKpi", paramStr);
                    myThreads[i].start();
                    myThreads[i].join();
                } else if (typeId.equals("2")) {
                    //专题
                    topicList.add(id);
                    //开子线程
                    myThreads[i] = new MyThread(restTemplate,"http://DW3-NEWQUERY-HOMEPAGE-ZUUL-TEST/subject/specialForHomepage/icon", id);
                    myThreads[i].start();
                    myThreads[i].join();
                } else if (typeId.equals("3")) {
                    //报告
                    reportList.add(id);
                    //开子线程
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-TEST/reportPPT/pptReportForHomepage/info", id);
                    myThreads[i].start();
                    myThreads[i].join();
                } else {
                    log.info("es返回了不存在的type！外星type！");
                }
            }
        }else {
            log.info("es没有返回数据！");
        }
        log.info("指标有-------->" + kpiList);
        log.info("专题有-------->" + topicList);
        log.info("报告有-------->" + reportList);
    }


    /**
     * 综合搜索接口：组合esList和所有服务的返回结果
     * @param esList es查询结果
     * @param dataList 所有服务查询结果
     * @param kpiList es中的指标数据id集合
     * @param topicList es中的专题数据id集合
     * @param reportList es中的报告数据id集合
     *
     * @Author gp
     * @Date 2017/6/13
     */
    private List<Map<String,Object>> combineAllTypeData(List<Map<String, Object>> esList, List<Map<String, Object>> dataList,
                                                        List<String> kpiList, List<String> topicList, List<String> reportList) {
        List<Map<String, Object>> resList = new ArrayList<>();
        for (Map<String, Object> map1 : esList) {
            String typeId = map1.get("typeId").toString();
            //查询数据库得到跳转的url
            String url = homepageMapper.getUrlViaTypeId(typeId);
            String id1 = map1.get("id").toString();
            //指标数据处理
            if (typeId.equals("1") && kpiList.size() != 0) {
                for (Map<String, Object> map2 : dataList) {
                    String id2 = map2.get("id").toString();
                    //es中和指标服务返回的结果id对应上了，组合数据
                    if (id1.equals(id2)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("markType", typeId);
                        map.put("ord", map1.get("ord"));
                        map.put("id", id1);
                        map.put("url", url);
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("markName", map1.get("type"));
                        dataMap.put("title", map1.get("title"));
                        dataMap.put("dayOrMonth", map1.get("dayOrMonth"));
                        //这里的数据全部都只查询全国的数据
                        dataMap.put("area", "全国");
                        dataMap.put("date", map2.get("date"));
                        dataMap.put("dataName", map2.get("dataName"));
                        dataMap.put("dataValue", map2.get("dataValue"));
                        dataMap.put("chartType", map2.get("chartType"));
                        dataMap.put("unit", map2.get("unit"));
                        dataMap.put("chart", map2.get("chart"));
                        map.put("data", dataMap);
                        resList.add(map);
                    }
                }
            } else if (typeId.equals("2") && topicList.size() != 0) {
                //专题数据处理
                for (Map<String, Object> map2 : dataList) {
                    String id2 = map2.get("id").toString();
                    if (id1.equals(id2)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("markType", typeId);
                        map.put("ord", map1.get("ord"));
                        map.put("id", id1);
                        map.put("url", url);
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("src", map2.get("src"));
                        dataMap.put("title", map1.get("title"));
                        dataMap.put("content", map1.get("content"));
                        dataMap.put("type", map1.get("type"));
                        dataMap.put("tabName", map1.get("tabName"));
                        map.put("data", dataMap);
                        resList.add(map);
                    }
                }
            } else if (typeId.equals("3") && reportList.size() != 0) {
                //报告数据处理
                for (Map<String, Object> map2 : dataList) {
                    String id2 = map2.get("id").toString();
                    if (id1.equals(id2)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("markType", typeId);
                        map.put("ord", map1.get("ord"));
                        map.put("id", id1);
                        map.put("url", url);
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("title", map1.get("title"));
                        dataMap.put("img", map2.get("img"));
                        dataMap.put("type", map1.get("type"));
                        dataMap.put("tabName", map1.get("tabName"));
                        dataMap.put("issue", map2.get("issue"));
                        dataMap.put("issueTime", map2.get("issueTime"));
                        map.put("data", dataMap);
                        resList.add(map);
                    }
                }
            } else {
                log.info("es返回了不存在的type！" + "这条非法数据是：" + map1);
            }
        }
        return resList;
    }


    /**
     * 6-2.搜索：指标搜索接口
     * @Parameter searchStr 查询es的参数
     * @Parameter numStart 查询es的起始条数
     * @Parameter num 查询es的数据条数
     * @Parameter area 查询时选定的地域（前端传过来的）
     * @Parameter date 查询时选定的时间（前端传过来的）
     *
     * @Author gp
     * @Date 2017/5/31
     */
    public Map<String, Object> indexSearch(String paramStr, String numStart, String num, String area, String date) throws InterruptedException {
        //最终的返回结果
        Map<String, Object> resMap = new HashMap<>();
        //所有指标的同比环比数据
        List<Map<String, Object>> data = new ArrayList<>();
        //第一个指标的所有图表数据
        Map<String, Object> chartData = new HashMap<>();
        //第一个指标的日月标识
        String fitstDayOrMonth = "";

        //根据地域id得到地域的名称
        String areaStr = homepageMapper.getProvNameViaProvId(area);

        //1.根据搜索关键字查询ES，ES中根据权重排序，支持分页，结果中携带排序序号ES返回结果
        log.info("查询es的参数-------->" + paramStr);
        Map<String, Object> esMap = requestToES(paramStr);
        log.info("查询es的结果-------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        String nextFlag = isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //根据es返回的数据条数控制线程数组的大小，请求全部指标的同比环比数据
        MyThread[] myThreads = new MyThread[esCount];
        //用来给第一条指标数据发请求-请求它的图表数据
        MyThread chartThread = null;
        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //获得用于前端跳转的url
        String typeId = esList.get(0).get("typeId").toString();
        String url = homepageMapper.getUrlViaTypeId(typeId);

        //3.遍历es返回的所有的数据，开启子线程查询指标服务得到详细的数据
        if (esList.size() != 0) {
            url = homepageMapper.getUrlViaTypeId(esList.get(0).get("typeId").toString());
            //for循环得到需要查询的kpi字符串
            for (int i = 0; i < esList.size(); i++) {
                String id = esList.get(i).get("id").toString();
                if (i == 0 ){
                    //es返回的日月标识
                    String dayOrMonth = esList.get(i).get("dayOrMonth").toString();
                    if (dayOrMonth.equals("日报")){
                        fitstDayOrMonth = "1";
                    }else{
                        fitstDayOrMonth = "2";
                    }
                    //拼接所有图表数据接口的请求参数
                    String chartParam = area + "," + date + "," + id + "," + fitstDayOrMonth;
                    //请求图表数据
                    chartThread = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-TEST/index/indexForHomepage/allChartOfTheKpi", chartParam);
                    chartThread.start();
                    chartThread.join();
                    //拼接同比环比接口的请求参数
                    String dataParam = area + "," + date + "," + id;
                    //请求同比环比数据
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-TEST/index/indexForHomepage/dataOfAllKpi", dataParam);
                    myThreads[i].start();
                    myThreads[i].join();
                }else{
                    //拼接同比环比接口的请求参数
                    String dataParam = area + "," + date + "," + id;
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-TEST/index/indexForHomepage/dataOfAllKpi", dataParam);
                    myThreads[i].start();
                    myThreads[i].join();
                }
            }
        } else {
            log.info("es没有返回任何指标数据！！！");
        }

        //4.汇总指标服务返回的详细数据
        //得到所有指标的同比环比数据
        for (int i = 0; i < myThreads.length; i ++){
            Map<String, Object> map = (Map<String, Object>) myThreads[i].result;
            data.add(map);
        }
        log.info("指标服务查询出的同比环比数据是：" + data);
        //得到第一条指标的所有图表数据
        if (chartThread != null){
            chartData = (Map<String, Object>) chartThread.result;
            log.info("指标服务查询出的第一条指标的图表数据是：" + chartData);
        }else{
            log.info("没有开启查询第一条指标的所有图表数据的子线程！！！");
        }

        //5.组合es数据和指标服务返回的详细数据，组合好的数据直接放在esList中
        if (esList.size() == 0) {
            log.info("没有需要查询的指标id！！！");
        } else {
            for (int i = 0; i < esList.size(); i++) {
                Map<String, Object> map1 = esList.get(i);
                String id1 = map1.get("id").toString();
                //第一条数据
                if (i == 0 && chartData != null) {
                    map1.put("markType", map1.get("typeId"));
                    map1.put("markName", map1.get("type"));
                    map1.put("chartData", chartData.get("chartData"));
                    //找同比环比数据
                    if (data.size() != 0){
                        for (int j = 0; j < data.size(); j ++){
                            String id2 = data.get(j).get("id").toString();
                            if (id2.equals(id1)){
                                map1.put("dataName", data.get(j).get("dataName"));
                                map1.put("dataValue", data.get(j).get("dataValue"));
                            }
                        }
                    }
                    map1.put("url", url);
                    //数据是直接放在es返回结果中的，前端不要的字段需要去掉
                    map1.remove("typeId");
                    map1.remove("type");
                } else {
                    if (data != null && data.size() != 0) {
                        for (int j = 1; j < data.size(); j++) {
                            Map<String, Object> map2 = data.get(j);
                            String id2 = map2.get("id").toString();
                            if (id1.equals(id2)) {
                                map1.put("markType", map1.get("typeId"));
                                map1.put("markName", map1.get("type"));
                                map1.put("unit", map2.get("unit"));
                                map1.put("chartType", map2.get("chartType"));
                                map1.put("chart", map2.get("chart"));
                                map1.put("dataName", map2.get("dataName"));
                                map1.put("dataValue", map2.get("dataValue"));
                                map1.put("url", url);
                                if (!StringUtils.isBlank(areaStr)) {
                                    map1.put("area", areaStr);
                                } else {
                                    map1.put("area", "全国");
                                }
                                map1.put("date", date);
                                map1.remove("typeId");
                                map1.remove("type");
                            }
                        }
                    } else {
                        log.info("所有指标数据查询为空！");
                    }
                }
            }

        }
        resMap.put("data", esList);
        return resMap;
    }


    /**
     * 6-3.搜索：专题接口
     *
     * @Author gp
     * @Date 2017/5/31
     */
    public Map<String, Object> specialSearch(String paramStr, String numStart, String num) throws InterruptedException {
        //返回给前端的结果
        Map<String, Object> resMap = new HashMap<>();
        //服务返回的所有详细数据
        List<Map<String, Object>> data = new ArrayList<>();

        //1.根据搜索关键字查询ES，ES中根据权重排序，支持分页，结果中携带排序序号ES返回结果
        log.info("查询es的参数--------->" + paramStr);
        Map<String, Object> esMap = requestToES(paramStr);
        log.info("查询es的结果--------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        String nextFlag = isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //根据es返回的数据条数控制线程数组的大小
        MyThread[] myThreads = new MyThread[esCount];
        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //获得用于前端跳转的url
        String typeId = esList.get(0).get("typeId").toString();
        String url = homepageMapper.getUrlViaTypeId(typeId);

        //3.遍历es返回的所有的数据，开启子线程查询专题服务得到详细的数据
        for (int i = 0; i < esList.size(); i ++) {
            String id = esList.get(i).get("id").toString();
            myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-TEST-TEST/subject/specialForHomepage/icon", id);
            myThreads[i].start();
            myThreads[i].join();
        }

        //4.汇总专题服务返回的详细数据
        for (int i = 0; i < myThreads.length; i ++){
            Map<String, Object> map = (Map<String, Object>) myThreads[i].result;
            data.add(map);
        }
        log.info("专题服务查询出的数据是：" + data);

        //5.组合es数据和专题服务返回的详细数据，组合好的数据直接放在esList中
        combineTopicData(esList, data, url);

        resMap.put("data", esList);
        return resMap;
    }


    /**
     * 专题搜索接口：组合esList和专题服务返回的结果
     * @param esList es返回的数据
     * @param data 专题服务返回的数据
     * @param url 专题数据的跳转路径
     *
     * @Author gp
     * @Date 2017/6/14
     */
    private void combineTopicData(List<Map<String, Object>> esList, List<Map<String, Object>> data, String url) {
        if (esList.size() != 0) {
            if (data.size() != 0) {
                for (Map<String, Object> map1 : esList) {
                    String id1 = map1.get("id").toString();
                    for (Map<String, Object> map2 : data) {
                        String id2 = map2.get("id").toString();
                        if (id1.equals(id2)) {
                            map1.put("src", map2.get("src").toString());
                            map1.put("url", url);
                        }
                    }
                    map1.remove("typeId");
                }
            } else {
                log.info("专题服务查询出的数据为空！！！");
            }
        } else {
            log.info("es没有查询到专题数据！！！");
        }
    }


    /**
     * 6-4.搜索：报告接口
     *
     * @Author gp
     * @Date 2017/5/31
     */
    public Map<String, Object> reportPPTSearch(String paramStr, String numStart, String num) throws InterruptedException {
        //返回给前端的结果
        Map<String, Object> resMap = new HashMap<>();
        //报告服务返回的详细数据
        List<Map<String, Object>> data = new ArrayList<>();

        //1.根据搜索关键字查询ES，ES中根据权重排序，支持分页，结果中携带排序序号ES返回结果
        log.info("查询es的参数--------->" + paramStr);
        Map<String, Object> esMap = requestToES(paramStr);
        log.info("查询es的结果-------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        String nextFlag = isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //根据es返回的数据条数控制线程数组的大小
        MyThread[] myThreads = new MyThread[esCount];
        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //获得用于前端跳转的url
        String typeId = esList.get(0).get("typeId").toString();
        String url = homepageMapper.getUrlViaTypeId(typeId);

        //3.遍历es返回的所有的数据，开启子线程查询报告服务得到详细的数据
        for (int i = 0; i < esList.size(); i ++) {
            String id = esList.get(i).get("id").toString();
            myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-TEST/reportPPT/pptReportForHomepage/info", id);
            myThreads[i].start();
            myThreads[i].join();
        }

        //4.汇总报告服务返回的详细数据
        for (int i = 0; i < myThreads.length; i ++){
            Map<String, Object> map = (Map<String, Object>) myThreads[i].result;
            data.add(map);
        }
        log.info("专题服务查询出的数据是：" + data);

        //5.组合es数据和报告服务返回的详细数据，组合好的数据直接放在esList中
        combineReportData(esList, data, url);

        resMap.put("data", esList);
        return resMap;
    }


    /**
     * 报告搜索接口：组合esList和报告服务返回的结果
     * @param esList es返回的数据
     * @param data 报告服务返回的数据
     * @param url 报告数据的跳转路径
     *
     * @Author gp
     * @Date 2017/6/14
     */
    private void combineReportData(List<Map<String, Object>> esList, List<Map<String, Object>> data, String url) {
        if (esList.size() == 0) {
            log.info("es没有查询到报告数据！！！");
        } else {
            if (data.size() != 0) {
                //拼接数据
                for (Map<String, Object> map1 : esList) {
                    String id1 = map1.get("id").toString();
                    for (Map<String, Object> map2 : data) {
                        String id2 = map2.get("id").toString();
                        if (id1.equals(id2)) {
                            map1.put("img", map2.get("img"));
                            map1.put("issue", map2.get("issue").toString());
                            map1.put("issueTime", map2.get("issueTime").toString());
                            map1.put("url", url);
                        }
                    }
                    map1.remove("typeId");
                }
            } else {
                log.info("报告服务查询出的数据为空！！！");
            }
        }
    }


    /**
     * 7.地域组件接口
     *
     * @Author gp
     * @Date 2017/6/9
     */
    public List<Map<String, Object>> area() {
        List<Map<String, String>> areaList = homepageMapper.getArea();
        //找到所有的prov_id:31省+全国，放到provList里
        List<String> provList = new ArrayList<String>();
        for (Map<String, String> areaMap : areaList) {
            //没读到一个map，将flag=false，表示是一个新的prov_id,还没有放到provList里
            boolean flag = false;
            if (provList != null && provList.size() > 0) {
                //倒序查找provList
                for (int i = provList.size() - 1; i >= 0; i--) {
                    //for(String prov :provList){
                    String prov = provList.get(i);
                    if (prov.equals(areaMap.get("PROV_ID"))) {
                        flag = true;//在provList中找到了一样的prov_id,跳过
                        break;
                    }
                }
                if (flag == false) {
                    provList.add(areaMap.get("PROV_ID"));
                }
            } else {
                provList.add(areaMap.get("PROV_ID"));
            }
        }

        List<Map<String, Object>> resList = new ArrayList<>();
        for (String pro : provList) {
            Map<String, Object> provMap = new HashMap<>();
            provMap.put("proId", pro);

            List<Map<String, String>> cityList = new ArrayList<>();
            int i;
            for (i = 0; i < areaList.size(); i++) {
                Map<String, String> areaMap = areaList.get(i);
                if (pro.equals(areaMap.get("PROV_ID"))) {
                    provMap.put("proName", areaMap.get("PRO_NAME"));

                    Map<String, String> cityMap = new HashMap<>();
                    cityMap.put("cityId", areaMap.get("AREA_ID"));
                    cityMap.put("cityName", areaMap.get("AREA_DESC"));
                    cityList.add(cityMap);
                    provMap.put("city", cityList);

                    areaList.remove(areaMap);
                    i--;
                }

            }
            resList.add(provMap);
        }
        //System.out.println(resList);
        return resList;
    }


    /**
     * 8.日期组件接口
     *
     * @Author gp
     * @Date 2017/6/9
     */
    public String getMaxDate(String dateType) {
        String date = "";
        //是月标识
        if ((!StringUtils.isBlank(dateType)) && dateType.equals("2")) {
            date = homepageMapper.getMonthMaxDate();
        } else {
            //日或者全部标识
            date = homepageMapper.getDayMaxDate();
        }
        return date;
    }


    /**
     * 向es发请求，参数是"userId,searchType,search,tabId,startNum,num"
     *
     * @Author gp
     * @Date 2017/6/12
     */
    public Map<String, Object> requestToES(String paramStr) {
        RestTemplate restTemplateTmp = new RestTemplate();
        //查询参数有可能有中文，需要转码
        Map<String, Object> resMap = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType("text/html; charset=UTF-8");
        headers.setContentType(mediaType);
        HttpEntity<String> requestEntity = new HttpEntity<String>(paramStr, headers);
        resMap = restTemplateTmp.postForObject("http://10.249.216.108:8999/es/explore", requestEntity, Map.class);
        return resMap;
    }

}

