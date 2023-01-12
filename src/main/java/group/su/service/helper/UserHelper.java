package group.su.service.helper;

import group.su.dao.MissionDao;
import group.su.dao.UserDao;
import group.su.dao.impl.MissionDaoImpl;
import group.su.dao.impl.UserDaoImpl;
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

        return userInfo;
    }
}
