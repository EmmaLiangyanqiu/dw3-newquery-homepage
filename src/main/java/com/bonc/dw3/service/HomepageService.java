package com.bonc.dw3.service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.bonc.dw3.common.thread.MyCallable;
import com.bonc.dw3.common.thread.MyThread;
import com.bonc.dw3.mapper.UserInfoMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.bonc.dw3.mapper.HomepageMapper;
import org.springframework.web.client.RestTemplate;

/**
 * @author Candy
 */
@Service
@CrossOrigin(origins = "*")
public class HomepageService {
    /**
     * 日志对象
     */
    private static Logger log = LoggerFactory.getLogger(HomepageService.class);
    //通过zuul向其它服务发送请求的REST对象
    @Autowired
    private RestTemplate restTemplate;
    //mapper对象
    @Autowired
    HomepageMapper homepageMapper;
    @Autowired
    UserInfoMapper userInfoMapper;
    //系统变量对象
    @Autowired
    SystemVariableService systemVariableService;
    //子类service
    @Autowired
    HomepageSubclassService subclassService;

    /**
     * 1.头部栏组件接口
     */
    public Map<String, Object> headerSelect() {
        Map<String, Object> resMap = new HashMap<>(10);
        List<Map<String, String>> resList = homepageMapper.headerSelect();
        //默认是专题
        resMap.put("default", resList.get(1));
        resMap.put("selectList", resList);
        return resMap;
    }

    /**
     * 3.模块选项卡接口
     * @param markType 模块类型
     */
    public List<Map<String, String>> moduleTab(String markType) {
        List<Map<String, String>> resList = homepageMapper.moduleTab(markType);
        return resList;
    }

    /**
     * 6.搜索：全部接口
     * @param searchStr 查询es的参数
     * @param numStart  查询es的起始条数
     * @param num       查询es的数据条数
     * @param userId    用户Id
     */
    public Map<String, Object> allSearch(String searchStr,
                                         String numStart,
                                         String num,
                                         String userId) throws InterruptedException {
        //最后返回给前端的结果 {"nextFlag":"","data":""}
        Map<String, Object> resMap = new HashMap<>(5);
        //es返回的所有指标
        List<String> kpiList = new ArrayList<>();
        //es返回的所有专题
        List<String> topicList = new ArrayList<>();
        //es返回的所有报告
        List<String> reportList = new ArrayList<>();
        //es返回的所有报表
        List<String> statementList = new ArrayList<>();

        //1.查询ES，ES中根据权重排序，支持分页，结果中携带排序序号
        log.info("查询es的参数------->" + searchStr);
        Map<String, Object> esMap = subclassService.requestToES(searchStr);
        log.info("查询es的结果------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的数据的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        String nextFlag = subclassService.isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //根据es返回的数据条数控制线程数组的大小
        MyThread[] myThreads = new MyThread[esList.size()];

        //获取用户的省份权限，查询详细数据时就只能查询该省份
        String provId=userInfoMapper.queryProvByUserId(userId);
        String areaStr = homepageMapper.getProvNameViaProvId(provId);
        log.info("该用户的省份权限为：" + provId);

        //3.查询类型是全部，需要遍历所有的数据，根据typeId将数据分类并开启子线程查询各个服务得到详细的数据
        //es支持地域维度搜索，并过滤用户的省份权限，首页服务的全部搜索接口不再过滤省份权限
        startAllThreads(esList, myThreads, kpiList, topicList, reportList,statementList, userId, provId);

        //4.join全部线程
        subclassService.joinAllThreads(myThreads);

        //5.汇总所有服务返回的详细数据
        //所有服务返回的数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        dataList = subclassService.getMyThreadsData(myThreads);
        //报表详细数据
        List<Map<String, Object>> statementDataList = new ArrayList<>();
        if (statementList.size() != 0){
            //查询数据
            statementDataList = homepageMapper.selectStatementData(statementList);
            dataList.addAll(statementDataList);
        }

        //6.过滤无效数据
        List<Map<String, Object>> dataListFinally = new ArrayList<>();
        if ((dataList.size()) != 0 && (dataList != null)){
            for (int j = 0; j < dataList.size(); j++) {
                if (!dataList.get(j).containsKey("id")) {
                    log.info(dataList.get(j) + "----数据没有返回id，舍弃！！！");
                } else {
                    String id = (String) dataList.get(j).get("id");
                    if (StringUtils.isBlank(id)){
                        log.info(dataList.get(j) + "----数据返回无效的id，舍弃！！！");
                    }else{
                        dataListFinally.add(dataList.get(j));
                    }
                }
            }
        }else{
            log.info("全部数据为空！！！");
        }

        //7.组合es数据和所有服务返回的详细数据
        List<Map<String, Object>> resList = combineAllTypeData(esList, dataListFinally, kpiList, topicList, reportList, statementList, areaStr);
        resMap.put("data", resList);

        return resMap;
    }

    /**
     * 6-2.搜索：指标搜索接口
     * @param paramStr 查询es的参数
     * @param numStart 查询es的起始条数
     * @param num 查询es的数据条数
     * @param area 查询时选定的地域（前端传过来的）
     * @param date 查询时选定的时间（前端传过来的）
     * @param userId 用户id
     */
    public Map<String, Object> indexSearch(String paramStr,
                                           String numStart,
                                           String num,
                                           String area,
                                           String date,
                                           String userId) throws InterruptedException, ExecutionException {
        //最终的返回结果
        Map<String, Object> resMap = new HashMap<>(5);
        //所有指标的同比环比数据
        List<Map<String, Object>> data = new ArrayList<>();
        //第一个指标的所有图表数据
        Map<String, Object> chartData = new HashMap<>(15);
        //第一个指标的日月标识
        String fitstDayOrMonth = "";
        //跳转的url
        String url = "";
        //前端请求的起始条数
        int numStartValue = Integer.parseInt(numStart);

        //根据地域id得到地域的名称
        String areaStr = homepageMapper.getProvNameViaProvId(area);

        //1.根据搜索关键字查询ES，ES中根据权重排序，支持分页，结果中携带排序序号ES返回结果
        log.info("查询es的参数-------->" + paramStr);
        long esStart = System.currentTimeMillis();
        Map<String, Object> esMap = subclassService.requestToES(paramStr);
        long esTime = System.currentTimeMillis() - esStart;
        log.info("查询es的结果-------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        String nextFlag = subclassService.isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //根据es返回的数据条数控制线程数组的大小，请求全部指标的同比环比数据
        //MyThread[] myThreads = new MyThread[esList.size()];
        //用来给第一条指标数据发请求-请求它的图表数据
        //MyThread chartThread = null;

        ExecutorService threadPool = Executors.newCachedThreadPool();

        //用于打印时间
        long start = System.currentTimeMillis();

        //3.遍历es返回的所有的数据，开启子线程查询指标服务得到详细的数据
        //startKpiThreads(esList, myThreads, chartThread, numStartValue, fitstDayOrMonth, url, area, date);
        if (esList.size() != 0) {
            url = homepageMapper.getUrlViaTypeId(esList.get(0).get("typeId").toString());
            //for循环得到需要查询的kpi字符串
            for (int i = 0; i < esList.size(); i++) {
                String id = esList.get(i).get("id").toString();
                if (i == 0 && numStartValue == 1) {
                    //es返回的日月标识
                    String dayOrMonth = esList.get(i).get("dayOrMonth").toString();
                    if ("日报".equals(dayOrMonth)) {
                        fitstDayOrMonth = SystemVariableService.day;
                    } else {
                        fitstDayOrMonth = SystemVariableService.month;
                    }
                    //拼接所有图表数据接口的请求参数
                    String chartParam = area + "," + date + "," + id + "," + fitstDayOrMonth + "," + userId;
                    //请求图表数据
                    MyCallable chartCallable = new MyCallable(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE-V1/index/indexForHomepage/allChartOfTheKpi", chartParam);
                    Future chartFuture = threadPool.submit(chartCallable);
                    chartData = (Map<String, Object>) chartFuture.get();
                    //拼接同比环比接口的请求参数
                    String dataParam = area + "," + date + "," + id + "," + userId;
                    //请求同比环比数据
                    MyCallable dataCallable = new MyCallable(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE-V1/index/indexForHomepage/dataOfAllKpi", dataParam);
                    Future dataFuture = threadPool.submit(dataCallable);
                    Map<String, Object> map = (Map<String, Object>) dataFuture.get();
                    data.add(map);
                } else {
                    //拼接同比环比接口的请求参数
                    String dataParam = area + "," + date + "," + id + "," + userId;
                    MyCallable dataCallable = new MyCallable(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE-V1/index/indexForHomepage/dataOfAllKpi", dataParam);
                    Future dataFuture = threadPool.submit(dataCallable);
                    Map<String, Object> map = (Map<String, Object>) dataFuture.get();
                    data.add(map);
                }
            }
        } else {
            log.info("es没有返回任何指标数据！！！");
        }

        //4.join全部线程
        /*subclassService.joinAllThreads(myThreads);
        if (null != chartThread) {
            chartThread.join();
        }*/
        long threadTime = System.currentTimeMillis() - start;
        log.info("es查询到返回的时间:" + esTime + "ms");
        log.info("所有线程返回的时间:" + threadTime + "ms");
        long allThreadsJoin = System.currentTimeMillis();

        //5.汇总指标服务返回的详细数据
        //得到所有指标的同比环比数据
        //data = subclassService.getMyThreadsData(myThreads);
        //得到第一条指标的所有图表数据
        /*if (null != chartThread) {
            chartData = (Map<String, Object>) chartThread.result;
        } else {
            log.info("没有开启查询第一条指标的所有图表数据的子线程！！！");
        }*/
        log.info("汇总所有服务返回的数据耗时:" + (System.currentTimeMillis() - allThreadsJoin) + "ms");
        long getAllData = System.currentTimeMillis();

        //6.数据过滤：清理从指标服务返回的不合格数据(没有id的数据)
        //过滤图表数据
        String idStr = "id";
        if ((chartData != null) && (!chartData.containsKey(idStr))) {
            log.info(chartData + "------chartData没有返回id，舍弃！！！");
            chartData = null;
        }else if ((chartData != null) && (chartData.containsKey(idStr))){
            String id = (String) chartData.get("id");
            if (StringUtils.isBlank(id)){
                log.info(chartData + "------chartData返回无效的id，舍弃！！！");
                chartData = null;
            }
        }
        //过滤同比环比数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        if ((data.size()) != 0 && (data != null)){
            for (int j = 0; j < data.size(); j++) {
                if (!data.get(j).containsKey("id")) {
                    log.info(data.get(j) + "----同环比数据没有返回id，舍弃！！！");
                } else {
                    String id = (String) data.get(j).get("id");
                    if (StringUtils.isBlank(id)){
                        log.info(data.get(j) + "----同环比数据返回无效的id，舍弃！！！");
                    }else{
                        dataList.add(data.get(j));
                    }
                }
            }
        }else{
            log.info("全部指标的同比环比数据为空！！！");
        }
        log.info("过滤不合格数据耗时:" + (System.currentTimeMillis() - getAllData) + "ms");
        long filterData = System.currentTimeMillis();

        //7.组合es数据和指标服务返回的详细数据，组合好的数据直接放在esList中
        List<Map<String, Object>> resList = new ArrayList<>();
        if (esList.size() == 0) {
            log.info("没有需要查询的指标id！！！");
        } else {
            for (int i = 0; i < esList.size(); i++) {
                Map<String, Object> map1 = esList.get(i);
                String id1 = map1.get("id").toString();
                //第一条数据
                if (i == 0 && chartData != null && numStartValue == 1) {
                    Map<String, Object> map = new HashMap<>(20);
                    map.put("ord", map1.get("ord"));
                    map.put("dayOrMonth", map1.get("dayOrMonth"));
                    map.put("id", map1.get("id"));
                    map.put("isMinus", map1.get("isMinus"));
                    map.put("title", map1.get("title"));
                    map.put("markType", map1.get("typeId"));
                    map.put("markName", map1.get("type"));
                    map.put("chartData", chartData.get("chartData"));
                    //返回账期（格式转换）
                    String dateStr = subclassService.toChineseDateString(chartData.get("date").toString());
                    map.put("date", dateStr);
                    //返回地域
                    if (!StringUtils.isBlank(areaStr)) {
                        map.put("area", areaStr);
                    } else {
                        map.put("area", "全国");
                    }
                    map.put("url", url);
                    //找同比环比数据
                    if (dataList.size() != 0) {
                        for (int j = 0; j < dataList.size(); j++) {
                            String id2 = dataList.get(j).get("id").toString();
                            if (id2.equals(id1)) {
                                map.put("dataName", dataList.get(j).get("dataName"));
                                map.put("dataValue", dataList.get(j).get("dataValue"));
                                map.put("unit", dataList.get(j).get("unit"));
                            }
                        }
                    }
                    //看是否找到了同比环比数据，如果没找到，置空结构，字段不能少
                    if (!(map.containsKey("dataName") || map.containsKey("dataValue") || map.containsKey("unit"))) {
                        String[] a = {"-", "-", "-", "-"};
                        map.put("dataName", a);
                        map.put("dataValue", a);
                        map.put("unit", "");
                    }
                    //单位为null的判断和处理：这里不处理的话，下面toString时可能报错
                    String unit = subclassService.dealUnit(map.get("unit"));
                    map.put("unit", unit);
                    //是否占比指标
                    String unitNow = map.get("unit").toString();
                    String isPercentage = subclassService.isPercentageKpi(unitNow);
                    map.put("isPercentage", isPercentage);

                    resList.add(map);
                } else {
                    if (dataList != null && dataList.size() != 0) {
                        //除第一条指标以外的指标
                        for (int j = 0; j < dataList.size(); j++) {
                            Map<String, Object> map2 = dataList.get(j);
                            String id2 = map2.get("id").toString();
                            if (id1.equals(id2)) {
                                Map<String, Object> map = new HashMap<>(20);
                                map.put("ord", map1.get("ord"));
                                map.put("dayOrMonth", map1.get("dayOrMonth"));
                                map.put("id", map1.get("id"));
                                map.put("isMinus", map1.get("isMinus"));
                                map.put("title", map1.get("title"));
                                map.put("markType", map1.get("typeId"));
                                map.put("markName", map1.get("type"));

                                //单位为null的处理
                                String unit = subclassService.dealUnit(map2.get("unit"));
                                map.put("unit", unit);

                                //是否占比指标
                                String unitNow = map.get("unit").toString();
                                String isPercentage = subclassService.isPercentageKpi(unitNow);
                                map.put("isPercentage", isPercentage);

                                map.put("chartType", map2.get("chartType"));
                                map.put("chart", map2.get("chart"));
                                map.put("dataName", map2.get("dataName"));
                                map.put("dataValue", map2.get("dataValue"));
                                map.put("url", url);
                                if (!StringUtils.isBlank(areaStr)) {
                                    map.put("area", areaStr);
                                } else {
                                    map.put("area", "全国");
                                }
                                map.put("date", map2.get("date"));
                                resList.add(map);
                            }
                        }
                    } else {
                        log.info("所有指标数据查询为空！");
                    }
                }
            }

        }
        log.info("组合数据耗时:" + (System.currentTimeMillis() - filterData) + "ms");
        resMap.put("data", resList);

        return resMap;
    }

    /**
     * 6-3.搜索：专题接口
     * @param paramStr 查询es的参数
     * @param numStart 查询es请求的起始条数
     * @param num      查询es请求的条数
     */
    public Map<String, Object> specialSearch(String paramStr,
                                             String numStart,
                                             String num) throws InterruptedException {
        //返回给前端的结果
        Map<String, Object> resMap = new HashMap<>(5);
        //服务返回的所有详细数据
        List<Map<String, Object>> data = new ArrayList<>();

        //1.根据搜索关键字查询ES，ES中根据权重排序，支持分页，结果中携带排序序号ES返回结果
        log.info("查询es的参数--------->" + paramStr);
        Map<String, Object> esMap = subclassService.requestToES(paramStr);
        log.info("查询es的结果--------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        String nextFlag = subclassService.isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //根据es返回的数据条数控制线程数组的大小
        MyThread[] myThreads = new MyThread[esList.size()];
        //用于打印时间
        long start = System.currentTimeMillis();

        //3.遍历es返回的所有的数据，开启子线程查询专题服务得到详细的数据
        for (int i = 0; i < esList.size(); i++) {
            String id = esList.get(i).get("id").toString();
            myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE-V1/subject/specialForHomepage/icon", id);
            myThreads[i].start();
        }

        //4.join全部线程
        subclassService.joinAllThreads(myThreads);
        log.info("所有线程返回的时间:" + (System.currentTimeMillis() - start) + "ms");

        //5.汇总专题服务返回的详细数据
        data = subclassService.getMyThreadsData(myThreads);
        log.info("专题服务返回的数据为：" + data);
        log.info("汇总所有服务返回数据的时间:" + (System.currentTimeMillis() - start) + "ms");

        //5.组合es数据和专题服务返回的详细数据，组合好的数据直接放在esList中
        combineTopicData(esList, data);

        resMap.put("data", esList);
        log.info("拼接好数据的时间:" + (System.currentTimeMillis() - start) + "ms");
        return resMap;
    }

    /**
     * 6-4.搜索：报告接口
     * @param paramStr 查询es的参数
     * @param numStart 查询es请求的起始条数
     * @param num      查询es请求的条数
     */
    public Map<String, Object> reportPPTSearch(String paramStr,
                                               String numStart,
                                               String num) throws InterruptedException {
        //返回给前端的结果
        Map<String, Object> resMap = new HashMap<>(5);
        //报告服务返回的详细数据
        List<Map<String, Object>> data = new ArrayList<>();

        //1.根据搜索关键字查询ES，ES中根据权重排序，支持分页，结果中携带排序序号ES返回结果
        log.info("查询es的参数--------->" + paramStr);
        Map<String, Object> esMap = subclassService.requestToES(paramStr);
        log.info("查询es的结果-------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        String nextFlag = subclassService.isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //根据es返回的数据条数控制线程数组的大小
        MyThread[] myThreads = new MyThread[esList.size()];

        //用于打印时间
        long start = System.currentTimeMillis();

        //3.遍历es返回的所有的数据，开启子线程查询报告服务得到详细的数据
        for (int i = 0; i < esList.size(); i++) {
            String id = esList.get(i).get("id").toString();
            myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE-V1/reportPPT/pptReportForHomepage/info", id);
            myThreads[i].start();
        }

        //4.join全部线程
        subclassService.joinAllThreads(myThreads);
        log.info("所有线程返回的时间:" + (System.currentTimeMillis() - start) + "ms");

        //5.汇总报告服务返回的详细数据
        data = subclassService.getMyThreadsData(myThreads);
        log.info("报告服务返回的数据是--------->" + data);
        log.info("汇总所有服务返回数据的时间:" + (System.currentTimeMillis() - start) + "ms");

        //6.组合es数据和报告服务返回的详细数据，组合好的数据直接放在esList中
        combineReportData(esList, data);
        resMap.put("data", esList);
        log.info("拼接好数据的时间:" + (System.currentTimeMillis() - start) + "ms");
        return resMap;
    }

    /**
     * 6-5.搜索：报表接口
     * @param paramStr 查询es的参数
     * @param numStart 查询es请求的起始条数
     * @param num      查询es的条数
     */
    public Map<String, Object> statementSearch(String paramStr,
                                               String numStart,
                                               String num) {
        //返回前端的结果
        Map<String, Object> resMap = new HashMap<>(5);

        //1.根据搜索关键字查询ES，ES中根据权重排序，支持分页，结果中携带排序序号ES返回结果
        log.info("查询es的参数--------->" + paramStr);
        Map<String, Object> esMap = subclassService.requestToES(paramStr);
        log.info("查询es的结果--------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        String nextFlag = subclassService.isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");

        //3.遍历es返回的所有的数据，查询报表的详细数据（包括url,img,issue,issueTime）
        List<Map<String, Object>> data = new ArrayList<>();
        List<String> statementIdList = new ArrayList<>();
        if (esList.size() == 0) {
            log.info("es没有返回报表数据！");
        } else {
            for (int i = 0; i < esList.size(); i++) {
                statementIdList.add(esList.get(i).get("id").toString());
            }
            data = homepageMapper.selectStatementData(statementIdList);
        }

        //4.组合es数据和详细报表的数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (esList.size() == 0) {
            log.info("es没有返回报表数据！");
        } else {
            if (data.size() != 0) {
                //拼接数据
                for (Map<String, Object> map1 : esList) {
                    String id1 = map1.get("id").toString();
                    for (Map<String, Object> map2 : data) {
                        String id2 = map2.get("id").toString();
                        if (id1.equals(id2)){
                            Map<String, Object> dataMap = new HashMap<>(15);
                            dataMap.put("title", map1.get("title"));
                            dataMap.put("ord", map1.get("ord"));
                            dataMap.put("id", map1.get("id"));
                            dataMap.put("type", map1.get("type"));
                            dataMap.put("tabName", map1.get("dayOrMonth"));
                            dataMap.put("img", "u977.png");
                            //判断发布人字段是否为null
                            if(map2.get("issue") == null){
                                dataMap.put("issue", "未知");
                            }else{
                                dataMap.put("issue", map2.get("issue"));
                            }
                            //判断发布时间字段是否为null
                            if (map2.get("issueTime") == null){
                                dataMap.put("issueTime", "未知");
                            }else{
                                //-类型日期转换为年月日类型日期
                                String issueTimeStr = subclassService.toChineseDateString(map2.get("issueTime").toString());
                                dataMap.put("issueTime", issueTimeStr);
                            }
                            dataMap.put("url", map2.get("url"));
                            dataList.add(dataMap);
                        }
                    }
                }
            } else {
                log.info("报表查询出的详细数据为空！！！");
            }

        }
        resMap.put("data", dataList);
        log.info("报表数据为------>" + dataList);
        return resMap;
    }

    /**
     * 7.地域组件接口
     * @param userId 用户Id
     */
    public List<Map<String, Object>> area(String userId) {
        String provId = userInfoMapper.queryProvByUserId(userId);
        List<Map<String, String>> areaList = homepageMapper.getArea(provId);
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
                        //在provList中找到了一样的prov_id,跳过
                        flag = true;
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
            Map<String, Object> provMap = new HashMap<>(40);
            provMap.put("proId", pro);

            List<Map<String, String>> cityList = new ArrayList<>();
            int i;
            for (i = 0; i < areaList.size(); i++) {
                Map<String, String> areaMap = areaList.get(i);
                if (pro.equals(areaMap.get("PROV_ID"))) {
                    provMap.put("proName", areaMap.get("PRO_NAME"));

                    Map<String, String> cityMap = new HashMap<>(5);
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
        return resList;
    }

    /**
     * 8.日期组件接口
     * @param userId   用户Id
     * @param dateType 日月类型
     */
    public String getMaxDate(String dateType, String userId) {
        String date = "";
        String table = "";
        String yufabuUserIdStr = SystemVariableService.getYufabuUserId();
        String[] yufabuUserIds = yufabuUserIdStr.split(",");
        List<String> yufabuUserIdsList = Arrays.asList(yufabuUserIds);
        //userId属于预发布用户
        if (yufabuUserIdsList.contains(userId)) {
            table = SystemVariableService.kpiMaxDateTableYufabu;
        } else {
            //该用户不属于预发布用户
            table = SystemVariableService.kpiMaxDateTable;
        }
        //是月标识
        String monthFlag = "2";
        if ((!StringUtils.isBlank(dateType)) && monthFlag.equals(dateType)) {
            date = homepageMapper.getMonthMaxDate(table);
        } else {
            //日或者全部标识
            date = homepageMapper.getDayMaxDate(table);
        }
        return date;
    }


    /**
     * 综合搜索接口：开启所有线程
     * @param esList     es查询结果
     * @param myThreads  线程数组
     * @param kpiList    es中的指标数据id集合
     * @param topicList  es中的专题数据id集合
     * @param reportList es中的报告数据id集合
     */
    private void startAllThreads(List<Map<String, Object>> esList,
                                 MyThread[] myThreads,
                                 List<String> kpiList,
                                 List<String> topicList,
                                 List<String> reportList,
                                 List<String> statementList,
                                 String userId,
                                 String provId) throws InterruptedException {
        if (esList.size() != 0) {
            for (int i = 0; i < esList.size(); i++) {
                Map<String, Object> map = esList.get(i);
                //数据类型id
                String typeId = map.get("typeId").toString();
                //数据id
                String id = map.get("id").toString();
                //typeId=1指标；2专题；3报告；4报表
                if (typeId.equals(SystemVariableService.kpi)) {
                    //指标
                    kpiList.add(id);
                    //查询指标服务的参数处理："-1,-1,"查询的是全国，最大账期条件下的数据
                    String paramStr = provId + ",-1," + id + "," + userId;
                    //开子线程
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE-V1/index/indexForHomepage/dataOfAllKpi", paramStr);
                    myThreads[i].start();
                } else if (typeId.equals(SystemVariableService.subject)) {
                    //专题
                    topicList.add(id);
                    //开子线程
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE-V1/subject/specialForHomepage/icon", id);
                    myThreads[i].start();
                } else if (typeId.equals(SystemVariableService.report)) {
                    //报告
                    reportList.add(id);
                    //开子线程
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE-V1/reportPPT/pptReportForHomepage/info", id);
                    myThreads[i].start();
                } else if ("4".equals(typeId)){
                    //报表
                    statementList.add(id);
                } else {
                    log.info("es返回了不存在的type！外星type！");
                }
            }
        } else {
            log.info("es没有返回数据！");
        }
        log.info("指标有-------->" + kpiList);
        log.info("专题有-------->" + topicList);
        log.info("报告有-------->" + reportList);
        log.info("报表有-------->" + statementList);
    }

    /**
     * 综合搜索接口：组合esList和所有服务的返回结果
     * @param esList     es查询结果
     * @param dataList   所有服务查询结果
     * @param kpiList    es中的指标数据id集合
     * @param topicList  es中的专题数据id集合
     * @param reportList es中的报告数据id集合
     */
    private List<Map<String, Object>> combineAllTypeData(List<Map<String, Object>> esList,
                                                         List<Map<String, Object>> dataList,
                                                         List<String> kpiList,
                                                         List<String> topicList,
                                                         List<String> reportList,
                                                         List<String> statementList,
                                                         String areaStr) {
        List<Map<String, Object>> resList = new ArrayList<>();
        for (Map<String, Object> map1 : esList) {
            try {
                String typeId = map1.get("typeId").toString();
                String id1 = map1.get("id").toString();
                //指标数据处理
                if (typeId.equals(SystemVariableService.kpi) && kpiList.size() != 0) {
                    for (Map<String, Object> map2 : dataList) {
                        //查询数据库得到跳转的url
                        String url = homepageMapper.getUrlViaTypeId(typeId);
                        String id2 = map2.get("id").toString();
                        //es中和指标服务返回的结果id对应上了，组合数据
                        if (id1.equals(id2)) {
                            Map<String, Object> map = new HashMap<>(20);
                            map.put("markType", typeId);
                            map.put("ord", map1.get("ord"));
                            map.put("id", id1);
                            map.put("isMinus", map1.get("isMinus"));
                            map.put("url", url);
                            Map<String, Object> dataMap = new HashMap<>(20);
                            dataMap.put("markName", map1.get("type"));
                            dataMap.put("title", map1.get("title"));
                            dataMap.put("dayOrMonth", map1.get("dayOrMonth"));
                            //这个用户有什么省份的权限，这里的地域就是查的什么省份的数据，且只能看这个省份的数据
                            dataMap.put("area", areaStr);
                            dataMap.put("date", map2.get("date"));
                            dataMap.put("dataName", map2.get("dataName"));
                            dataMap.put("dataValue", map2.get("dataValue"));
                            dataMap.put("chartType", map2.get("chartType"));

                            //单位为null的处理：这里不处理的话，下面toString时可能报错
                            String unit = subclassService.dealUnit(map2.get("unit"));
                            dataMap.put("unit", unit);

                            //是否占比指标
                            String unitNow = dataMap.get("unit").toString();
                            String isPercentage = subclassService.isPercentageKpi(unitNow);
                            dataMap.put("isPercentage", isPercentage);

                            dataMap.put("chart", map2.get("chart"));
                            map.put("data", dataMap);
                            resList.add(map);
                        }
                    }
                } else if (typeId.equals(SystemVariableService.subject) && topicList.size() != 0) {
                    //专题数据处理
                    for (Map<String, Object> map2 : dataList) {
                        String id2 = map2.get("id").toString();
                        if (id1.equals(id2)) {
                            Map<String, Object> map = new HashMap<>(15);
                            map.put("markType", typeId);
                            map.put("ord", map1.get("ord"));
                            map.put("id", id1);
                            map.put("url", map2.get("url"));
                            Map<String, Object> dataMap = new HashMap<>(10);
                            dataMap.put("src", map2.get("src"));
                            dataMap.put("title", map1.get("title"));
                            dataMap.put("content", map1.get("content"));
                            dataMap.put("type", map1.get("type"));
                            dataMap.put("tabName", map1.get("tabName"));
                            map.put("data", dataMap);
                            resList.add(map);
                        }
                    }
                } else if (typeId.equals(SystemVariableService.report) && reportList.size() != 0) {
                    //查询数据库得到跳转的url
                    String url = homepageMapper.getUrlViaTypeId(typeId);
                    //报告数据处理
                    for (Map<String, Object> map2 : dataList) {
                        String id2 = map2.get("id").toString();
                        if (id1.equals(id2)) {
                            Map<String, Object> map = new HashMap<>(15);
                            map.put("markType", typeId);
                            map.put("ord", map1.get("ord"));
                            map.put("id", id1);
                            map.put("url", url);
                            Map<String, Object> dataMap = new HashMap<>(10);
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
                } else if ("4".equals(typeId) && statementList.size() != 0){
                    //报表数据处理
                    for (Map<String, Object> map2 : dataList) {
                        String id2 = map2.get("id").toString();
                        if (id1.equals(id2)) {
                            Map<String, Object> map = new HashMap<>(15);
                            map.put("markType", typeId);
                            map.put("ord", map1.get("ord"));
                            map.put("id", id1);
                            map.put("url", map2.get("url"));
                            Map<String, Object> dataMap = new HashMap<>(10);
                            dataMap.put("img", "u977.png");
                            dataMap.put("title", map1.get("title"));
                            dataMap.put("type", map1.get("type"));
                            dataMap.put("tabName", map1.get("dayOrMonth"));
                            dataMap.put("issue", map2.get("issue"));

                            //判断发布时间字段是否为null
                            if (map2.get("issueTime") == null){
                                dataMap.put("issueTime", "未知");
                            }else{
                                //-类型日期转换为年月日类型日期
                                String issueTimeStr = subclassService.toChineseDateString(map2.get("issueTime").toString());
                                dataMap.put("issueTime", issueTimeStr);
                            }

                            map.put("data", dataMap);
                            resList.add(map);
                        }
                    }
                }
                else {
                    log.info("es返回了不存在的type！" + "这条非法数据是：" + map1);
                }
            } catch (NullPointerException e) {
                log.info("----存在服务没有返回正常数据！！！");
            }
        }
        return resList;
    }

    /**
     * 专题搜索接口：组合esList和专题服务返回的结果
     * @param esList es返回的数据
     * @param data   专题服务返回的数据
     */
    private void combineTopicData(List<Map<String, Object>> esList,
                                  List<Map<String, Object>> data) {
        if (esList.size() != 0) {
            if (data.size() != 0) {
                for (Map<String, Object> map1 : esList) {
                    String id1 = map1.get("id").toString();
                    for (Map<String, Object> map2 : data) {
                        String id2 = map2.get("id").toString();
                        if (id1.equals(id2)) {
                            map1.put("src", map2.get("src").toString());
                            map1.put("url", map2.get("url").toString());
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
     * 报告搜索接口：组合esList和报告服务返回的结果
     * @param esList es返回的数据
     * @param data   报告服务返回的数据
     */
    private void combineReportData(List<Map<String, Object>> esList,
                                   List<Map<String, Object>> data) {
        if (esList.size() == 0) {
            log.info("es没有查询到报告数据！！！");
        } else {
            if (data.size() != 0) {
                //获得用于前端跳转的url
                String typeId = esList.get(0).get("typeId").toString();
                String url = homepageMapper.getUrlViaTypeId(typeId);
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

}

