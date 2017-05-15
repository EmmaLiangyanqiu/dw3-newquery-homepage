package com.bonc.dw3.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemVariablesMapper {

	List<Map<String,Object>> getSystemVariables();
}
