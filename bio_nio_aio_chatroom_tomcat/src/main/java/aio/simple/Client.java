package aio.simple;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 */
public class Client {
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 8888;
    private static final String QUIT = "quit";

    private static AsynchronousSocketChannel clientChannel;

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

    public void start() throws IOException, ExecutionException, InterruptedException {
        // 创建client
        clientChannel = AsynchronousSocketChannel.open();
        Future<Void> future = clientChannel.connect(new InetSocketAddress(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT));

        future.get();// 等待建立连接成功

        // 用户写入数据
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            String input = consoleReader.readLine();
            ByteBuffer byteBuffer = ByteBuffer.wrap(input.getBytes());// 控制台输入的信息写入buffer
            Future<Integer> writeFuture = clientChannel.write(byteBuffer);

            writeFuture.get();//等待写入数据成功

            byteBuffer.flip();

            // 等待服务器端的返回的数据
            Future<Integer> readFuture = clientChannel.read(byteBuffer);

            // 等待收到数据以后写入
            readFuture.get();
            String echo = new String(byteBuffer.array());
            System.out.println("客户端收到信息："+echo);
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        new Client().start();
    }
}
