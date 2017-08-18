package com.bonc.dw3.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by ysl on 2017/8/7.
 */
@Mapper
public interface UserInfoMapper {
    String queryProvByUserId(String userId);
    List<String> queryProvById(@Param("provId") String provId);
}
