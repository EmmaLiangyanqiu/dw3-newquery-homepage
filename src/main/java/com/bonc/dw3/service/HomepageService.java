package com.bonc.dw3.service;

import java.util.*;

import com.bonc.dw3.common.thread.MyThread;
import com.bonc.dw3.mapper.UserInfoMapper;
import freemarker.ext.beans.HashAdapter;
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

    //系统变量对象
    @Autowired
    SystemVariableService systemVariableService;


    /**
     * 1.头部栏组件接口
     *
     * @Author gp
     * @Date 2017/5/27
     */
    public Map<String, Object> headerSelect() {
        Map<String, Object> resMap = new HashMap<>();

        List<Map<String, String>> resList = homepageMapper.headerSelect();
        //默认是专题
        resMap.put("default", resList.get(1));
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
    public Map<String, Object> allSearch(String searchStr, String numStart, String num, String userId) throws InterruptedException {
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
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        String nextFlag = isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //根据es返回的数据条数控制线程数组的大小
        MyThread[] myThreads = new MyThread[esList.size()];

        //获取用户的省份权限，一下查询详细数据时就只能查询该省份
        String provId=userInfoMapper.queryProvByUserId(userId);
        String areaStr = homepageMapper.getProvNameViaProvId(provId);
        log.info("该用户的省份权限为：" + provId);

        //3.查询类型是全部，需要遍历所有的数据，根据typeId将数据分类并开启子线程查询各个服务得到详细的数据
        startAllThreads(esList, myThreads, kpiList, topicList, reportList, provId);

        //4.join全部线程
        joinAllThreads(myThreads);

        //5.汇总所有服务返回的详细数据
        dataList = getMyThreadsData(myThreads);

        //6.组合es数据和所有服务返回的详细数据
        List<Map<String, Object>> resList = combineAllTypeData(esList, dataList, kpiList, topicList, reportList, areaStr);
        resMap.put("data", resList);

        return resMap;
    }


    /**
     * 6-2.搜索：指标搜索接口
     *
     * @Parameter searchStr 查询es的参数
     * @Parameter numStart 查询es的起始条数
     * @Parameter num 查询es的数据条数
     * @Parameter area 查询时选定的地域（前端传过来的）
     * @Parameter date 查询时选定的时间（前端传过来的）
     * @Author gp
     * @Date 2017/5/31
     */
    public Map<String, Object> indexSearch(String paramStr,
                                           String numStart,
                                           String num,
                                           String area,
                                           String date) throws InterruptedException {
        //最终的返回结果
        Map<String, Object> resMap = new HashMap<>();
        //所有指标的同比环比数据
        List<Map<String, Object>> data = new ArrayList<>();
        //第一个指标的所有图表数据
        Map<String, Object> chartData = new HashMap<>();
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
        Map<String, Object> esMap = requestToES(paramStr);
        long esTime = System.currentTimeMillis() - esStart;
        log.info("查询es的结果-------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        String nextFlag = isNext(esCount, count);
        resMap.put("nextFlag", nextFlag);

        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //根据es返回的数据条数控制线程数组的大小，请求全部指标的同比环比数据
        MyThread[] myThreads = new MyThread[esList.size()];
        //用来给第一条指标数据发请求-请求它的图表数据
        MyThread chartThread = null;

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
                    if (dayOrMonth.equals("日报")) {
                        fitstDayOrMonth = systemVariableService.day;
                    } else {
                        fitstDayOrMonth = systemVariableService.month;
                    }
                    //拼接所有图表数据接口的请求参数
                    String chartParam = area + "," + date + "," + id + "," + fitstDayOrMonth;
                    //请求图表数据
                    chartThread = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE/index/indexForHomepage/allChartOfTheKpi", chartParam);
                    chartThread.start();
                    //拼接同比环比接口的请求参数
                    String dataParam = area + "," + date + "," + id;
                    //请求同比环比数据
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE/index/indexForHomepage/dataOfAllKpi", dataParam);
                    myThreads[i].start();
                } else {
                    //拼接同比环比接口的请求参数
                    String dataParam = area + "," + date + "," + id;
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE/index/indexForHomepage/dataOfAllKpi", dataParam);
                    myThreads[i].start();
                }
            }
        } else {
            log.info("es没有返回任何指标数据！！！");
        }

        //4.join全部线程
        joinAllThreads(myThreads);
        if (null != chartThread) {
            chartThread.join();
        }
        long threadTime = System.currentTimeMillis() - start;
        log.info("es查询到返回的时间:" + esTime + "ms");
        log.info("所有线程返回的时间:" + threadTime + "ms");
        long allThreadsJoin = System.currentTimeMillis();

        //5.汇总指标服务返回的详细数据
        //得到所有指标的同比环比数据
        data = getMyThreadsData(myThreads);
        //得到第一条指标的所有图表数据
        if (null != chartThread) {
            chartData = (Map<String, Object>) chartThread.result;
            //log.info("chartData------------------------------>" + chartData);
        } else {
            log.info("没有开启查询第一条指标的所有图表数据的子线程！！！");
        }
        log.info("汇总所有服务返回的数据耗时:" + (System.currentTimeMillis() - allThreadsJoin) + "ms");
        long getAllData = System.currentTimeMillis();

        //数据过滤：清理从指标服务返回的不合格数据(没有id的数据)
        //过滤图表数据
        if ((chartData != null) && (!chartData.containsKey("id"))) {
            log.info(chartData + "------chartData没有返回id，舍弃！！！");
            chartData = null;
        }
        //过滤同比环比数据
        List<Map<String, Object>> dataList = new ArrayList<>();
        if ((data.size()) != 0 && (data != null)){
            for (int j = 0; j < data.size(); j++) {
                if (!data.get(j).containsKey("id")) {
                    log.info(data.get(j) + "----同比环比数据没有返回id，舍弃！！！");
                } else {
                    dataList.add(data.get(j));
                }
            }
        }else{
            log.info("全部指标的同比环比数据为空！！！");
        }
        log.info("过滤不合格数据耗时:" + (System.currentTimeMillis() - getAllData) + "ms");
        long filterData = System.currentTimeMillis();

        //6.组合es数据和指标服务返回的详细数据，组合好的数据直接放在esList中
        //List<Map<String, Object>> resList = combineKpiData(esList, dataList, chartData, numStartValue, url, areaStr);
        List<Map<String, Object>> resList = new ArrayList<>();
        if (esList.size() == 0) {
            log.info("没有需要查询的指标id！！！");
        } else {
            for (int i = 0; i < esList.size(); i++) {
                Map<String, Object> map1 = esList.get(i);
                String id1 = map1.get("id").toString();
                //第一条数据
                if (i == 0 && chartData != null && numStartValue == 1) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("ord", map1.get("ord"));
                    map.put("dayOrMonth", map1.get("dayOrMonth"));
                    map.put("id", map1.get("id"));
                    map.put("isMinus", map1.get("isMinus"));
                    map.put("title", map1.get("title"));
                    map.put("markType", map1.get("typeId"));
                    map.put("markName", map1.get("type"));
                    map.put("chartData", chartData.get("chartData"));
                    map.put("date", chartData.get("date"));
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
                    String unit = dealUnit(map.get("unit"));
                    map.put("unit", unit);
                    //是否占比指标
                    String unitNow = map.get("unit").toString();
                    String isPercentage = isPercentageKpi(unitNow);
                    map.put("isPercentage", isPercentage);

                    resList.add(map);
                    //log.info("第一条指标数据=================================》" + map);
                } else {
                    //除第一条指标以外的指标
                    if (dataList != null && dataList.size() != 0) {
                        for (int j = 0; j < dataList.size(); j++) {
                            Map<String, Object> map2 = dataList.get(j);
                            String id2 = map2.get("id").toString();
                            if (id1.equals(id2)) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("ord", map1.get("ord"));
                                map.put("dayOrMonth", map1.get("dayOrMonth"));
                                map.put("id", map1.get("id"));
                                map.put("isMinus", map1.get("isMinus"));
                                map.put("title", map1.get("title"));
                                map.put("markType", map1.get("typeId"));
                                map.put("markName", map1.get("type"));
                                //map.put("unit", map2.get("unit"));
                                //单位为null的处理：这里不处理的话，下面toString时可能报错
                                String unit = dealUnit(map2.get("unit"));
                                map.put("unit", unit);

                                //是否占比指标
                                String unitNow = map.get("unit").toString();
                                String isPercentage = isPercentageKpi(unitNow);
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

        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //根据es返回的数据条数控制线程数组的大小
        MyThread[] myThreads = new MyThread[esList.size()];
        //用于打印时间
        long start = System.currentTimeMillis();

        //3.遍历es返回的所有的数据，开启子线程查询专题服务得到详细的数据
        for (int i = 0; i < esList.size(); i++) {
            String id = esList.get(i).get("id").toString();
            myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE/subject/specialForHomepage/icon", id);
            myThreads[i].start();
        }

        //4.join全部线程
        joinAllThreads(myThreads);
        log.info("所有线程返回的时间:" + (System.currentTimeMillis() - start) + "ms");

        //5.汇总专题服务返回的详细数据
        data = getMyThreadsData(myThreads);
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

        //es查询到的数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        //根据es返回的数据条数控制线程数组的大小
        MyThread[] myThreads = new MyThread[esList.size()];

        //用于打印时间
        long start = System.currentTimeMillis();

        //3.遍历es返回的所有的数据，开启子线程查询报告服务得到详细的数据
        for (int i = 0; i < esList.size(); i++) {
            String id = esList.get(i).get("id").toString();
            myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE/reportPPT/pptReportForHomepage/info", id);
            myThreads[i].start();
        }

        //4.join全部线程
        joinAllThreads(myThreads);
        log.info("所有线程返回的时间:" + (System.currentTimeMillis() - start) + "ms");

        //5.汇总报告服务返回的详细数据
        data = getMyThreadsData(myThreads);
        log.info("汇总所有服务返回数据的时间:" + (System.currentTimeMillis() - start) + "ms");

        //6.组合es数据和报告服务返回的详细数据，组合好的数据直接放在esList中
        combineReportData(esList, data);
        resMap.put("data", esList);
        log.info("拼接好数据的时间:" + (System.currentTimeMillis() - start) + "ms");
        return resMap;
    }
    @Autowired
    UserInfoMapper userInfoMapper;

    /**
     * 7.地域组件接口
     *
     * @Author gp
     * @Date 2017/6/9
     */
    public List<Map<String, Object>> area(String userId) {
        String provId=userInfoMapper.queryProvByUserId(userId);
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
    public String getMaxDate(String dateType, String userId) {
        String date = "";
        String table = "";
        String yufabuUserIdStr = SystemVariableService.getYufabuUserId();
        String[] yufabuUserIds = yufabuUserIdStr.split(",");
        List<String> yufabuUserIdsList = Arrays.asList(yufabuUserIds);
        //userId属于预发布用户
        if (yufabuUserIdsList.contains(userId)){
            table = SystemVariableService.kpiMaxDateTableYufabu;
        }else{
            //该用户不属于预发布用户
            table = SystemVariableService.kpiMaxDateTable;
        }

        //是月标识
        if ((!StringUtils.isBlank(dateType)) && dateType.equals("2")) {
            date = homepageMapper.getMonthMaxDate(table);
        } else {
            //日或者全部标识
            date = homepageMapper.getDayMaxDate(table);
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
        resMap = restTemplateTmp.postForObject("http://10.249.216.108:8998/es/explore", requestEntity, Map.class);
        return resMap;
    }


    /**
     * 汇总所有线程返回的数据
     *
     * @param myThreads
     * @Author gp
     * @Date 2017/7/13
     */
    private List<Map<String,Object>> getMyThreadsData(MyThread[] myThreads) {
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
     *
     * @param myThreads 线程数组
     * @Author gp
     * @Date 2017/6/21
     */
    private void joinAllThreads(MyThread[] myThreads) throws InterruptedException {
        for (int i = 0; i < myThreads.length; i++) {
            myThreads[i].join();
        }
    }


    /**
     * 判断是否还有下一页
     *
     * @param esCount es返回结果的条数
     * @param count   前端已经显示的数据条数
     * @Author gp
     * @Date 2017/6/13
     */
    private String isNext(int esCount, int count) {
        String nextFlag = "";
        //如果现在前端显示的总条数小于es的总数，那么还有下一页，反之没有下一页了
        if (count < esCount) {
            nextFlag = systemVariableService.hasNext;
        } else {
            nextFlag = systemVariableService.noNext;
        }
        return nextFlag;
    }


    /**
     * 综合搜索接口：开启所有线程
     *
     * @param esList     es查询结果
     * @param myThreads  线程数组
     * @param kpiList    es中的指标数据id集合
     * @param topicList  es中的专题数据id集合
     * @param reportList es中的报告数据id集合
     * @Author gp
     * @Date 2017/6/13
     */
    private void startAllThreads(List<Map<String, Object>> esList,
                                 MyThread[] myThreads,
                                 List<String> kpiList,
                                 List<String> topicList,
                                 List<String> reportList,
                                 String provId) throws InterruptedException {
        if (esList.size() != 0) {
            for (int i = 0; i < esList.size(); i++) {
                Map<String, Object> map = esList.get(i);
                //数据类型id
                String typeId = map.get("typeId").toString();
                //数据id
                String id = map.get("id").toString();
                //typeId=1指标；2专题；3报告
                if (typeId.equals(systemVariableService.kpi)) {
                    //指标
                    kpiList.add(id);

                    //查询指标服务的参数处理："-1,-1,"查询的是全国，最大账期条件下的数据
                    //String paramStr = "-1,-1," + id;
                    String paramStr = provId + ",-1," + id;
                    //开子线程
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE/index/indexForHomepage/dataOfAllKpi", paramStr);
                    myThreads[i].start();
                } else if (typeId.equals(systemVariableService.subject)) {
                    //专题
                    topicList.add(id);
                    //开子线程
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE/subject/specialForHomepage/icon", id);
                    myThreads[i].start();
                } else if (typeId.equals(systemVariableService.report)) {
                    //报告
                    reportList.add(id);
                    //开子线程
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-HBASE/reportPPT/pptReportForHomepage/info", id);
                    myThreads[i].start();
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
    }


    /**
     * 综合搜索接口：组合esList和所有服务的返回结果
     *
     * @param esList     es查询结果
     * @param dataList   所有服务查询结果
     * @param kpiList    es中的指标数据id集合
     * @param topicList  es中的专题数据id集合
     * @param reportList es中的报告数据id集合
     * @Author gp
     * @Date 2017/6/13
     */
    private List<Map<String, Object>> combineAllTypeData(List<Map<String, Object>> esList,
                                                         List<Map<String, Object>> dataList,
                                                         List<String> kpiList,
                                                         List<String> topicList,
                                                         List<String> reportList,
                                                         String areaStr) {
        List<Map<String, Object>> resList = new ArrayList<>();
        for (Map<String, Object> map1 : esList) {
            try {
                String typeId = map1.get("typeId").toString();
                //查询数据库得到跳转的url
                String url = homepageMapper.getUrlViaTypeId(typeId);
                String id1 = map1.get("id").toString();
                //指标数据处理
                if (typeId.equals(systemVariableService.kpi) && kpiList.size() != 0) {
                    for (Map<String, Object> map2 : dataList) {
                        String id2 = map2.get("id").toString();
                        //es中和指标服务返回的结果id对应上了，组合数据
                        if (id1.equals(id2)) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("markType", typeId);
                            map.put("ord", map1.get("ord"));
                            map.put("id", id1);
                            map.put("isMinus", map1.get("isMinus"));
                            map.put("url", url);
                            Map<String, Object> dataMap = new HashMap<>();
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
                            String unit = dealUnit(map2.get("unit"));
                            dataMap.put("unit", unit);

                            //是否占比指标
                            String unitNow = dataMap.get("unit").toString();
                            String isPercentage = isPercentageKpi(unitNow);
                            dataMap.put("isPercentage", isPercentage);

                            dataMap.put("chart", map2.get("chart"));
                            map.put("data", dataMap);
                            resList.add(map);
                        }
                    }
                } else if (typeId.equals(systemVariableService.subject) && topicList.size() != 0) {
                    //专题数据处理
                    for (Map<String, Object> map2 : dataList) {
                        String id2 = map2.get("id").toString();
                        if (id1.equals(id2)) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("markType", typeId);
                            map.put("ord", map1.get("ord"));
                            map.put("id", id1);
                            map.put("url", map2.get("url"));
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
                } else if (typeId.equals(systemVariableService.report) && reportList.size() != 0) {
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
            } catch (NullPointerException e) {
                log.info("----存在服务没有返回正常数据！！！");
            }
        }
        return resList;
    }


    /**
     * 单位是否为null的判断，为null时处理为空字符串
     * 不为null时，取它本身即可
     *
     * @Author gp
     * @Date 2017/7/31
     */
    private String dealUnit(Object o) {
        String unit = "";
        if (o != null){
            unit = o.toString();
        }
        return unit;
    }


    /**
     * 判断是否占比指标
     *
     * @Author gp
     * @Date 2017/7/31
     */
    private String isPercentageKpi(String unitNow) {
        String isPercentage = "";
        if ((!StringUtils.isBlank(unitNow)) && ("%".equals(unitNow) || "PP".equals(unitNow) || "pp".equals(unitNow))){
            isPercentage = "1";
        }else{
            isPercentage = "0";
        }
        return isPercentage;
    }


    /**
     * 指标搜索接口：组合es数据和服务返回的详细数据
     *
     * @param esList es搜索返回的结果
     * @param dataList 所有服务返回的同比环比数据
     * @param chartData 指标的图表数据
     * @param numStartValue 前端请求的起始数据
     * @param url 指标的跳转路径
     * @param areaStr 前端传的地域
     * @Author gp
     * @Date 2017/7/13
     */
    /*private List<Map<String,Object>> combineKpiData(List<Map<String, Object>> esList,
                                                    List<Map<String, Object>> dataList,
                                                    Map<String, Object> chartData,
                                                    int numStartValue,
                                                    String url,
                                                    String areaStr) {
        List<Map<String, Object>> resList = new ArrayList<>();
        if (esList.size() == 0) {
            log.info("没有需要查询的指标id！！！");
        } else {
            for (int i = 0; i < esList.size(); i++) {
                Map<String, Object> map1 = esList.get(i);
                String id1 = map1.get("id").toString();
                //第一条数据
                if (i == 0 && chartData != null && numStartValue == 1) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("ord", map1.get("ord"));
                    map.put("dayOrMonth", map1.get("dayOrMonth"));
                    map.put("id", map1.get("id"));
                    map.put("title", map1.get("title"));
                    map.put("markType", map1.get("typeId"));
                    map.put("markName", map1.get("type"));
                    map.put("chartData", chartData.get("chartData"));
                    map.put("date", chartData.get("date"));
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
                    resList.add(map);
                    log.info("第一条指标数据=================================》" + map);
                } else {
                    //除第一条指标以外的指标
                    if (dataList != null && dataList.size() != 0) {
                        for (int j = 0; j < dataList.size(); j++) {
                            Map<String, Object> map2 = dataList.get(j);
                            String id2 = map2.get("id").toString();
                            if (id1.equals(id2)) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("ord", map1.get("ord"));
                                map.put("dayOrMonth", map1.get("dayOrMonth"));
                                map.put("id", map1.get("id"));
                                map.put("title", map1.get("title"));
                                map.put("markType", map1.get("typeId"));
                                map.put("markName", map1.get("type"));
                                map.put("unit", map2.get("unit"));
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
        return resList;
    }*/


    /**
     * 指标搜索接口：开启线程查询指标服务
     * @param esList es搜索引擎返回的结果
     * @param myThreads 线程数组
     * @param chartThread 请求图表接口的线程
     * @param numStartValue 前端搜索的指标起始值
     * @param fitstDayOrMonth 第一条指标日月标识
     * @param url 指标跳转的路径
     * @param area 前端参数：地域
     * @param date 前端参数：日期
     *
     * @Author gp
     * @Date 2017/7/13
     */
    /*private void startKpiThreads(List<Map<String, Object>> esList,
                                 MyThread[] myThreads,
                                 MyThread chartThread,
                                 int numStartValue,
                                 String fitstDayOrMonth,
                                 String url,
                                 String area,
                                 String date) {
        if (esList.size() != 0) {
            url = homepageMapper.getUrlViaTypeId(esList.get(0).get("typeId").toString());
            //for循环得到需要查询的kpi字符串
            for (int i = 0; i < esList.size(); i++) {
                String id = esList.get(i).get("id").toString();
                log.info("*************************************" + numStartValue);
                if (i == 0 && numStartValue == 1) {
                    //es返回的日月标识
                    String dayOrMonth = esList.get(i).get("dayOrMonth").toString();
                    if (dayOrMonth.equals("日报")) {
                        fitstDayOrMonth = systemVariableService.day;
                    } else {
                        fitstDayOrMonth = systemVariableService.month;
                    }
                    //拼接所有图表数据接口的请求参数
                    String chartParam = area + "," + date + "," + id + "," + fitstDayOrMonth;
                    log.info("*************************************" + chartParam);
                    //请求图表数据
                    chartThread = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-TEST/index/indexForHomepage/allChartOfTheKpi", chartParam);
                    chartThread.start();
                    //拼接同比环比接口的请求参数
                    String dataParam = area + "," + date + "," + id;
                    //请求同比环比数据
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-TEST/index/indexForHomepage/dataOfAllKpi", dataParam);
                    myThreads[i].start();
                } else {
                    //拼接同比环比接口的请求参数
                    String dataParam = area + "," + date + "," + id;
                    myThreads[i] = new MyThread(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL-TEST/index/indexForHomepage/dataOfAllKpi", dataParam);
                    myThreads[i].start();
                }
            }
        } else {
            log.info("es没有返回任何指标数据！！！");
        }

    }*/


    /**
     * 专题搜索接口：组合esList和专题服务返回的结果
     *
     * @param esList es返回的数据
     * @param data   专题服务返回的数据
     * @Author gp
     * @Date 2017/6/14
     */
    private void combineTopicData(List<Map<String, Object>> esList, List<Map<String, Object>> data) {
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
     *
     * @param esList es返回的数据
     * @param data   报告服务返回的数据
     * @Author gp
     * @Date 2017/6/14
     */
    private void combineReportData(List<Map<String, Object>> esList, List<Map<String, Object>> data) {
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

