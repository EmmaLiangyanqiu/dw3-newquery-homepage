package com.bonc.dw3.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.CrossOrigin;

@Mapper
@CrossOrigin( origins="*")
public interface HomepageMapper {

    /**
     * 1.头部栏组件
     *
     * @Author gp
     * @Date 2017/5/27
     */
    List<Map<String, String>> headerSelect();


    /**
     * 2-1.菜单树组件接口：根据用户查找他的roleid
     *
     * @Author gp
     * @Date 2017/5/29
     */
    String selectRoleByUserId(String userId);

    /**
     * 2-2.菜单树组件接口：根据roleList查找用户的大部分的menuId
     *
     * @Author gp
     * @Date 2017/5/29
     */
    List<String> selectMostMenu(@Param("roleList") String[] roleList);

    /**
     * 2-3.菜单树组件接口：根据menuList查找用户的全部的menu
     *
     * @Author gp
     * @Date 2017/5/29
     */
    List<Map<String,String>> selectAllMenu(@Param("menuList") Set<String> menuList);

    /**
     * 2-4.菜单树接口：根据userId查询用户的rolein和roleout
     *
     * @Author gp
     * @Date 2017/5/29
     */
    Map<String,String> selectRoleInOut(String userId);


    /**
     * 3.模块选项卡接口
     * @Parameter markType 模块类型
     *
     * @Author gp
     * @Date 2017/5/27
     */
    List<Map<String, String>> moduleTab(String markType);


    /**
     * 4-1.近期访问接口：筛选列表接口
     *
     * @Author gp
     * @Date 2017/5/29
     */
    List<Map<String, String>> recentVisit();


    /**
     * 根据typeId查询跳转的url
     *
     * @Author gp
     * @Date 2017/5/31
     */
    String getUrlViaTypeId(String typeId);







	/**
	 * 查询条件
	 * @return
	 */
	List<Map<String, String>> select();
	/**
	 * 表头
	 * @return
	 */
	List<String> title();
	/**
	 * 查询指标树
	 * @return
	 */
	List<Map<String,Object>> selectKpiTree();
	
	/**
	 * 查询基础数据
	 * @param paramMap
	 * @return
	 */
	List<Map<String,Object>> selectDataByKylin(Map<String,Object> paramMap);
	
	/**
	 * 省份下钻指标树
	 * @return
	 */
	List<Map<String,Object>> selectProvTree(@Param("prov") String prov, @Param("kid") String kid);
	
	/**
	 * 右键下钻-省份，客户类型和合约类型
	 * @param paramMap
	 * @return
	 */
	List<Map<String,Object>> rightClickByKylin(Map<String,Object> paramMap);
	
	/**
	 * 客户类型下钻指标树 
	 * @param client
	 * @param kid
	 * @return
	 */
	List<Map<String,Object>> selectClientTree(@Param("client") List<String> client,@Param("kid")String kid);
	
	/**
	 * 渠道类型下钻指标树
	 * @param channel
	 * @param kid
	 * @return
	 */
	List<Map<String,Object>> selectChannelTree(@Param("channel") List<String> channel,@Param("kid")String kid);
	
	/**
	 * 合约类型下钻指标树
	 * @param contract
	 * @param kid
	 * @return
	 */
	List<Map<String,Object>> selectContractTree(@Param("contract") List<String> contract,@Param("kid")String kid);
	
	/**
	 * 渠道类型下钻数据
	 * @param paramMap
	 * @return
	 */
	List<Map<String,Object>> queryChannelByKylin(Map<String,Object> paramMap);
	
	/**
	 * 省份下钻地势树
	 * @param
	 * @param city
	 * @param kid
	 * @return
	 */
	List<Map<String,Object>> selectCityTree(@Param("area") String area,@Param("proid") String proid, @Param("city") String city, @Param("kid") String kid);

	/**
	 * 省份下钻地势数据
	 * @param paramMap
	 * @return
	 */
	List<Map<String,Object>> cityDataByKylin(Map<String,Object> paramMap);
	
	/**
	 * 趋势图月份
	 * @param kid
	 * @param date
	 * @param eleDate
	 * @return
	 */
	List<Map<String,Object>> trendTree(@Param("kid") String kid,@Param("date") String date,@Param("eleDate") String eleDate);
	
	/**
	 * 趋势图数据
	 * @param paramMap
	 * @return
	 */
	List<Map<String,Object>> trend(Map<String,Object> paramMap);
	/**
	 * 合约占比数据
	 * @param paramMap
	 * @return
	 */
	List<Map<String, Object>> contract(Map<String, Object> paramMap);
	
	/**
	 * 合约占比类型
	 * @param kid
	 * @param contract
	 * @return
	 */
	List<Map<String, Object>> contractTree(@Param("kid")String kid, @Param("contract")List<String> contract);
	/**
	 * 客户占比类型
	 * @param kid
	 * @param client
	 * @return
	 */
	List<Map<String, Object>> clientTree(@Param("kid")String kid, @Param("client")List<String> client);
	/**
	 * 客户占比数据
	 * @param paramMap
	 * @return
	 */
	List<Map<String, Object>> client(Map<String, Object> paramMap);
	/**
	 * 渠道占比数据
	 * @param paramMap
	 * @return
	 */
	List<Map<String, Object>> channel(Map<String, Object> paramMap);
	/**
	 * 渠道占比的指标单位情况
	 * @param kid
	 * @param channel
	 * @return
	 */
	List<Map<String, Object>> channelTree(@Param("kid")String kid,@Param("channel")List<String> channel);
	/**
	 * 默认图标kid
	 * @return
	 */
	Map<String, String> kid();
	/**
	 * area码表
	 * @return
	 */
	List<Map<String, String>> areaCode();
	
	/**
	 * 单位
	 * @param kid
	 * @return
	 */
	Map<String,String> getUnit(@Param("kid") String kid);
	
    /**
     * 通过code模糊查询默认参数和code
     *
     * @Author gp
     * @Date 2017/5/3
     */
    List<Map<String, String>> getInfosViaCode(String code);
}

