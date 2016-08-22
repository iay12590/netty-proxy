package com.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

public class HexDumpProxyFrontendHandler extends ChannelInboundHandlerAdapter {

    private final String remoteHost;
    private final int remotePort;

    // As we use inboundChannel.eventLoop() when buildling the Bootstrap this does not need to be volatile as
    // the outboundChannel will use the same EventLoop (and therefore Thread) as the inboundChannel.
    private Channel outboundChannel;

    public HexDumpProxyFrontendHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();

        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new RemoteProxyInitializer(inboundChannel))
                .option(ChannelOption.AUTO_READ, false);
        ChannelFuture f = b.connect(remoteHost, remotePort);
        outboundChannel = f.channel();
        f.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    // connection complete start to read first data
                    inboundChannel.read();
                } else {
                    // Close the connection if the connection attempt has failed.
                    inboundChannel.close();
                }
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest){
            FullHttpRequest req = (FullHttpRequest) msg;

            //发送到目标远程服务的请求
            StringBuilder rebuildRequest = new StringBuilder();

            rebuildRequest.append(req.method()).append(" ")
                    .append(req.uri()).append(" ")
                    .append("HTTP/1.1").append("\n");

            for (Map.Entry h: req.headers()){
                String hKey = (String) h.getKey();
                String hValue = (String) h.getValue();
                if(hKey.equalsIgnoreCase("host")){
                    hValue = HexDumpProxy.REMOTE_HOST + ":"+HexDumpProxy.REMOTE_PORT;
                }
                rebuildRequest.append(hKey).append(":").append(hValue).append("\n");
            }

            //区分请求头部结束，添加 \n 换行标识
            rebuildRequest.append("\n");
            if("POST".equals(req.method().toString())) {
                //请求体
                ByteBuf content = req.content();
                String reqBody = buff2String(content);
                System.out.println("request body:\n" + reqBody);
                rebuildRequest.append(reqBody);
            }

            //modifry host header
            req.headers().set("Host", HexDumpProxy.REMOTE_HOST);

            //发送请求数据
            System.out.println(msg);

            outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // was able to flush out data, start to read the next chunk
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                        System.out.println("---------------------------- Error -------------------------------");
                        future.cause().printStackTrace();
                    }
                }
            });

        }else{
            System.out.println("ignore............");
            super.channelRead(ctx, msg);
        }
    }


    public static String buff2String(ByteBuf buf){
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        try {
            String str = new String(req,"UTF-8");
            return str;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}