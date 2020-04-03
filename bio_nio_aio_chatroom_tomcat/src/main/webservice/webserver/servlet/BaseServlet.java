package webserver.servlet;

import webserver.common.constant.HttpServer;
import webserver.common.status.HttpStatus;
import webserver.common.utils.ConnectorUtils;
import webserver.connector.Request;
import webserver.connector.Response;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 只要用户输入127.0.0.1:8080/servlet/{servletName}，我们就将这个URI提取出具体的servlet名字，
 * 使用java.net包下的URLClassLoader将这个Servlet类加载并实例化，然后调用它的service()方法，一次Servlet调用就这样完成啦
 */
public class BaseServlet implements Servlet {
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        System.out.println("Start invoke TestServlet ... ");
        PrintWriter printWriter = servletResponse.getWriter();
//        printWriter.println("当前时间为："+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        printWriter.println("hello world!!");
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
