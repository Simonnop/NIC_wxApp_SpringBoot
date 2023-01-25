package group.su;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import group.su.dao.impl.MissionDaoImpl;
import group.su.dao.impl.UserDaoImpl;
import group.su.pojo.Mission;
import group.su.service.ManagerService;
import group.su.service.UserService;
import group.su.service.helper.MissionHelper;
import group.su.service.helper.UserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

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

    @Test
    void contextLoads() {

    }

    @Test
    void get() {

    }

    @Test
    void test() {
        System.out.println(userHelper.getUserAllInfo("userid","U202116999"));
    }
}
