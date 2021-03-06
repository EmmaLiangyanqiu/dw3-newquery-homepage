package com.bonc.dw3.service;

import com.bonc.dw3.mapper.SystemVariablesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author gp
 */
@Component
public class SystemVariableService {

	@Autowired
	private  SystemVariablesMapper systemVariablesMapper;

    /**
     * 日标识
     */
    public static final String DAY = "code_1003";

    /**
     * 月标识
     */
    public static final String MONTH = "code_1004";

    /**
     * 有下一页
     */
	public static final String HASNEXT = "code_1018";

    /**
     * 没有下一页
     */
	public static final String NONEXT = "code_1019";

    /**
     * 指标
     */
    public static final String KPI = "code_1015";

    /**
     * 专题
     */
    public static final String SUBJECT = "code_1016";

    /**
     * 报告
     */
    public static final String REPORT = "code_1017";

    /**
     * 预发布用户userId
     */
    public static final String YUFABUUSERID = "code_1020";

    /**
     * 预发布-指标-最大账期表
     */
    public static final String KPIMAXDATETABLE_YUFABU = "code_1021";

    /**
     * 发布-指标-最大账期表
     */
    public static final String KPIMAXDATETABLE = "code_1022";


    /**
     * 系统变量  日标识
     */
    public static String day = "";

    /**
     * 月标识
     */
    public static String month = "";

    /**
     * 有下一页
     */
	public static String hasNext = "";

    /**
     * 没有下一页
     */
	public static String noNext ="";

    /**
     * 指标
     */
    public static String kpi ="";

    /**
     * 专题
     */
    public static String subject ="";

    /**
     * 报告
     */
    public static String report ="";

    /**
     * 预发布用户userId
     */
    public static String yufabuUserId = "";

    /**
     * 预发布-指标-最大账期表
     */
    public static String kpiMaxDateTableYufabu = "";

    /**
     * 发布-指标-最大账期表
     */
    public static String kpiMaxDateTable = "";

	List<Map<String,Object>> systemVariablesList=new LinkedList<>();

	/**
	 * 初始化系统变量
	 */
	@PostConstruct
	public void init(){
		systemVariablesList =systemVariablesMapper.getSystemVariables();
		if(systemVariablesList != null){
			for(Map<String,Object> variables:systemVariablesList){
			    //日标识
                if(DAY.equals(variables.get("code"))){
                    setDay(variables.get("value").toString());
                }
                //月标识
                if(MONTH.equals(variables.get("code"))){
                    setMonth(variables.get("value").toString());
                }
			    //有下一页
				if(HASNEXT.equals(variables.get("code"))){
					setHasNext(variables.get("value").toString());
				}
				//没有下一页
				if(NONEXT.equals(variables.get("code"))){
					setNoNext(variables.get("value").toString());
				}
				//指标
                if(KPI.equals(variables.get("code"))){
                    setKpi(variables.get("value").toString());
                }
                //专题
                if(SUBJECT.equals(variables.get("code"))){
                    setSubject(variables.get("value").toString());
                }
                //报告
                if(REPORT.equals(variables.get("code"))){
                    setReport(variables.get("value").toString());
                }
                //预发布用户userId
                if (YUFABUUSERID.equals(variables.get("code"))){
                    setYufabuUserId(variables.get("value").toString());
                }
                //预发布指标最大账期表
                if (KPIMAXDATETABLE_YUFABU.equals(variables.get("code"))){
                    setKpiMaxDateTableYufabu(variables.get("value").toString());
                }
                //发布指标最大账期表
                if (KPIMAXDATETABLE.equals(variables.get("code"))){
                    setKpiMaxDateTable(variables.get("value").toString());
                }
			}
		}
    }

    public static String getHasNext() {
        return hasNext;
    }

    public static void setHasNext(String hasNext) {
        SystemVariableService.hasNext = hasNext;
    }

    public static String getNoNext() {
        return noNext;
    }

    public static void setNoNext(String noNext) {
        SystemVariableService.noNext = noNext;
    }

    public static String getDay() {
        return day;
    }

    public static void setDay(String day) {
        SystemVariableService.day = day;
    }

    public static String getMonth() {
        return month;
    }

    public static void setMonth(String month) {
        SystemVariableService.month = month;
    }

    public static String getKpi() {
        return kpi;
    }

    public static void setKpi(String kpi) {
        SystemVariableService.kpi = kpi;
    }

    public static String getSubject() {
        return subject;
    }

    public static void setSubject(String subject) {
        SystemVariableService.subject = subject;
    }

    public static String getReport() {
        return report;
    }

    public static void setReport(String report) {
        SystemVariableService.report = report;
    }

    public static String getYufabuUserId() {
        return yufabuUserId;
    }

    public static void setYufabuUserId(String yufabuUserId) {
        SystemVariableService.yufabuUserId = yufabuUserId;
    }

    public static String getKpiMaxDateTableYufabu() {
        return kpiMaxDateTableYufabu;
    }

    public static void setKpiMaxDateTableYufabu(String kpiMaxDateTableYufabu) {
        SystemVariableService.kpiMaxDateTableYufabu = kpiMaxDateTableYufabu;
    }

    public static String getKpiMaxDateTable() {
        return kpiMaxDateTable;
    }

    public static void setKpiMaxDateTable(String kpiMaxDateTable) {
        SystemVariableService.kpiMaxDateTable = kpiMaxDateTable;
    }
}
