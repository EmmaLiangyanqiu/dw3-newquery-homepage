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
    public List<Map<String, Object>> allSearch(String searchStr, String numStart, String num) {
        Map<String, Object> resMap = new HashMap<>();
        List<Map<String, Object>> resList = new ArrayList<>();
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
        //参数处理，如果参数为""时，为了zuul不报错，需要传参为no让具体的服务直接返回空list
        if (kpiStr.equals("")) {
            kpiStr = "no";
            System.out.println("es的查询结果中没有指标！！");
        } else {
            kpiStr = "-1,-1," + kpiStr;
        }
        if (subjectStr.equals("")){
            subjectStr = "no";
        }
        if (reportPPTStr.equals("")){
            reportPPTStr = "no";
        }
        MyRunable kpiRunable = new MyRunable(restTemplate, "http://DW3-NEWQUERY-HOMEPAGE-ZUUL/indexDetails/SlaverKpi/receiveKpi", kpiStr);
        Thread kpiThread = new Thread(kpiRunable);
        kpiThread.start();
        //请求报告服务
        reportPPTStr = "";
        MyRunable reportRunable = new MyRunable(restTemplateTmp, "http://192.168.110.57:7071/pptReportForHomepage/info", reportPPTStr);
        Thread reportThread = new Thread(reportRunable);
        reportThread.start();
        //请求专题服务
        subjectStr = "";
        MyRunable subjectRunable = new MyRunable(restTemplateTmp, "http://192.168.110.57:7071/specialForHomepage/icon", subjectStr);
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
        return reOrder(resList);
    }


    /**
     * 6-2.搜索：指标接口
     *
     * @Author gp
     * @Date 2017/5/31
     */









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


    /**
     * 6-3.搜索：专题接口
     *
     * @Author gp
     * @Date 2017/5/31
     */
    public Map<String,Object> specialSearch(String paramStr, String numStart, String num) {
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
        System.out.println(typeId);
        url = homepageMapper.getUrlViaTypeId(typeId);
        System.out.println(url);
        for (Map<String, Object> map : esList){
            String id = map.get("id").toString();
            if (specialStr.equals("")){
                specialStr = specialStr + id;
            }else {
                specialStr = specialStr + "," + id;
            }
        }
        if (specialStr.equals("")){
            System.out.println("没有需要查询的专题id！！！");
        }else {
            data = restTemplate.postForObject("http://DW3-NEWQUERY-HOMEPAGE-ZUUL/subject/specialForHomepage/icon", specialStr, List.class);
            System.out.println("专题服务查询出的数据是：" + data);
        }

        //4.将服务查询出的数据放到es的结果中
        for (Map<String, Object> map1 : esList){
            String id1 = map1.get("id").toString();
            for (Map<String, Object> map2 : data){
                String id2 = map2.get("id").toString();
                if (id1.equals(id2)){
                    map1.put("src", map2.get("src").toString());
                    map1.put("url", url);
                }
            }
        }
        resMap.put("data", esList);
        return resMap;
    }


    /**
     * 根据typeId查询跳转的url
     *
     * @Author gp
     * @Date 2017/5/31
     */
    public String getUrlViaTypeId(String typeId){
        String url = homepageMapper.getUrlViaTypeId(typeId);
        return url;
    }
}

