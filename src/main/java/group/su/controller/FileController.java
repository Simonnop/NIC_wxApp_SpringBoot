package group.su.controller;

import com.alibaba.fastjson.JSONObject;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionHandler;
import group.su.exception.ExceptionKind;
import group.su.service.impl.ManagerServiceImpl;
import group.su.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@RestController
public class FileController {

    private final UserServiceImpl userService;

    private final ManagerServiceImpl managerService;

    @Autowired
    public FileController(UserServiceImpl userService,ManagerServiceImpl managerService) {
        this.userService = userService;
        this.managerService = managerService;
    }

    @RequestMapping("NIC/upload")
    public String multiUpload(@RequestParam("file") MultipartFile[] files,
                              @RequestParam("missionID") String missionID,
                              @RequestParam("userid") String userid,
                              @RequestParam("kind") @Nullable String kind,
                              HttpServletRequest req) throws UnsupportedEncodingException {
        JSONObject result = new JSONObject();
        try {
            if (missionID == null || userid == null) {
                throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
            }
            // 这种方法可以保存多个文件
            for (MultipartFile multipartFile : files) {
                if (kind == null) {
                    userService.saveFile(multipartFile, missionID, userid);
                } else if (kind.equals("layout")) {
                    managerService.saveLayoutFiles(multipartFile, missionID, userid);
                } else {
                    throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
                }
            }
            result.put("code", 502);
            result.put("msg", "文件上传成功");
        } catch (Exception e) {
            ExceptionHandler.handleException(e, result, req, FileController.class);
        }
        String resultStr = result.toJSONString();
        System.out.println(resultStr);
        return resultStr;
    }
}


