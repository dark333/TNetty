package com.core;

import java.util.Date;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class SimpleHandler extends SimpleChannelInboundHandler<FullHttpRequest>{
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		
		FullHttpRequest request = msg;
		Date currentDate = new Date();
		/*System.out.println(request.uri());
		System.out.println(request.method().name());*/
		for(Entry<String, String> entry:request.headers()){
			System.out.println(entry.getKey()+":"+entry.getValue());
		}
		System.out.println(request.content().toString(CharsetUtil.UTF_8));
		String s = "{\"status\":200}";
		ByteBuf buf = Unpooled.copiedBuffer(s,CharsetUtil.UTF_8);
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,buf);
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "Application/json; charset=UTF-8");
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
		response.headers().set(HttpHeaderNames.DATE, currentDate.toString() );
		ctx.writeAndFlush(response);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	//	String err = "java.io.IOException: Connection reset by peer";
		String err = "java.io.IOException";
		if(err.equalsIgnoreCase(cause.getClass().getName())){
			System.out.println("Connection reset by peer");
			ctx.close();
		}else{
			super.exceptionCaught(ctx, cause);
		}	
	}
}
