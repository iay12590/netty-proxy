package com.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 * Created by jerry on 2016/8/19.
 */
public final class BackendClientChannelInit extends ChannelInitializer<SocketChannel> {
    Channel inboundChannel;

    public BackendClientChannelInit(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(
                new HttpClientCodec(),
                new HttpObjectAggregator(512 * 1024 * 1024),
                new BackendClientHandler(this.inboundChannel));
    }
}
