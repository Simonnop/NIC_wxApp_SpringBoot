package group.su.exception;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ExceptionHandler {

    public static <T> void handleException(Exception e, JSONObject result,
                                           HttpServletRequest req, Class<T> clazz) throws UnsupportedEncodingException {
        Logger logger = Logger.getLogger(clazz);
        if (e instanceof AppRuntimeException) {
            result.put("code", ((AppRuntimeException) e).getCode());
            result.put("msg", ((AppRuntimeException) e).getMsg());
        } else {
            result.put("code", 98);
            result.put("msg", "后端" + clazz.getName() + "处理错误");
            logger.error(URLDecoder.decode(req.getQueryString(), "utf-8"));
            printException(logger, e);
        }
    }

    public static void printException(Logger logger, Exception e) {
        logger.error(e);
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.toString().contains("javax.servlet.http.HttpServlet.service")) {
                return;
            }
            logger.error(element);
        }
    }
}
