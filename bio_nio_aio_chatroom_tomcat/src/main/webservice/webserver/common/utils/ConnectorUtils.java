package webserver.common.utils;

import webserver.common.status.HttpStatus;

/**
 * http协议 + 状态码 + 状态理由
 */
public class ConnectorUtils {

    public static final String PROTOCOL = "HTTP/1.1";

    public static final String CARRIAGE = "\r";

    public static final String NEWLINE = "\n";

    public static final String SPACE = " ";

    public static String renderStatus(HttpStatus status){
        StringBuilder sb = new StringBuilder(PROTOCOL)
                .append(SPACE)
                .append(status.getStatusCode())
                .append(SPACE)
                .append(status.getReason())
                .append(CARRIAGE).append(NEWLINE)
                .append(CARRIAGE).append(NEWLINE);
        return sb.toString();
    }
}
