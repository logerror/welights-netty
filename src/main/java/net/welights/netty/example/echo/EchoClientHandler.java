package net.welights.netty.example.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable                                //1
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", //2
                CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        System.out.println("Client received: " + in.toString(CharsetUtil.UTF_8));    //3
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {                    //4
        cause.printStackTrace();
        ctx.close();
    }
}

/**
 * 1.@Sharable标记这个类的实例可以在 channel 里共享
 * <p>
 * 2.当被通知该 channel 是活动的时候就发送信息
 * <p>
 * 3.记录接收到的消息
 * <p>
 * 4.记录日志错误并关闭 channel
 * <p>
 * 建立连接后该 channelActive() 方法被调用一次。逻辑很简单：一旦建立了连接，字节序列被发送到服务器。该消息的内容并不重要;在这里，我们使用了 Netty 编码字符串 “Netty
 * rocks!” 通过覆盖这种方法，我们确保东西被尽快写入到服务器。
 * <p>
 * 接下来，我们覆盖方法 channelRead0()。这种方法会在接收到数据时被调用。注意，由服务器所发送的消息可以以块的形式被接收。即，当服务器发送 5 个字节是不是保证所有的 5
 * 个字节会立刻收到 - 即使是只有 5 个字节，channelRead0() 方法可被调用两次，第一次用一个ByteBuf（Netty的字节容器）装载3个字节和第二次一个 ByteBuf 装载 2
 * 个字节。唯一要保证的是，该字节将按照它们发送的顺序分别被接收。 （注意，这是真实的，只有面向流的协议如TCP）。
 * <p>
 * 第三个方法重写是 exceptionCaught()。正如在 EchoServerHandler （清单2.2），所述的记录 Throwable 并且关闭通道，在这种情况下终止 连接到服务器。
 * <p>
 * SimpleChannelInboundHandler vs. ChannelInboundHandler
 * <p>
 * 何时用这2个要看具体业务的需要。在客户端，当 channelRead0() 完成，我们已经拿到的入站的信息。 当方法返回，SimpleChannelInboundHandler 会小心的释放对
 * ByteBuf（保存信息） 的引用。 而在 EchoServerHandler,我们需要将入站的信息返回给发送者，write() 是异步的在 channelRead()返回时，可能还没有完成。
 * 所以，我们使用 ChannelInboundHandlerAdapter,无需释放信息。最后在 channelReadComplete() 我们调用 ctxWriteAndFlush()
 * 来释放信息
 */