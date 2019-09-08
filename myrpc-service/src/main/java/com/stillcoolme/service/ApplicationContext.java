package com.stillcoolme.service;

import com.alibaba.fastjson.JSONObject;
import com.stillcoolme.service.common.InvokeUtils;
import com.stillcoolme.service.model.RpcResponse;
import com.stillcoolme.service.netty.NettyServer;
import com.stillcoolme.service.registry.MulticastRegistry;
import com.stillcoolme.service.registry.Registry;
import com.stillcoolme.service.registry.RegistryInfo;
import com.stillcoolme.service.registry.ZookeeperRegistry;
import com.stillcoolme.service.netty.NettyClient;
import com.stillcoolme.service.utils.LoadBalancer;
import com.stillcoolme.service.utils.RandomLoadbalancer;
import com.stillcoolme.service.utils.RoundRobinLoadbalancer;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author: stillcoolme
 * @date: 2019/8/21 19:01
 * @description: 为了框架有一个统一的入口，定义一个类叫做ApplicationContext，可以认为这是一个应用程序上下文
 **/
public class ApplicationContext<T> {
    private String registryUrl;
    private List<ServiceConfig> serviceConfigs;
    private List<ServiceConfig> referenceConfigs;

    /**
     * 注册中心
     */
    private Registry registry;
    /**
     * 接口方法对应method对象
     * 在收到网络请求的时候，需要调用反射的方式调用method对象，所以存起来。
     */
    private Map<String, Method> interfaceMethods = new ConcurrentHashMap<>();
    // 保存接口和服务地址
    private Map<Class, List> interfacesMethodRegistryList = new ConcurrentHashMap();
    private Map<RegistryInfo, ChannelHandlerContext> channels = new ConcurrentHashMap();

    /**
     * 响应队列
     */
    private ConcurrentLinkedQueue<RpcResponse> responses = new ConcurrentLinkedQueue();

    /**
     * 负责生成requestId的类
     */
    private LongAdder requestIdWorker = new LongAdder();

    private LoadBalancer loadBalancer = new RoundRobinLoadbalancer();


    public ApplicationContext(String registryUrl, List serviceConfigs, String x, Integer port) throws Exception {
        // 1. 保存需要暴露的接口配置
        this.serviceConfigs = serviceConfigs == null ? new ArrayList<>() : serviceConfigs;
        this.referenceConfigs = referenceConfigs == null ? new ArrayList<>() : referenceConfigs;

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

        // step 5：启动处理响应的processor
        initProcessor();
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
    private void doRegistry(RegistryInfo registryInfo) throws Exception {
        // 服务提供者的配置
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

        // 服务消费者的配置
        for(ServiceConfig config: referenceConfigs) {
            // 在注册的时候，我们需要将需要消费的接口，通过注册中心抓取出来
            List registryInfos = registry.fetchRegistry(config.getType());
            if (registryInfos != null) {
                // 保存接口和服务地址
                interfacesMethodRegistryList.put(config.getType(), registryInfos);
                // 初始化网络连接
                initChannel(registryInfos);
            }
        }
    }

    private void initChannel(List<RegistryInfo> registryInfos) throws InterruptedException {
        for (RegistryInfo info : registryInfos) {
            if (!channels.containsKey(info)) {
                System.out.println("开始建立连接：" + info.getIp() + ", " + info.getPort());
                NettyClient client = new NettyClient(info.getIp(), info.getPort());
                // 针对每一个唯一的RegistryInfo建立一个连接, 设置一个callback，用于收到消息的时候回调
                client.setMessageCallback(message -> {
                    // 服务端返回响应消息，先压入队列。但是这样一来，我们应该怎么把响应结果返回给调用的地方呢？
                    // 我们可以这样做：起一个或多个后台线程，然后从队列中拿出响应，然后根据响应从我们之前保存的inProcessInvoker中找出对应的Invoker，然后把结果返回回去
                    RpcResponse response = JSONObject.parseObject(message, RpcResponse.class);
                    responses.offer(response);
                    // 这里被唤醒之后，就会有多个线程去争抢那个响应，因为队列是线程安全的，所以这里多个线程可以获取到响应结果。
                    synchronized (ApplicationContext.this) {
                        ApplicationContext.this.notifyAll();
                    }
                });
                // 等待连接建立
                ChannelHandlerContext ctx = client.getCtx();
                channels.put(info, ctx);
            }
        }
    }


    /**
     * 获取调用服务
     */
    @SuppressWarnings("unchecked")
    public T getService(Class clazz) {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                if ("equals".equals(methodName) || "hashCode".equals(methodName)) {
                    throw new IllegalAccessException("不能访问" + methodName + "方法");
                }
                if ("toString".equals(methodName)) {
                    return clazz.getName() + "#" + methodName;
                }

                // step 1: 获取服务地址列表
                List registryInfos = interfacesMethodRegistryList.get(clazz);
                if (registryInfos == null) {
                    throw new RuntimeException("无法找到服务提供者");
                }

                // step 2： 负载均衡
                RegistryInfo registryInfo = loadBalancer.choose(registryInfos);
                ChannelHandlerContext ctx = channels.get(registryInfo);
                // 可能会有多个线程同时调用同一个接口的同一个方法，这样的identify是相同的。
                // 所以我们需要用 identify + requestId的方式来找到某个响应是对应的哪个请求
                String identify = InvokeUtils.buildInterfaceMethodIdentify(clazz, method);
                String requestId;
                synchronized (ApplicationContext.this) {
                    requestIdWorker.increment();
                    requestId = String.valueOf(requestIdWorker.longValue());
                }
                Invoker invoker = new DefaultInvoker(method.getReturnType(), ctx, requestId, identify);
                inProgressInvoker.put(identify + "#" + requestId, invoker);
                return invoker.invoke(args);
            }
        });

    }


    private void initProcessor() {
        // 事实上，这里可以通过配置文件读取，启动多少个processor
        int num = 3;
        ResponseProcessor[] processors = new ResponseProcessor[num];
        for (int i = 0; i < 3; i++) {
            processors[i] = new ResponseProcessor();
        }
    }

    public class ResponseProcessor implements Runnable{
        @Override
        public void run() {
            System.out.println("启动响应处理线程：" + Thread.currentThread().getName());
            while (true) {
                // 多个线程在这里获取响应，只有一个成功
                RpcResponse response = responses.poll();
                if (response == null) {
                    try {
                        synchronized (ApplicationContext.this) {
                            // 如果没有响应，先休眠
                            ApplicationContext.this.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("收到一个响应：" + response);
                    String interfaceMethodIdentify = response.getInterfaceMethodIdentify();
                    String requestId = response.getRequestId();
                    String key = interfaceMethodIdentify + "#" + requestId;
                    Invoker invoker = inProgressInvoker.remove(key);
                    invoker.setResult(response.getResult());
                }
            }
        }
    }


}
