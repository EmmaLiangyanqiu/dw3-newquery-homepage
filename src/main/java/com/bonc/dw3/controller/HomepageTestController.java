package com.bonc.dw3.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(value = "首页查询-2", description ="示例数据")
@CrossOrigin(origins ="*")
@Controller
@RequestMapping("/test/HomePage")
public class HomepageTestController {

    /**
     * 1.头部栏组件接口
     *
     * @Author gp
     * @Date 2017/5/22
     */
    @ApiOperation("1.头部栏组件接口")
    @PostMapping("/headerSelect")
    public String headerSelect(@ApiParam("用户id")@RequestParam("userId")String userId,
                               @ApiParam("登陆令牌")@RequestParam("token")String token,
                               Model model){
        String[] a = {"01,综合", "02,指标", "03,专题", "04,报告"};
        List<Map<String, Object>> selectList = new ArrayList<>();
        Map<String, Object> resMap = new HashMap<>();
        for (int i = 0; i < a.length; i ++){
            String[] b = a[i].split(",");
            Map<String, Object> map = new HashMap<>();
            map.put("id", b[0]);
            map.put("name", b[1]);
            selectList.add(map);
        }
        resMap.put("default", selectList.get(0));
        resMap.put("selectList", selectList);
        //return resMap;
        //System.out.println(resMap);
        model.addAttribute("resMap", resMap);
        return "headerSelect";
    }


    /**
     * 2.菜单树组件接口
     *
     * @Author gp
     * @Date 2017/5/22
     */
    @ApiOperation("2.菜单树组件接口")
    @PostMapping("/nav")
    public String nav(@ApiParam("用户id")@RequestParam("userId")String userId,
                      @ApiParam("登陆令牌")@RequestParam("token")String token,
                      Model model){
        String[] title1 = {"01,移动业务计费收入,/indexDetails,0101", "02,4G业务计费收入,/indexDetails,0101"};
        String[] a001 = {"01,移动业务用户类,/homePage,0101", "02,移动业务使用类,/homePage,0101"};
        String[] a002 = {"01,移动业务,/homePage,0101", "02,宽带业务,/homePage,0101"};
        String[] title2 = {"01,运营总览,/special,0101", "02,三大战役,/special,0101"};
        String[] b001 = {"01,运营概况,/special,0101", "02,移动业务,/special,0101"};
        String[] b002 = {"01,212C业务专题,/special,0101", "02,冰淇淋业务专题,/special,0101"};
        Map<String, Object> resMap = new HashMap<>();
        List<Map<String, Object>> svgList = new ArrayList<>();
        List<Map<String, Object>> titleList1 = titleList(title1);
        List<Map<String, Object>> nodeList1 = nodeList(a001);
        List<Map<String, Object>> nodeList2 = nodeList(a002);
        List<Map<String, Object>> titleList2 = titleList(title2);
        List<Map<String, Object>> nodeList21 = nodeList(b001);
        List<Map<String, Object>> nodeList22 = nodeList(b002);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("id", "02");
        map1.put("name", "指标");
        map1.put("url", "/index");
        map1.put("imgName", "comp");
        Map<String, Object> titleMap1 = new HashMap<>();
        titleMap1.put("titleClassId", "001");
        titleMap1.put("titleClassName", "基础指标");
        titleMap1.put("list", titleList1);
        map1.put("titleList", titleMap1);
        Map<String, Object> nodeMap1 = new HashMap<>();
        nodeMap1.put("classId", "001");
        nodeMap1.put("className", "基础指标");
        nodeMap1.put("nodes", nodeList1);
        Map<String, Object> nodeMap2 = new HashMap<>();
        nodeMap2.put("classId", "002");
        nodeMap2.put("className", "分析指标");
        nodeMap2.put("nodes", nodeList2);
        List<Map<String, Object>> treeList1 = new ArrayList<>();
        treeList1.add(nodeMap1);
        treeList1.add(nodeMap2);
        map1.put("treeList", treeList1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("id", "03");
        map2.put("name", "专题");
        map2.put("url", "/special");
        map2.put("imgName", "special");
        Map<String, Object> titleMap2 = new HashMap<>();
        titleMap2.put("titleClassId", "001");
        titleMap2.put("titleClassName", "基础业务");
        titleMap2.put("list", titleList2);
        map2.put("titleList", titleMap2);
        Map<String, Object> nodeMap21 = new HashMap<>();
        nodeMap21.put("classId", "001");
        nodeMap21.put("className", "基础业务");
        nodeMap21.put("nodes", nodeList21);
        Map<String, Object> nodeMap22 = new HashMap<>();
        nodeMap22.put("classId", "002");
        nodeMap22.put("className", "创新业务");
        nodeMap22.put("nodes", nodeList22);
        List<Map<String, Object>> treeList2 = new ArrayList<>();
        treeList2.add(nodeMap21);
        treeList2.add(nodeMap22);
        map2.put("treeList", treeList2);

        svgList.add(map1);
        svgList.add(map2);
        resMap.put("svgList", svgList);
        //System.out.println(resMap);
        //return resMap;
        model.addAttribute("resMap", resMap);
        return "nav";
    }


    /**
     * 3.模块选项卡接口
     *
     * @Author gp
     * @Date 2017/5/22
     */
    @ApiOperation("3.模块选项卡接口")
    @PostMapping("/moduleTab")
    public String moduleTab(@ApiParam("用户id")@RequestParam("userId")String userId,
                            @ApiParam("登陆令牌")@RequestParam("token")String token,
                            @ApiParam("类型id")@RequestParam("markType")String markType,
                            Model model){
        List<Map<String, Object>> resList = new ArrayList<>();
        String[] aaa = {"0101,全部", "0102,日","0103,月"};
        for (int i = 0; i < aaa.length; i ++){
            String[] a = aaa[i].split(",");
            Map<String, Object> map = new HashMap<>();
            map.put("tabId", a[0]);
            map.put("tabName", a[1]);
            resList.add(map);
        }
        //return resList;
        model.addAttribute("resList", resList);
        return "moduleTab";
    }



    /**
     * 4-1.近期访问-筛选分类接口
     *
     * @Author gp
     * @Date 2017/5/23
     */
    @ApiOperation("4-1.近期访问-筛选分类接口")
    @PostMapping("/recentVisit")
    public String recentVisit(@ApiParam("用户id")@RequestParam("userId")String userId,
                              @ApiParam("登陆令牌")@RequestParam("token")String token,
                              Model model){
        Map<String, Object> resMap = new HashMap<>();
        String[] a = {"01,综合,/homePage", "02,指标,/index", "03,专题,/special", "04,报告,/report"};
        List<Map<String, Object>> selectList = new ArrayList<>();
        for (int i = 0; i < a.length; i ++){
            String[] b = a[i].split(",");
            Map<String, Object> map = new HashMap<>();
            map.put("id", b[0]);
            map.put("name", b[1]);
            map.put("url", b[2]);
            selectList.add(map);
        }
        resMap.put("default", selectList.get(0));
        resMap.put("selectList", selectList);

        //return resMap;
        model.addAttribute("resMap", resMap);
        return "recentVisit";
    }


    /**
     * 4-2.近期访问-访问内容接口
     *
     * @Author gp
     * @Date 2017/5/23
     */
    @ApiOperation("4-2.近期访问-访问内容接口")
    @PostMapping("/recentVisitList")
    public String recentVisitList(@ApiParam("用户id")@RequestParam("userId")String userId,
                                  @ApiParam("登陆令牌")@RequestParam("token")String token,
                                  @ApiParam("类型id")@RequestParam("markType")String markType,
                                  Model model){
        Map<String, Object> resMap = new HashMap<>();
        String[] a = {"专题,301,线下实体渠道发展用户,/special,0101",
                      "指标,201,20M及以上速率发展用户数,/index,0101",
                      "报告,401,移动业务发展用户,/report,0101"};
        List<Map<String, Object>> recentVisitList = new ArrayList<>();
        for (int i = 0; i < a.length; i ++){
            String[] b = a[i].split(",");
            Map<String, Object> map = new HashMap<>();
            map.put("class", b[0]);
            map.put("detailId", b[1]);
            map.put("detailName", b[2]);
            map.put("detailUrl", b[3]);
            map.put("detailFlag", b[4]);
            recentVisitList.add(map);
        }
        resMap.put("recentVisitList", recentVisitList);
        //return resMap;
        model.addAttribute("resMap", resMap);
        return "recentVisitList";
    }


    /**
     * 6-1.搜索-全部搜索接口
     *
     * @Author gp
     * @Date 2017/5/23 
     */
    @ApiOperation("6-1.搜索-全部搜索接口")
    @PostMapping("/allSearch")
    public String allSearch(@ApiParam("用户id")@RequestParam("userId")String userId,
                            @ApiParam("登陆令牌")@RequestParam("token")String token,
                            @ApiParam("搜索类型")@RequestParam("searchType")String searchType,
                            @ApiParam("搜索内容")@RequestParam("search")String search,
                            @ApiParam("分类：全部日月标识")@RequestParam("tabId")String tabId,
                            @ApiParam("分页起始")@RequestParam("numStart")String numStart,
                            @ApiParam("每一页记录条数")@RequestParam("num")String num,
                            Model model){
        Map<String, Object> resMap = new HashMap<>();
        List<Map<String, Object>> resList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();

        map1.put("markType", "02");
        map1.put("ord", "1");
        map1.put("id", "2001");
        map1.put("url", "http://ip:port/indexDetails/1001");
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("markName", "指标");
        dataMap.put("title ", "移动业务计费收入");
        dataMap.put("dayOrMonth", "日");
        dataMap.put("area", "全国");
        dataMap.put("date", "2017年5月10日");
        List<String> dataNameList = new ArrayList<>();
        String[] a = {"当日值", "本月累计", "同比", "环比"};
        for (String b : a){
            dataNameList.add(b);
        }
        dataMap.put("dataName", dataNameList);
        List<String> dataValueList = new ArrayList<>();
        String[] a1 = {"1234","1234","56%","12%"};
        for (String b1 : a1){
            dataValueList.add(b1);
        }
        dataMap.put("dataValue", dataValueList);
        dataMap.put("chartType", "line");
        List<Integer> chartDataList = new ArrayList<>();
        for (int i = 1; i <= 7; i ++){
            chartDataList.add(i);
        }
        dataMap.put("chartData", chartDataList);
        String[] a2 = {"4月30日", "5月1日", "5月2日", "5月3日", "5月4日", "5月5日", "5月6日"};
        List<String> chartXList = new ArrayList<>();
        for (String b2 : a2){
            chartXList.add(b2);
        }
        dataMap.put("chartX", chartXList);
        map1.put("data", dataMap);
        resList.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("markType", "03");
        map2.put("ord", "2");
        map2.put("id", "3001");
        map2.put("url", "http://ip:port/indexDetails/2001");
        Map<String, Object> dataMap2 = new HashMap<>();
        dataMap2.put("src", "u97.png");
        dataMap2.put("title", "4G用户专题");
        dataMap2.put("content", "包括全国整体业务发展状况、用户获取、用户迁转、流量价值释放业务的发展情况及宽带业务运营的主要月指标展示");
        dataMap2.put("type", "专题");
        dataMap2.put("tabName", "全部");
        map2.put("data", dataMap2);
        resList.add(map2);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("markType", "04");
        map3.put("ord", "3");
        map3.put("id", "4001");
        map3.put("url", "http://ip:port/indexDetails/4001");
        Map<String, Object> dataMap3 = new HashMap<>();
        dataMap3.put("title", "2G终端入网质态分析");
        dataMap3.put("type", "报告");
        dataMap3.put("tabName", "月");
        dataMap3.put("issue", "张三");
        dataMap3.put("issueTime", "2017年5月10日");
        String[] a3 = {"img1", "img2", "img3", "img4"};
        List<String> imgList = new ArrayList<>();
        for (String b : a3){
            imgList.add(b);
        }
        dataMap3.put("img", imgList);
        map3.put("data", dataMap3);
        resList.add(map3);
        resMap.put("data", resList);
        resMap.put("nextFlag", "0");
        //return resList;
        System.out.println(resMap);
        model.addAttribute("resMap", resMap);
        return "allSearch";
    }


    /**
     * 6-2.搜索-指标搜索接口
     *
     * @Author gp
     * @Date 2017/5/23
     */
    @ApiOperation("6-2.搜索-指标搜索接口")
    @PostMapping("/indexSearch")
    public String indexSearch(@ApiParam("用户id")@RequestParam("userId")String userId,
                              @ApiParam("登陆令牌")@RequestParam("token")String token,
                              @ApiParam("搜索类型")@RequestParam("searchType")String searchType,
                              @ApiParam("搜索内容")@RequestParam("search")String search,
                              @ApiParam("分页起始")@RequestParam("numStart")String numStart,
                              @ApiParam("每一页记录条数")@RequestParam("num")String num,
                              @ApiParam("分类：全部日月标识")@RequestParam("dayOrmonth")String dayOrmonth,
                              @ApiParam("地域")@RequestParam("area")String area,
                              @ApiParam("日期")@RequestParam("date")String date,
                              Model model){
        List<Map<String, Object>> resList = new ArrayList<>();
        Map<String, Object> resMap = new HashMap<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("indexName", "移动业务计费收入");
        map1.put("id", "2005");
        map1.put("url", "http://ip:port/indexDetails/2005");
        map1.put("markType", "02");
        map1.put("ord", "1");
        List<Map<String, Object>> chartDataList = new ArrayList<>();

        Map<String, Object> chartDataMap1 = new HashMap<>();
        chartDataMap1.put("chartType", "line");
        List<Integer> dataList = new ArrayList<>();
        for (int i = 1; i < 8; i ++){
            dataList.add(i);
        }
        chartDataMap1.put("data", dataList);
        String[] a = {"4月30日","5月1日","5月2日","5月3日","5月4日","5月5日","5月6日"};
        List<String> chartXList = new ArrayList<>();
        for (String b : a){
            chartXList.add(b);
        }
        chartDataMap1.put("chartX", chartXList);
        chartDataList.add(chartDataMap1);

        Map<String, Object> chartDataMap2 = new HashMap<>();
        chartDataMap2.put("chartType", "monthBar");
        chartDataMap2.put("unit", "万");
        List<Integer> sequentialDataList = new ArrayList<>();
        for (int i = 1; i < 13; i ++){
            sequentialDataList.add(i);
        }
        chartDataMap2.put("sequentialData", sequentialDataList);
        chartDataMap2.put("totalData", sequentialDataList);
        List<String> chartXList1 = new ArrayList<>();
        String[] a2 = {"6月","7月","8月","9月","10月","11月","12月","1月","2月","3月","4月","5月","6月"};
        for (String b : a2){
            chartXList1.add(b);
        }
        chartDataMap2.put("chartX", chartXList1);
        chartDataList.add(chartDataMap2);

        Map<String, Object> chartDataMap3 = new HashMap<>();
        chartDataMap3.put("chartType", "cityBar");
        chartDataMap3.put("unit", "万");
        chartDataMap3.put("sequentialData", sequentialDataList);
        chartDataMap3.put("totalData", sequentialDataList);
        chartDataMap3.put("chartX", chartXList1);
        chartDataList.add(chartDataMap3);

        Map<String, Object> chartDataMap4 = new HashMap<>();
        chartDataMap4.put("chartType", "pie");
        chartDataMap4.put("unit", "万");
        List<Map<String, Object>> dataList1 = new ArrayList<>();
        String[] a3 = {"流量王A套餐发展用户,182", "日租卡套餐发展用户,34",
                "其他套餐发展用户,260", "2I2C业务发展用户,102"};
        for (String b : a3){
            String[] s = b.split(",");
            Map<String, Object> map = new HashMap<>();
            map.put("name", s[0]);
            map.put("value", s[1]);
            dataList1.add(map);
        }
        chartDataMap4.put("data", dataList1);
        chartDataList.add(chartDataMap4);

        Map<String, Object> chartDataMap5 = new HashMap<>();
        chartDataMap5.put("chartType", "cityRank");
        chartDataMap5.put("unit", "万");
        List<String> tableTitleList = new ArrayList<>();
        String[] a4 = {"排名","城市","月累","环比"};
        for (String b : a4){
            tableTitleList.add(b);
        }
        chartDataMap5.put("tableTitle", tableTitleList);
        List<Map<String, Object>> tableValueList = new ArrayList<>();
        List<String> value1 = new ArrayList<>();
        value1.add("123456789");
        value1.add("12%");
        String[] a5 = {"1,北京", "2,上海"};
        for (String b : a5){
            String s[] = b.split(",");
            Map<String, Object> map = new HashMap<>();
            map.put("rank", s[0]);
            map.put("cityName", s[1]);
            map.put("value", value1);
            tableValueList.add(map);
        }

        chartDataMap5.put("tableValue", tableValueList);
        chartDataList.add(chartDataMap5);
        map1.put("chartData", chartDataList);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("indexName", "移动业务计费收入");
        map2.put("id", "2006");
        map2.put("url", "http://ip:port/indexDetails/2006");
        map2.put("markType", "02");
        map2.put("ord", "2");
        map2.put("markName", "指标");
        map2.put("dayOrMonth", "日");
        map2.put("area", "全国");
        map2.put("date", "2017年5月10日");
        String[] a6 = {"当日值","本月累计","同比","环比"};
        List<String> dataNameList = new ArrayList<>();
        for (String b : a6){
            dataNameList.add(b);
        }
        map2.put("dataName", dataNameList);
        String[] a7 = {"1234","1234","56%","12%"};
        List<String> dataValueList = new ArrayList<>();
        for(String b : a7){
            dataValueList.add(b);
        }
        map2.put("dataValue", dataValueList);
        map2.put("chartType", "line");
        map2.put("unit", "万");
        map2.put("data", dataList);
        map2.put("chartX", chartXList);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("indexName", "4g业务计费收入");
        map3.put("id", "2007");
        map3.put("url", "http://ip:port/indexDetails/2007");
        map3.put("markType", "02");
        map3.put("ord", "3");
        map3.put("markName", "指标");
        map3.put("dayOrMonth", "月");
        map3.put("area", "全国");
        map3.put("date", "2017年5月");
        map3.put("dataName", dataNameList);
        map3.put("dataValue", dataValueList);
        map3.put("chartType", "line");
        map3.put("unit", "千万");
        map3.put("data", dataList);
        map3.put("chartX", chartXList);

        resList.add(map1);
        resList.add(map2);
        resList.add(map3);

        resMap.put("nextFlag", "0");
        resMap.put("data", resList);

        //return resList;
        System.out.println(resMap);
        model.addAttribute("resMap", resMap);
        return "indexSearch";
    }


    /**
     * 6-3.搜索-专题搜索接口
     *
     * @Author gp
     * @Date 2017/5/23
     */
    @ApiOperation("6-3.搜索-专题搜索接口")
    @PostMapping("/specialSearch")
    public String specialSearch(@ApiParam("用户id")@RequestParam("userId")String userId,
                                @ApiParam("登陆令牌")@RequestParam("token")String token,
                                @ApiParam("搜索类型")@RequestParam("searchType")String searchType,
                                @ApiParam("搜索内容")@RequestParam("search")String search,
                                @ApiParam("分类：全部日月标识")@RequestParam("tabId")String tabId,
                                @ApiParam("分页起始")@RequestParam("numStart")String numStart,
                                @ApiParam("每一页记录条数")@RequestParam("num")String num,
                                Model model){
        List<Map<String, Object>> resList = new ArrayList<>();
        Map<String, Object> resMap = new HashMap<>();
        String[] a = {"u977.png|重点产品攻坚行动日报表|1|3001|http://ip:port/specialReport/3001|包括全国整体业务发展状况、用户获取、用户迁转、流量价值释放业务的发展情况及宽带业务运营的主要日指标展示。|专题|全部",
                      "u97.png|4G用户专题|2|3002|http://ip:port/specialReport/3002|包括全国整体业务发展状况、用户获取、用户迁转、流量价值释放业务的发展情况及宽带业务运营的主要月指标展示|专题|全部",
                      "u999.png|重点产品攻坚行动月考核|3|3003|http://ip:port/specialReport/3003|重点产品有效发展行动计划通报内容，包括各省考核指标的评分结果及完成情况。|专题|全部",
                      "u1010.png|重点产品攻坚行动月报表|4|3004|http://ip:port/specialReport/3004|包括全国整体业务发展状况、用户获取、用户迁转、流量价值释放业务的发展情况及宽带业务运营的主要月指标展示。|专题|全部",
                      "u9778.png|重点产品攻坚行动日报表|5|3005|http://ip:port/specialReport/3005|包括全国整体业务发展状况、用户获取、用户迁转、流量价值释放业务的发展情况及宽带业务运营的主要日指标展示。|专题|全部"};
        for (int i = 0; i < a.length; i ++){
            String[] s = a[i].split("\\|");
            Map<String, Object> map = new HashMap<>();
            map.put("src", s[0]);
            map.put("title", s[1]);
            map.put("ord", s[2]);
            map.put("id", s[3]);
            map.put("url", s[4]);
            map.put("content", s[5]);
            map.put("type", s[6]);
            map.put("tabName", s[7]);
            resList.add(map);
        }
        resMap.put("nextFlag", "0");
        resMap.put("data", resList);
        //return resList;
        System.out.println(resMap);
        model.addAttribute("resMap", resMap);
        return "specialSearch";
    }


    /**
     * 6-4.搜索-报告搜索接口
     *
     * @Author gp
     * @Date 2017/5/23
     */
    @ApiOperation("6-4.搜索-报告搜索接口")
    @PostMapping("/reportSearch")
    public String reportSearch(@ApiParam("用户id")@RequestParam("userId")String userId,
                               @ApiParam("登陆令牌")@RequestParam("token")String token,
                               @ApiParam("搜索类型")@RequestParam("searchType")String searchType,
                               @ApiParam("搜索内容")@RequestParam("search")String search,
                               @ApiParam("分页起始")@RequestParam("numStart")String numStart,
                               @ApiParam("每一页记录条数")@RequestParam("num")String num,
                               Model model){
        List<Map<String, Object>> resList = new ArrayList<>();
        Map<String, Object> resMap = new HashMap<>();
        String[] a = {"2G终端入网质态分析|1|4001|http://ip:port/Report/4001|报告|月|张三|2017年5月10日",
                      "渠道成本效益分析|2|4002|http://ip:port/Report/4002|报告|月|李四|2017年5月10日",
                      "移动用户离网分析|3|4003|http://ip:port/Report/4003|报告|月|王五|2017年5月10日",
                      "融合业务发展因素分析|4|4004|http://ip:port/Report/4004|报告|月|赵六|2017年5月10日"};
        String[] a1 = {"img1","img2","img3","img4"};
        List<String> imgList = new ArrayList<>();
        for (String b : a1){
            imgList.add(b);
        }
        for (String b : a){
            String[] s = b.split("\\|");
            Map<String, Object> map = new HashMap<>();
            map.put("title", s[0]);
            map.put("ord", s[1]);
            map.put("id", s[2]);
            map.put("url", s[3]);
            map.put("img", imgList);
            map.put("type", s[4]);
            map.put("tabName", s[5]);
            map.put("issue", s[6]);
            map.put("issueTime", s[7]);
            resList.add(map);
        }
        resMap.put("nextFlag", "0");
        resMap.put("data", resList);
        System.out.println(resMap);
        //return resList;
        model.addAttribute("resMap", resMap);
        return "reportSearch";
    }



    /**
     * 循环放titleList的方法
     *
     * @Author gp
     * @Date 2017/5/22
     */
    public List<Map<String, Object>> titleList(String[] title){
        List<Map<String, Object>> titleList = new ArrayList<>();
        for (int i = 0; i < title.length; i ++){
            String[] a = title[i].split(",");
            Map<String, Object> map = new HashMap<>();
            map.put("titleId", a[0]);
            map.put("titleName", a[1]);
            map.put("titleUrl", a[2]);
            map.put("flag", a[3]);
            titleList.add(map);
        }
        return titleList;
    }


    /**
     * 循环放nodeList的方法
     *
     * @Author gp
     * @Date 2017/5/22
     */
    public List<Map<String, Object>> nodeList(String[] aaa){
        List<Map<String, Object>> nodeList = new ArrayList<>();
        for (int i = 0; i < aaa.length; i ++){
            String[] a = aaa[i].split(",");
            Map<String, Object> map = new HashMap<>();
            map.put("nodeId", a[0]);
            map.put("nodeName", a[1]);
            map.put("nodeUrl", a[2]);
            map.put("flag", a[3]);
            nodeList.add(map);
        }
        return nodeList;
    }


}