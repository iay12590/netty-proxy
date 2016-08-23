package com.proxy;



import io.netty.channel.*;

/**
 * Created by jerry on 2016/8/19.
 */
public class BackendClientHandler extends ChannelInboundHandlerAdapter {

    private final Channel inboundChannel;

    public BackendClientHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        System.out.println("---------------------------\n" + msg);
        inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.cause().printStackTrace();
                    future.channel().close();
                }
            }
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ProxyHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("----------------------------BackendClientHandler Error -------------------------------");
        cause.printStackTrace();
        ProxyHandler.closeOnFlush(ctx.channel());
    }
}