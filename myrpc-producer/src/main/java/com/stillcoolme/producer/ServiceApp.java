package com.stillcoolme.producer;

import com.stillcoolme.producer.service.HelloService;
import com.stillcoolme.producer.service.impl.HelloServiceImpl;
import com.stillcoolme.service.ApplicationContext;
import com.stillcoolme.service.ServiceConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/9/8 10:57
 * @description: 服务提供者
 *  作为rpc的服务端程序
 */
public class ServiceApp {

    static String ZK_ADDR = "zookeeper://10.2.5.1:2181,10.2.5.3:2181,10.2.5.4:2181";
    static Integer SERVICE_PORT = 50071;


    public static void main(String[] args) throws Exception {
        HelloService service = new HelloServiceImpl();
        ServiceConfig config = new ServiceConfig<>(HelloService.class, service);
        List serviceConfigList = new ArrayList<>();
        serviceConfigList.add(config);
        ApplicationContext context = new ApplicationContext(ZK_ADDR,
                serviceConfigList, null, SERVICE_PORT);


    }
}
