package com.bonc.dw3.service;

import com.bonc.dw3.common.thread.MyThread;
import com.bonc.dw3.mapper.HomepageMapper;
import com.bonc.dw3.mapper.UserInfoMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    //mapper对象
    @Autowired
    HomepageMapper homepageMapper;
    @Autowired
    UserInfoMapper userInfoMapper;

    /**
     * 向es搜索引擎发请求
     *
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
        resMap = restTemplateTmp.postForObject("http://10.249.216.117:8998/es/explore", requestEntity, Map.class);
        return resMap;
    }

    /**
     * 判断是否还有下一页
     *
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
     * 过滤无效数据
     *
     * @param dataList 需要过滤的数据
     * @return 过滤后的数据
     */
    public List<Map<String, Object>> filterAllData(List<Map<String, Object>> dataList) {
        List<Map<String, Object>> dataListFinally = new ArrayList<>();
        if ((dataList.size()) != 0 && (dataList != null)) {
            for (int j = 0; j < dataList.size(); j++) {
                if (!dataList.get(j).containsKey("id")) {
                    log.info(dataList.get(j) + "----数据没有返回id，舍弃！！！");
                } else {
                    String id = (String) dataList.get(j).get("id");
                    if (StringUtils.isBlank(id)) {
                        log.info(dataList.get(j) + "----数据返回无效的id，舍弃！！！");
                    } else {
                        dataListFinally.add(dataList.get(j));
                    }
                }
            }
        }
        return dataListFinally;
    }

    /**
     * 过滤无效数据
     *
     * @param chartData 需要过滤的数据
     * @return 过滤后的数据
     */
    public Map<String, Object> filterAllData(Map<String, Object> chartData) {
        Map<String, Object> dataMapFinally = new HashMap<>(10);
        String idStr = "id";
        if ((chartData != null) && (!chartData.containsKey(idStr))) {
            log.info(chartData + "------chartData没有返回id，舍弃！！！");
            dataMapFinally = null;
        } else if ((chartData != null) && (chartData.containsKey(idStr))) {
            String id = (String) chartData.get("id");
            if (StringUtils.isBlank(id)) {
                log.info(chartData + "------chartData返回无效的id，舍弃！！！");
                dataMapFinally = null;
            } else {
                dataMapFinally = chartData;
            }
        }
        return dataMapFinally;
    }

    /**
     * 获得所有详细数据
     *
     * @param dataFutures Future对象集合
     * @return 详细数据集
     */
    public List<Map<String, Object>> getAllDataFromFutures(List<Future> dataFutures) throws ExecutionException, InterruptedException {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < dataFutures.size(); i++) {
            Map<String, Object> map = null;
            map = (Map<String, Object>) dataFutures.get(i).get();
            log.info("详细数据为：{}", map);
            data.add(map);
        }
        return data;
    }

    /**
     * 根据报表id集合获取报表的详细数据
     *
     * @param statementList 报表id集合
     * @return 报表的详细数据
     */
    public List<Map<String, Object>> getStatementData(List<String> statementList) {
        List<Map<String, Object>> statementDataList = new ArrayList<>();
        if (statementList.size() != 0) {
            //查询数据
            statementDataList = homepageMapper.selectStatementData(statementList);
        }
        return statementDataList;
    }

    /**
     * 综合搜索接口：组合esList和所有服务的返回结果
     *
     * @param esList   es查询结果
     * @param dataList 详细数据
     */
    public List<Map<String, Object>> combineAllTypeData(List<Map<String, Object>> esList,
                                                        List<Map<String, Object>> dataList) {
        List<Map<String, Object>> resList = new ArrayList<>();
        for (Map<String, Object> map1 : esList) {
            try {
                String typeId = map1.get("typeId").toString();
                String id1 = map1.get("id").toString();
                //省份名称
                String areaStr = "";
                //省份id
                String provId = "";
                Map<String, Object> dimensionMap = (Map<String, Object>) map1.get("dimension");
                if (dimensionMap != null) {
                    provId = dimensionMap.get("provId").toString();
                    areaStr = homepageMapper.getProvNameViaProvId(provId);
                    log.info("该用户的省份名称为：" + areaStr);
                }
                //指标数据处理
                if (typeId.equals(SystemVariableService.kpi)) {
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
                } else if (typeId.equals(SystemVariableService.subject)) {
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
                } else if (typeId.equals(SystemVariableService.report)) {
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
                } else if ("4".equals(typeId)) {
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
                            if (map2.get("issueTime") == null) {
                                dataMap.put("issueTime", "未知");
                            } else {
                                //-类型日期转换为年月日类型日期
                                String issueTimeStr = toChineseDateString(map2.get("issueTime").toString());
                                dataMap.put("issueTime", issueTimeStr);
                            }

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
     * 指标搜索接口：组合es和指标服务返回的详细数据
     *
     * @param esList
     * @param chartDataFinally
     * @param dataList
     * @param areaStr
     * @return
     */
    public List<Map<String, Object>> combineKpiData(List<Map<String, Object>> esList,
                                                    Map<String, Object> chartDataFinally,
                                                    List<Map<String, Object>> dataList,
                                                    String url,
                                                    String areaStr,
                                                    int numStartValue) {
        List<Map<String, Object>> resList = new ArrayList<>();
        if (esList.size() == 0) {
            log.info("没有需要查询的指标id！！！");
        } else {
            for (int i = 0; i < esList.size(); i++) {
                Map<String, Object> map1 = esList.get(i);
                String id1 = map1.get("id").toString();
                Map<String, Object> dimensionMap = (Map<String, Object>) map1.get("dimension");
                String provId = dimensionMap.get("provId").toString();
                String cityId = dimensionMap.get("cityId").toString();
                String selectType = dimensionMap.get("selectType").toString();
                String date = dimensionMap.get("date").toString();

                //第一条数据
                if (i == 0 && chartDataFinally != null && numStartValue == 1) {
                    Map<String, Object> map = new HashMap<>(20);
                    map.put("ord", map1.get("ord"));
                    map.put("dayOrMonth", map1.get("dayOrMonth"));
                    map.put("id", map1.get("id"));
                    map.put("isMinus", map1.get("isMinus"));
                    map.put("title", map1.get("title"));
                    map.put("markType", map1.get("typeId"));
                    map.put("markName", map1.get("type"));
                    map.put("chartData", chartDataFinally.get("chartData"));
                    //返回账期（格式转换）
                    String dateStr = toChineseDateString(chartDataFinally.get("date").toString());
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
                    String unit = dealUnit(map.get("unit"));
                    map.put("unit", unit);
                    //是否占比指标
                    String unitNow = map.get("unit").toString();
                    String isPercentage = isPercentageKpi(unitNow);
                    map.put("isPercentage", isPercentage);
                    //结合ES数据
                    map.put("dimension", dimensionMap);
                    map.put("provId", map1.get("provId"));
                    map.put("cityId", map1.get("cityId"));
                    map.put("selectType", map1.get("selectType"));
                    map.put("date", map1.get("date"));

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
                                map.put("dimension", dimensionMap);
                                map.put("provId", map2.get("provId"));
                                map.put("cityId", map2.get("cityId"));
                                map.put("selectType", map2.get("selectType"));

                                resList.add(map);
                            }
                        }
                    }
                }
            }
        }
        return resList;
    }

    /**
     * 专题搜索接口：组合esList和专题服务返回的结果
     *
     * @param esList es返回的数据
     * @param data   专题服务返回的数据
     */
    public List<Map<String, Object>> combineTopicData(List<Map<String, Object>> esList,
                                                      List<Map<String, Object>> data) {
        List<Map<String, Object>> resList = new ArrayList<>();
        if (esList.size() != 0 && data.size() != 0) {
            for (Map<String, Object> map1 : esList) {
                String id1 = map1.get("id").toString();
                for (Map<String, Object> map2 : data) {
                    String id2 = map2.get("id").toString();
                    if (id1.equals(id2)) {
                        Map<String, Object> map = new HashMap<>(10);
                        map.put("ord", map1.get("ord"));
                        map.put("tabName", map1.get("tabName"));
                        map.put("id", map1.get("id"));
                        map.put("title", map1.get("title"));
                        map.put("type", map1.get("type"));
                        map.put("content", map1.get("content"));
                        map.put("src", map2.get("src").toString());
                        map.put("url", map2.get("url").toString());
                        resList.add(map);
                    }
                }
            }
        }
        return resList;
    }

    /**
     * 报告搜索接口：组合esList和报告服务返回的结果
     *
     * @param esList es返回的数据
     * @param data   报告服务返回的数据
     */
    public List<Map<String, Object>> combineReportData(List<Map<String, Object>> esList,
                                                       List<Map<String, Object>> data) {
        List<Map<String, Object>> resList = new ArrayList<>();
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
                            Map<String, Object> map = new HashMap<>(10);
                            map.put("title", map1.get("title"));
                            map.put("ord", map1.get("ord"));
                            map.put("id", map1.get("id"));
                            map.put("type", map1.get("type"));
                            map.put("tabName", map1.get("tabName"));
                            map.put("img", map2.get("img"));
                            map.put("issue", map2.get("issue").toString());
                            map.put("issueTime", map2.get("issueTime").toString());
                            map.put("url", url);
                            resList.add(map);
                        }
                    }
                }
            }
        }
        return resList;
    }

    /**
     * 报表搜索接口：组合报表数据
     *
     * @param esList es数据
     * @param data   详细数据
     * @return
     */
    public List<Map<String, Object>> combineStatementData(List<Map<String, Object>> esList, List<Map<String, Object>> data) {
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
                        if (id1.equals(id2)) {
                            Map<String, Object> dataMap = new HashMap<>(15);
                            dataMap.put("title", map1.get("title"));
                            dataMap.put("ord", map1.get("ord"));
                            dataMap.put("id", map1.get("id"));
                            dataMap.put("type", map1.get("type"));
                            dataMap.put("tabName", map1.get("dayOrMonth"));
                            dataMap.put("img", "u977.png");
                            //判断发布人字段是否为null
                            if (map2.get("issue") == null) {
                                dataMap.put("issue", "未知");
                            } else {
                                dataMap.put("issue", map2.get("issue"));
                            }
                            //判断发布时间字段是否为null
                            if (map2.get("issueTime") == null) {
                                dataMap.put("issueTime", "未知");
                            } else {
                                //-类型日期转换为年月日类型日期
                                String issueTimeStr = toChineseDateString(map2.get("issueTime").toString());
                                dataMap.put("issueTime", issueTimeStr);
                            }
                            dataMap.put("url", map2.get("url"));
                            dataList.add(dataMap);
                        }
                    }
                }
            }
        }
        return dataList;
    }


    /**
     * 单位是否为null的判断，为null时处理为空字符串,不为null时，取它本身即可
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

    /**
     * 将-类的日期转换为年月日类型的账期
     *
     * @param date 2017-11或2017-11-12
     * @return 如：2017年11月12日
     */
    public String toChineseDateString(String date) {
        String chartDataDate = date.replace("-", "");
        String dateStr = "";
        int dayLength = 6;
        if (chartDataDate.length() == dayLength) {
            dateStr = chartDataDate.substring(0, 4) + "年" + chartDataDate.substring(4) + "月";
        } else {
            dateStr = chartDataDate.substring(0, 4) + "年" + chartDataDate.substring(4, 6) + "月" + chartDataDate.substring(6) + "日";
        }
        return dateStr;
    }

}
