package com.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

import java.nio.charset.Charset;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by jerry on 2016/8/19.
 */
public class ProxyHandler extends ChannelInboundHandlerAdapter {

    private final String remoteHost;
    private final int remotePort;
    private Channel outboundChannel;

    public ProxyHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();

        // 发起远端真实服务器的链接
        Bootstrap b = new Bootstrap();
        b.group(new NioEventLoopGroup())
                .channel(ctx.channel().getClass())
                .handler(new BackendClientChannelInit(inboundChannel))
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
            //hit except request
            if(!hitExceptRequest(ctx, req)) {
                passthroughUnexcept(ctx, req);
            }
        }else{
            System.out.println("ignore............");
            super.channelRead(ctx, msg);
        }
    }

    /**
     * 返回预设的请求结果
     * @param ctx
     * @param req
     * @return
     */
    private boolean hitExceptRequest(final ChannelHandlerContext ctx, FullHttpRequest req){
        String uri = req.uri();
        uri = uri.contains("?")?req.uri().substring(0, req.uri().indexOf("?")): uri;
        System.out.println("uri:" + uri);
        boolean hitExcept = false;
        for(ExceptRequest e: ExceptRequest.exceptRequestList){
            if(uri.equalsIgnoreCase(e.getExceptRequest().getUri())){
                hitExcept = true;
                CharSequence contnt = e.getExceptResponse().getContent();
                FullHttpResponse response = null;
                if(StringUtils.isEmpty(contnt)){  // empty content, response (404)Not Found
                    response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND,
                            Unpooled.copiedBuffer("404  Not Found", Charset.forName("UTF-8")));
                }else {
                    response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                            Unpooled.copiedBuffer(contnt, Charset.forName("UTF-8")));
                    String contentType = e.getExceptResponse().getContentType();
                    response.headers().set(CONTENT_TYPE, StringUtils.isEmpty(contentType) ? Response.JSON_CONTENT_TYPE : contentType);
                    response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                }
                ctx.channel().writeAndFlush(response).addListener(new ChannelFutureListener() {
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
        }
        return hitExcept;
    }

    /**
     * 未预设的请求，继续请求远端真实服务器
     * @param ctx
     * @param req
     */
    private void passthroughUnexcept(final ChannelHandlerContext ctx, final FullHttpRequest req){
        //发送请求数据到真实服务器
        req.headers().set("Host", ProxyServer.REMOTE_HOST);
        System.out.println(req + "\n");
        outboundChannel.writeAndFlush(req).addListener(new ChannelFutureListener() {
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