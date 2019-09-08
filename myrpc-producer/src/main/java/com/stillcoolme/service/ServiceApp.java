package com.stillcoolme.service;

import com.stillcoolme.provider.ApplicationContext;
import com.stillcoolme.provider.ServiceConfig;
import com.stillcoolme.service.service.HelloService;
import com.stillcoolme.service.service.impl.HelloServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/9/8 10:57
 * @description:
 *  服务提供者
 */
public class ServiceApp {

    static String ZK_ADDR = "zookeeper://localhost1:2181,localhost2:2181,localhost3:2181";
    static Integer SERVICE_PORT = 50071;

    public static void main(String[] args) throws InterruptedException {
        HelloService service = new HelloServiceImpl();
        ServiceConfig config = new ServiceConfig<>(HelloService.class, service);
        List serviceConfigList = new ArrayList<>();
        serviceConfigList.add(config);
        ApplicationContext context = new ApplicationContext(ZK_ADDR,
                serviceConfigList, null, SERVICE_PORT);



    }
}
