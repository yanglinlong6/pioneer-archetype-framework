package com.glsx.plat.loggin.thread;

import com.alibaba.fastjson.JSONObject;
import com.glsx.plat.common.annotation.SysLog;
import com.glsx.plat.common.enums.OperateType;
import com.glsx.plat.loggin.AbstractLogginStrategy;
import com.glsx.plat.loggin.entity.SysLogEntity;
import com.glsx.plat.web.utils.IpUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * todo 挪到loggin组件里面
 */
@Slf4j
public class LogginTask implements Callable<String> {

    final static GsonBuilder builder = new GsonBuilder();

    final static Gson gson = builder.create();

    private HttpServletRequest request;
    private String application;
    private Method method;
    private List<Object> args;

    private Map<String, Object> userInfo;
    private SysLog sysLogMark;
    private AbstractLogginStrategy strategy;

    public LogginTask(HttpServletRequest request, String application, Method method, List<Object> args, Map<String, Object> userInfo, SysLog sysLogMark, AbstractLogginStrategy strategy) {
        this.request = request;
        this.application = application;
        this.method = method;
        this.args = args;
        this.userInfo = userInfo;
        this.sysLogMark = sysLogMark;
        this.strategy = strategy;
    }

    @Override
    public String call() throws Exception {
        // 封装
        SysLogEntity sysLogEntity = wrapSysLogEntity();

        // 存储
        storeSysLogEntity(sysLogEntity);

        return sysLogEntity.getId();
    }

    /**
     * 封装日志对象
     */
    public SysLogEntity wrapSysLogEntity() {
        //创建日志类
        SysLogEntity sysLog = new SysLogEntity();
        sysLog.setApplication(application);
        sysLog.setModule(sysLogMark.module());
        sysLog.setAction(sysLogMark.action().getType());
        sysLog.setRemark(sysLogMark.value());
        Long operatorId = 0L;
        String operator = "";
        if (OperateType.LOGIN.getType().equals(sysLogMark.action().getType())) {
            //登录没参数？？？，不可能
            if (args.get(0) instanceof String) {
                operator = (String) args.get(0);
            } else {
                JSONObject loginArg = (JSONObject) JSONObject.toJSON(args.get(0));
                if (loginArg.containsKey("account")) {
                    operator = loginArg.getString("account");
                } else if (loginArg.containsKey("username")) {
                    operator = loginArg.getString("username");
                }
            }
        } else {
            try {
                operatorId = Long.valueOf(String.valueOf(userInfo.get("userId")));
            } catch (Exception e) {
                log.error("操作人标识转换异常");
            }
            operator = (String) userInfo.get("account");
            sysLog.setTenant((String) userInfo.get("tenant"));
            sysLog.setBelongOrg((String) userInfo.get("belong"));
        }
        sysLog.setOperatorName(operator);
        sysLog.setOperator(operatorId);
        sysLog.setIp(IpUtils.getIpAddr(request));
        sysLog.setCreatedDate(new Date());

        //保存请求参数
        if (sysLogMark.saveRequest()) {
            sysLog.setRequestData(gson.toJson(args));
        }
        return sysLog;
    }

    /**
     * 存储日志
     *
     * @param sysLog
     */
    public void storeSysLogEntity(SysLogEntity sysLog) {
        log.info("记录日志,保存DB、MQ等 :{}", sysLog);
        strategy.saveLog(sysLog);
    }

}
