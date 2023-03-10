package group.su.service.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import group.su.dao.MissionDao;
import group.su.dao.UserDao;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionKind;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserHelper {

    final UserDao userDao;
    final MissionDao missionDao;

    @Autowired
    public UserHelper(UserDao userDao, MissionDao missionDao) {
        this.userDao = userDao;
        this.missionDao = missionDao;
    }

    public Document getUserLoginInfo(String field, String value) {

        Document userAllInfo = getUserAllInfo(field, value);

        userAllInfo.remove("authorityLevel");
        userAllInfo.remove("QQ");
        userAllInfo.remove("tel");
        userAllInfo.remove("classStr");
        userAllInfo.remove("password");
        userAllInfo.remove("gender");
        userAllInfo.remove("month_performance");
        userAllInfo.remove("total_performance");
        return userAllInfo;
    }

    public Document getUserAllInfo(String field, String value) {

        Document userInfo = userDao.searchUserByInputEqual(field, value).first();
        if (userInfo == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        int levelCount = 1;
        for (Integer level : userInfo.getList("authorityLevel", Integer.class)) {
            userInfo.put("authority" + levelCount++, level);
        }
        userInfo.remove("_id");
        userInfo.remove("authorityLevel");

        return userInfo;
    }

    public Document getUserInfoInMission(String field, String value) {

        Document userAllInfo = getUserAllInfo(field, value);

        String[] fields = {"username","classStr","tel","QQ","userid","head"};

        return new Document() {{
            for (String field : fields
            ) {
                put(field, userAllInfo.get(field));
            }
        }};
    }

    //通过openid查询 user是否存在
    public Document queryUserInfoByKey(String openid){
        Document userInfo = userDao.searchUserByInputEqual("openid",openid).first();
        return userInfo;
    }


    //更新
    public void updateUserKey(String s1,String s2 ){
        userDao.updateInUser(s1,s1,s2, s2);
    }


}
