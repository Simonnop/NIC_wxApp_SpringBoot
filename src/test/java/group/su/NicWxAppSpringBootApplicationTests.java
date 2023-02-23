package group.su;

import group.su.dao.impl.MissionDaoImpl;
import group.su.dao.impl.UserDaoImpl;
import group.su.service.ManagerService;
import group.su.service.UserService;
import group.su.service.helper.UserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Map;

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
        Map<String, ArrayList<Map<String, String>>> totalStuffByDepartment =
                managerService.getTotalStuffGroupedByInput("classStr");
        System.out.println(totalStuffByDepartment);
    }

    @Test
    void test() {



    }
}
