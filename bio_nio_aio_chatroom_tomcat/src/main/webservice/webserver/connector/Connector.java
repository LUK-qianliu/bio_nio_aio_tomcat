package webserver.connector;


import webserver.processor.ServletProcessor;
import webserver.processor.StaticProcessor;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class Connector implements Runnable {

    private static final int DEFAULT_PORT = 8888;

    private ServerSocketChannel server;
    private Selector selector;
    private int port;

    InputStream input;
    OutputStream output;// 建立连接的流

    public Connector() {
        this(DEFAULT_PORT);
    }

    public Connector(int port) {
        this.port = port;
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            // 创建ServerSocket，绑定、监听端口
            server = ServerSocketChannel.open();// 创建一个channel
            server.configureBlocking(false);//设置为nio
            server.socket().bind(new InetSocketAddress(port));

            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT); // 将selector注册到server中，select监婷听用户请求
            System.out.println("启动服务器，监听端口："+port+"...");
            while (true) {
                // 等待客户端连接
                selector.select();// 阻塞在这里，直到selector监听的事件发生
                Set<SelectionKey> keys = selector.selectedKeys();// 返回监听的channel的selectorKey
                for (SelectionKey key : keys) {
                    //处理被触发的事件
                    handles(key);
                }
                keys.clear();// 将所有的selectorKey清空，用于下一次while循环
            }
        } catch (IOException e) {
            // 浏览器可以识别状态码，当状态码表示请求不成功时（如404），似乎会断开socket，所以这里不进行处理
            e.printStackTrace();
        } finally {
            close(server);
            selector.wakeup();
        }
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 处理accept、读写事件
    private void handles(SelectionKey key) throws IOException {
        // ACCEPT事件 - 和客户端建立了连接
        if(key.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register(selector,SelectionKey.OP_READ); // 开启读模式
            System.out.println(getClientName(client) +"已连接");
        }
        // READ事件 - 客户端发送了消息
        else {
            SocketChannel client = (SocketChannel) key.channel();
            key.cancel(); // 关闭客户端和selector的连接，selector以后就无法使用该通道
            client.configureBlocking(true);

            input = client.socket().getInputStream(); // 循环读取信息
            output = client.socket().getOutputStream(); // 循环读取信息


            // 创建请求request，并且传入输入流（有客户端请求的信息）
            Request request = new Request(input);
            // request通过输入流的信息，分析出客户端想要的资源
            request.parse();

            // 创建响应response，并且传入输出流（方便将获取的资源发送给客户端）
            Response response = new Response(output);
            // response需要request的uri（客户端请求的资源）
            response.setRequest(request);

            //根据URI调用不同的处理器处理请求
            if (request.getUri().startsWith("servlet/")) {
                new ServletProcessor().process(request, response);
            } else {
                new StaticProcessor().process(response);
            }

            close(client);//前段是heep短链接，这里需要关闭资源
        }
    }

    private int getClientName( SocketChannel client){
        return  client.socket().getPort();
    }
}

