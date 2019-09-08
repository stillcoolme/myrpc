package com.stillcoolme;

import com.stillcoolme.provider.ApplicationContext;
import com.stillcoolme.provider.ServiceConfig;
import com.stillcoolme.service.HelloService;
import com.stillcoolme.service.service.impl.HelloServiceImpl;

import java.util.ArrayList;
import java.util.List;

public class TestProducer {

    public static void main(String[] args) {
        String connectionString = "zookeeper://10.2.5.2:2181";
        HelloService service = new HelloServiceImpl();
        ServiceConfig config = new ServiceConfig<>(HelloService.class, service);
        List serviceConfigList = new ArrayList<>();
        serviceConfigList.add(config);
        try {
            ApplicationContext ctx = new ApplicationContext(connectionString, serviceConfigList, null, 50071);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
