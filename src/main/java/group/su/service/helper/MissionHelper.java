package group.su.service.helper;

import com.mongodb.client.FindIterable;
import group.su.dao.MissionDao;
import group.su.dao.MissionDaoTest;
import group.su.dao.UserDao;
import group.su.dao.impl.MissionDaoImpl;
import group.su.dao.impl.UserDaoImpl;
import group.su.pojo.Mission;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MissionHelper {

    final UserDao userDao;
    final MissionDao missionDao;
    @Autowired
    MissionDaoTest missionDaoTest;

    @Autowired
    public MissionHelper(UserDao userDao, MissionDao missionDao) {
        this.userDao = userDao;
        this.missionDao = missionDao;
    }

    public void updateMissionStatus(String missionID) {

        Mission mission = missionDaoTest.findMissionByMissionID(missionID);
        for (String kind : calculateLack(mission).getReporterNeeds().keySet()
        ) {
            // 缺少的记者数不为零
            if (!(mission.getReporterLack()
                    .get(kind)
                    .equals(0))) {
                return;
            }
        }

        Map<String, String> status = mission.getStatus();
        status.put("status.接稿", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        mission.setStatus(status);
        missionDaoTest.save(mission);

        System.out.println("updated");
    }

    public ArrayList<Document> changeFormAndCalculate(FindIterable<Document> documents) {
        // 改成可以 addAll 的格式并计算 缺少人数
        ArrayList<Document> missionArray = new ArrayList<>();

        for (Document document : documents) {
            // 计算还缺少的人数
            missionArray.add(calculateLack(document));
        }

        return missionArray;
    }

    public ArrayList<Mission> changeFormAndCalculate(List<Mission> missionList) {
        // 改成可以 addAll 的格式并计算 缺少人数
        ArrayList<Mission> missionArray = new ArrayList<>();

        for (Mission mission : missionList) {
            // 计算还缺少的人数
            missionArray.add(calculateLack(mission));
        }

        return missionArray;
    }

    public Document calculateLack(Document document) {

        document.remove("_id");
        // 计算还缺少的人数
        Document reporterNeeds = (Document) document.get("reporterNeeds");
        Document reporters = (Document) document.get("reporters");
        Document reporterLack = new Document();

        for (String str : reporterNeeds.keySet()
        ) {
            reporterLack.put(str, (Integer) reporterNeeds.get(str)
                    - reporters.getList(str, String.class).size());
        }
        document.put("reporterLack", reporterLack);

        return document;
    }

    public Mission calculateLack(Mission mission) {

        Map<String, Integer> reporterNeeds = mission.getReporterNeeds();
        Map<String, List<String>> reporters = mission.getReporters();
        Map<String, Integer> reporterLack = new HashMap<>();

        for (String str : reporterNeeds.keySet()
        ) {
            reporterLack.put(str,  reporterNeeds.get(str) - reporters.get(str).size());
        }
        mission.setReporterLack(reporterLack);

        return mission;
    }
}
