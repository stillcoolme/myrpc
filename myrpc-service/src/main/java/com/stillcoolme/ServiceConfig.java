package com.stillcoolme;

/**
 * @author: stillcoolme
 * @date: 2019/8/21 18:53
 * @description:
 * 我们不会引入类似Spring之类的容器框架，
 * 所以我们需要定义一个服务提供者的配置类，
 * 它用于定义这个服务提供者是什么接口，具体的实例对象是什么。
 * 有了这个东西之后，我们就知道需要暴露哪些接口了
 **/
public class ServiceConfig<T> {

    public Class type;
    public T instance;

    public ServiceConfig(Class type, T instance) {
        this.type = type;
        this.instance = instance;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public T getInstance() {
        return instance;
    }

    public void setInstance(T instance) {
        this.instance = instance;
    }
}
