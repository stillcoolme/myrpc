package com.stillcoolme.service.service.impl;

import com.stillcoolme.service.service.HelloService;
import com.stillcoolme.service.model.TestBean;

public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(TestBean testBean) {
        return "helloService: 我收到了消息：" + testBean;
    }
}
