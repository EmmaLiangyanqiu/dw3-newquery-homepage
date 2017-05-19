package com.bonc.dw3.service;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
@CrossOrigin(origins="*")
public class HomepageService {

    /**
     * 1.搜索条件-全部接口
     *
     * @Author gp
     * @Date 2017/5/18
     */
    public List<Map<String, Object>> allSearch(String searchStr){
        List<Map<String, Object>> resList = new ArrayList<>();
        List<Map<String, Object>> topicList = new ArrayList<>();
        List<Map<String, Object>> reportList = new ArrayList<>();
        List<Map<String, Object>> kpiList = new ArrayList<>();

        System.out.println("查询es的参数--------->" + searchStr);
        //查询es
        RestTemplate restTemplate = new RestTemplate();
        Object res = restTemplate.postForObject("http://192.168.110.57:7070/es/explore", searchStr, Object.class);
        List<Map<String, Object>>  esList = (List<Map<String, Object>>) res;
        System.out.println("查询es的结果-------->" + esList);
        //查询类型是全部，需要遍历所有的数据，查看它们的type是什么，送到相应的服务
        for (Map<String, Object> esMap : esList){
            String type = esMap.get("Type").toString();
            if (type.equals("KPI_Name")){
                kpiList.add(esMap);
            }else if (type.equals("Report_Name")){
                reportList.add(esMap);
            }else if (type.equals("Topic_Name")){
                topicList.add(esMap);
            }
        }
        System.out.println("专题有-------->" + topicList);
        System.out.println("报告有-------->" + reportList);
        System.out.println("kpi有-------->" + kpiList);


        return resList;
    }






	@Autowired
    HomepageMapper monthReportMapper;

	//@Autowired
	DateUtils dateUtil;
	
	private static Logger log = LoggerFactory.getLogger(HomepageService.class);

	
	/**
	 * 1-1 查询条件
	 * @param
	 */
	public List<Map<String,Object>> select(){
		List<Map<String,String>> selectList = monthReportMapper.select();
		//循环遍历查询结果selectList，找到唯一的tid（父id），写进keyList里
		List<String> keyList = new ArrayList<String>();
		for(Map<String,String> map : selectList){
			boolean flag = false;//每循环出一个map，都将flag值设为false，表示ksyList里面没有该值
			if(null!=keyList&&keyList.size()>0){
				for(String key:keyList){
					if(key.equals(map.get("TID"))){
						flag=true;
						break;
					}
				}
				//没有该值，存进去
				if(flag==false){
					keyList.add(map.get("TID"));	
					}
			}
			else{
				keyList.add(map.get("TID"));
			}
		}
		List<Map<String,Object>> resList = new ArrayList<Map<String,Object>>();
		//处理：循环遍历keyList，将相同父id的查询条件放进一个map里
		for(String tid:keyList){
			Map<String,Object> selectMap = new HashMap<String,Object>();
			selectMap.put("tid", tid);
		    List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
			for(Map<String,String> paramMap: selectList){
				if(paramMap.get("TID").equals(tid)){
					Map<String,String> dataMap = new HashMap<String,String>();
					dataMap.put("id",paramMap.get("ID"));
					dataMap.put("text", paramMap.get("TEXT"));
					dataList.add(dataMap);
					selectMap.put("tname", paramMap.get("TNAME"));
				}
			}
			selectMap.put("data", dataList);
			resList.add(selectMap);
		}
		return resList;
	}
	
	/**
	 * 1-2 指标数据接口
	 * @param area
	 * @param prov
	 * @param city
	 * @param client
	 * @param channel
	 * @param contract
	 * @param date
	 * @return map，包括指标数据和对应列名称
	 * @throws ParseException
	 */
	public Map<String,Object> kpiData(String area,String prov,String city,List<String> client,
			List<String> channel,List<String> contract,String date) throws ParseException{
		Map<String, Object> paramMap = processParam(area, prov, city, client, channel, contract, date,null,null,null);
        Map<String,Object> kpiMap = new HashMap<String,Object>();
        
        //表头	
      	List<String> titleList = new LinkedList<String>();
      	titleList.addAll(monthReportMapper.title()); 
      	kpiMap.put("title", titleList);
      	
        List<Map<String,Object>> dataList = new ArrayList<>();
        //1、获取指标树
        List<Map<String,Object>> kpiTreeList = monthReportMapper.selectKpiTree();
        //2、获取基础数据
        DynamicDataSourceContextHolder.setDataSourceType("kylin");
        List<Map<String,Object>> mDataList = monthReportMapper.selectDataByKylin(paramMap);
        DynamicDataSourceContextHolder.clearDataSourceType();
        //3、组合树结构和数据
        for(Map<String,Object> kpiTree : kpiTreeList){
        	String kpiCode = (String) kpiTree.get("KPICODE");
        	if(StringUtils.isBlank(kpiCode)){
        		//输出空结构:将数据表的4个值写进map里
        		dataList.add(blankExt(kpiTree));
        	}else{
        		//flag为0表示没有在数据表找到对应的kpicode
        		int flag =0;
        		for(Map<String,Object> mData :mDataList){
        			if(kpiCode.equals(mData.get("KPI_CODE"))){
        				//处理输出数据
        				dataList.add(dealData(kpiTree,mData,date));
        				mDataList.remove(mData);
        				flag = 1;
        				break;
        			}
        		}
        		if(flag==0){
        			dataList.add(blankExt(kpiTree));
        		}
        	}
        }
		kpiMap.put("datalist", dataList);
		return kpiMap;
	}
	
	/**
	 * 1-3 右键下钻总接口
	 * @param area
	 * @param prov
	 * @param city
	 * @param client
	 * @param channel
	 * @param contract
	 * @param date
	 * @param kid
	 * @param rightclick
	 * @return
	 * @throws ParseException
	 */
	public List<Map<String,Object>> rightKpi(String area,String prov, String city,List<String> client,
			List<String> channel,List<String> contract,String date,String kid,String rightclick) throws ParseException{
		Map<String,Object> paramMap = processParam(area, prov, city, client, channel, contract, date, rightclick,null,kid);
		if(client.size()==1&&"-1".equals(client.get(0))){
			client = null;
		}
		if(channel.size()==1&&"-1".equals(channel.get(0))){
			channel = null;
		}
		if(contract.size()==1&&"-1".equals(contract.get(0))){
			contract = null;
		}
		//1、从oracle中查询指标树结构
		List<Map<String,Object>> treeList = new ArrayList<>();
		if(rightclick.equals("1")){
			treeList = monthReportMapper.selectProvTree(prov, kid);
		}else if(rightclick.equals("2")){
			treeList = monthReportMapper.selectClientTree(client, kid);
		}else if(rightclick.equals("3")){
			treeList = monthReportMapper.selectChannelTree(channel, kid);
		}else{
			treeList = monthReportMapper.selectContractTree(contract, kid);
		}
		
		//2、查数据
		DynamicDataSourceContextHolder.setDataSourceType("kylin");
		List<Map<String,Object>> rightList = new ArrayList<>();
		if(rightclick.equals("3")){
			rightList = monthReportMapper.queryChannelByKylin(paramMap);
		}else {
			rightList = monthReportMapper.rightClickByKylin(paramMap);
		}
		DynamicDataSourceContextHolder.clearDataSourceType();
		//3、组合
		List<Map<String,Object>> resList = dealCombination(treeList,rightList,date,rightclick);
		return resList;
	}
	
	/**
	 * 1-4 省份下钻地势接口
	 * @param area
	 * @param prov
	 * @param city
	 * @param client
	 * @param channel
	 * @param contract
	 * @param date
	 * @param kid
	 * @param proid
	 * @return
	 * @throws ParseException 
	 */
    public List<Map<String,Object>> cityData(String area, String prov, String city, List<String> client, List<String> channel, 
    		List<String> contract, String date, String kid,String proid) throws ParseException{
		Map<String,Object> paramMap = processParam(area, prov, city, client, channel, contract, date, null,proid,kid); 
         
        List<Map<String,Object>> resList = new ArrayList<>();
        //1、从oracle获取指标树
        List<Map<String,Object>> cityTreeList = monthReportMapper.selectCityTree(area,proid,city,kid);
        //2、从kylin获取基础数据
        DynamicDataSourceContextHolder.setDataSourceType("kylin");
        List<Map<String,Object>> cityList = monthReportMapper.cityDataByKylin(paramMap);
        DynamicDataSourceContextHolder.clearDataSourceType();
        for(Map<String,Object> cityTree : cityTreeList){
        	String id = (String) cityTree.get("ID");
        	int flag =0;
        	for(Map<String,Object> data :cityList){
        		if(id.equals(data.get("ID"))){
        			//处理输出数据
        			resList.add(dealData(cityTree,data,date));
        			cityList.remove(data);
        			flag = 1;
        			break;
        		}
        	}
        	if(flag==0){
        		resList.add(blankExt(cityTree));
        	}
        	}
    	return resList;
    }
    
    /**
     * 1-5 趋势图
     * @param area
     * @param prov
     * @param city
     * @param client
     * @param channel
     * @param contract
     * @param kid
     * @param date
     * @return
     * @throws ParseException
     */
    public Map<String,Object> trend(String area,String prov, String city,List<String> client,
			List<String> channel,List<String> contract,String kid,String date) throws ParseException{
		
		Map<String,Object> paramMap = processParam(area, prov, city, client, channel, contract, date, null, null,kid);
		//计算往前推11个月的月份
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");  
        Date d1 = sdf.parse(date);
        Date d2 = dateUtil.getElevthDate(d1);
        String eleDate = sdf.format(d2).replace("-", "");
        paramMap.put("eleDate", eleDate);
        date = date.replace("-", "");
        //1、获取月份
        List<Map<String,Object>> trendTreeList = monthReportMapper.trendTree(kid, date, eleDate);
        Map<String,String> unit = monthReportMapper.getUnit(kid);
        //2、获取数据
        DynamicDataSourceContextHolder.setDataSourceType("kylin");
        List<Map<String,Object>> trendDataList = monthReportMapper.trend(paramMap);
        DynamicDataSourceContextHolder.clearDataSourceType();
        //3、组合
        List<Map<String,Object>> trendList = combination(trendTreeList,trendDataList);
        List<String> trendPeriod = new LinkedList<String>();
 	    List<String> trendData = new LinkedList<String>();
 	   
 	   //处理数据
 	   for(Map<String, Object> map:trendList){
 		   trendPeriod.add((String) map.get("ID"));
 		   String data = ((List<String>) map.get("data")).get(0);
 		   trendData.add(data);
 		}
 	   	Map<String,Object> trendMap = new HashMap<String,Object>();
        
 		trendMap.put("period", trendPeriod);
 		trendMap.put("data", trendData);
 		trendMap.put("unit", unit.get("UNIT"));
		return trendMap;
    }
   
    /**
     * 1-6 合约类型占比
     * @param area
     * @param prov
     * @param city
     * @param client
     * @param channel
     * @param contract
     * @param kid
     * @param date
     * @return
     */
	public List<Map<String,Object>> contract(String area,String prov, String city,List<String> client,
			List<String> channel,List<String> contract,String kid,String date){
		
        Map<String, Object> paramMap = processParam(area, prov, city, client, channel, contract, date, null, null,kid);
		if(contract.size()==1&&"-1".equals(contract.get(0))){
			contract = null;
		}
        //合约类型
        List<Map<String, Object>> conTreeList = monthReportMapper.contractTree(kid,contract);
        //合约数据
        DynamicDataSourceContextHolder.setDataSourceType("kylin");
        List<Map<String, Object>> conDataList = monthReportMapper.contract(paramMap);
        DynamicDataSourceContextHolder.clearDataSourceType();
        
        List<Map<String, Object>> contractList = combination(conTreeList,conDataList);
        List<Map<String, Object>> resList = new  ArrayList<>();
		for(Map<String, Object> map:contractList){
        	Map<String, Object> dataMap = new HashMap<String, Object>();
        	String data = ((List<String>)map.get("data")).get(0);
        	dataMap.put("data", data);
        	dataMap.put("title", (String) map.get("TITLE"));
        	resList.add(dataMap);
        }
		return resList;
	}

	/**
	 * 1-7 公众集客占比
	 * @param area
	 * @param prov
	 * @param city
	 * @param client
	 * @param channel
	 * @param contract
	 * @param kid
	 * @param date
	 * @return
	 */
	public List<Map<String,Object>> client(String area,String prov, String city,List<String> client,
			List<String> channel,List<String> contract,String kid,String date){
		
		Map<String,Object> paramMap = processParam(area, prov, city, client, channel, contract, date, null, null,kid);
		if(client.size()==1&&"-1".equals(client.get(0))){
			client = null;
		}
		List<Map<String, Object>> clientTreeList = monthReportMapper.clientTree(kid,client);
		DynamicDataSourceContextHolder.setDataSourceType("kylin");
		List<Map<String, Object>> clientDataList = monthReportMapper.client(paramMap);
		DynamicDataSourceContextHolder.clearDataSourceType();
		
		List<Map<String, Object>> clientList = combination(clientTreeList, clientDataList);
		List<Map<String, Object>> resList = new  ArrayList<>();
		
		for(Map<String, Object> map:clientList){
        	Map<String, Object> dataMap = new HashMap<String, Object>();
        	String data = ((List<String>) map.get("data")).get(0);
        	dataMap.put("data", data);
        	dataMap.put("title", map.get("TITLE"));
        	resList.add(dataMap);
        }
		return resList;
	}
	
	/**
	 * 1-8 渠道类型占比
	 * @param area
	 * @param prov
	 * @param city
	 * @param client
	 * @param channel
	 * @param contract
	 * @param kid
	 * @param date
	 * @return
	 */
	public List<Map<String,Object>> channel(String area, String prov,String city,List<String> client,
			List<String> channel,List<String> contract,String kid,String date){
		
        Map<String,Object> paramMap = processParam(area, prov, city, client, channel, contract, date, null, null,kid);
		if(channel.size()==1&&"-1".equals(channel.get(0))){
			channel = null;
		}
		List<Map<String, Object>> chaTreeList = monthReportMapper.channelTree(kid,channel);
		DynamicDataSourceContextHolder.setDataSourceType("kylin");
		List<Map<String, Object>> chaDataList = monthReportMapper.channel(paramMap);
		DynamicDataSourceContextHolder.clearDataSourceType();
		
		List<Map<String,Object>> channelList = combination(chaTreeList, chaDataList);
		List<Map<String,Object>> resList = new ArrayList<>();
		
		for(Map<String, Object> map:channelList){
        	Map<String, Object> dataMap = new HashMap<String, Object>();
        	String data = ((List<String>) map.get("data")).get(0);
        	dataMap.put("data", data);
        	dataMap.put("title", map.get("TITLE"));
        	resList.add(dataMap);
        }
		return resList;
	
	}
	
	/**
	 * 1-9 返回默认指标id
	 * @return
	 */
	public Map<String,String> kid(){
		Map<String,String> kidMap = monthReportMapper.kid();
		return kidMap;
	}
	
	/**
	 * 通过code模糊查询默认参数和code
	 * @param code
	 * @return
	 */
    public List<Map<String, String>> getInfosViaCode(String code){
        List<Map<String, String>> resultList = new ArrayList<>();
        resultList = monthReportMapper.getInfosViaCode(code);
        return resultList;
    }	
	/**
	 * 趋势图、合约占比、客户占比组合
	 * @param treeList 
	 * @param dataList
	 * @return
	 */
	private List<Map<String,Object>> combination(List<Map<String, Object>> treeList,
			List<Map<String, Object>> dataList) {
		List<Map<String,Object>> resList = new ArrayList<>();
		for(Map<String,Object> tree : treeList){
			String id = (String) tree.get("ID");
			int flag = 0;
			for(Map<String,Object> data :dataList){
				if(!StringUtils.isBlank(id)&&id.equals(data.get("ID"))){
					//处理输出数据
					tree.put("data", format(tree,data));
					dataList.remove(data);
					flag = 1;
					break;
				}
			}
			//没有找到对应的数据
			if(flag==0){
				List<String> data = new ArrayList<>();
				data.add("0");
				tree.put("data", data);
			}
			resList.add(tree);
		}
		return resList;
	}

	/**
	 * 组合右键下钻的指标树和数据
	 * @param treeList
	 * @param rightList
	 * @return
	 * @throws ParseException 
	 */
	private List<Map<String, Object>> dealCombination(List<Map<String, Object>> treeList,
			List<Map<String, Object>> rightList,String date,String rightclick) throws ParseException {
		List<Map<String,Object>> resList = new ArrayList<>();
		for(Map<String,Object> tree : treeList){
			String id = (String) tree.get("ID");
			int flag = 0;
			for(Map<String,Object> right :rightList){
				if(!StringUtils.isBlank(id)&&id.equals(right.get("ID"))){
					//处理输出数据
					Map<String,Object> resMap = dealData(tree,right,date);
					if("1".equals(rightclick)){
						resMap.put("province", "1");
					}else{
						resMap.put("province", "0");
					}
					resList.add(resMap);
					rightList.remove(right);
					flag = 1;
					break;
				}
			}
			//没有找到对应的数据
			if(flag==0){
				Map<String,Object> resMap = blankExt(tree);
				if("1".equals(rightclick)){
					resMap.put("province", "1");
				}else{
					resMap.put("province", "0");
				}
				resList.add(resMap);
			}
		}
		return resList;
	}
	
	

	/**
	 * 处理传入的参数
	 * @param area 是否是139城市 1:139城市 2：全国
	 * @param prov 省份id
	 * @param city 地市id
	 * @param client 客户类型
	 * @param channel 渠道类型
	 * @param contract 合约类型
	 * @param date 账期
	 * @return 参数列表
	 */
	private Map<String, Object> processParam(String area, String prov, String city, List<String> client, List<String> channel,
			List<String> contract, String date,String rightClick, String proid,String kid) {
		Map<String,Object> paramMap = new HashMap<String,Object>();
//		判断渠道编码是否为-1，-1：传null
		if(channel.size()==1&&"-1".equals(channel.get(0))){
			paramMap.put("channel", null);
		}else{
			paramMap.put("channel", channel);
		}
		//判断合约类型
		if(contract.size()==1&&"-1".equals(contract.get(0))){
			paramMap.put("contract", null);
		}else{
			paramMap.put("contract", contract);
		}
		//判断合约类型
		if(client.size()==1&&"-1".equals(client.get(0))){
			paramMap.put("client", null);
		}else{
			paramMap.put("client", client);
		}
        paramMap.put("area", area);
        
        String areaParam = "";
        String cityParam = "";
        if("1".equals(rightClick)){
        	//地域允许下钻
        	if("-1".equals(city)){
        		if("111".equals(prov)||"112".equals(prov)||"113".equals(prov)){
        			cityParam = "-1";
            	}else{
            		areaParam = prov;
            		cityParam = "-1";
            	}
        	}
        }else{
	        //判断是否是139城市
	        if("1".equals(area)){
	        	//判断省份是否选择的全国
	        	if("111".equals(prov) || "114".equals(prov)){
	        		if("-1".equals(city)){
	        			areaParam = "114";
	        			cityParam = "-1";
	        		}else{
	        			cityParam = city;
	        		}
	        	}else if("112".equals(prov) || "113".equals(prov)){
	        		if("-1".equals(city)){
	        			areaParam = prov;
	        			cityParam = "-1";
	        		}else{
	        			cityParam = city;
	        		}
	        	}else{
	    			areaParam = prov;
	    			cityParam = city;
	        	}
	        }else {
	        	//判断省份是否选择的全国
	        	if("111".equals(prov) || "112".equals(prov) || "113".equals(prov)){
	        		if("-1".equals(city)){
	        			areaParam = prov;
	        			cityParam = "-1";
	        		}else{
	        			cityParam = city;
	        		}
	        	}else{
	    			areaParam = prov;
	    			cityParam = city;
	        	}
	        }
        }
       
        if("1".equals(area)){
        	paramMap.put("table", "V_DM_KPI_M_0010_139");
        }else{
        	paramMap.put("table", "V_DM_KPI_M_0010");
        }
        paramMap.put("kid", kid);
        paramMap.put("prov", areaParam);
        paramMap.put("city", cityParam);
        paramMap.put("date", date.replaceAll("-", ""));
        paramMap.put("rightClick", rightClick);
        paramMap.put("proid", proid);
		return paramMap;
	}

    /**
     * 处理输出空结构
     * @param kpiTree 指标树结构map 
     * @return 指标树结构和数据的组合的map
     */
	private Map<String, Object> blankExt(Map<String, Object> kpiTree) {
		//组合values并将kpicode赋值为-
		String[] values = {"-","-","-","-"} ;
		String kpiCode = (String) kpiTree.get("KPICODE");
		if(StringUtils.isBlank(kpiCode)){
			kpiTree.put("kpiCode", "-");
			kpiTree.put("channel_drill", "-");
			kpiTree.put("contract_drill", "-");
			kpiTree.put("region_drill", "-");
			kpiTree.put("user_drill", "-");
		}
		kpiTree.put("values", values);
		kpiTree.remove("UNIT");
		kpiTree.remove("FORMAT");
		kpiTree.remove("UATIO");
		return kpiTree;
	}
	
	/**
	 * 输出数据处理
	 * @param kpiTree 指标树结构map
	 * @param mData   数据map
	 * @param date	     月账期
	 * @return
	 * @throws ParseException
	 */
    private Map<String,Object> dealData(Map<String,Object> kpiTree, Map<String,Object> mData,String date) throws ParseException{
    	//1、计算平均值、环比、同比
    	Map<String,Object> values = formula(mData,date);
    	//2、格式化输出
    	kpiTree.put("values", format(kpiTree,values));
    	kpiTree.remove("UNIT");
    	kpiTree.remove("FORMAT");
    	kpiTree.remove("UATIO");
    	return kpiTree;
    }
    
    /**
     * 计算公式
     * @param mData 数据map
     * @param date 月账期
     * @return 处理后的数据map
     * @throws ParseException
     */
	private Map<String,Object> formula(Map<String, Object> mData,String date) throws ParseException {
		Map<String,Object> valueMap =new HashMap<>();
		DecimalFormat    df   = new DecimalFormat("######0.00");
//     1、计算本月平均值和上月平均值
		Double dy = (Double)mData.get("DY");
		Double sy = (Double) mData.get("SY");
		Double bnlj = (Double) mData.get("BNLJ");
		Double qnlj = (Double) mData.get("QNLJ");
    	double dypj = dy/DyDays(date);
    	double sypj = sy/LmDays(date);
//		2、计算累计同比
    	if(qnlj==0){
    		valueMap.put("LJTB", "-");
    	}else{
    		String ljtb = df.format(((bnlj-qnlj)/Math.abs(qnlj)*100))+"%";
    		valueMap.put("LJTB", ljtb);
    	}
//		3、计算日均环比
    	if(sypj==0){
    		valueMap.put("RJHB", "-");
    	}else{
    		String rjhb = df.format(((dypj-sypj)/Math.abs(sypj)*100))+"%";
    		valueMap.put("RJHB", rjhb);
    	}
    	valueMap.put("DY", dy);
    	valueMap.put("BNLJ", bnlj);
    	return valueMap;
	}

	/**
	 * 处理单位：精确度和百分号  
	 * @param kpiTree  指标树结构map
	 * @param values 经过公示计算过的数据map
	 * @return 四个数据值的集合
	 */
	private List<String> format(Map<String, Object> kpiTree,
				Map<String, Object> values) {
		//1、先除 2、判断保留
			Double uatio = ((BigDecimal) kpiTree.get("UATIO")).doubleValue();
			String format = (String) kpiTree.get("FORMAT"); 
			Double dy =  (Double)values.get("DY");
			DecimalFormat  df = new DecimalFormat("######0.00");
			DecimalFormat  dm = new DecimalFormat("######0");
			dy = dy/uatio;
			String dyz ="";
			String bnljz ="";
			String unit="";
			if(null != kpiTree.get("UNIT")){
				unit = (String) kpiTree.get("UNIT");
			}
			//处理本年累计值
			if(null != values.get("BNLJ")){
				Double bnlj = (Double) values.get("BNLJ");
				if(bnlj!=0){
					bnlj = bnlj/uatio;
					if(!StringUtils.isBlank(format)&&"FM9999999999990.00".equals(format)){
						bnljz = df.format(bnlj);
					}else{
						bnljz = dm.format(bnlj);
					}
					//当月值是否加%
					if(!StringUtils.isBlank(unit)&&unit.equals("%")){
						bnljz = bnljz+"%";
					}
					values.put("BNLJ", bnljz);
				}else{
					values.put("BNLJ", "-");
				}
			}
			//当月值精确度
			if(!StringUtils.isBlank(format)&&"FM9999999999990.00".equals(format)){
				dyz = df.format(dy);
			}else{
				dyz = dm.format(dy);
			}
			//当月值是否加%
			if(!StringUtils.isBlank(unit)&&unit.equals("%")){
				 dyz = dyz+"%";
			}
			values.put("DY", dyz);
			List<String> value = new ArrayList<>();
			value.add((String) values.get("DY"));
			if(null != values.get("BNLJ")){
				value.add((String) values.get("BNLJ"));
			}
			if(null != values.get("RJHB")){
				value.add((String) values.get("RJHB"));
			}			
			if(null != values.get("LJTB")){
				value.add((String) values.get("LJTB"));
			}
			return value;
		}
	
	/**
	 * 以date为参数，计算当月天数
	 * @param date 月账期
	 * @return 当月天数
	 * @throws ParseException
	 */
	private int DyDays(String date) throws ParseException {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");  
        Date d1 = sdf.parse(date);  
        Date d2 = dateUtil.getNextDate(d1); 
        int result = dateUtil.daysBetween(d1,d2);
        return result;
	}
	
	/**
	 * 以date为参数，计算上月天数
	 * @param date 月账期
	 * @return 上月天数
	 * @throws ParseException
	 */
	private int LmDays(String date) throws ParseException {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");  
        Date d1 = sdf.parse(date);  
        Date d2 = dateUtil.getLastDate(d1); 
        int result = dateUtil.daysBetween(d2,d1);
        return result;
	}
}

