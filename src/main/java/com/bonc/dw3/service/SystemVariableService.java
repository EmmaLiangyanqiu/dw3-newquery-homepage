package com.bonc.dw3.service;

import com.bonc.dw3.mapper.SystemVariablesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class SystemVariableService {
	
	@Autowired
	private  SystemVariablesMapper systemVariablesMapper;
	

	public static final String FULLCITYCODE = "code_1006";
	public static final String DATEDAYCODE = "code_1005";
	
	
	public static String acctTypeDay = "day";//4G专题日账期标记
	public static String cityType ="2";//4G专题地域-全国标记
	
	List<Map<String,Object>> systemVariablesList=new LinkedList<>();
	
	/**
	 * 初始化系统变量
	 */
	@PostConstruct
	public void init(){
		systemVariablesList =systemVariablesMapper.getSystemVariables();		
		if(systemVariablesList != null){			
			for(Map<String,Object> variables:systemVariablesList){
				if(DATEDAYCODE.equals(variables.get("SYS_CODE"))){
					acctTypeDay = variables.get("CODE_VALUE").toString();
				}
				
				if(FULLCITYCODE.equals(variables.get("SYS_CODE"))){
					cityType = variables.get("CODE_VALUE").toString();
				}
			}
		}
    }
	
	 
}
