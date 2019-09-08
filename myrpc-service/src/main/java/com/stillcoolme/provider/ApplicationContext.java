package com.stillcoolme.provider;

import com.stillcoolme.provider.common.InvokeUtils;
import com.stillcoolme.provider.netty.NettyServer;
import com.stillcoolme.provider.registry.MulticastRegistry;
import com.stillcoolme.provider.registry.Registry;
import com.stillcoolme.provider.registry.RegistryInfo;
import com.stillcoolme.provider.registry.ZookeeperRegistry;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: stillcoolme
 * @date: 2019/8/21 19:01
 * @description: 为了框架有一个统一的入口，定义一个类叫做ApplicationContext，可以认为这是一个应用程序上下文
 **/
public class ApplicationContext {
    private String registryUrl;
    private List<ServiceConfig> serviceConfigs;

    /**
     * 注册中心
     */
    private Registry registry;
    /**
     * 接口方法对应method对象
     * 在收到网络请求的时候，需要调用反射的方式调用method对象，所以存起来。
     */
    private Map<String, Method> interfaceMethods = new ConcurrentHashMap<>();


    public ApplicationContext(String registryUrl, List serviceConfigs, String x, Integer port) throws InterruptedException {
        // 1. 保存需要暴露的接口配置
        this.serviceConfigs = serviceConfigs == null ? new ArrayList<>() : serviceConfigs;
        // step 2: 实例化注册中心
        initRegistry(registryUrl);
        // step 3: 将接口注册到注册中心，从注册中心获取接口，初始化服务接口列表
        RegistryInfo registryInfo = null;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String hostname = addr.getHostName();
            String hostAddress = addr.getHostAddress();
            registryInfo = new RegistryInfo(hostname, hostAddress, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // 获取本机的的基本信息构造成RegistryInfo，然后调用了doRegistry方法
        doRegistry(registryInfo);

        // step 4：初始化Netty服务器，接受到请求，直接打到服务提供者的service方法中
        if (!this.serviceConfigs.isEmpty()) {
            // 需要暴露接口才暴露
            NettyServer nettyServer = new NettyServer(this.serviceConfigs, interfaceMethods);
            try {
                nettyServer.init(port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化注册中心
     * 根据url的schema来判断是哪种注册中心
     *
     * @param registryUrl
     */
    private void initRegistry(String registryUrl) {
        // 不同的注册中心判断
        if (registryUrl.startsWith("zookeeper://")) {
            registryUrl = registryUrl.substring(12);
            registry = new ZookeeperRegistry(registryUrl);
        } else if (registryUrl.startsWith("multicast://")) {
            registry = new MulticastRegistry(registryUrl);
        }

    }

    /**
     * 1. 将接口注册到注册中心中
     * 2. 对于每一个接口的每一个方法，生成一个唯一标识，保存在interfaceMethods集合中
     *
     * @param registryInfo
     * @throws Exception
     */
    private void doRegistry(RegistryInfo registryInfo) {
        for (ServiceConfig config : serviceConfigs) {
            Class type = config.getType();
            try {
                registry.register(type, registryInfo);
            } catch (Exception e) {
                // TODO 整合日志系统。。。
                e.printStackTrace();
            }
            Method[] declaredMethods = type.getDeclaredMethods();
            for (Method method : declaredMethods) {
                String identify = InvokeUtils.buildInterfaceMethodIdentify(type, method);
                // 在收到网络请求的时候，需要调用反射的方式调用method对象，所以存起来。
                interfaceMethods.put(identify, method);
            }
        }
    }


}
