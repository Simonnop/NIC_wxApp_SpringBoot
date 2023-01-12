package group.su.controller;

import com.alibaba.fastjson.JSONObject;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionHandler;
import group.su.exception.ExceptionKind;
import group.su.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@RestController
public class FileController {

    private final UserServiceImpl userService;

    @Autowired
    public FileController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @RequestMapping("NIC/upload")
    public String multiUpload(@RequestParam("file") MultipartFile[] files,
                              @RequestParam("missionID") String missionID,
                              @RequestParam("userid") String userid,
                              HttpServletRequest req) throws UnsupportedEncodingException {
        JSONObject result = new JSONObject();
        try {
            if (missionID == null || userid == null) {
                throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
            }
            // 这种方法可以保存多个文件
            for (MultipartFile multipartFile : files) {
                userService.saveFile(multipartFile, missionID, userid);
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


