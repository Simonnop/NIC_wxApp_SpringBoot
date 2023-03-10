package group.su;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.FindIterable;
import group.su.dao.AffairDao;
import group.su.dao.ConfigDao;
import group.su.dao.LessonDao;
import group.su.dao.MissionDao;
import group.su.dao.impl.AffairDaoImpl;
import group.su.dao.impl.MissionDaoImpl;
import group.su.dao.impl.UserDaoImpl;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionKind;
import group.su.pojo.Affair;
import group.su.pojo.Mission;
import group.su.service.ManagerService;
import group.su.service.UserService;
import group.su.service.helper.MissionHelper;
import group.su.service.helper.UserHelper;
import group.su.service.util.TimeUtil;
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
    @Autowired
    TimeUtil timeUtil;
    @Autowired
    ConfigDao configDao;
    @Autowired
    LessonDao lessonDao;
    @Autowired
    AffairDao affairDao;

    @Test
    void contextLoads() {

    }

    @Test
    void get() {
        Affair affair = new Affair("test", "三公", "2023-03-07", "12:00", "13:00");
        affair.setPublisher("U202116999");
        affair.setInvolveUsers(new ArrayList<String>(){{
            add("U202111390");}});
        affair.generateRandomID();
        userService.addAffair(affair);

        System.out.println(userService.showAffairOnDate("U202111390", "2023-03-07"));

        affairDao.delete("affairID", affair.getAffairID());

        System.out.println(userService.showAffairOnDate("U202111390", "2023-03-07"));
    }

    @Test
    void test() {

        System.out.println(managerService.showMissionGotDraft());
    }
}
