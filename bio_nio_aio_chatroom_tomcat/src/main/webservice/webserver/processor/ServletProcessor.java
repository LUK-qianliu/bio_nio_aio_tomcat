package webserver.processor;

import webserver.common.constant.HttpServer;
import webserver.common.status.HttpStatus;
import webserver.common.utils.ConnectorUtils;
import webserver.connector.Request;
import webserver.connector.Response;
import webserver.connector.facade.RequestFacade;
import webserver.connector.facade.ResponseFacade;
import webserver.servlet.BaseServlet;

import javax.servlet.Servlet;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;

public class ServletProcessor {

    /**
     * 加载Servlet的URL CLass Loader
     **/
//    public URLClassLoader URL_CLASS_LOADER;
    ClassLoader classLoader = BaseServlet.class.getClassLoader();

    // 完成类加载器的初始化
    public ServletProcessor(){
        try {
//            URL servletClassPath = new File(HttpServer.WEB_ROOT,"servlet").toURI().toURL();
//            URL_CLASS_LOADER = new URLClassLoader(new URL[]{servletClassPath});

        } catch (Exception e) {
            System.out.println("initialized servlet classloader is fail! "+e);
            throw new RuntimeException(e);
        }
    }




    /**
     * 根据Request执行相应的Servlet
     * @param request  request对象
     * @param response response对象
     */
    public void process(Request request, Response response) throws IOException {
        //根据请求的URI截取Servlet的名字
        String servletName = this.parseServletName(request.getUri());
        //使用URLClassLoader加载这个Servlet
        Class servletClass;
        try {
            servletClass = classLoader.loadClass("webserver.servlet."+servletName);
        } catch (ClassNotFoundException e) {
            System.out.println("servlet not found: "+servletName);
            //实例化失败则调用404页面
            response.sendStaticResource();
            return;
        }
        try {
            //实例化这个Servlet
            Servlet servlet = (Servlet) servletClass.newInstance();
            RequestFacade requestFacade = new RequestFacade(request);
            ResponseFacade  responseFacade = new ResponseFacade(response);

            responseFacade.getWriter().println(ConnectorUtils.renderStatus(HttpStatus.SC_OK));// 生成http头部
            servlet.service(requestFacade,responseFacade);
//            servlet.service(new RequestFacade(request), new ResponseFacade(response));
        } catch (Exception e) {
            System.out.println("Invoke Servlet {} is fail! "+servletName);
        }
    }

    /**
     * 解析到用户请求的Servlet类名
     *
     * @param uri 请求URI
     * @return Servlet类名
     */
    private String parseServletName(String uri) {
        return uri.substring(uri.lastIndexOf("/") + 1);
    }
}
