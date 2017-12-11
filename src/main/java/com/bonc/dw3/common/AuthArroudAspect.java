package com.bonc.dw3.common;

import com.alibaba.fastjson.JSONObject;
import com.bonc.dw3.mapper.UserInfoMapper;
import com.bonc.dw3.schedule.RequestProvEveryday;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by ysl on 2017/8/7.
 * @author
 */
@Aspect
@Component
public class AuthArroudAspect {
    private static final Logger log= LoggerFactory.getLogger(AuthArroudAspect.class);

    private static final String POINTME = "execution (* com.bonc.dw3.controller.HomepageController.indexSearch(..))";
    //private static final String POINTHE = "execution (* com.bonc.dw3.controller.KpiController.prov*(..))";

    @Autowired
    UserInfoMapper infoMapper;

    @Around(POINTME)
    public Object authWork(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result=null;
        Object[] args=joinPoint.getArgs();
        if (args!=null&&args[0]!=null){
            Map<String,Object> param= (Map<String, Object>) args[0];
            String userId= (String) param.get("userId");
            String provinceId= (String) param.get("area");
            if (userId==null||"".equals(userId)){
                log.error("未传入userId!");
            }

            //查询该用户的provId
            String provId=infoMapper.queryProvByUserId(userId);
            System.out.println("******************用户权限省份为：" + provId);

            //如果未查询到该用户的省份，默认设置为  安徽
            if (provId==null||"".equals(provId)){
                provId="030";
            }
            //如果该用户所在的省份为以下三种情况时
            if (!RequestProvEveryday.containTheProv(provinceId,provId)){
                param.put("area",provId);
            }
            try {
                result=joinPoint.proceed(args);
            }catch (Throwable e){
                throw e;
            }
        }else {
            //没有参数的情况
            try {
                result=joinPoint.proceed(args);
            }catch (Throwable e){
                throw e;
            }
        }
        return result;
    }

    /*@Around(POINTHE)
    public Object anotherWork(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result=null;
        Object[] args=joinPoint.getArgs();
        if (args!=null&&args[0]!=null){
            String param= (String) args[0];
            JSONObject jsonObject = JSONObject.parseObject(param);
            String userId= (String) jsonObject.get("userId");
            String provinceId= (String) jsonObject.get("provId");
            if (userId==null||"".equals(userId)){
                log.error("未传入userId!");
            }
            //查询该用户的provId
            String provId=infoMapper.queryProvByUserId(userId);

            //如果未查询到该用户的省份，默认设置为  安徽
            if (provId==null||"".equals(provId)){
                provId="030";
            }
            //如果该用户所在的省份为以下三种情况时
            if (!RequestProvEveryday.containTheProv(provinceId,provId)){
                jsonObject.put("provId",provId);
            }
            try {
                String str=jsonObject.toJSONString();
                args[0]=str;
                result=joinPoint.proceed(args);
            }catch (Throwable e){
                throw e;
            }
        }else {
            //没有参数的情况
            try {
                result=joinPoint.proceed(args);
            }catch (Throwable e){
                throw e;
            }
        }
        return result;
    }*/
}
