package nio.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;

public class ChatServer {
    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int Buffer = 1024;

    private ServerSocketChannel server;//服务端的channel
    private Selector selector;
    private ByteBuffer rBuffer = ByteBuffer.allocate(Buffer);
    private ByteBuffer wBuffer = ByteBuffer.allocate(Buffer);
    private Charset charset = Charset.forName("UTF-8");
    private int port;

    public ChatServer() {
    }

    public ChatServer(int port) {
        this.port = port;
    }

    public void start(){
        try {
            server = ServerSocketChannel.open();// 创建一个channel
            server.configureBlocking(false);//设置为nio
            server.socket().bind(new InetSocketAddress(port));

            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT); // 将selector注册到server中，select监婷听用户请求
            System.out.println("启动服务器，监听端口："+port+"...");
            while (true) {
                selector.select();// 阻塞在这里，直到selector监听的事件发生
                Set<SelectionKey> keys = selector.selectedKeys();// 返回监听的channel的selectorKey
                for(SelectionKey key:keys){
                    //处理被触发的事件
                    handles(key);
                }
                keys.clear();// 将所有的selectorKey清空，用于下一次while循环
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(selector);
        }
    }

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
        else if(key.isReadable()){
            SocketChannel client = (SocketChannel) key.channel();
            String fwdMsg = receive(client); // 循环读取信息
            if(fwdMsg.isEmpty()){
                //客户端异常
                key.cancel();
                selector.wakeup();
            }else{
                System.out.println(getClientName(client)+":"+fwdMsg);
                // 检查用户是否退出
                if(readyToQuit(fwdMsg)){
                    key.cancel();
                    selector.wakeup();
                    System.out.println(getClientName(client)+"已断开连接");
                }else{
                    // 转发消息
                    forwardMessage(client,fwdMsg);
                }
            }
        }
    }

    // 转发消息
    private void forwardMessage(SocketChannel client, String fwdMsg) {
        selector.keys().forEach(key->{
            SelectableChannel connectedClient = key.channel();// 通过selector中获取所有的selectorKey和channel
            if(connectedClient instanceof ServerSocketChannel) return;
            if(key.isValid() && !client.equals(connectedClient)){
                wBuffer.clear(); // 清空缓存，读模式变为写模式
                wBuffer.put(charset.encode(getClientName(client)+":"+fwdMsg)); // 写入缓存中
                wBuffer.flip(); // 写模式切换为读模式
                while (wBuffer.hasRemaining()){ // position到limit之间还有数据
                    try {
                        int length = ((SocketChannel) connectedClient).write(wBuffer);// 将缓存中的数据发出

                        if(length<0){
                            System.out.println("客户端无法发送数据!");

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while(client.read(rBuffer)>0); // 循环读取到rBuffer中
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    // 根据端口作为client名
    private String getClientName(SocketChannel client){
        return "客户端["+client.socket().getPort()+"]";
    }

    private boolean readyToQuit(String msg){
        return QUIT.equals(msg);
    }

    // 关闭selector
    private void close(Closeable... closeable){
        for(Closeable c:closeable){
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(7777);
        chatServer.start();
    }
}
