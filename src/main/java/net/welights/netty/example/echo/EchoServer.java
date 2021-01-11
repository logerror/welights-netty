package net.welights.netty.example.echo;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        args = new String[]{"8090"};
        if (args.length != 1) {
            System.err.println("Usage: " + EchoServer.class.getSimpleName() + " <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);        //1
        new EchoServer(port).start();                //2
    }

    public void start() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup(); //3
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)                                //4
                    .channel(NioServerSocketChannel.class)        //5
                    .localAddress(new InetSocketAddress(port))    //6
                    .childHandler(new ChannelInitializer<SocketChannel>() { //7
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });

            ChannelFuture f = b.bind().sync();            //8
            System.out.println(EchoServer.class.getName() + " started and listen on " + f.channel().localAddress());
            f.channel().closeFuture().sync();            //9
        } finally {
            group.shutdownGracefully().sync();            //10
        }
    }

}


/**
 * 1.设置端口值（抛出一个 NumberFormatException 如果该端口参数的格式不正确）
 * <p>
 * 2.呼叫服务器的 start() 方法
 * <p>
 * 3.创建 EventLoopGroup
 * <p>
 * 4.创建 ServerBootstrap
 * <p>
 * 5.指定使用 NIO 的传输 Channel
 * <p>
 * 6.设置 socket 地址使用所选的端口
 * <p>
 * 7.添加 EchoServerHandler 到 Channel 的 ChannelPipeline
 * <p>
 * 8.绑定的服务器;sync 等待服务器关闭
 * <p>
 * 9.关闭 channel 和 块，直到它被关闭
 * <p>
 * 10.关机的 EventLoopGroup，释放所有资源。
 * <p>
 * 在这个例子中，代码创建 ServerBootstrap 实例（步骤4）。由于我们使用在 NIO 传输，我们已指定 NioEventLoopGroup（3）接受和处理新连接，指定
 * NioServerSocketChannel（5）为信道类型。在此之后，我们设置本地地址是 InetSocketAddress 与所选择的端口（6）如。服务器将绑定到此地址来监听新的连接请求。
 * <p>
 * 第七步是关键：在这里我们使用一个特殊的类，ChannelInitializer 。当一个新的连接被接受，一个新的子 Channel 将被创建， ChannelInitializer
 * 会添加我们EchoServerHandler 的实例到 Channel 的 ChannelPipeline。正如我们如前所述，这个处理器将被通知如果有入站信息。
 * <p>
 * 虽然 NIO 是可扩展性，但它的正确配置是不简单的。特别是多线程，要正确处理也非易事。幸运的是，Netty 的设计封装了大部分复杂性，尤其是通过抽象，例如
 * EventLoopGroup，SocketChannel 和 ChannelInitializer，其中每一个将在更详细地在第3章中讨论。
 * <p>
 * 在步骤8，我们绑定的服务器，等待绑定完成。 （调用 sync() 的原因是当前线程阻塞）在第9步的应用程序将等待服务器 Channel 关闭（因为我们 在 Channel 的
 * CloseFuture 上调用 sync()）。现在，我们可以关闭下 EventLoopGroup 并释放所有资源，包括所有创建的线程（10）。
 * <p>
 * NIO 用于在本实施例，因为它是目前最广泛使用的传输，归功于它的可扩展性和彻底的不同步。但不同的传输的实现是也是可能的。例如，如果本实施例中使用的 OIO 传输，我们将指定
 * OioServerSocketChannel 和 OioEventLoopGroup。 Netty 的架构，包括更关于传输信息，将包含在第4章。在此期间，让我们回顾下在服务器上执行，我们只研究重要步骤。
 * <p>
 * 服务器的主代码组件是
 * <p>
 * EchoServerHandler 实现了的业务逻辑 在 main() 方法，引导了服务器 执行后者所需的步骤是：
 * <p>
 * 创建 ServerBootstrap 实例来引导服务器并随后绑定 创建并分配一个 NioEventLoopGroup 实例来处理事件的处理，如接受新的连接和读/写数据。 指定本地
 * InetSocketAddress 给服务器绑定 通过 EchoServerHandler 实例给每一个新的 Channel 初始化 最后调用 ServerBootstrap.bind()
 * 绑定服务器 这样服务器初始化完成，可以被使用了。
 */