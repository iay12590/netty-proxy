package com.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

/**
 * Created by jerry  on 2016/8/19.
 */
public final class ProxyChannelInit extends ChannelInitializer<SocketChannel> {

    private final String remoteHost;
    private final int remotePort;

    public ProxyChannelInit(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(
                new HttpServerCodec(),
                new HttpObjectAggregator(512 * 1024 * 1024),
                new ProxyHandler(remoteHost, remotePort));
    }
}