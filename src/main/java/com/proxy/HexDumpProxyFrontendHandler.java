package com.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.internal.StringUtil;

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
        b.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
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

    StringBuffer remoteRequestBody = new StringBuffer();
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        if (outboundChannel.isActive()) {
            String data = (String) msg;
            if(data.trim().length()==0){
                remoteRequestBody.append("\n");
                System.out.println("---remote request body:\n" +remoteRequestBody + "---");
                outboundChannel.writeAndFlush(remoteRequestBody).addListener(new ChannelFutureListener() {
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
                System.out.println("---proxy:" +data + "---");
                if(data.startsWith("Host")){
                    remoteRequestBody.append("Host:beta-api.qoo-app.com\n");
                }else{
                    remoteRequestBody.append(data+"\n");
                }
            }
        }
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