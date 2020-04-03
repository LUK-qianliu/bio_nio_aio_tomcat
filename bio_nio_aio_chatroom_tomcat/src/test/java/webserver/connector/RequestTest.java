package webserver.connector;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class RequestTest {

    private static final String REQUEST_STRING = "GET /index.html HTTP/1.1";

    @Test
    public void parseTest(){
        InputStream inputStream = new ByteArrayInputStream(REQUEST_STRING.getBytes());
        Request request = new Request(inputStream);
        request.parse();
        Assert.assertEquals("index.html", request.getUri());
    }
}
