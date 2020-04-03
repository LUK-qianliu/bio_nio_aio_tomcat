package webserver.processor;

import webserver.connector.Response;

import java.io.IOException;

public class StaticProcessor {

    public void process(Response response){
        try {
            response.sendStaticResource();
        } catch (IOException e) {
            // 不处理浏览器断开连接等错误
        }
    }

}
