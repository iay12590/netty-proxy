package com.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;

/**
 * Created by jerry on 2016/8/19.
 */
public class RemoteProxyInitializer extends ChannelInitializer<SocketChannel> {
    Channel inboundChannel;

    public RemoteProxyInitializer(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(
                new HttpClientCodec(),
                new HttpObjectAggregator(512 * 1024),
                new HexDumpProxyBackendHandler(this.inboundChannel));
    }
}
