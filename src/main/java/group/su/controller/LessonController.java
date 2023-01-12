package group.su.controller;

import com.alibaba.fastjson.JSONObject;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionHandler;
import group.su.exception.ExceptionKind;
import group.su.service.helper.SocketHelper;
import group.su.service.impl.UserServiceImpl;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

@RestController
public class LessonController {

    private final UserServiceImpl userService;
    private final SocketHelper socketHelper;

    @Autowired
    public LessonController(UserServiceImpl userService,SocketHelper socketHelper) {
        this.userService = userService;
        this.socketHelper = socketHelper;
    }

    @RequestMapping("/NIC/lesson")
    public String lessonRequestDistributor(@RequestParam("method") String method,
                                     @RequestParam("data") String data,
                                     HttpServletRequest req) throws UnsupportedEncodingException {
        JSONObject result = new JSONObject();
        JSONObject dataJson = JSONObject.parseObject(data);
        try {
            switch (method) {
                case "add":
                    result = addLessonResponse(dataJson);
                    break;
                case "get":
                    result = getLessonResponse(dataJson);
                    break;
                default:
                    throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(e, result, req, LessonController.class);
        }
        String resultStr = result.toJSONString();
        System.out.println(resultStr);
        return resultStr;
    }

    private JSONObject getLessonResponse(JSONObject dataJson) {

        String userid = (String) dataJson.get("userid");
        //Integer weekStart = (Integer) dataJson.get("weekStart");
        //Integer weekEnd = (Integer) dataJson.get("weekEnd");
        if (userid == null) {
            throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
        }
        ArrayList<Document> lessons = userService.showLessons(userid);
        return new JSONObject() {{
            put("code", 702);
            put("msg", "课表查询成功");
            put("data", lessons);
        }};
    }

    private JSONObject addLessonResponse(JSONObject dataJson) {

        JSONObject result = new JSONObject();

        String userid = (String) dataJson.get("userid");
        String password = (String) dataJson.get("password");
        if (userid == null && password == null) {
            throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
        }

        result.put("code", 702);
        result.put("msg", "课表爬取成功");

        String lesson = null;
        int count = 0;
        for (; ; ) {
            if (count == 5) {
                result.put("code", 703);
                result.put("msg", "课表爬取失败");
                break;
            }
            if (lesson == null) {
                lesson = socketHelper.getUserLesson(userid, password);
                count++;
            } else {
                break;
            }
        }

        return result;
    }
}
