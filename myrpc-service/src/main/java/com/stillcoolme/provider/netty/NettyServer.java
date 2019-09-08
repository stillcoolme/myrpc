package com.stillcoolme.provider.netty;

import com.stillcoolme.provider.ServiceConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.util.List;
import java.util.Map;

/**
 * @author: stillcoolme
 * @date: 2019/8/22 9:14
 * @description:
 *
 **/
public class NettyServer {

    /**
     * 负责调用方法的handler
     */
    private RpcInvokeHandler rpcInvokeHandler;

    public NettyServer(List<ServiceConfig> serviceConfigs, Map interfaceMethods) throws InterruptedException {
        // 所有逻辑都在RpcInvokeHandler中，这里面传进去了配置的服务接口实例，
        // 以及服务接口实例每个接口方法唯一标识对应的Method对象的Map集合。
        this.rpcInvokeHandler = new RpcInvokeHandler(serviceConfigs, interfaceMethods);
    }

    public int init(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ByteBuf delimiter = Unpooled.copiedBuffer("$$");
                        // 设置按照分隔符“&&”来切分消息，单条消息限制为 1MB
                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024 * 1024, delimiter));
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(rpcInvokeHandler);
                    }
                });
        ChannelFuture future = bootstrap.bind(port).sync();
        System.out.println("启动NettyService，端口为：" + port);
        return port;
    }

}
