package cn.liyu.chat_demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 聊天程序服务器端
 *
 * @author liyu
 * @date 2020/4/24 16:04
 * @description
 */
public class ChatServer {

    /**
     * 服务器端端口号
     */
    private int port;

    public ChatServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline(); //得到 Pipeline 链
                            //往 Pipeline 链中添加一个解码器
                            pipeline.addLast("decoder", new StringDecoder());
                            //往 Pipeline 链中添加一个编码器
                            pipeline.addLast("encoder", new StringEncoder());
                            //往 Pipeline 链中添加一个自定义的业务处理对象
                            pipeline.addLast("handler", new ChatServerHandler());
                            // 增加心跳事件支持
                            // 第一个参数: 读空闲 4 秒
                            // 第二个参数： 写空闲 8 秒
                            // 第三个参数： 读写空闲 12 秒
                            pipeline.addLast(new IdleStateHandler(4, 8, 12))
                                    .addLast(new NettyServerHeartHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            System.out.println("Netty Chat Server 启动......");
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("Netty Chat Server 关闭......");
        }
    }

    public static void main(String[] args) throws Exception {
        new ChatServer(9999).run();
    }
}

