package group.su.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionHandler;
import group.su.exception.ExceptionKind;
import group.su.pojo.Mission;
import group.su.service.impl.ManagerServiceImpl;
import group.su.service.impl.UserServiceImpl;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

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
                                           @RequestParam("data") @Nullable String data,
                                           HttpServletRequest req) throws UnsupportedEncodingException {
        JSONObject result = new JSONObject();
        JSONObject dataJson = JSONObject.parseObject(data);
        try {
            switch (method) {
                case "add":
                    result = addMissionResponse(dataJson);
                    break;
                case "getTag":
                    result = getTagResponse();
                    break;
                case "examine":  // TODO 待测试
                    result = examineMissionResponse(dataJson);
                    break;
                case "delete":  // TODO 待测试
                    result = deleteMissionResponse(dataJson);
                    break;
                case "alter":   // TODO 待测试
                    result = alterMissionResponse(dataJson);
                    break;
                case "return":
                    result = returnMissionResponse(dataJson);
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
                    result = showMissionGotDraft(dataJson);
                    break;
                case "showNeedLayout":
                    result = showMissionNeedLayout();
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
                case "uploadURL":
                    result = uploadArticleURL(dataJson);
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

    private JSONObject showMissionNeedLayout() {
        return new JSONObject() {{
            put("data", managerService.showMissionNeedLayout());
            put("code", 302);
            put("msg", "查询待排版任务成功");
        }};
    }

    private JSONObject showMissionDraftToTeacher() {
        return new JSONObject() {{
            put("data", managerService.showMissionGotDraftToTeacher());
            put("code", 302);
            put("msg", "查询待辅导员审核稿件任务成功");
        }};
    }

    private JSONObject uploadArticleURL(JSONObject dataJson) {
        String missionID = String.valueOf(dataJson.get("missionID"));
        String userid = String.valueOf(dataJson.get("userid"));
        String url = String.valueOf(dataJson.get("url"));
        managerService.uploadArticleURL(missionID, userid, url);

        return new JSONObject() {{
            put("code", 202);
            put("msg", "URL上传成功");
        }};
    }

    private JSONObject returnMissionResponse(JSONObject dataJson) {

        String missionID = String.valueOf(dataJson.get("missionID"));
        String userid = String.valueOf(dataJson.get("userid"));
        String comment = String.valueOf(dataJson.get("comment"));
        managerService.thrashBack(missionID, userid, comment);

        return new JSONObject() {{
            put("code", 202);
            put("msg", "打回操作成功");
        }};
    }

    private JSONObject alterMissionResponse(JSONObject dataJson) {

        String missionID = String.valueOf(dataJson.get("missionID"));

        int missionElement = Integer.parseInt(dataJson.get("element").toString());
        String publisher = (String) dataJson.get("publisher");
        // parseObject 参数要求是字符串
        Mission mission = JSONObject.parseObject(JSON.toJSONString(dataJson), Mission.class);
        mission.setElement(missionElement);

        managerService.alterMission(missionID, mission, publisher);

        return new JSONObject() {{
            put("code", 202);
            put("msg", "任务更改成功");
        }};
    }

    private JSONObject deleteMissionResponse(JSONObject dataJson) {

        String missionID = (String) dataJson.get("missionID");
        managerService.deleteMission(missionID);

        return new JSONObject() {{
            put("code", 202);
            put("msg", "任务删除成功");
        }};
    }

    private JSONObject examineMissionResponse(JSONObject dataJson) {

        String kind = (String) dataJson.get("kind");

        String userid = (String) dataJson.get("userid");
        String missionID = (String) dataJson.get("missionID");
        String stars = (String) dataJson.get("stars");
        String comment = (String) dataJson.get("review");

        String ddl = (String) dataJson.get("ddl");
        String postscript = (String) dataJson.get("postscript");

        String[] tags = dataJson.getObject("tag", String[].class);

        if (userid == null || missionID == null || kind == null) {
            throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
        }

        if (kind.equals("editor")) {
            managerService.examineDraftByEditor(missionID, userid, stars, comment, tags);
        } else if (kind.equals("teacher")) {
            managerService.examineDraftByTeacher(missionID, userid, stars, comment, ddl, postscript, tags);
        } else {
            throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
        }

        return new JSONObject() {{
            put("code", 402);
            put("msg", "提交审核成功");
        }};
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

        String tag1 = (String) dataJson.get("tag1");
        String tag2 = (String) dataJson.get("tag2");
        String missionID = (String) dataJson.get("missionID");

        if (missionID != null) {
            return new JSONObject() {{
                put("data", userService.showMissionById(missionID));
                put("code", 302);
                put("msg", "指定查询任务成功");
            }};
        } else if (tag1 != null) {
            return new JSONObject() {{
                put("data", userService.showMissionByTag(tag1, tag2));
                put("code", 302);
                put("msg", "指定查询任务成功");
            }};
        } else {
            throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
        }
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

    private JSONObject showMissionGotDraft(JSONObject dataJson) {

        String kind = (String) dataJson.get("kind");

        return new JSONObject() {{

            ArrayList<Document> documents = new ArrayList<>();

            if (kind.equals("editor")) {
                documents = managerService.showMissionGotDraft();
            } else if (kind.equals("teacher")) {
                documents = managerService.showMissionGotDraftToTeacher();
            }

            put("data", documents);
            put("code", 302);
            put("msg", "查询已有稿件任务成功");
        }};
    }

    private JSONObject addMissionResponse(JSONObject dataJson) {

        int missionElement = Integer.parseInt(dataJson.get("element").toString());
        String publisher = (String) dataJson.get("publisher");
        // 整理 tags 格式
        String tags = dataJson.get("tags").toString().replace("\\", "");
        dataJson.put("tags", JSONObject.parseObject(tags));

        // parseObject 参数要求是字符串
        Mission mission = JSONObject.parseObject(JSON.toJSONString(dataJson), Mission.class);
        mission.setElement(missionElement);

        managerService.addMission(mission, publisher);

        return new JSONObject() {{
            put("code", 202);
            put("msg", "任务添加成功");
            put("missionID", mission.getMissionID());
        }};
    }

    private JSONObject getTagResponse() {
        return new JSONObject() {{
            put("code", 302);
            put("msg", "查询tag成功");
            put("data", userService.showTag());
        }};
    }

}
