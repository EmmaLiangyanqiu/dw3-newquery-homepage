package com.bonc.dw3.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bonc.dw3.service.MonthReportByKylinService;

@Api(value = "渠道报表KYLIN", description ="月报")
@CrossOrigin(origins ="*")
@Controller
@RequestMapping("/MonthReport")
public class MonthReportByKylinController {
    
	@Autowired
	MonthReportByKylinService monthReportService;
	
	/**
	 * 筛选条件接口
	 * @param model
	 * @return
	 */
	@ApiOperation("筛选条件接口")
	@PostMapping("/select")
	public String select(Model model){
		List<Map<String,Object>> resList = monthReportService.select();
		model.addAttribute("resList", resList);
		return "select";
	}

	/**
	 * 指标数据接口
	 * @param area
	 * @param prov
	 * @param city
	 * @param client
	 * @param channel
	 * @param contract
	 * @param date
	 * @param model
	 * @return
	 * @throws ParseException 
	 */
	@ApiOperation("指标数据接口")
	@PostMapping("/data")
	public String kpiData(@ApiParam("地市编码") @RequestParam("area") String area,
			@ApiParam("省份编码") @RequestParam("prov") String prov,
			@ApiParam("city编码") @RequestParam("city") String city,
			@ApiParam("客户编码") @RequestParam("client") List<String> client,
			@ApiParam("渠道编码") @RequestParam("channel")  List<String> channel,
			@ApiParam("合约编码") @RequestParam("contract") List<String> contract,
			@ApiParam("账期") @RequestParam("date") String date,
			Model model) throws ParseException{
		Map<String,Object> kpiMap = monthReportService.kpiData(area, prov,city, client, channel, contract,date);
		model.addAttribute("kpiMap", kpiMap);
		return "kpi";
	}

	/**
	 * 指标右键下钻接口
	 * @param area
	 * @param prov
	 * @param city
	 * @param client
	 * @param channel
	 * @param contract
	 * @param date
	 * @param kid
	 * @param rightclick
	 * @param model
	 * @return
	 * @throws ParseException 
	 */
	@ApiOperation("指标右键下钻")
	@PostMapping("/kpiData")
	public String rightKpi(@ApiParam("地市编码") @RequestParam("area") String area,
			@ApiParam("省份编码") @RequestParam("prov") String prov,
			@ApiParam("city编码") @RequestParam("city") String city,
			@ApiParam("客户编码") @RequestParam("client") List<String> client,
			@ApiParam("渠道编码") @RequestParam("channel")  List<String> channel,
			@ApiParam("合约编码") @RequestParam("contract") List<String> contract,
			@ApiParam("账期") @RequestParam("date") String date,
			@ApiParam("指标id") @RequestParam("kid") String kid,
			@ApiParam("右击选择类型") @RequestParam("rightclick") String rightclick,
			Model model) throws ParseException{
		List<Map<String,Object>> kpiDataList = monthReportService.rightKpi(area, prov, city, client, channel, contract, date, kid, rightclick);
		model.addAttribute("kpiDataList", kpiDataList);
		return "rightClick";
	}
	
	/**
	 * 地势下钻
	 * @param area
	 * @param prov
	 * @param city
	 * @param client
	 * @param channel
	 * @param contract
	 * @param date
	 * @param kid
	 * @param proid
	 * @param model
	 * @return
	 * @throws ParseException 
	 */
	@ApiOperation("省份下钻地势接口")
	@PostMapping("/cityData")
	public String cityData(@ApiParam("地市编码") @RequestParam("area") String area,
			@ApiParam("省份编码") @RequestParam("prov") String prov,
			@ApiParam("city编码") @RequestParam("city") String city,
			@ApiParam("客户编码") @RequestParam("client") List<String> client,
			@ApiParam("渠道编码") @RequestParam("channel")  List<String> channel,
			@ApiParam("合约编码") @RequestParam("contract") List<String> contract,
			@ApiParam("账期") @RequestParam("date") String date,
			@ApiParam("指标id") @RequestParam("kid") String kid,
			@ApiParam("省份id") @RequestParam("proid") String proid,
			Model model) throws ParseException{
		List<Map<String,Object>> kpiDataList = monthReportService.cityData(area, prov, city, client, channel, contract, date, kid,proid);
		model.addAttribute("kpiDataList", kpiDataList);
		return "cityData";
	}

	/**
	 * 趋势图
	 * @param area
	 * @param prov
	 * @param city
	 * @param client
	 * @param channel
	 * @param contract
	 * @param kid
	 * @param date
	 * @param model
	 * @return
	 * @throws ParseException 
	 */
	@ApiOperation("趋势图")
	@PostMapping("/trend")
	public String trend(@ApiParam("地市编码") @RequestParam("area") String area,
			@ApiParam("省份编码") @RequestParam("prov") String prov,
			@ApiParam("city编码") @RequestParam("city") String city,
			@ApiParam("客户编码") @RequestParam("client") List<String> client,
			@ApiParam("渠道编码") @RequestParam("channel")  List<String> channel,
			@ApiParam("合约编码") @RequestParam("contract") List<String> contract,
			@ApiParam("指标id") @RequestParam("kid") String kid,
			@ApiParam("账期") @RequestParam("date") String date,
			Model model) throws ParseException{
		 Map<String,Object> trendMap = monthReportService.trend(area, prov, city, client, channel, contract, kid,date);
		 model.addAttribute("trendMap", trendMap);
	     return "trend";
	}

	/**
	 * 合约情况占比
	 * @param area
	 * @param prov
	 * @param city
	 * @param client
	 * @param channel
	 * @param contract
	 * @param kid
	 * @param date
	 * @param model
	 * @return
	 */
	@ApiOperation("合约情况占比")
	@PostMapping("/contract")
	public String contract(@ApiParam("地市编码") @RequestParam("area") String area,
			@ApiParam("省份编码") @RequestParam("prov") String prov,
			@ApiParam("city编码") @RequestParam("city") String city,
			@ApiParam("客户编码") @RequestParam("client") List<String> client,
			@ApiParam("渠道编码") @RequestParam("channel")  List<String> channel,
			@ApiParam("合约编码") @RequestParam("contract") List<String> contract,
			@ApiParam("指标id") @RequestParam("kid") String kid,
			@ApiParam("账期") @RequestParam("date") String date,Model model){
		List<Map<String, Object>> contractList = monthReportService.contract(area, prov,city, client, channel, contract, kid,date);
		model.addAttribute("contractList", contractList);
		return "contract";
	}
	
	/**
	 * 公众集客占比
	 * @param area
	 * @param prov
	 * @param city
	 * @param client
	 * @param channel
	 * @param contract
	 * @param kid
	 * @param date
	 * @param model
	 * @return
	 */
	@ApiOperation("公众集客占比")
	@PostMapping("/client")
	public String client(@ApiParam("地市编码") @RequestParam("area") String area,
			@ApiParam("省份编码") @RequestParam("prov") String prov,
			@ApiParam("city编码") @RequestParam("city") String city,
			@ApiParam("客户编码") @RequestParam("client") List<String> client,
			@ApiParam("渠道编码") @RequestParam("channel")  List<String> channel,
			@ApiParam("合约编码") @RequestParam("contract") List<String> contract,
			@ApiParam("指标id") @RequestParam("kid") String kid,
			@ApiParam("账期") @RequestParam("date") String date,Model model){
		List<Map<String, Object>> clientList = monthReportService.client(area, prov, city, client, channel, contract, kid, date);
	    model.addAttribute("clientList", clientList);
		return "client";
	}
	
	/**
	 * 渠道分类占比
	 * @param area
	 * @param prov
	 * @param city
	 * @param client
	 * @param channel
	 * @param contract
	 * @param kid
	 * @param date
	 * @param model
	 * @return
	 */
	@ApiOperation("渠道分类占比")
	@PostMapping("/channel")
	public String channel(@ApiParam("地市编码") @RequestParam("area") String area,
			@ApiParam("省份编码") @RequestParam("prov") String prov,
			@ApiParam("city编码") @RequestParam("city") String city,
			@ApiParam("客户编码") @RequestParam("client") List<String> client,
			@ApiParam("渠道编码") @RequestParam("channel")  List<String> channel,
			@ApiParam("合约编码") @RequestParam("contract") List<String> contract,
			@ApiParam("指标id") @RequestParam("kid") String kid,
			@ApiParam("账期") @RequestParam("date") String date,Model model){
		List<Map<String, Object>> channelList = monthReportService.channel(area,prov, city, client, channel, contract, kid, date);
		model.addAttribute("channelList", channelList);
	    return "channel";
	}
	

	/**
	 * 默认图标Kid接口
	 * @param model
	 * @return
	 */
	@ApiOperation(" 默认图标Kid接口")
	@PostMapping("/kid")
	public String kid(Model model){
		Map<String,String> kidMap = monthReportService.kid();
		model.addAttribute("kidMap", kidMap);
		return "kid";
	}
	
	
}