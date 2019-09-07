package com.stillcoolme.service.impl;

import com.stillcoolme.model.TestBean;
import com.stillcoolme.service.HelloService;

public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(TestBean testBean) {
        return "牛逼,我收到了消息：" + testBean;
    }
}
