package com.bonc.dw3.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ysl on 2017/8/7.
 * @author Candy
 */
@Mapper
public interface UserInfoMapper {
    /**
     * 根据用户id查询其权限省份
     *
     * @param userId 用户id
     * @return 权限省份
     * @Author gp
     * @Date 2017/12/11
     */
    String queryProvByUserId(String userId);


    /**
     * 查询省份名称
     *
     * @param provId 省份id
     * @return 省份名称
     * @Author gp
     * @Date 2017/12/11
     */
    List<String> queryProvById(@Param("provId") String provId);
}
