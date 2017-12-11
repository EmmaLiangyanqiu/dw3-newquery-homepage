package com.bonc.dw3.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * @author gp
 */
@Mapper
@CrossOrigin( origins="*")
public interface HomepageMapper {

    /**
     * 1.头部栏组件
     *
     * @return 返回头部栏id，name的list
     * @Author gp
     * @Date 2017/5/27
     */
    List<Map<String, String>> headerSelect();


    /**
     * 2-1.菜单树组件接口：根据用户查找他的roleid
     *
     * @param userId 用户id
     * @Author gp
     * @Date 2017/5/29
     * @return 用户的角色id
     */
    String selectRoleByUserId(String userId);

    /**
     * 2-2.菜单树组件接口：根据roleList查找用户的大部分的menuId
     *
     * @param roleList 用户的角色list
     * @return 用户对应的菜单id
     * @Author gp
     * @Date 2017/5/29
     */
    List<String> selectMostMenu(@Param("roleList") String[] roleList);

    /**
     * 2-3.菜单树组件接口：根据menuList查找用户的全部的menu
     *
     * @param menuList 用户对应的全部的菜单id
     * @return 菜单树信息
     * @Author gp
     * @Date 2017/5/29
     */
    List<Map<String,String>> selectAllMenu(@Param("menuList") Set<String> menuList);

    /**
     * 2-4.菜单树接口：根据userId查询用户的rolein和roleout
     *
     * @param userId 用户id
     * @return 用户拥有和未拥有的角色id
     * @Author gp
     * @Date 2017/5/29
     */
    Map<String,String> selectRoleInOut(String userId);


    /**
     * 3.模块选项卡接口
     *
     * @param markType 数据类型
     * @return 模块信息
     * @Author gp
     * @Date 2017/5/27
     */
    List<Map<String, String>> moduleTab(String markType);


    /**
     * 4-1.近期访问接口：筛选列表接口
     *
     * @return 机器访问的筛选列表
     * @Author gp
     * @Date 2017/5/29
     */
    List<Map<String, String>> recentVisit();


    /**
     * 根据typeId查询跳转的url
     *
     * @param typeId 指标等数据的类型id
     * @return 数据跳转的url
     * @Author gp
     * @Date 2017/5/31
     */
    String getUrlViaTypeId(String typeId);


    /**
     * 根据省份id获取省份名称
     *
     * @param provId 省份id
     * @return 省份名称
     * @Author gp
     * @Date 2017/5/29
     */
    String getProvNameViaProvId(String provId);


    /**
     * 7.地域组件接口
     *
     * @param provId 省份id
     * @return 地域接口详细信息
     * @Author gp
     * @Date 2017/5/27
     */
    List<Map<String, String>> getArea(@Param("provId")String provId);


    /**
     * 8-1.日期组件接口：最大日账期
     *
     * @param table 查询最大账期使用的表
     * @return 日的最大账期
     * @Author gp
     * @Date 2017/6/9
     */
    String getDayMaxDate(@Param("table")String table);


    /**
     * 8-2.日期组件接口：最大月账期-
     *
     * @param table 查询最大账期使用的表
     * @return 月的最大账期
     * @Author gp
     * @Date 2017/6/9
     */
    String getMonthMaxDate(@Param("table")String table);


    /**
     * 报表搜索接口
     *
     * @param statementIdList 报表id的list
     * @return 报表的详细信息
     * @Author gp
     * @Date 2017/5/29
     */
    List<Map<String, Object>> selectStatementData(@Param("statementIdList") List<String> statementIdList);

}

