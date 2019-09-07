package com.stillcoolme.common;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: stillcoolme
 * @date: 2019/8/21 19:38
 * @description:
 **/
public class InvokeUtils {

    /**
     * 对接口的方法使用他们的限定名和参数来组成一个唯一的标识，
     * 比如 HelloService#sayHello(TestBean)生成的大概是这样的：
     *  interface=com.stillcoolme.service.HelloService&method=sayHello&
     *  parameter=com.stillcoolme.service.TestBean
     * @param clazz
     * @param method
     * @return
     */
    public static String buildInterfaceMethodIdentify(Class clazz, Method method) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("interface", clazz.getName());
        map.put("method", method.getName());
        Parameter[] parameters = method.getParameters();
        if (parameters.length > 0) {
            StringBuilder param = new StringBuilder();
            for (int i = 0; i < parameters.length; i++) {
                Parameter p = parameters[i];
                param.append(p.getType().getName());
                if(i < parameters.length - 1) {
                    param.append(",");
                }
            }
            map.put("parameter", param.toString());
        }
        return map2String(map);
    }

    private static String map2String(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            sb.append(entry.getKey() + "=" + entry.getValue());
            if(iterator.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

}
