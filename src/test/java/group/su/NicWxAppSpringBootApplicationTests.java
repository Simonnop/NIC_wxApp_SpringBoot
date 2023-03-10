package group.su;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.FindIterable;
import group.su.dao.MissionDao;
import group.su.dao.impl.MissionDaoImpl;
import group.su.dao.impl.UserDaoImpl;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionKind;
import group.su.pojo.Mission;
import group.su.service.ManagerService;
import group.su.service.UserService;
import group.su.service.helper.MissionHelper;
import group.su.service.helper.UserHelper;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
class NicWxAppSpringBootApplicationTests {

    @Autowired
    ManagerService managerService;
    @Autowired
    UserService userService;
    @Autowired
    UserDaoImpl userDao;
    @Autowired
    MissionDaoImpl missionDao;
    @Autowired
    UserHelper userHelper;
    @Autowired
    MissionHelper missionHelper;

    @Test
    void contextLoads() {

    }

    @Test
    void get() {
        ArrayList<Document> documents = managerService.showMissionGotDraftToTeacher();

        for (Document document : documents) {
            System.out.println(document);
        }
    }

    @Test
    void test() {

        managerService.uploadArticleURL("2023010319","U202116999","https://www.bilibili.com");

        System.out.println(
                missionHelper.showUserInfoInMission(
                        missionDao.searchMissionByInput(
                                "missionID", "2023010319").first()));

    }
}
