package com.bonc.dw3.service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.bonc.dw3.common.thread.MyCallable;
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
     *
     * @param markType 模块类型
     */
    public List<Map<String, String>> moduleTab(String markType) {
        List<Map<String, String>> resList = homepageMapper.moduleTab(markType);
        return resList;
    }

    /**
     * 6.搜索：全部接口
     *
     * @param searchStr 查询es的参数
     * @param numStart  查询es的起始条数
     * @param num       查询es的数据条数
     * @param userId    用户Id
     */
    public Map<String, Object> allSearch(String searchStr,
                                         String numStart,
                                         String num,
                                         String userId) throws InterruptedException, ExecutionException {
        //最终返回的结果
        Map<String, Object> resMap = new HashMap<>(5);
        //es返回的所有报表id集合
        List<String> statementList = new ArrayList<>();

        //1.查询ES
        Map<String, Object> esMap = subclassService.requestToES(searchStr);
        log.info("\r\n" + "查询es的参数：" + searchStr + "\r\n" + "查询es的结果：" + esMap);

        //es返回的keyword
        Map<String, Object> keywordsMap = (Map<String, Object>) esMap.get("keyword");
        List<Map<String, Object>> keywordsList = new ArrayList<>();
        keywordsList.add(keywordsMap);
        resMap.put("keyword", keywordsList);

        //2.判断是否还有下一页
        //es查询到的数据的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的数据的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        //是否有下一页
        String nextFlag = subclassService.isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //--------------------------------->这里后期不再需要，由es返回用户的权限省份信息，带到每一条详细数据上//获取用户的省份权限，查询详细数据时就只能查询该省份
        //暂且注释，es稳定测试后可删除
//        String provId = userInfoMapper.queryProvByUserId(userId);
//        String areaStr = homepageMapper.getProvNameViaProvId(provId);
//        log.info("该用户的省份权限为：" + provId);

        //3.获取es查询到的基础数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");

//        Map<String, Object> dimensionMap = (Map<String, Object>)esList.get(0).get("dimension");
//        String provId = dimensionMap.get("provId").toString();
//        String areaStr = dimensionMap.get("cityId").toString();
//        log.info("该用户的省份权限为：" + provId);

        //4.创建线程池
        ExecutorService pool = Executors.newFixedThreadPool(12);
        long start = System.currentTimeMillis();
        //5.根据typeId将数据分类并开启子线程查询各个服务（报表的只记录报表id）
        List<Future> dataFutures = startAllThreads(pool, esList, statementList, userId);

        //6.汇总所有服务返回的详细数据(除报表数据外)
        List<Map<String, Object>> dataList = subclassService.getAllDataFromFutures(dataFutures);
        //报表详细数据
        List<Map<String, Object>> statementDataList = subclassService.getStatementData(statementList);
        //将报表详细数据添加到所有数据中
        dataList.addAll(statementDataList);
        log.info("获取全部数据耗时：" + (System.currentTimeMillis() - start) + "ms");

        //7.关闭线程池
        pool.shutdown();
        //8.过滤无效数据
        List<Map<String, Object>> dataListFinally = subclassService.filterAllData(dataList);
        //9.组合es数据和详细数据
        List<Map<String, Object>> resList = subclassService.combineAllTypeData(esList, dataListFinally);
        resMap.put("data", resList);
        return resMap;
    }

    /**
     * 6-2.搜索：指标搜索接口
     *
     * @param paramStr 查询es的参数
     * @param numStart 查询es的起始条数
     * @param num      查询es的数据条数
     * @param userId   用户id
     */
    public Map<String, Object> indexSearch(String paramStr,
                                           String numStart,
                                           String num,
                                           String userId) throws InterruptedException, ExecutionException {
        //最终的返回结果
        Map<String, Object> resMap = new HashMap<>(5);

        //1.查询ES
        long esStart = System.currentTimeMillis();
        Map<String, Object> esMap = subclassService.requestToES(paramStr);
        log.info("\r\n" + "查询es的参数：" + paramStr + "\r\n" + "查询es的结果：" + esMap + "\r\n" + "查询es耗时：" + (System.currentTimeMillis() - esStart) + "ms");

        //2.es返回的关键词
        Map<String, Object> keywordsMap = (Map<String, Object>) esMap.get("keyword");
        List<Map<String, Object>> keywordsList = new ArrayList<>();
        keywordsList.add(keywordsMap);
        resMap.put("keyword", keywordsList);
        //3.判断是否还有下一页数据
        //es查询到的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        //判断有无下一页
        String nextFlag = subclassService.isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //4.获取es查询到的基础数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //5.创建线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(12);
        //同环比线程的返回结果
        List<Future> dataFutures = new ArrayList<>();
        //图表数据的返回结果
        List<Future> chartFutures = new ArrayList<>();

        //6.开启子线程查询指标服务
        //第一个指标的日月标识
        String firstDayOrMonth = "";
        //跳转的url
        String url = "";
        //es搜索得到的provId
        String provId = "";
        //es搜索得到的查询日期
        String date="";

        //前端请求的起始条数
        int numStartValue = Integer.parseInt(numStart);
        long getDataStart = System.currentTimeMillis();

        if (esList.size() != 0) {
            //跳转的url
            url = homepageMapper.getUrlViaTypeId(esList.get(0).get("typeId").toString());
            //遍历基础数据
            for (int i = 0; i < esList.size(); i++) {
                String id = esList.get(i).get("id").toString();
                Map<String, Object> dimensionMap = (Map<String, Object>)esList.get(i).get("dimension");
                date=dimensionMap.get("date").toString();
                provId = dimensionMap.get("provId").toString();
                if (i == 0 && numStartValue == 1) {
                    //es返回的日月标识
                    String dayOrMonth = esList.get(i).get("dayOrMonth").toString();
                    if ("日报".equals(dayOrMonth)) {
                        firstDayOrMonth = SystemVariableService.day;
                    } else {
                        firstDayOrMonth = SystemVariableService.month;
                    }
                    //拼接所有图表数据接口的请求参数
                    String chartParam = provId + "," + date + "," + id + "," + firstDayOrMonth + "," + userId;
                    //请求图表数据
                    MyCallable chartCallable = new MyCallable(restTemplate, "http://DW3-NEWQUERY-KPI-HOMEPAGE-TEST/indexForHomepage/allChartOfTheKpi", chartParam);
                    Future chartFuture = threadPool.submit(chartCallable);
                    chartFutures.add(chartFuture);
                    //拼接同比环比接口的请求参数
                    String dataParam = provId + "," + date + "," + id + "," + userId;
                    //请求同比环比数据
                    MyCallable dataCallable = new MyCallable(restTemplate, "http://DW3-NEWQUERY-KPI-HOMEPAGE-TEST/indexForHomepage/dataOfAllKpi", dataParam);
                    Future dataFuture = threadPool.submit(dataCallable);
                    dataFutures.add(dataFuture);
                } else {
                    //拼接同比环比接口的请求参数
                    String dataParam = provId + "," + date + "," + id + "," + userId;
                    MyCallable dataCallable = new MyCallable(restTemplate, "http://DW3-NEWQUERY-KPI-HOMEPAGE-TEST/indexForHomepage/dataOfAllKpi", dataParam);
                    Future dataFuture = threadPool.submit(dataCallable);
                    dataFutures.add(dataFuture);
                }
            }
        }
        //6.汇总指标服务返回的详细数据
        //第一个指标的所有图表数据
        Map<String, Object> chartData = new HashMap<>(20);
        if (chartFutures.size() != 0) {
            chartData = (Map<String, Object>) chartFutures.get(0).get();
        }
        log.info("返回的图标数据为:，{}", chartData);
        //所有指标的同比环比数据
        List<Map<String, Object>> data = null;
        data = subclassService.getAllDataFromFutures(dataFutures);
        log.info("获取全部数据耗时：" + (System.currentTimeMillis() - getDataStart) + "ms");

        //7.关闭线程池
        threadPool.shutdown();

        //8.过滤无效数据
        //过滤图表数据
        Map<String, Object> chartDataFinally = subclassService.filterAllData(chartData);
        //过滤同比环比数据
        List<Map<String, Object>> dataList = subclassService.filterAllData(data);

        //9.组合es数据和详细数据
        //根据地域id得到地域的名称
        String areaStr = homepageMapper.getProvNameViaProvId(provId);
        //组合数据
        List<Map<String, Object>> resList = subclassService.combineKpiData(esList, chartDataFinally, dataList, url, areaStr, numStartValue);
        resMap.put("data", resList);
        return resMap;
    }

    /**
     * 6-3.搜索：专题接口
     *
     * @param paramStr 查询es的参数
     * @param numStart 查询es请求的起始条数
     * @param num      查询es请求的条数
     */
    public Map<String, Object> specialSearch(String paramStr,
                                             String numStart,
                                             String num) throws InterruptedException, ExecutionException {
        //返回给前端的结果
        Map<String, Object> resMap = new HashMap<>(5);

        //1.查询ES
        Map<String, Object> esMap = subclassService.requestToES(paramStr);
        log.info("\r\n" + "查询es的参数：" + paramStr + "\r\n" + "查询es的结果：" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        //是否有下一页
        String nextFlag = subclassService.isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //3.获取es查询到的基础数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //4.创建线程池
        ExecutorService pool = Executors.newFixedThreadPool(12);

        //5.开启子线程查询专题服务
        List<Future> futureList = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < esList.size(); i++) {
            String id = esList.get(i).get("id").toString();
            //开启子线程
            MyCallable topicCallable = new MyCallable(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE-V1/subject/specialForHomepage/icon", id);
            Future topicFuture = pool.submit(topicCallable);
            futureList.add(topicFuture);
        }

        //6.汇总专题服务返回的详细数据
        List<Map<String, Object>> data = subclassService.getAllDataFromFutures(futureList);
        log.info("获取全部数据耗时：" + (System.currentTimeMillis() - start) + "ms");
        //7.关闭线程池
        pool.shutdown();
        //8.过滤无效数据
        List<Map<String, Object>> dataFinally = subclassService.filterAllData(data);
        //9.组合es数据和专题服务返回的详细数据，组合好的数据直接放在esList中
        List<Map<String, Object>> resList = subclassService.combineTopicData(esList, dataFinally);
        resMap.put("data", resList);
        return resMap;
    }

    /**
     * 6-4.搜索：报告接口
     *
     * @param paramStr 查询es的参数
     * @param numStart 查询es请求的起始条数
     * @param num      查询es请求的条数
     */
    public Map<String, Object> reportPPTSearch(String paramStr,
                                               String numStart,
                                               String num) throws InterruptedException, ExecutionException {
        //返回给前端的结果
        Map<String, Object> resMap = new HashMap<>(5);

        //1.查询ES
        Map<String, Object> esMap = subclassService.requestToES(paramStr);
        log.info("\r\n" + "查询es的参数：" + paramStr + "\r\n" + "查询es的结果：" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        //是否有下一页
        String nextFlag = subclassService.isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //3.获取es查询到的基础数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //4.创建线程池
        ExecutorService pool = Executors.newFixedThreadPool(12);

        //5.开启子线程查询报告服务
        List<Future> futureList = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < esList.size(); i++) {
            String id = esList.get(i).get("id").toString();
            MyCallable reportCallable = new MyCallable(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE-V1/reportPPT/pptReportForHomepage/info", id);
            Future reportFuture = pool.submit(reportCallable);
            futureList.add(reportFuture);
        }

        //6.汇总报告服务返回的详细数据
        List<Map<String, Object>> data = subclassService.getAllDataFromFutures(futureList);
        log.info("获取全部数据耗时：" + (System.currentTimeMillis() - start) + "ms");
        //7.关闭线程池
        pool.shutdown();
        //8.过滤无效数据
        List<Map<String, Object>> dataFinally = subclassService.filterAllData(data);
        //9.组合es数据和报告服务返回的详细数据，组合好的数据直接放在esList中
        List<Map<String, Object>> resList = subclassService.combineReportData(esList, dataFinally);
        resMap.put("data", resList);
        return resMap;
    }

    /**
     * 6-5.搜索：报表接口
     *
     * @param paramStr 查询es的参数
     * @param numStart 查询es请求的起始条数
     * @param num      查询es的条数
     */
    public Map<String, Object> statementSearch(String paramStr,
                                               String numStart,
                                               String num) {
        //返回前端的结果
        Map<String, Object> resMap = new HashMap<>(5);

        //1.查询ES
        Map<String, Object> esMap = subclassService.requestToES(paramStr);
        log.info("\r\n" + "查询es的参数：" + paramStr + "\r\n" + "查询es的结果：" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        //判断是否有下一页
        String nextFlag = subclassService.isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //3.获取es查询到的基础数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");

        //4.获取报表的详细数据（包括url,img,issue,issueTime）
        //得到报表id集合
        List<String> statementIdList = new ArrayList<>();
        if (esList.size() == 0) {
            log.info("es没有返回报表数据！");
        } else {
            for (int i = 0; i < esList.size(); i++) {
                statementIdList.add(esList.get(i).get("id").toString());
            }
        }
        //查询报表的详细数据
        List<Map<String, Object>> data = subclassService.getStatementData(statementIdList);

        //5.组合es数据和详细报表的数据
        List<Map<String, Object>> resList = subclassService.combineStatementData(esList, data);
        resMap.put("data", resList);
        return resMap;
    }

    /**
     * 7.地域组件接口
     *
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
     *
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
     *
     * @param esList        es查询结果
     * @param statementList 报表id集合
     * @param userId        用户id
     */
    private List<Future> startAllThreads(ExecutorService pool,
                                         List<Map<String, Object>> esList,
                                         List<String> statementList,
                                         String userId) throws InterruptedException, ExecutionException {
        List<Future> futureList = new ArrayList<>();
        //es返回的所有指标
        List<String> kpiList = new ArrayList<>();
        //es返回的所有专题
        List<String> topicList = new ArrayList<>();
        //es返回的所有报告
        List<String> reportList = new ArrayList<>();

        //遍历es返回的数据，去不同的服务请求详细数据
        if (esList.size() != 0) {
            for (int i = 0; i < esList.size(); i++) {
                Map<String, Object> map = esList.get(i);
                //数据类型id
                String typeId = map.get("typeId").toString();
                //数据id
                String id = map.get("id").toString();
                //省份id
                String provId = "";
                //es返回日期
                String date = "";
                if ("1".equals(typeId)){
                    Map<String, Object> dimensionMap = (Map<String, Object>)map.get("dimension");
                    provId = dimensionMap.get("provId").toString();
                }

                //typeId=1指标；2专题；3报告；4报表
                if (typeId.equals(SystemVariableService.kpi)) {
                    //指标
                    kpiList.add(id);
                    //得到日期关键词供指标搜索使用
                    Map<String, Object> dimensionMap = (Map<String, Object>)esList.get(i).get("dimension");
                    date=dimensionMap.get("date").toString();
                    //查询指标服务的参数处理："-1,-1,"查询的是全国，最大账期条件下的数据
                    String paramStr =provId + "," + date + "," + id + "," + userId;
                    //开子线程
                    MyCallable kpiCallable = new MyCallable(restTemplate, "http://DW3-NEWQUERY-KPI-HOMEPAGE-TEST/indexForHomepage/dataOfAllKpi", paramStr);
                    Future kpiFuture = pool.submit(kpiCallable);
                    futureList.add(kpiFuture);
                } else if (typeId.equals(SystemVariableService.subject)) {
                    //专题
                    topicList.add(id);
                    //开子线程
                    MyCallable topicCallable = new MyCallable(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE-V1/subject/specialForHomepage/icon", id);
                    Future topicFuture = pool.submit(topicCallable);
                    futureList.add(topicFuture);
                } else if (typeId.equals(SystemVariableService.report)) {
                    //报告
                    reportList.add(id);
                    //开子线程
                    MyCallable reportCallable = new MyCallable(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE-V1/reportPPT/pptReportForHomepage/info", id);
                    Future reportFuture = pool.submit(reportCallable);
                    futureList.add(reportFuture);
                } else if ("4".equals(typeId)) {
                    //报表
                    statementList.add(id);
                } else {
                    log.info("es返回了不存在的type！外星type！");
                }
            }
        }
        log.info("\r\n" + "指标:" + kpiList + "\r\n" + "专题:" + topicList + "\r\n" + "报告:" + reportList + "\r\n" + "报表:" + statementList);
        return futureList;
    }

}

