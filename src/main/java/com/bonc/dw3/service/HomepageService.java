package com.bonc.dw3.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.bonc.dw3.common.thread.MyRunable;
import freemarker.ext.beans.HashAdapter;
import io.swagger.models.auth.In;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.bonc.dw3.common.datasource.DynamicDataSourceContextHolder;
import com.bonc.dw3.common.util.DateUtils;
import com.bonc.dw3.mapper.HomepageMapper;
import org.springframework.web.client.RestTemplate;

@Service
@CrossOrigin(origins = "*")
public class HomepageService {

    /**
     * 日志对象
     */
    private static Logger log = LoggerFactory.getLogger(HomepageService.class);

    /**
     * 向其它服务发送请求REST对象
     */
    @Autowired
    private RestTemplate restTemplate;

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
        System.out.println(resMap);
        return resMap;
    }


    /**
     * 2.菜单树组件接口
     *
     * @Author gp
     * @Date 2017/5/29
     */
    public List<Map<String, String>> getAllMenu(String userId) {
        String roleId = homepageMapper.selectRoleByUserId(userId);
        String[] roleList = roleId.split(",");
        List<String> mostMenu = homepageMapper.selectMostMenu(roleList);
        Map<String, String> inAndOut = homepageMapper.selectRoleInOut(userId);
        String[] inMenu = inAndOut.get("rolein").split(",");
        String[] outMenu = inAndOut.get("roleout").split(",");
        Set<String> menuSet = new HashSet<>(mostMenu);
        for (String s : inMenu) {
            menuSet.add(s);
        }
        for (String s : outMenu) {
            menuSet.remove(s);
        }
        List<Map<String, String>> result = homepageMapper.selectAllMenu(menuSet);
        System.out.println("result------>" + result);
        return result;
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
     * 4-1.近期访问接口：筛选列表接口
     *
     * @Author gp
     * @Date 2017/5/27
     */
    public Map<String, Object> recentVisit() {
        Map<String, Object> resMap = new HashMap<>();
        List<Map<String, String>> resList = homepageMapper.recentVisit();
        resMap.put("default", resList.get(0));
        resMap.put("selectList", resList);
        System.out.println(resMap);
        return resMap;
    }

    /**
     * 4-2.近期访问接口：近期访问列表
     *
     * @Author gp
     * @Date 2017/5/25
     */
    public Map<String, Object> recentVisitList(String paramStr) {
        RestTemplate restTemplateTmp = new RestTemplate();
        Map<String, Object> recentVisitMap = restTemplateTmp.postForObject("http://192.168.110.57:9981/es/fetch", paramStr, Map.class);
        return recentVisitMap;
    }


    /**
     * 6-1.搜索：全部接口
     *
     * @Author gp
     * @Date 2017/5/18
     */
    public Map<String, Object> allSearch(String searchStr, String numStart, String num) {
        Map<String, Object> resMap = new HashMap<>();
        List<Map<String, Object>> resList = new ArrayList<>();
        //List<Map<String, Object>> dataList = new ArrayList<>();
        String subjectStr = "";
        String reportPPTStr = "";
        String kpiStr = "";
        String nextFlag = "";

        //1.根据搜索关键字查询ES，ES中根据权重排序，支持分页，结果中携带排序序号ES返回结果
        RestTemplate restTemplateTmp = new RestTemplate();
        Map<String, Object> esMap = restTemplateTmp.postForObject("http://192.168.110.57:7070/es/explore", searchStr, Map.class);
        System.out.println("查询es的参数--------->" + searchStr);
        System.out.println("查询es的结果-------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        //如果现在前端显示的总条数小于es的总数，那么还有下一页，反之没有下一页了
        if (count < esCount) {
            nextFlag = "1";
        } else {
            nextFlag = "0";
        }

        //3.查询类型是全部，需要遍历所有的数据，根据分类id从相应的服务中查询数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        for (Map<String, Object> map : esList) {
            String type = map.get("typeId").toString();
            //type=1指标；3报告；2专题
            if (type.equals("1")) {
                //kpiStr为空时，拼接字符串不要逗号
                if (kpiStr.equals("")) {
                    kpiStr = map.get("id").toString();
                } else {
                    kpiStr = kpiStr + "," + map.get("id");
                }
            } else if (type.equals("3")) {
                if (reportPPTStr.equals("")) {
                    reportPPTStr = map.get("id").toString();
                } else {
                    reportPPTStr = reportPPTStr + "," + map.get("id");
                }
            } else if (type.equals("2")) {
                if (subjectStr.equals("")) {
                    subjectStr = map.get("id").toString();
                } else {
                    subjectStr = subjectStr + "," + map.get("id");
                }
            }
        }
        System.out.println("kpiStr-------->" + kpiStr);
        System.out.println("专题有-------->" + subjectStr);
        System.out.println("报告有-------->" + reportPPTStr);

        //4.多线程分别请求别的服务拿到详细的数据
        //参数处理，如果参数为""时，不开线程
        if (!kpiStr.equals("")) {
            kpiStr = "-1,-1," + kpiStr;
        } else {
            System.out.println("es查询结果中没有指标数据！！！");
        }
        MyRunable kpiRunable = new MyRunable(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL/indexDetails/SlaverKpi/dataOfAllKpi", kpiStr);
        Thread kpiThread = new Thread(kpiRunable);
        kpiThread.start();
        //请求报告服务
        //MyRunable reportRunable = new MyRunable(restTemplateTmp, "http://192.168.110.57:7071/pptReportForHomepage/info", reportPPTStr);
        MyRunable reportRunable = new MyRunable(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL/reportPPT/pptReportForHomepage/info", reportPPTStr);
        Thread reportThread = new Thread(reportRunable);
        reportThread.start();
        //请求专题服务
        MyRunable subjectRunable = new MyRunable(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL/subject/specialForHomepage/icon", subjectStr);
        Thread subjectThread = new Thread(subjectRunable);
        subjectThread.start();
        try {
            kpiThread.join();
            reportThread.join();
            subjectThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //多线程拿到的所有数据
        List<Map<String, Object>> kpiResult = (List<Map<String, Object>>) kpiRunable.result;
        System.out.println("指标子节点返回:" + kpiResult);
        List<Map<String, Object>> reportResult = (List<Map<String, Object>>) reportRunable.result;
        System.out.println("报告服务返回:" + reportResult);
        List<Map<String, Object>> subjectResult = (List<Map<String, Object>>) subjectRunable.result;
        System.out.println("专题服务返回:" + subjectResult);
        /*dataList.addAll(kpiResult);
        dataList.addAll(reportResult);
        dataList.addAll(subjectResult);*/

        //组合数据
        resMap.put("nextFlag", nextFlag);
        for (Map<String, Object> map1 : esList){
            String typeId = map1.get("typeId").toString();
            String url = homepageMapper.getUrlViaTypeId(typeId);
            System.out.println("url------>" + url);
            String id1 = map1.get("id").toString();
            if (typeId.equals("1")){
                //指标
                for (Map<String, Object> map2 : kpiResult){
                    String id2 = map2.get("id").toString();
                    if (id1.equals(id2)){
                        Map<String, Object> map = new HashMap<>();
                        map.put("markType", map1.get("typeId"));
                        map.put("ord", map1.get("ord"));
                        map.put("id", id1);
                        map.put("url", url);
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("markName", map1.get("type"));
                        dataMap.put("title", map1.get("title"));
                        dataMap.put("dayOrMonth", map1.get("dayOrMonth"));
                        dataMap.put("dayOrMonth", "全国");
                        dataMap.put("date", map2.get("date"));
                        dataMap.put("dataName", map2.get("dataName"));
                        dataMap.put("dataValue", map2.get("dataValue"));
                        dataMap.put("chartType", map2.get("chartType"));
                        dataMap.put("chartData", map2.get("data"));
                        dataMap.put("chartX", map2.get("chartX"));
                        map.put("data", dataMap);
                        resList.add(map);
                    }
                }
            }else if (typeId.equals("2")){
                //专题
                for (Map<String, Object> Map2 : subjectResult){
                    String id2 = Map2.get("id").toString();

                }
            }else if (typeId.equals("3")){
                //报告
                for (Map<String, Object> Map2 : reportResult){
                    String id2 = Map2.get("id").toString();

                }
            }
        }
        resMap.put("data", resList);
        /*if(!kpiStr.equals("")){
            resList.addAll(requestToKPI(kpiStr));
        }*/

        //log.info("专题有-------->" + subjectList);
        /*if(subjectList.size() > 0){
            resList.addAll(requestToSubject(subjectList));
        }*/

        //log.info("报告有-------->" + reportPPTList);
        /*if(reportPPTList.size() > 0){
        	resList.addAll(requestToReportPPT(reportPPTList));
        }*/

        //对返回结果依照ES的次序重新排序
        //return reOrder(resList);
        return resMap;
    }


    /**
     * 6-2.搜索：指标接口
     *
     * @Author gp
     * @Date 2017/5/31
     */
    public Map<String, Object> indexSearch(String paramStr, String numStart, String num, String area, String date) {
        Map<String, Object> resMap = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> chartData = new HashMap<>();
        String nextFlag = "";
        String firstKpi = "";
        String fitstDayOrMonth = "";
        String kpiStr = "";
        String url = "";

        //1.根据搜索关键字查询ES，ES中根据权重排序，支持分页，结果中携带排序序号ES返回结果
        RestTemplate restTemplateTmp = new RestTemplate();
        System.out.println("查询es的参数--------->" + paramStr);
        Map<String, Object> esMap = restTemplateTmp.postForObject("http://192.168.110.57:7070/es/explore", paramStr, Map.class);
        System.out.println("查询es的结果-------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        //如果现在前端显示的总条数小于es的总数，那么还有下一页，反之没有下一页了
        if (count < esCount) {
            nextFlag = "1";
        } else {
            nextFlag = "0";
        }
        resMap.put("nextFlag", nextFlag);

        //3.循环将收到的数据的id拼接成字符串发送给专题服务，获取数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        String typeId = esList.get(0).get("typeId").toString();
        url = homepageMapper.getUrlViaTypeId(typeId);
        System.out.println("url------>" + url);
        for (int i = 0; i < esList.size(); i++) {
            String id = esList.get(i).get("id").toString();
            if (i == 0) {
                firstKpi = firstKpi + id;
                fitstDayOrMonth = esList.get(i).get("dayOrMonth").toString();
            } else {
                if (kpiStr.equals("")) {
                    kpiStr = kpiStr + id;
                } else {
                    kpiStr = kpiStr + "," + id;
                }
            }
        }
        if (kpiStr.equals("")) {
            System.out.println("没有需要查询的指标id！！！");
        } else {
            String dataParam = area + "," + date + "," + firstKpi + "," + kpiStr;
            System.out.println(dataParam);
            data = restTemplateTmp.postForObject("http://192.168.110.57:7071/indexForHomepage/dataOfAllKpi", dataParam, List.class);
            String chartDataParam = area + "," + date + "," + firstKpi + "," + fitstDayOrMonth;
            System.out.println(chartDataParam);
            chartData = restTemplateTmp.postForObject("http://192.168.110.67:7071/indexForHomepage/allChartOfTheKpi", chartDataParam, Map.class);
            System.out.println("指标服务查询出的数据是：" + data);
            System.out.println("指标服务查询出的图表数据是：" + chartData);
        }

        //4.将服务查询出的数据放到es的结果中，拼接结果
        for (int i = 0; i < esList.size(); i++) {
            Map<String, Object> map1 = esList.get(i);
            String id1 = map1.get("id").toString();
            if (i == 0){
                map1.put("indexName", map1.get("title"));
                map1.put("markType", map1.get("typeId"));
                map1.put("markName", map1.get("type"));
                map1.put("chartData", chartData.get("chartData"));
                map1.put("dataName", data.get(0).get("dataName"));
                map1.put("dataValue", data.get(0).get("dataValue"));
                map1.put("url", url);
                map1.remove("typeId");
                map1.remove("type");
                map1.remove("title");
            }else {
                for (int j = 1; j < data.size(); j ++){
                    Map<String, Object> map2 = data.get(j);
                    String id2 = map2.get("id").toString();
                    if (id1.equals(id2)){
                        map1.put("indexName", map1.get("title"));
                        map1.put("markType", map1.get("typeId"));
                        map1.put("markName", map1.get("type"));
                        map1.put("unit", map2.get("unit"));
                        map1.put("chartType", map2.get("chartType"));
                        map1.put("data", map2.get("data"));
                        map1.put("chartX", map2.get("chartX"));
                        map1.put("dataName", map2.get("dataName"));
                        map1.put("dataValue", map2.get("dataValue"));
                        map1.put("url", url);
                        map1.put("area", area);
                        map1.put("date", date);
                        map1.remove("typeId");
                        map1.remove("type");
                        map1.remove("title");
                    }
                }
            }
        }
        resMap.put("data", esList);
        System.out.println("resMap---------->" + resMap);
        return resMap;
    }


    /**
     * 6-3.搜索：专题接口
     *
     * @Author gp
     * @Date 2017/5/31
     */
    public Map<String, Object> specialSearch(String paramStr, String numStart, String num) {
        Map<String, Object> resMap = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        String nextFlag = "";
        String specialStr = "";
        String url = "";

        //1.根据搜索关键字查询ES，ES中根据权重排序，支持分页，结果中携带排序序号ES返回结果
        RestTemplate restTemplateTmp = new RestTemplate();
        Map<String, Object> esMap = restTemplateTmp.postForObject("http://192.168.110.57:7070/es/explore", paramStr, Map.class);
        System.out.println("查询es的参数--------->" + paramStr);
        System.out.println("查询es的结果-------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        //如果现在前端显示的总条数小于es的总数，那么还有下一页，反之没有下一页了
        if (count < esCount) {
            nextFlag = "1";
        } else {
            nextFlag = "0";
        }
        resMap.put("nextFlag", nextFlag);

        //3.循环将收到的数据的id拼接成字符串发送给专题服务，获取数据
        List<Map<String, Object>> esList = new ArrayList<>();
        esList = (List<Map<String, Object>>) esMap.get("data");
        String typeId = esList.get(0).get("typeId").toString();
        url = homepageMapper.getUrlViaTypeId(typeId);
        for (Map<String, Object> map : esList) {
            String id = map.get("id").toString();
            if (specialStr.equals("")) {
                specialStr = specialStr + id;
            } else {
                specialStr = specialStr + "," + id;
            }
        }
        if (specialStr.equals("")) {
            System.out.println("没有需要查询的专题id！！！");
        } else {
            data = restTemplate.postForObject("http://DW3-NEWQUERY-HOMEPAGE-ZUUL/subject/specialForHomepage/icon", specialStr, List.class);
            System.out.println("专题服务查询出的数据是：" + data);
        }

        //4.将服务查询出的数据放到es的结果中，拼接结果
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
        resMap.put("data", esList);
        return resMap;
    }


    /**
     * 6-4.搜索：报告接口
     *
     * @Author gp
     * @Date 2017/5/31
     */
    public Map<String, Object> reportPPTSearch(String paramStr, String numStart, String num) {
        Map<String, Object> resMap = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        String nextFlag = "";
        String reportPPTStr = "";
        String url = "";

        //1.根据搜索关键字查询ES，ES中根据权重排序，支持分页，结果中携带排序序号ES返回结果
        RestTemplate restTemplateTmp = new RestTemplate();
        Map<String, Object> esMap = restTemplateTmp.postForObject("http://192.168.110.57:7070/es/explore", paramStr, Map.class);
        System.out.println("查询es的参数--------->" + paramStr);
        System.out.println("查询es的结果-------->" + esMap);

        //2.判断是否还有下一页数据
        //es查询到的记录的总条数
        int esCount = Integer.parseInt(esMap.get("count").toString());
        //前端显示的总条数
        int count = Integer.parseInt(numStart) + Integer.parseInt(num) - 1;
        //如果现在前端显示的总条数小于es的总数，那么还有下一页，反之没有下一页了
        if (count < esCount) {
            nextFlag = "1";
        } else {
            nextFlag = "0";
        }
        resMap.put("nextFlag", nextFlag);

        //3.循环将收到的数据的id拼接成字符串发送给专题服务，获取数据
        List<Map<String, Object>> esList = (List<Map<String, Object>>) esMap.get("data");
        String typeId = esList.get(0).get("typeId").toString();
        url = homepageMapper.getUrlViaTypeId(typeId);
        for (Map<String, Object> map : esList) {
            String id = (String) map.get("id");
            if (reportPPTStr.equals("")) {
                reportPPTStr = reportPPTStr + id;
            } else {
                reportPPTStr = reportPPTStr + "," + id;
            }
        }
        if (reportPPTStr.equals("")) {
            System.out.println("没有需要查询的专题id！！！");
        } else {
            data = restTemplate.postForObject("http://DW3-NEWQUERY-HOMEPAGE-ZUUL/reportPPT/pptReportForHomepage/info", reportPPTStr, List.class);
            System.out.println("专题服务查询出的数据是：" + data);
        }

        //4.将服务查询出的数据放到es的结果中，拼接结果
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
        resMap.put("data", esList);
        return resMap;
    }


    /**
     * 对返回结果依照ES的次序重新排序
     *
     * @param resList 处理后的结果，带有ord字段，按ord字段排序
     * @return 有序的结果
     */
    private List<Map<String, Object>> reOrder(List<Map<String, Object>> resList) {
        //按ord字段排序
        return null;
    }

    /**
     * 向指标服务请求数据
     *
     * @return
     */
    public List<Map<String, Object>> requestToKPI(String kpiStr) {
        String result = restTemplate.postForObject("http://DW3-NEWQUERY-HOMEPAGE-ZUUL/indexDetails/SlaverKpi/select", kpiStr, String.class);
        log.info("result is " + result);
        return null;
    }

    /**
     * 向专题服务请求数据
     *
     * @param list
     * @return
     */
    public List<Map<String, Object>> requestToSubject(List<String> list) {

        return null;
    }

    /**
     * 向报告服务请求数据
     *
     * @param list
     * @return
     */
    public List<Map<String, Object>> requestToReportPPT(List<String> list) {

        return null;
    }


}

