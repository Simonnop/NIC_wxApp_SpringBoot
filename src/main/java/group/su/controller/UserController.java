package group.su.controller;

import com.alibaba.fastjson.JSONObject;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionHandler;
import group.su.exception.ExceptionKind;
import group.su.service.helper.UserHelper;
import group.su.service.impl.ManagerServiceImpl;
import group.su.service.impl.UserServiceImpl;
import group.su.service.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@RestController
public class UserController {
    private final UserServiceImpl userService;
    private final UserHelper userHelper;

    private final ManagerServiceImpl managerService;

    @Autowired
    public UserController(UserServiceImpl userService,UserHelper userHelper, ManagerServiceImpl managerService) {
        this.userService = userService;
        this.userHelper = userHelper;
        this.managerService = managerService;
    }

    @RequestMapping("/NIC/login")
    public String loginRequestDistributor(@RequestParam("method") String method,
                                     @RequestParam("data") String data,
                                     HttpServletRequest req) throws UnsupportedEncodingException {
        JSONObject result = new JSONObject();
        JSONObject dataJson = JSONObject.parseObject(data);
        try {
            switch (method) {
                case "signUp":
                    result = signUpResponse(dataJson);
                    break;
                case "signIn":
                    result = signInResponse(dataJson);
                    break;
                case "tourist":
                    result = touristResponse(dataJson);
                    break;
                default:
                    throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(e, result, req, UserController.class);
        }
        String resultStr = result.toJSONString();
        System.out.println(resultStr);
        return resultStr;
    }

    @RequestMapping("/NIC/user")
    public String userRequestDistributor(@RequestParam("method") String method,
                                          @RequestParam("data") String data,
                                          HttpServletRequest req) throws UnsupportedEncodingException {
        JSONObject result = new JSONObject();
        JSONObject dataJson = JSONObject.parseObject(data);
        try {
            switch (method) {
                case "userInfo":
                    result = showMissionTakenResponse(dataJson);
                    break;
                case "showFinished":
                    result = showFinishedMission(dataJson);
                default:
                    throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(e, result, req, UserController.class);
        }
        String resultStr = result.toJSONString();
        System.out.println(resultStr);
        return resultStr;
    }

    @RequestMapping("/NIC/allUser")
    public String allUserRequestDistributor(@RequestParam("method") String method,
                                         @RequestParam("data") @Nullable String data,
                                         HttpServletRequest req) throws UnsupportedEncodingException {
        JSONObject result = new JSONObject();
        JSONObject dataJson = JSONObject.parseObject(data);
        try {
            switch (method) {
                case "showByGroup":
                    result = showAllUserByGroup();
                default:
                    throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(e, result, req, UserController.class);
        }
        String resultStr = result.toJSONString();
        System.out.println(resultStr);
        return resultStr;
    }

    private JSONObject showAllUserByGroup() {
        return new JSONObject() {{
            put("code", 602);
            put("msg", "查询用户列表");
            put("data", managerService.getTotalStuffByDepartment());
        }};
    }


    private JSONObject showMissionTakenResponse(JSONObject dataJson) {
        return new JSONObject() {{
            put("code", 602);
            put("msg", "查询用户已接任务成功");
            put("data", userService.showTakenMission(
                    "userid",
                    (String) dataJson.get("userid")));
        }};
    }

    private JSONObject showFinishedMission(JSONObject dataJson) {
        return new JSONObject() {{
            put("data", userService.showFinishedMission("userid",
                    (String) dataJson.get("userid")));
            put("code", 602);
            put("msg", "查询用户已完成任务成功");
        }};
    }

    private JSONObject signUpResponse(JSONObject dataJson) {

        JSONObject result = new JSONObject();

        String userid = (String) dataJson.get("userid");
        String password = (String) dataJson.get("password");
        if (userid == null && password == null) {
            throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
        }

        Boolean correctLogin = userService.tryLogin(userid, password);
        if (correctLogin) {
            result.put("code", 102);
            result.put("msg", "登录成功");
            result.put("data", userHelper.getUserLoginInfo("userid", userid));
            result.put("time", TimeUtil.getCurrentWeekInfo());
        } else {
            result.put("code", 101);
            result.put("msg", "密码错误");
        }
        System.out.println(result);
        return result;
    }

    private JSONObject signInResponse(JSONObject dataJson) {
        return new JSONObject();
    }

    private JSONObject touristResponse(JSONObject dataJson) {
        return new JSONObject();
    }
}
