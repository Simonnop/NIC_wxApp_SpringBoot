package group.su.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import group.su.controller.util.Encryption;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionHandler;
import group.su.exception.ExceptionKind;
import group.su.service.helper.UserHelper;
import group.su.service.impl.ManagerServiceImpl;
import group.su.service.impl.UserServiceImpl;
import group.su.service.util.TimeUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

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


    //微信用户登录：得到code,获取session_ley +openid,并自定义登录态
    @RequestMapping("/NIC/wxlogin") // 登录
    public String wxloginRequestDistributor(@RequestParam("code") String code,
                                            @RequestParam("userid") String useridOfNewUser,
                                            HttpServletRequest req) throws IOException {

        if (code != null) {
            Document appconfig = userService.getAppconfig();
            String url = "https://api.weixin.qq.com/sns/jscode2session?";
            url += "appid=" + appconfig.get("appid");
            url += "&secret=" + appconfig.get("appsecret");
            url += "&js_code=" + code;
            url += "&grant_type=authorization_code";
//              执行get请求.
            CloseableHttpResponse response = HttpClients.createDefault().execute(new HttpGet(url));
//              获取响应实体
            String html = EntityUtils.toString(response.getEntity());
            JSONObject result = JSON.parseObject(html);
//            得到 openid session_key
            String openid = (String) result.get("openid");
            String session_key = (String) result.get("session_key");

            if(openid==null){
                //code无效
                return WxloginCodeInvalid().toJSONString();
            }

            System.out.println("code:  "+code);
            System.out.println("openid:  "+openid);
            System.out.println("session_key:  "+session_key);

            //查询用户是否存在
            Document user = userService.queryUserInfoByKey(openid);
            if (user == null) {
                //此用户不存在或者是新用户
                if(useridOfNewUser!=null){
                    //新用户，绑定openid以及session_key
                    userService.updateUserKey(useridOfNewUser,openid);
                    userService.updateUserKey(useridOfNewUser,session_key);
                    return WxloginOfNewUserLogin().toJSONString();
                }
                 return WxloginOfNewUser().toJSONString();
            }else{
                //更新session_key
                userService.updateUserKey(openid,session_key);
                String userid = user.getString("userid");
                String password = user.getString("password");
                return WxloginOfOldUser(userid,password).toJSONString();
            }
        } else {
            //code为空
            return WxloginCodeEmpty().toJSONString();
        }
    }




//            if( result.get("session_key")!=null){
//////        这里只是对将openid作为key，再进行十六进制转换的方式加密   后期再改进
////                key = new Encryption().EncryptedString((String) result.get("openid"));
////        查询是否有该key
//                Document user = userHelper.queryUserInfoByKey(key);
////        如果没有，则原来的已过期（或者空），进行更新（填）
//                if (user == null) {
//                    String userid = req.getParameter("userid");
//                    userHelper.updateUserKey(userid,key,openid,session_key);
//                }
//                System.out.println(result);
//                //返回一个加密的key
//                msg ="ok";
//                data= "{"+"login_key:"+ key +",msg"+msg+"}";
//                return data;
//            }else {
//                msg = "code无效";
//                data = "{"+"login_key:"+ null +",msg"+msg+"}";
//                return data;
//            }
//        }else{
//            msg="code为空";
//            data = "{"+"login_key:"+null+",msg"+msg+"}";
//            return data ;
//        }




//    @RequestMapping("/NIC/wxrelogin") // 登录
//    public String wxreloginRequestDistributor(
//                                              HttpServletRequest req) throws IOException {
//
//
//    }

//    //登录的再次验证
//    @RequestMapping("/NIC/wxrelogin") // 登录
//    public String wxreloginRequestDistributor(@RequestParam("login_key") String login_key,
//                                              @RequestParam("encryptedData") String encryptedData,
//                                              @RequestParam("iv") String iv,
//                                              HttpServletRequest req) throws IOException {
////          小程序端将 encryptedData iv login_key 的值传到后端
////            encryptedData iv seesion_key 用于解密获取用户信息
////            login_key 用于校验用户登录状态
////            if(req.getParameter())
//
//            if(encryptedData!=null){
//        //       根据login_key得到对应用户的openid 和session_key
//                Document user = userHelper.queryUserInfoByKey(login_key);
//                String openid= (String) user.get("openid");
//                String session_key= (String) user.get("session_key");
////                利用微信官方提供算法拿到用户的开放数据
//
//            }else {
//
//            }
//        return null;
//    }

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
                case "grouped":  // TODO 待测试
                    result = showAllUserGrouped(dataJson);
                case "sorted":  // TODO 待测试
                    result = showAllUserSorted(dataJson);
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


    //微信登陆（code 无效）
    private JSONObject WxloginCodeInvalid(){
        return new JSONObject() {{
            put("code", 701);
            put("msg", "code无效");
            put("data", null);
        }};
    }

    //微信登陆（code空）
    private JSONObject WxloginCodeEmpty(){
        return new JSONObject() {{
            put("code", 701);
            put("msg", "code为空值");
            put("data", null);
        }};
    }

    private JSONObject WxloginOfOldUser(String userid,String password){
        return new JSONObject() {{
            put("code", 702);
            put("msg", "老用户登录成功");
            put("data", new JSONObject(){{
                put("userid",userid);
                put("password",password);
            }
            });
        }};
    }

    private JSONObject WxloginOfNewUser(){
        return new JSONObject() {{
            put("code", 703);
            put("msg", "新用户登录或账号不存在");
            put("data", null);
        }};
    }

    private JSONObject WxloginOfNewUserLogin(){
        return new JSONObject() {{
            put("code", 702);
            put("msg", "新用户登录成功");
            put("data", null);
        }};
    }


    private JSONObject showAllUserSorted(JSONObject dataJson) {
        String sortItem = (String) dataJson.get("sortItem");
        return new JSONObject() {{
            put("code", 602);
            put("msg", "查询用户列表成功");
            put("data", managerService.getTotalStuffSortedByInput(sortItem));
        }};
    }

    private JSONObject showAllUserGrouped(JSONObject dataJson) {

        String groupItem = (String) dataJson.get("groupItem");
        return new JSONObject() {{
            put("code", 602);
            put("msg", "查询用户列表成功");
            put("data", managerService.getTotalStuffGroupedByInput(groupItem));
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
