package com.bonc.dw3.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2016/9/12.
 */
@CrossOrigin(origins ="*")
@Mapper
public interface TbuserMapper {

    //查找该用户的所有menu
    String selectRoleByUserId(String userId);

    List<String> selectMostMenu(@Param("roleList") String[] roleList);

    Map<String,String> selectRoleInOut(String userId);

    List<Map<String,String>> selectAllMenu(@Param("menuList") Set<String> menuList);

}
