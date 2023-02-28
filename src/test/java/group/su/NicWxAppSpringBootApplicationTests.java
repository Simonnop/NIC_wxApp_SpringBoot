package group.su;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.FindIterable;
import group.su.dao.impl.MissionDaoImpl;
import group.su.dao.impl.UserDaoImpl;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionKind;
import group.su.service.ManagerService;
import group.su.service.UserService;
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

    @Test
    void contextLoads() {

    }

    @Test
    void get() {
        managerService.examineDraftByEditor("2023011915","U202116999",
                "10","可以","可爱捏","可爱");

    }

    @Test
    void test() {

        String sortItem = "username";

        Map<String, ArrayList<Document>> totalStuffSortedByInput = managerService.getTotalStuffSortedByInput(sortItem);

        System.out.println(totalStuffSortedByInput.toString());

        String a = new JSONObject() {{
            put("a", totalStuffSortedByInput);
        }}.toJSONString();

        System.out.println(a);

    }
}
