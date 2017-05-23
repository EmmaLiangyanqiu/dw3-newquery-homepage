package com.bonc.dw3.controller;

import freemarker.ext.beans.HashAdapter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(value = "首页查询-2", description ="示例数据")
@CrossOrigin(origins ="*")
@RestController
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
    public Map<String, Object> headerSelect(@ApiParam("用户id")@RequestParam("userId")String userId,
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
        return resMap;
        //System.out.println(resMap);
        //model.addAttribute("resMap", resMap);
        //return "headerSelect";
    }


    /**
     * 2.菜单树组件接口
     *
     * @Author gp
     * @Date 2017/5/22
     */
    @ApiOperation("2.菜单树组件接口")
    @PostMapping("/nav")
    public Map<String, Object> nav(@ApiParam("用户id")@RequestParam("userId")String userId,
                                   @ApiParam("登陆令牌")@RequestParam("token")String token,
                                   Model model){
        String[] title1 = {"01,移动业务计费收入,/indexDetails,0101", "02,4G业务计费收入,/indexDetails,0101"};
        String[] a001 = {"01,移动业务用户类,/homePage,0101", "02,移动业务实用类,/homePage,0101"};
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
        map1.put("titleList", titleList1);
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
        map2.put("titleList", titleList2);
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
        return resMap;
    }


    /**
     * 3.模块选项卡接口
     *
     * @Author gp
     * @Date 2017/5/22
     */
    @ApiOperation("3.模块选项卡接口")
    @PostMapping("/moduleTab")
    public List<Map<String, Object>> moduleTab(@ApiParam("用户id")@RequestParam("userId")String userId,
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
        return resList;
    }



    /**
     * 4-1.近期访问-筛选分类接口
     *
     * @Author gp
     * @Date 2017/5/23
     */
    @ApiOperation("4-1.近期访问-筛选分类接口")
    @PostMapping("/recentVisit")
    public Map<String, Object> recentVisit(@ApiParam("用户id")@RequestParam("userId")String userId,
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

        return resMap;
    }


    /**
     * 4-2.近期访问-访问内容接口
     *
     * @Author gp
     * @Date 2017/5/23
     */
    @ApiOperation("4-2.近期访问-访问内容接口")
    @PostMapping("/recentVisitList")
    public Map<String, Object> recentVisitList(@ApiParam("用户id")@RequestParam("userId")String userId,
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
        return resMap;
    }


    /**
     * 6-1.搜索-全部搜索接口
     *
     * @Author gp
     * @Date 2017/5/23 
     */
    @ApiOperation("6-1.搜索-全部搜索接口")
    @PostMapping("/allSearch")
    public List<Map<String, Object>> allSearch(@ApiParam("用户id")@RequestParam("userId")String userId,
                                               @ApiParam("登陆令牌")@RequestParam("token")String token,
                                               @ApiParam("搜索类型")@RequestParam("searchType")String searchType,
                                               @ApiParam("搜索内容")@RequestParam("str")String search,
                                               @ApiParam("分类：全部日月标识")@RequestParam("tabId")String tabId,
                                               @ApiParam("分页起始")@RequestParam("startNum")String numStart,
                                               @ApiParam("每一页记录条数")@RequestParam("num")String num,
                                               Model model){
        List<Map<String, Object>> resList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("markType", "02");
        map1.put("ord", "1");
        map1.put("id", "1001");
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
        map2.put("id", "2001");
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

        return resList;
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