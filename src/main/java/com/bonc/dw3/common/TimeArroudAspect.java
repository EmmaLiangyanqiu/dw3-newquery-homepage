package com.bonc.dw3.common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class TimeArroudAspect implements EnvironmentAware{
	/**
	 * 日志对象
	 */
    private Logger logger = LoggerFactory.getLogger(TimeArroudAspect.class);
    
    @Autowired
    private Environment env;
    
    // 一秒钟，即1000ms
    private static final long ONE_MINUTE = 1000;

    // service层的统计耗时切面，类型必须为final String类型的,注解里要使用的变量只能是静态常量类型的
    public static final String POINT = "execution (* com.bonc.dw3.*.*.*(..))";

    /**
     * 统计方法执行耗时Around环绕通知
     * @param joinPoint
     * @return
     */
    @Around(POINT)
    public Object timeAround(ProceedingJoinPoint joinPoint) throws Throwable{
        // 定义返回对象、得到方法需要的参数
        Object obj = null;
        Object[] args = joinPoint.getArgs();
        long startTime = System.currentTimeMillis();

        try {
            obj = joinPoint.proceed(args);
        } catch (Throwable e) {
            logger.error("统计某方法执行耗时环绕通知出错", e);
            throw e;
        }

        // 获取执行的方法名
        long endTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();

        // 打印耗时的信息
        this.printExecTime(methodName, startTime, endTime);

        return obj;
    }

    /**
     * 打印方法执行耗时的信息，如果超过了一定的时间，才打印
     * @param methodName
     * @param startTime
     * @param endTime
     */
    private void printExecTime(String methodName, long startTime, long endTime) {
        long diffTime = endTime - startTime;
        String ifOpenStr = env.getProperty("system.timearround.ifOpen");
        String timeLimitStr =  env.getProperty("system.timearround.timeLimit");
        if(null == ifOpenStr){
        	ifOpenStr = "false";
        }
        if(null == timeLimitStr){
        	timeLimitStr = "120000";
        }
        boolean ifOpen = Boolean.valueOf(ifOpenStr);
        int timeLimit = Integer.parseInt(timeLimitStr);
        if (ifOpen && diffTime > timeLimit) {
        	logger.warn("-----" + methodName + " 方法执行耗时：" + diffTime + " ms");
        }
    }
    
    @Override
	public void setEnvironment(Environment environment) {
		this.env = environment;
	}
}