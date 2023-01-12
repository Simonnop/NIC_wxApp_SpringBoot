package group.su;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import group.su.dao.impl.MissionDaoImpl;
import group.su.dao.impl.UserDaoImpl;
import group.su.pojo.Mission;
import group.su.service.ManagerService;
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
    UserDaoImpl userDao;

    @Autowired
    MissionDaoImpl missionDao;

    @Resource
    MongoTemplate mongoTemplate;

    @Test
    void contextLoads() {

    }

    @Test
    void get() {

        Map<String, Integer> availableTime = managerService.findAvailableTime(1);

        System.out.println(availableTime);
/*
        Mission mission = missionDao.findMissionById("1935121201");

        String missionString = JSONObject.toJSONString(mission);

        System.out.println(missionString);

        JSONObject jsonObject = JSONObject.parseObject(missionString);
*/


    }

    @Test
    void addMission() {
        JSONObject result = new JSONObject();
        String data = "{\"place\": \"这里\"," +
                      "\"title\": \"测试新字段\"," +
                      "\"publisher\": \"U202116999\"," +
                      "\"element\": \"1\"," +
                      "\"description\": \"如题\"," +
                      "\"time\": {\"year\": 1935,\"month\": 12,\"day\": 12,\"beginHour\": 12,\"beginMinute\": 0,\"endHour\": 13,\"endMinute\": 0}," +
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
