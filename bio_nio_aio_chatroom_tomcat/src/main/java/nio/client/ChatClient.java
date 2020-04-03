package nio.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

public class ChatClient {
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private String host;
    private int port;
    private SocketChannel client; // 哭护短的channel
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    private Selector selector;
    private Charset charset = Charset.forName("UTF-8");

    public ChatClient() {
    }

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public boolean readyToQuit(String msg){
        return QUIT.equals(msg);
    }
    private void close(Closeable... closeable){
        for(Closeable c:closeable){
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void start(){
        try {
            client = SocketChannel.open();
            client.configureBlocking(false);

            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT);
            client.connect(new InetSocketAddress(host,port));// 连接服务器
            while (selector.isOpen()){
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                keys.forEach(key->{
                    handles(key);
                });
                keys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClosedSelectorException e){

        } finally {
            close(selector);
        }
    }

    private void handles(SelectionKey key) {
        // CONNECT连接就绪事件
        if (key.isConnectable()){
            SocketChannel channel = (SocketChannel) key.channel();
            if(client.isConnectionPending()){
                try {
                    client.finishConnect();
                    //处理用户输入
                    new Thread(new UserInputHandler(this)).start();
                    client.register(selector,SelectionKey.OP_READ);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // READ事件
        else if(key.isReadable()){
            SocketChannel client = (SocketChannel) key.channel();
            String msg = null;
            try {
                msg = recieve(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(msg.isEmpty()){
                // 服务器异常
                close(selector);
            }else{
                System.out.println(msg);
            }
        }
    }

    public void send(String msg) throws IOException {
        if (msg.isEmpty()) return;
        wBuffer.clear();
        wBuffer.put(charset.encode(msg));
        wBuffer.flip();
        while (wBuffer.hasRemaining()){
            client.write(wBuffer);
        }
        if(readyToQuit(msg)){
            close(selector);
        }
    }

    private String recieve(SocketChannel client) throws IOException {
        rBuffer.clear();
        while(client.read(rBuffer)>0);
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("127.0.0.1",7777);
        client.start();
    }
}