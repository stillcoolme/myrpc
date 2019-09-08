package com.stillcoolme.service.netty;

import com.alibaba.fastjson.JSONObject;
import com.stillcoolme.service.ServiceConfig;
import com.stillcoolme.service.model.RpcRequest;
import com.stillcoolme.service.model.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: stillcoolme
 * @date: 2019/8/22 9:20
 * @description:
 **/
public class RpcInvokeHandler extends ChannelInboundHandlerAdapter {
    /**
     * 接口方法唯一标识对应的Method对象
     */
    private Map<String, Method> interfaceMethods;
    /**
     * 接口对应的实现类
     */
    private Map<Class, Object> interfaceToInstance;

    public RpcInvokeHandler(List<ServiceConfig> serviceConfigList, Map<String, Method> interfaceMethods) {
        this.interfaceMethods = interfaceMethods;
        this.interfaceToInstance = new ConcurrentHashMap<>();
        for (ServiceConfig config : serviceConfigList) {
            interfaceToInstance.put(config.getType(), config.getInstance());
        }
    }

    /**
     * 接受消息，将消息解析成RpcRequest
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = (String) msg;
        // 这里拿到的是一串JSON数据，解析为Request对象，
        // 事实上这里解析网络数据，可以用序列化方式，定一个接口，可以实现JSON格式序列化，或者其他序列化
        // 但是demo版本就算了。
        System.out.println("接收到消息：" + msg);
        RpcRequest request = RpcRequest.parse(message, ctx);
        threadPoolExecutor.execute(new RpcInvokeTask(request));

    }

    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10,
            32, 60000, TimeUnit.SECONDS, new LinkedBlockingDeque<>(100),
            new ThreadFactory() {
                AtomicInteger atomicInteger = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "IO-thread-" + atomicInteger.incrementAndGet());
                }
            });

    private class RpcInvokeTask implements Runnable {
        private RpcRequest rpcRequest;

        RpcInvokeTask(RpcRequest rpcRequest) {
            this.rpcRequest = rpcRequest;
        }

        @Override
        public void run() {
            /*
             * 数据大概是这样子的
             * {
             *  "interfaces":"interface=com.study.rpc.test.producer.HelloService&method=sayHello¶meter=com
             *  .study.rpc.test.producer.TestBean",
             *  "requestId":"3",
             *  "parameter":{
             *      "com.study.rpc.test.producer.TestBean":{
             *            "age":20,
             *            "name":"张三"
             *       }
             *   }
             * }
             */
            // 这里希望能拿到每一个服务对象的每一个接口的特定声明 interfaces，由请求接口的 接口类，方法名，方法参数类名组成的字符串
            try {
                String interfaceIdentity = rpcRequest.getInterfaceIdentity();
                Method method = interfaceMethods.get(interfaceIdentity);
                // 请求接口的 接口类，方法名，方法参数类名 map
                Map<String, String> map = string2Map(interfaceIdentity);
                String interfaceName = map.get("interfaces");
                Class interfaceClass = Class.forName(interfaceName);
                // 客户端要调用的接口类
                Object requestObject = interfaceToInstance.get(interfaceClass);
                String parameter = map.get("parameter");
                Object result;
                if (parameter != null) {
                    String[] parameterTypeClass = parameter.split(",");
                    Map<String, Object> parameterMap = rpcRequest.getParameterMap();
                    Object[] parameterInstance = new Object[parameterTypeClass.length];
                    for (int i = 0; i < parameterTypeClass.length; i++) {
                        parameterInstance[i] = parameterMap.get(parameterTypeClass[i]);
                    }
                    result = method.invoke(requestObject, parameterInstance);
                } else {
                    result = method.invoke(requestObject);
                }
                ChannelHandlerContext ctx = rpcRequest.getCtx();
                String requestId = rpcRequest.getRequestId();
                RpcResponse response = RpcResponse.create(JSONObject.toJSONString(result), interfaceIdentity,
                        requestId);
                String res = JSONObject.toJSONString(response) + "$$";
                ByteBuf byteBuf = Unpooled.copiedBuffer(res.getBytes());
                ctx.writeAndFlush(byteBuf);
                System.out.println("影响给客户端: " + res);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    public static Map<String, String> string2Map(String str) {
        String[] split = str.split("&");
        Map<String, String> map = new HashMap<>(16);
        for (String s : split) {
            String[] split1 = s.split("=");
            map.put(split1[0], split1[1]);
        }
        return map;
    }
}
