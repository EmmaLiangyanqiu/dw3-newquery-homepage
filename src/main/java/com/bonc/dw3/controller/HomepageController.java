package com.bonc.dw3.controller;

import com.bonc.dw3.service.HomepageService;
import com.bonc.dw3.service.MenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(value = "首页查询-1", description ="测试")
@CrossOrigin(origins ="*")
@Controller
@RequestMapping("/HomePage")
public class HomepageController {

    //homepage service对象
    @Autowired
    HomepageService homepageService;

    //菜单栏 service对象
    @Autowired
    MenuService menuService;


    /**
     * 1.头部栏组件接口
     * @Parameter paramMap 例：{"userId":"41","token":"2"}
     *
     * @Author gp
     * @Date 2017/5/27
     */
    @ApiOperation("1.头部栏组件接口")
    @PostMapping("/headerSelect")
    public String headerSelect(@ApiParam("请求参数json串")@RequestBody Map<String, Object> paramMap,
                               Model model){
        Map<String, Object> resMap = homepageService.headerSelect();

        model.addAttribute("resMap", resMap);
        return "headerSelect";
    }


    /**
     * 2.菜单树组件接口
     * @Parameter paramMap 例：{"userId":"41","token":"2"}
     *
     * @Author gp
     * @Date 2017/5/29
     */
    /*@ApiOperation("2.菜单树接口")
    @PostMapping("/nav")
    public String nav(@ApiParam("请求参数json串")@RequestBody Map<String, Object> paramMap,
                      Model model){
        String userId = paramMap.get("userId").toString();
        Map<String, Object> resMap = menuService.data(userId);

        model.addAttribute("resMap", resMap);
        return "nav";
    }*/


    /**
     * 3.模块选项卡接口
     * @Parameter paramMap 例：{"userId":"41","token":"2","markType":"999"}
     *
     * @Author gp
     * @Date 2017/5/27
     */
    @ApiOperation("3.模块选项卡接口")
    @PostMapping("/moduleTab")
    public String moduleTab(@ApiParam("请求参数json串")@RequestBody Map<String, Object> paramMap,
                            Model model){
        String markType = paramMap.get("markType").toString();
        List<Map<String, String>> resList = homepageService.moduleTab(markType);

        model.addAttribute("resList", resList);
        return "moduleTab";
    }


    /**
     * 6.搜索接口:全部搜索
     * @Parameter paramMap 例：{"userId":"41","token":"2","searchType":"999","search":"","tabId":"-1","numStart":"1","num":"10"}
     *
     * @Author gp
     * @Date 2017/5/16
     */
    @ApiOperation("6-1.搜索-全部搜索接口")
    @PostMapping("/allSearch")
    public String allSearch(@ApiParam("请求参数json串")@RequestBody Map<String, Object> paramMap,
                            Model model) throws InterruptedException {
        String userId = paramMap.get("userId").toString();
        String searchType = paramMap.get("searchType").toString();
        String search = paramMap.get("search").toString();
        String tabId = paramMap.get("tabId").toString();
        String numStart = paramMap.get("numStart").toString();
        String num = paramMap.get("num").toString();

        //es查询参数处理
        String paramStr = userId + "," + searchType + "," + search + "," + tabId +"," +numStart + "," + num;
        //查询es并拼接结果
        Map<String, Object> resMap = homepageService.allSearch(paramStr, numStart, num);

        model.addAttribute("resMap", resMap);
        return "allSearch";
    }


    /**
     * 6-1.搜索：指标接口
     * @Parameter paramMap 例：{"userId":"41","token":"2","searchType":"1","search":"","dayOrmonth":"-1","numStart":"1","num":"10","area":"010","date":"2016-10-01"}
     *
     * @Author gp
     * @Date 2017/5/31
     */
    @ApiOperation("6-2.搜索-指标搜索接口")
    @PostMapping("/indexSearch")
    public String indexSearch(@ApiParam("请求参数json串")@RequestBody Map<String, Object> paramMap,
                              Model model) throws InterruptedException {
        String userId = paramMap.get("userId").toString();
        String searchType = paramMap.get("searchType").toString();
        String search = paramMap.get("search").toString();
        String dayOrmonth = paramMap.get("dayOrmonth").toString();
        String numStart = paramMap.get("numStart").toString();
        String num = paramMap.get("num").toString();
        String area = paramMap.get("area").toString();
        String date = paramMap.get("date").toString();

        //es查询参数处理
        String paramStr = userId + "," + searchType + "," + search + "," + dayOrmonth +"," +numStart + "," + num;
        //查询es并拼接结果
        Map<String, Object> resMap = homepageService.indexSearch(paramStr, numStart, num, area, date);
        //数据为空
        List<Map<String, Object>> resList = (List<Map<String, Object>>) resMap.get("data");
        if (resList.size() == 0){
            model.addAttribute("resMap", new HashMap<>());
        }else{
            model.addAttribute("resMap", resMap);
        }

        return "indexSearch";
    }


    /**
     * 7.搜索：专题接口
     * @Parameter paramMap 例：{"userId":"41","token":"2","searchType":"2","search":"","tabId":"-1","numStart":"1","num":"10"}
     *
     * @Author gp
     * @Date 2017/5/31
     */
    @ApiOperation("7.搜索-专题搜索接口")
    @PostMapping("/specialSearch")
    public String specialSearch(@ApiParam("请求参数json串")@RequestBody Map<String, Object> paramMap,
                                Model model) throws InterruptedException {
        Map<String, Object> resMap = new HashMap<>();

        String userId = paramMap.get("userId").toString();
        String searchType = paramMap.get("searchType").toString();
        String search = paramMap.get("search").toString();
        String tabId = paramMap.get("tabId").toString();
        String numStart = paramMap.get("numStart").toString();
        String num = paramMap.get("num").toString();

        //es查询参数处理
        String paramStr = userId + "," + searchType + "," + search + "," + tabId +"," +numStart + "," + num;
        //查询es获得数据
        resMap = homepageService.specialSearch(paramStr, numStart, num);

        model.addAttribute("resMap", resMap);
        return "specialSearch";
    }


    /**
     * 8.搜索：报告接口
     * @Parameter paramMap 例：{"userId":"41","token":"2","searchType":"3","search":"","numStart":"1","num":"10"}
     *
     * @Author gp
     * @Date 2017/6/9
     */
    @ApiOperation("8.搜索-报告搜索接口")
    @PostMapping("/reportSearch")
    public String reportSearch(@ApiParam("请求参数json串")@RequestBody Map<String, Object> paramMap,
                               Model model) throws InterruptedException {
        Map<String, Object> resMap = new HashMap<>();

        String userId = paramMap.get("userId").toString();
        String searchType = paramMap.get("searchType").toString();
        String search = paramMap.get("search").toString();
        String numStart = paramMap.get("numStart").toString();
        String num = paramMap.get("num").toString();

        //tabId用-1占位
        String paramStr = userId + "," + searchType + "," + search + "," + "-1," +numStart + "," + num;
        //查询es获得数据
        resMap = homepageService.reportPPTSearch(paramStr, numStart, num);

        model.addAttribute("resMap", resMap);
        return "reportSearch";
    }


    /**
     * 9.地域组件接口
     * @Parameter paramMap 例：{"userId":"41","token":"2"}
     *
     * @Author gp
     * @Date 2017/6/9
     */
    @ApiOperation("9.地域接口")
    @PostMapping("/area")
    public String area(@ApiParam("请求参数对象") @RequestBody Map<String,Object> paramMap,
                       Model model){
        model.addAttribute("dataList", homepageService.area());
        return "area";
    }


    /**
     * 10.日期组件接口
     * @Parameter paramMap 例：{"userId":"41","token":"2","dateType":"1"}
     *
     * @Author gp
     * @Date 2017/6/9
     */
    @ApiOperation("10.日期接口")
    @PostMapping("/maxDate")
    public String maxDate(@ApiParam("请求参数对象") @RequestBody Map<String, String> paramMap,
                          Model model ){
        String dateType=paramMap.get("dateType").toString();
        String date = homepageService.getMaxDate(dateType);

        model.addAttribute("date", date);
        return "date";
    }


    /**
     * 4-1.近期访问组件接口：筛选列表接口
     *
     * @Author gp
     * @Date 2017/5/29
     */
    /*@ApiOperation("4-1.近期访问组件接口：筛选列表接口")
    @PostMapping("/recentVisit")
    public String recentVisit(*//*@ApiParam("用户id")@RequestParam("userId")String userId,
                              @ApiParam("登陆令牌")@RequestParam("token")String token,*//*
                              @ApiParam("请求参数json串")@RequestBody Map<String, Object> paramMap,
                              Model model){
        String userId = paramMap.get("userId").toString();
        String token = paramMap.get("token").toString();
        Map<String, Object> resMap = homepageService.recentVisit();
        model.addAttribute("resMap", resMap);
        return "recentVisit";
    }*/


    /**
     * 4-2.近期访问组件接口：近期访问列表接口
     *
     * @Author gp
     * @Date 2017/5/25
     */
    /*@ApiOperation("4-2.近期访问组件接口：近期访问列表接口")
    @PostMapping("/recentVisitList")
    public String recentVisitList(*//*@ApiParam("用户ID")@RequestParam("userId")String userId,
                                  @ApiParam("登陆令牌")@RequestParam("token")String token,
                                  @ApiParam("查询类型指标专题报告")@RequestParam("selectId")String selectId,*//*
                                  @ApiParam("请求参数json串")@RequestBody Map<String, Object> paramMap,
                                  Model model){
        String userId = paramMap.get("userId").toString();
        String token = paramMap.get("token").toString();
        String selectId = paramMap.get("selectId").toString();
        String paramStr = userId + "," + token + "," + selectId;
        Map<String, Object> recentVisitMap = homepageService.recentVisitList(paramStr);
        model.addAttribute("resMap", recentVisitMap);
        return "recentVisitList";
    }*/

}