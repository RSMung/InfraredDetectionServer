package server3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class Server3 {
	private String state = "Server_state";
	
	public void bind(int port) throws Exception{
		//配置服务端的NIO线程组
    	//一个用于服务端接收客户端的连接,另一个用于进行SocketChannel的网络读写
    	EventLoopGroup bossGroup = new NioEventLoopGroup();
    	EventLoopGroup workerGroup = new NioEventLoopGroup();
    	try {
    		//用于启动NIO服务端的辅助启动类,降低服务端开发复杂度
    		ServerBootstrap b = new ServerBootstrap();
    		b.group(bossGroup,workerGroup)
    		.channel(NioServerSocketChannel.class)
    		.option(ChannelOption.SO_BACKLOG, 1024)
    		.childHandler(new ChildChannelHandler());//绑定IO事件的处理类
    		
    		//绑定端口,同步等待成功
    		//用于异步操作的通知回调
    		ChannelFuture f = b.bind(port).sync();
    		System.out.println(state+"--bind被执行");
    		
    		//服务端链路关闭之后main函数才退出
    		f.channel().closeFuture().sync();
    	}catch (Exception e) {
    		e.printStackTrace();
		}finally {
			System.out.println(state+"--释放资源");
			//释放资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	private class ChildChannelHandler extends ChannelInitializer<SocketChannel>{
    	int frameLength = 1544-1;//4629  2335

		@Override
		protected void initChannel(SocketChannel arg0) throws Exception {
			System.out.println(state+"--initChannel执行");
			// 添加对象编码器 在服务器对外发送消息的时候自动将实现序列化的POJO对象编码
			arg0.pipeline().addLast(new ObjectEncoder());
			arg0.pipeline().addLast(new FixedLengthFrameDecoder(frameLength));
			arg0.pipeline().addLast(new ServerHandler3());
		}
    	
    }
	
	public static void main(String[] args) throws Exception{
    	int port = 1234;
    	new Server3().bind(port);
    }

}
