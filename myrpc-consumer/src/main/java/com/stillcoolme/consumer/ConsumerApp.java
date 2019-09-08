package com.stillcoolme.consumer;

import com.stillcoolme.producer.model.TestBean;
import com.stillcoolme.producer.service.HelloService;
import com.stillcoolme.service.ApplicationContext;

import java.util.Collections;

/**
 * @author: stillcoolme
 * @date: 2019/9/8 15:42
 * @description:
 */
public class ConsumerApp {

    public static void main(String[] args) throws Exception {
        String connectionString = "zookeeper://localhost1:2181,localhost2:2182,localhost3:2181";
        ReferenceConfig config = new ReferenceConfig(HelloService.class);
        ApplicationContext ctx = new ApplicationContext<HelloService>(connectionString, Collections.singletonList(config), null,
                50070);
        HelloService helloService = (HelloService) ctx.getService(HelloService.class);
        System.out.println("sayHello(TestBean)结果为：" + helloService.sayHello(new TestBean("张三", 20)));
    }
}
