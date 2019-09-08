package com.stillcoolme.service;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author: stillcoolme
 * @date: 2019/9/8 15:01
 * @description:
 */
public class DefaultInvoker<T> implements Invoker {
    private ChannelHandlerContext ctx;
    private String requestId;
    private String identify;
    private Class returnType;

    private T result;

    DefaultInvoker(Class returnType, ChannelHandlerContext ctx, String requestId, String identify) {
        this.returnType = returnType;
        this.ctx = ctx;
        this.requestId = requestId;
        this.identify = identify;
    }

    /**
     * 组装发送给服务端的参数
     *
     * @param args
     * @return
     */
    @Override
    public T invoke(Object[] args) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("interfaces", identify);
        JSONObject params = new JSONObject();
        if (args != null) {
            for (Object object : args) {
                params.put(object.getClass().getName(), object);
            }
        }
        jsonObject.put("parameter", params);
        jsonObject.put("requestId", requestId);
        System.out.println("发送给服务端JSON为：" + jsonObject.toJSONString());
        String msg = jsonObject.toJSONString() + "$$";
        ByteBuf byteBuf = Unpooled.buffer(msg.getBytes().length);
        byteBuf.writeBytes(msg.getBytes());
        ctx.writeAndFlush(byteBuf);
        // 调用Invoker的invoke方法之后，会运行到waitForResult()这里，这里已经把请求通过网络发送出去了，但是就会被卡住。
        // 这是因为我们的网络请求的结果不是同步返回的，有可能是客户端同时发起很多个请求，所以我们不可能在这里让他同步阻塞等待的。
        waitForResult();
        return result;
    }

    @Override
    public void setResult(String result) {
        synchronized (this) {
            this.result = (T) JSONObject.parseObject(result, returnType);
            notifyAll();
        }
    }


    private void waitForResult() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
