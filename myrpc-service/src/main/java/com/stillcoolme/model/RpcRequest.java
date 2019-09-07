package com.stillcoolme.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author: stillcoolme
 * @date: 2019/8/22 11:38
 * @description:
 **/
public class RpcRequest {

    private String interfaceIdentity;
    private Map<String, Object> parameterMap = new HashMap<>();
    private ChannelHandlerContext ctx;
    private String requestId;

    public static RpcRequest parse(String message, ChannelHandlerContext ctx) throws ClassNotFoundException {
        /*
         * {
         *   "interfaces":"interface=com.study.rpc.test.producer.HelloService&method=sayHello2¶meter=java.lang
         * .String,com.study.rpc.test.producer.TestBean",
         *   "parameter":{
         *      "java.lang.String":"haha",
         *      "com.study.rpc.test.producer.TestBean":{
         *              "name":"小王",
         *              "age":20
         *        }
         *    }
         * }
         */
        JSONObject jsonObject = JSON.parseObject(message);
        String interfaces = jsonObject.getString("interfaces");
        JSONObject parameter = jsonObject.getJSONObject("parameter");
        String requestId = jsonObject.getString("requestId");
        Set<String> strings = parameter.keySet();
        RpcRequest request = new RpcRequest();
        request.setInterfaceIdentity(interfaces);
        Map<String, Object> parameterMap = new HashMap<>(16);
        for (String key : strings) {
            if(key.equals("java.lang.String")) {
                parameterMap.put(key, parameter.getString(key));
            } else {
                Class clazz = Class.forName("key");
                Object object = parameter.getObject(key, clazz);
                parameterMap.put(key, object);
            }
        }
        request.setParameterMap(parameterMap);
        request.setCtx(ctx);
        request.setRequestId(requestId);
        return request;
    }

    public String getInterfaceIdentity() {
        return interfaceIdentity;
    }

    public void setInterfaceIdentity(String interfaceIdentity) {
        this.interfaceIdentity = interfaceIdentity;
    }

    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, Object> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
