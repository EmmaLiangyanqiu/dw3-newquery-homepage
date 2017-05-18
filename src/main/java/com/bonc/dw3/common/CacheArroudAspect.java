/*
package com.bonc.dw3.common;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.bonc.dw3.service.MonthReportByKylinService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class CacheArroudAspect implements EnvironmentAware {


    @Autowired
    MonthReportByKylinService service;

    */
/**
     * 日志对象
     *//*

    private Logger logger = LoggerFactory.getLogger(MonthReportByKylinService.class);

    @Autowired
    private Environment env;

    @Autowired
    RestTemplate restTemplate;


    // service层的统计耗时切面，类型必须为final String类型的,注解里要使用的变量只能是静态常量类型的
    public static final String POINT = "execution (* com.bonc.dw3.controller.*.*(..))";

    */
/**
     * 统计方法执行耗时Around环绕通知
     *
     * @param joinPoint
     * @return
     *//*

    @Around(POINT)
    public Object timeAround(ProceedingJoinPoint joinPoint) throws Throwable {


        Object re = new Object();
        //获取参数
        Object[] args = joinPoint.getArgs();
        //请求参数
        String argsStr = new String();
        //请求路径
        String pathStr = new String();
        //查询默认参数的code
        String code = "code";
        //发请求
        //RestTemplate restTemplateTmp = new RestTemplate();
        //从数据库查询得到的默认参数字符串
        String defaultParamsStr = new String();
        //拼接好的查缓存服务的code
        String codeForSearch = new String();
        //查询可用的缓存服务列表
        //List<Map<String, String>> cacheServerList = new ArrayList<>();
        //String cacheServer = new String();
        //String urlStr = new String();

        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        HttpServletResponse response = sra.getResponse();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");

        pathStr = request.getRequestURI().toLowerCase();
        String[] paths = pathStr.split("/");
        PrintWriter out = null;

        //得到缓存服务器地址
        */
/*cacheServerList = service.getCacheServerList();
        if (cacheServerList.size() != 0){
            cacheServer = cacheServerList.get(0).get("IP_ADDRESS") + ":" + cacheServerList.get(0).get("PORT");
            urlStr = "http://" + cacheServer + "/CacheServer/result";
            //logger.info("缓存服务url为=========" + urlStr);
        }else{
            logger.info("没有缓存服务可以使用！！！");
        }*//*


        //拼接查默认参数的code
        for (int i = 1; i < paths.length; i++) {
            code = code + "_" + paths[i];
        }
        //logger.info("查询默认参数的code------>" + code);

        //拼接参数为字符串，以逗号分隔
        for (int i = 0; i < args.length - 1; i++) {
            if (i == 0) {
                argsStr += args[i].toString();
            } else {
                argsStr += "," + args[i].toString();
            }
        }
        logger.info("请求参数------>" + argsStr);

        //判断请求来自前端还是来自数据缓存服务
        //cacheType字段为null时表示请求来自前端，为cache时表示请求来自缓存服务
        if (request.getHeader("cacheType") == null) {
            logger.info("请求来自前端:" + pathStr);
            //判断是否需要缓存->判断code在数据库中是否存放
            String cacheCode = code + "%";
            List<Map<String, String>> cacheList = service.getInfosViaCode(cacheCode);
            //没找到对应的code，不需要缓存
            if(cacheList.size() == 0){
            	re = joinPoint.proceed(args);
            }else{
                //首先判断有没有参数，没有参数的直接取缓存，有参数判断是否默认参数
                if (argsStr.equals("")) {
                    //logger.info("缓存服务url为=========" + urlStr);
                    //re = restTemplateTmp.postForObject(urlStr, code, String.class);
                    re = restTemplate.postForObject("http://CACHESERVER/CacheServer/result", code, String.class);
                    logger.info("re------------------->" + re.toString());
                    try {
                        out = response.getWriter();
                        out.write(re.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (out != null) {
                            out.close();
                        }
                    }
                } else {
                    //有参数，其中新发展用户的dataTable接口例外，直接查库
                    if (pathStr.equals("/newuser/datatable")) {
                        re = joinPoint.proceed(args);
                    } else {
//                        String likeCode = code + "%";
//                        //logger.info("用于模糊查询的code:::::" + likeCode);
//                        List<Map<String, String>> infosList = service.getInfosViaCode(likeCode);
                        if (cacheList.size() == 1){
                            defaultParamsStr = cacheList.get(0).get("VALUE");
                            codeForSearch = cacheList.get(0).get("CODE");
                            logger.info("用于查询缓存服务的code--------》" + codeForSearch);
                        }else{
                            for (Map<String, String> map : cacheList){
                                if (argsStr.equals(map.get("VALUE"))){
                                    defaultParamsStr = map.get("VALUE");
                                    codeForSearch = map.get("CODE");
                                    logger.info("用于查询缓存服务的code--------》" + codeForSearch);
                                    //logger.info("默认参数----》" + defaultParamsStr);
                                }
                            }
                        }
                        //判断是不是默认参数
                        //String defaultParamsStr = service.getDefaultParamsViaCode(code);
                        if (!StringUtils.isBlank(defaultParamsStr) && argsStr.equals(defaultParamsStr)) {
                            //re = restTemplateTmp.postForObject("http://192.168.31.6:7318/CacheServer/result", code, String.class);
                            //logger.info("缓存服务url为=========" + urlStr);
                            re = restTemplate.postForObject("http://CACHESERVER/CacheServer/result", codeForSearch, String.class);
                            logger.info("re------------------->" + re.toString());
                            try {
                                out = response.getWriter();
                                out.write(re.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (out != null) {
                                    out.close();
                                }
                            }
                        } else {
                            re = joinPoint.proceed(args);
                        }                       
                    }
                }
            }
        } else {
            //来自缓存服务
            //直接给缓存服务返回相应的值
            logger.info("该请求来自缓存服务:" + pathStr);
            re = joinPoint.proceed(args);
        }

        return re;
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }
}
*/
