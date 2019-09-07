package com.stillcoolme.registry;

import com.alibaba.fastjson.JSONArray;
import com.stillcoolme.common.InvokeUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2019/8/21 19:29
 * @description:
 *  zookeeper注册中心在初始化的时候，会建立好连接。
 *  然后注册的时候，针对clazz接口的每一个方法，都会生成一个唯一标识
 **/
public class ZookeeperRegistry implements Registry {
    private CuratorFramework client;

    public ZookeeperRegistry(String connectString) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        client.start();
        try {
            Stat myRPC = client.checkExists().forPath("/myRPC");
            if(myRPC == null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .forPath("/myRPC");
            }
            System.out.println("Zookeeper Client初始化完毕......");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册到zk的信息大概这样：
     * /myRPC/interface=com.stillcoolme.producer.HelloService&method=sayHello&
     *     parameter=com.stillcoolme.test.producer.TestBean
     *     [
     *         {
     *             "hostname":peer1,
     *             "port":8080
     *         },
     *         {
     *             "hostname":peer2,
     *             "port":8081
     *         }
     *     ]
     *  在服务消费的时候就可以拿到这样的注册信息，然后知道可以调用那台机器的那个端口。
     * @param clazz        类
     * @param registryInfo 本机的注册信息
     * @throws Exception
     */
    @Override
    public void register(Class clazz, RegistryInfo registryInfo) throws Exception {
        // 1. 注册的时候，先从zk中获取数据
        // 2. 将自己的服务器地址加入注册中心中

        // 为每一个接口的每一个方法注册一个临时节点，然后key为接口方法的唯一标识，data为服务地址列表
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            String key = InvokeUtils.buildInterfaceMethodIdentify(clazz, method);
            String path = "/myRPC/" + key;
            Stat stat = client.checkExists().forPath(path);
            List registryInfos;
            if (stat != null) {
                // 如果这个接口已经有人注册过了，把数据拿回来，然后将自己的信息保存进去
                byte[] bytes = client.getData().forPath(path);
                String data = new String(bytes, StandardCharsets.UTF_8);
                registryInfos = JSONArray.parseArray(data, RegistryInfo.class);
                if(registryInfos.contains(registryInfo)) {
                    // 正常来说，zk的临时节点，断开连接后，直接就没了，但是重启会经常发现存在节点，所以有了这样的代码
                    System.out.println("地址列表已经包含本机【" + key + "】，不注册了");
                } else {
                    registryInfos.add(registryInfo);
                    client.setData().forPath(path, JSONArray.toJSONString(registryInfos).getBytes());
                    System.out.println("注册到注册中心，路径为：【" + path + "】 信息为：" + registryInfo);
                }
            } else {
                registryInfos = new ArrayList();
                registryInfos.add(registryInfo);
                client.create()
                        .creatingParentsIfNeeded()
                        // 临时节点，断开连接就关闭
                        // 之所以采用临时节点是因为：如果机器宕机了，连接断开之后，消费者可以通过zookeeper的watcher机制感知到
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path, JSONArray.toJSONString(registryInfos).getBytes());
                System.out.println("注册到注册中心，路径为：【" + path + "】 信息为：" + registryInfo);
            }
        }

    }
}
