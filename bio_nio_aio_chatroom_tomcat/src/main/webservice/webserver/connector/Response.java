package webserver.connector;

import webserver.common.status.HttpStatus;
import webserver.common.utils.ConnectorUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import java.io.*;
import java.util.Locale;

public class Response implements ServletResponse {

    private static final int BUFFER_SIZE = 1024;
    Request request;
    OutputStream output;

    public Response(OutputStream output) {
        this.output = output;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * 将需要的文件的读到流中，编写好http头部，并将流读到发送到output中
     * @throws IOException
     */
    public void sendStaticResource() throws IOException {
        try{
            // 通过request的uri，获取资源的路径
            String filePath = getClass()
                    .getClassLoader()
                    .getResource(request.getUri()).getFile();
            // 创建资源文件
            File file = new File(filePath.substring(1 , filePath.length()));
            // 将资源写入流里面，HttpStatus.SC_OK是状态码
            write(file , HttpStatus.SC_OK);
        } catch (Exception e) {
            // 当出现错误时，简单处理 ，发送404.html给客户端
            String errorFilePath = getClass().getClassLoader().getResource("404.html").getFile();
            // 将资源写入流里面，HttpStatus.SC_NOT_FOUND是状态码
            write(new File(errorFilePath.substring(1 , errorFilePath.length())) ,
                    HttpStatus.SC_NOT_FOUND);

        }
    }

    /**
     * 输出流中：协议 + 状态码 + 状态信息 + 文件流
     * @param resource 需要写入的文件
     * @param status  写入的状态码
     * @throws IOException
     */
    private void write(File resource , HttpStatus status) throws IOException {

        try(FileInputStream fis = new FileInputStream(resource)){// 小括号中的资源会在大括号中的代码执行结后自动释放
            // 先将协议、状态码等必要信息写入流中，ConnectorUtils是工具类
            output.write(ConnectorUtils.renderStatus(status).getBytes());
            byte[] buffer = new byte[BUFFER_SIZE];
            int length = 0;
            // 把资源文件写入流中
            while((length = fis.read(buffer , 0 , BUFFER_SIZE)) != -1){
                output.write(buffer , 0 ,length);
            }
        }
    }

    // 下面是重写ServletResponse中的方法
    public String getCharacterEncoding() {
        return null;
    }

    public String getContentType() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    public PrintWriter getWriter() throws IOException {
        PrintWriter printWriter = new PrintWriter(output,true);
        return printWriter;
    }

    public void setCharacterEncoding(String s) {

    }

    public void setContentLength(int i) {

    }

    public void setContentType(String s) {

    }

    public void setBufferSize(int i) {

    }

    public int getBufferSize() {
        return 0;
    }

    public void flushBuffer() throws IOException {

    }

    public void resetBuffer() {

    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {

    }

    public void setLocale(Locale locale) {

    }

    public Locale getLocale() {
        return null;
    }
}
