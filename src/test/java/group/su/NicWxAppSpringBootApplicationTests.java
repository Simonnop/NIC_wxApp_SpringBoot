package group.su;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import group.su.dao.MissionDaoTest;
import group.su.dao.UserDaoTest;
import group.su.dao.impl.MissionDaoImpl;
import group.su.dao.impl.UserDaoImpl;
import group.su.pojo.Mission;
import group.su.pojo.User;
import group.su.service.ManagerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;

@SpringBootTest
class NicWxAppSpringBootApplicationTests {

    @Autowired
    ManagerService managerService;

    @Autowired
    UserDaoImpl userDao;

    @Autowired
    MissionDaoImpl missionDao;

    @Resource
    MongoTemplate mongoTemplate;

    @Autowired
    MissionDaoTest missionDaoTest;

    @Autowired
    UserDaoTest userDaoTest;

    @Test

    void contextLoads() {

    }

    @Test
    void testAny() {


        /*User user = new User();
        user.setClassStr("管实2101");
        user.setIdentity("摸鱼部部员");
        user.setUserid("U202116999");
        user.setUsername("test");
        user.setAuthorityLevel(new ArrayList<Integer>() {{
            add(1);
            add(1);
            add(0);
        }});
        user.setPassword("123456");
        user.setQQ("123456798");
        user.setTel("45678913513");
        user.setMissionTaken(new ArrayList<>());
        user.setMissionCompleted(new ArrayList<>());

        userDaoTest.insert(user);*/

        User user = userDaoTest.findByUserid("U202116999");
        user.setTel("00000000000");
        userDaoTest.save(user);
    }

    @Test
    void addMission() {
        JSONObject result = new JSONObject();
        String data = "{\"place\": \"这里\"," +
                      "\"title\": \"测试新字段\"," +
                      "\"publisher\": \"U202116999\"," +
                      "\"element\": \"1\"," +
                      "\"description\": \"如题\"," +
                      "\"time\": {\"year\": 1235,\"month\": 12,\"day\": 12,\"beginHour\": 12,\"beginMinute\": 0,\"endHour\": 13,\"endMinute\": 0}," +
                      "\"reporterNeeds\": {\"photo\": 1,\"article\": 1}}";

        System.out.println(data);

        try {

            JSONObject dataJson = JSONObject.parseObject(data);

            int missionElement = Integer.parseInt((String) dataJson.get("element"));
            String publisher = (String) dataJson.get("publisher");
            // parseObject 参数要求是字符串
            Mission mission = JSONObject.parseObject(JSON.toJSONString(dataJson), Mission.class);
            mission.setElement(missionElement);

            managerService.addMission(mission, publisher);

            result.put("code", 202);
            result.put("msg", "任务添加成功");

        } catch (Exception e) {
            result.put("code", 203);
            result.put("msg", "任务信息错误");
            throw e;
        } finally {
            String resultStr = result.toJSONString();
            System.out.println(resultStr);
        }
    }
}
