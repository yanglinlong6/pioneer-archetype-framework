package com.glsx.plat.loggin.aop;

import com.glsx.plat.common.annotation.SysLog;
import com.glsx.plat.common.utils.StringUtils;
import com.glsx.plat.core.web.R;
import com.glsx.plat.jwt.util.JwtUtils;
import com.glsx.plat.loggin.LogginConstants;
import com.glsx.plat.loggin.LogginStrategyFactory;
import com.glsx.plat.loggin.thread.LogginTask;
import com.glsx.plat.redis.service.GainIdService;
import com.glsx.plat.web.utils.IpUtils;
import com.glsx.plat.web.utils.SessionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author payu
 */
@Slf4j
@Aspect
@Order(20)
@Component
public class SysLogAspect {

    final static GsonBuilder builder = new GsonBuilder();

    final static Gson gson = builder.create();

    @Resource
    private GainIdService gainIdService;

    @Resource
    private JwtUtils jwtUtils;

    @Qualifier("threadPoolTaskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private LogginStrategyFactory logginStrategyFactory;

    @Pointcut("@annotation(com.glsx.plat.common.annotation.SysLog)")
    public void logPointCut() {
    }


    @Before("logPointCut()")
    public void webpre(JoinPoint joinPoint) {
        String traceId = gainIdService.gainId(LogginConstants.LOG_REQ_ID);
        MDC.put(LogginConstants.LOG_REQ_ID, traceId);

        // ????????????
        try {
            saveSysLog(joinPoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????????????????
     */
    @Around("logPointCut()")
    public Object timeAround(ProceedingJoinPoint jp) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //????????????,??????????????????????????????,??????????????????????????????
        Object result = jp.proceed();
        //????????????(??????)
        long time = System.currentTimeMillis() - beginTime;

        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Object target = jp.getTarget();
        Method method = methodSignature.getMethod();
        //????????????????????????
        SysLog sysLog = method.getAnnotation(SysLog.class);
        String methodName = methodSignature.getName();
        if (sysLog.printSpendTime()) {
            log.info(target.getClass().getName() + "." + methodName + " ????????????:" + DurationFormatUtils.formatDuration(time, "HH:mm:ss.S") + "; ??????????????? :" + time + " ms");
        }
        return result;
    }

    /**
     * ??????????????????
     *
     * @param jp
     * @param returnValue ???????????????
     */
    @AfterReturning(pointcut = "logPointCut()", returning = "returnValue")
    public void doAfterReturning(JoinPoint jp, Object returnValue) {
        log.info("Returning Args : {}", new Gson().toJson(returnValue));
        // ??????????????????????????????
        String logTraceId = MDC.get(LogginConstants.MDC_LOG_DB_ID);
        if (StringUtils.isNotEmpty(logTraceId)) {
            if (returnValue != null) {
                R r = (R) returnValue;
                try {
                    logginStrategyFactory.getStrategy().updateLogStatus(logTraceId, r.isSuccess() ? "??????" : "??????");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        MDC.clear();
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param joinPoint
     */
    public void saveSysLog(JoinPoint joinPoint) throws Exception {

        //??????????????????
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Object target = joinPoint.getTarget();

        // ????????????
        SysLog sysLogMark = method.getAnnotation(SysLog.class);

        // ????????????????????????
        // ?????? @WebLog ?????????????????????
        String methodDescription = sysLogMark.value();

        HttpServletRequest request = SessionUtils.request();

        // ????????????????????????
        log.info("========================================== Start ==========================================");
        // ???????????? url
        log.info("URL            : {}", request.getRequestURL().toString());
        // ??????????????????
        log.info("Description    : {}", methodDescription);
        // ?????? Http method
        log.info("HTTP Method    : {}", request.getMethod());
        // ???????????? controller ??????????????????????????????
        log.info("Class Method   : {}.{}", methodSignature.getDeclaringTypeName(), methodSignature.getName());
        // ??????????????? IP
        log.info("IP             : {}", IpUtils.getIpAddr(request));
        // ??????????????????
        List<Object> args = Lists.newArrayList();
        Object[] originArgs = joinPoint.getArgs();
        if (originArgs != null) {
            for (int i = 0; i < originArgs.length; i++) {
                if (originArgs[i] instanceof ServletRequest || originArgs[i] instanceof ServletResponse) {
                    continue;
                }
                args.add(originArgs[i]);
            }
        }
        log.info("Request Args   : {}", gson.toJson(args));
        // ?????????
        Map<String, Object> userInfo = parseUserInfoByToken(request);
        log.info("OperatorInfo   : {}", userInfo.toString());

        String application = jwtUtils.getApplication();

        if (sysLogMark.saveLog()) {
            Future<String> future = executor.submit(new LogginTask(request, application, method, args, userInfo, sysLogMark, logginStrategyFactory.getStrategy()));
            String logId = future.get();
            log.info("LogginTask future.get:::" + logId);
            MDC.put(LogginConstants.MDC_LOG_DB_ID, logId);
        }
    }

    public Map<String, Object> parseUserInfoByToken(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isNotEmpty(token)) {
            // jwt??????token,????????????id
            return jwtUtils.parseClaim(token);
        }
        return Maps.newHashMap();
    }

}
