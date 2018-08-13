package com.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NServer {
	public static void main(String[] args) {
		int port = 9999;
		EventLoopGroup bossGroup = new NioEventLoopGroup();	//用于处理接收请求
		EventLoopGroup workerGroup = new NioEventLoopGroup();	//用于处理具体I/O操作,默认大小为系统内核两倍
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {
	
				@Override
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					ChannelPipeline pipleline = socketChannel.pipeline();
					pipleline.addLast("http-decoder",new HttpRequestDecoder());
					pipleline.addLast("http-encoder",new HttpResponseEncoder());
					pipleline.addLast("http-aggregator",new HttpObjectAggregator(128*1024));	//http消息聚合器
					pipleline.addLast("http-chunked",new ChunkedWriteHandler());	//大数据异步传输
					pipleline.addLast(new SimpleHandler());
				}
				
			});
			
			bootstrap.option(ChannelOption.SO_BACKLOG, 1024)	//设置握手成功队列大小
					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);	//可以重复利用之前分配的内存空间。这个可以有效减少内存的使用
			
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childOption(ChannelOption.TCP_NODELAY, true)	//禁用Nagle,Nagle算法是将小的数据包组装为更大的帧然后进行发送
					.childOption(ChannelOption.SO_SNDBUF, 128*1024)	//网络发送基础缓冲区
					.childOption(ChannelOption.SO_RCVBUF, 128*1024)	//网络接收基础缓冲区
					.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);	//可以重复利用之前分配的内存空间。这个可以有效减少内存的使用
			
			ChannelFuture cf = bootstrap.bind(port).sync();
			System.out.println("server start at "+port+" ...");
			cf.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			System.out.println("启动失败");
		} finally {
			//关闭资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
