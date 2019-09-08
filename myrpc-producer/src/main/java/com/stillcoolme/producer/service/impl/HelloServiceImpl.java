package com.stillcoolme.producer.service.impl;

import com.stillcoolme.producer.service.HelloService;
import com.stillcoolme.producer.model.TestBean;

public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(TestBean testBean) {
        return "helloService: 我收到了消息：" + testBean;
    }
}
