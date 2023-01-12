package group.su.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.lang.Nullable;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionHandler;
import group.su.exception.ExceptionKind;
import group.su.pojo.Mission;
import group.su.service.impl.ManagerServiceImpl;
import group.su.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@RestController
public class MissionController {

    private final ManagerServiceImpl managerService;
    private final UserServiceImpl userService;

    @Autowired
    public MissionController(ManagerServiceImpl managerService, UserServiceImpl userService) {
        this.managerService = managerService;
        this.userService = userService;
    }

    @RequestMapping("/NIC/manage")
    public String manageRequestDistributor(@RequestParam("method") String method,
                                           @RequestParam("data") String data,
                                           HttpServletRequest req) throws UnsupportedEncodingException {
        JSONObject result = new JSONObject();
        JSONObject dataJson = JSONObject.parseObject(data);
        try {
            switch (method) {
                case "add":
                    result = addMissionResponse(dataJson);
                    break;
                case "getTag":
                    result = getTagResponse(dataJson);
                    break;
                default:
                    throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(e, result, req, MissionController.class);
        }
        String resultStr = result.toJSONString();
        System.out.println(resultStr);
        return resultStr;
    }

    @RequestMapping("/NIC/show")
    public String showRequestDistributor(@RequestParam("method") String method,
                                         @RequestParam("data") @Nullable String data,
                                         HttpServletRequest req) throws UnsupportedEncodingException {
        JSONObject result = new JSONObject();
        JSONObject dataJson = JSONObject.parseObject(data);
        try {
            switch (method) {
                case "showAll":
                    result = showAllMission();
                    break;
                case "showNeed":
                    result = showNeedMission();
                    break;
                case "showGotDraft":
                    result = showMissionGotDraft();
                    break;
                case "showByInput":
                    result = showMissionByInput(dataJson);
                    // 时间 状态 评分 标签
                    break;
                default:
                    throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(e, result, req, MissionController.class);
        }
        String resultStr = result.toJSONString();
        System.out.println(resultStr);
        return resultStr;
    }

    @RequestMapping("/NIC/take")
    public String takeRequestDistributor(@RequestParam("method") String method,
                                         @RequestParam("data") String data,
                                         HttpServletRequest req) throws UnsupportedEncodingException {
        JSONObject result = new JSONObject();
        JSONObject dataJson = JSONObject.parseObject(data);
        try {
            switch (method) {
                case "take":
                    result = takeMission(dataJson);
                    break;
                case "quit":
                    result = quitMission(dataJson);
                    break;
                default:
                    throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
            }
        } catch (Exception e) {
            ExceptionHandler.handleException(e, result, req, MissionController.class);
        }
        String resultStr = result.toJSONString();
        System.out.println(resultStr);
        return resultStr;
    }

    private JSONObject takeMission(JSONObject dataJson) {

        String userid = (String) dataJson.get("userid");
        String missionID = (String) dataJson.get("missionID");
        String kind = (String) dataJson.get("kind");
        if (userid == null || missionID == null || kind == null) {
            throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
        }
        userService.tryGetMission(userid, missionID, kind);

        return new JSONObject() {{
            put("code", 402);
            put("msg", "任务参加成功");
        }};
    }

    private JSONObject quitMission(JSONObject dataJson) {
        return new JSONObject();
    }

    private JSONObject showMissionByInput(JSONObject dataJson) {

        String missionID = (String) dataJson.get("missionID");
        if (missionID == null) {
            throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
        }
        return new JSONObject() {{
            put("data", userService.showMissionById(missionID));
            put("code", 302);
            put("msg", "指定查询任务成功");
        }};
    }

    private JSONObject showAllMission() {
        return new JSONObject() {{
            put("data", userService.showAllMission());
            put("code", 302);
            put("msg", "全部查询任务成功");
        }};
    }

    private JSONObject showNeedMission() {
        return new JSONObject() {{
            put("data", userService.showNeedMission());
            put("code", 302);
            put("msg", "全部缺人任务成功");
        }};

    }

    private JSONObject showMissionGotDraft() {
        return new JSONObject() {{
            put("data", managerService.showMissionGotDraft());
            put("code", 302);
            put("msg", "查询已有稿件任务成功");
        }};
    }

    private JSONObject addMissionResponse(JSONObject dataJson) {

        int missionElement = Integer.parseInt((String) dataJson.get("element"));
        String publisher = (String) dataJson.get("publisher");
        // parseObject 参数要求是字符串
        Mission mission = JSONObject.parseObject(JSON.toJSONString(dataJson), Mission.class);
        mission.setElement(missionElement);

        managerService.addMission(mission, publisher);

        return new JSONObject() {{
            put("code", 202);
            put("msg", "任务添加成功");
        }};
    }


    private JSONObject getTagResponse(JSONObject dataJson) {
        return new JSONObject();
    }

}
