package com.stillcoolme.service.registry;

import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/8/21 19:19
 * @description:
 * 注册中心设计
 * 因为注册中心可能会提供多种来供用户选择，所以这里需要定义一个注册中心的接口
 *
 **/
public interface Registry {

    /**
     * 将服务提供者接口注册到注册中心
     * 这里我们提供一个注册的方法,这个方法的语义是将clazz对应的接口注册到注册中心。
     * 接收两个参数，一个是接口的class对象，另一个是注册信息
     * @param clazz        类
     * @param registryInfo 本机的注册信息
     */
    void register(Class clazz, RegistryInfo registryInfo) throws Exception;

    /**
     * 为服务消费者抓取注册表
     * @param clazz
     * @return 服务提供者所在的机器列表
     * @throws Exception
     */
    List fetchRegistry(Class clazz) throws Exception;

}
