package com.bonc.dw3.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

/**
 * @author gp
 */
@Mapper
public interface SystemVariablesMapper {

    /**
     * 系统变量查询
     *
     * @return 返回所有系统变量的详细的值
     * @Author gp
     * @Date 2017/12/11
     */
	List<Map<String,Object>> getSystemVariables();
}
