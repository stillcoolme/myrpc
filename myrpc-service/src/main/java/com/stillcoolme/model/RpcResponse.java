package com.stillcoolme.model;

/**
 * @author: stillcoolme
 * @date: 2019/8/29 10:56
 * @description:
 *  里面包含了请求的结果JSON串，接口方法唯一标识，请求ID。数据大概看起来这个样子：
 *  {"interfaceMethodIdentify":"interface=com.stillcoolme.rpc.test.producer.HelloService&method=sayHello&
 *  parameter=com.stillcoolme.rpc.test.producer.TestBean",
 *  "requestId":"3",
 *  "result":"\"牛逼,我收到了消息：TestBean{name='张三', age=20}\""}
 *
 * 通过这样的信息，客户端就可以通过响应结果解析出来。
 **/
public class RpcResponse {
    private String result;

    private String interfaceMethodIdentify;

    private String requestId;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getInterfaceMethodIdentify() {
        return interfaceMethodIdentify;
    }

    public void setInterfaceMethodIdentify(String interfaceMethodIdentify) {
        this.interfaceMethodIdentify = interfaceMethodIdentify;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public static RpcResponse create(String result, String interfaceMethodIdentify, String requestId) {
        RpcResponse response = new RpcResponse();
        response.setResult(result);
        response.setInterfaceMethodIdentify(interfaceMethodIdentify);
        response.setRequestId(requestId);
        return response;
    }
}
