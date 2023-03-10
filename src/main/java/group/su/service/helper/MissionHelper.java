package group.su.service.helper;

import com.mongodb.client.FindIterable;
import group.su.dao.MissionDao;
import group.su.dao.UserDao;
import group.su.dao.impl.MissionDaoImpl;
import group.su.dao.impl.UserDaoImpl;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionKind;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Service
public class MissionHelper {

    final UserDao userDao;
    final MissionDao missionDao;
    final UserHelper userHelper;

    @Autowired
    public MissionHelper(UserDao userDao, MissionDao missionDao, UserHelper userHelper) {
        this.userDao = userDao;
        this.missionDao = missionDao;
        this.userHelper = userHelper;
    }

    public void updateMissionStatus(String missionID) {

        Document document = missionDao.searchMissionByInput("missionID", missionID).first();
        for (String kind : ((Document) calculateLack(document).get("reporterNeeds")).keySet()
        ) {
            // 缺少的记者数不为零
            if (!((Document) document.get("reporterLack"))
                    .get(kind)
                    .equals(0)) {
                return;
            }
        }

        missionDao.updateInMission(
                "missionID", missionID,
                "status.接稿", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
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

        return showUserInfoInMission(document);
    }

    public Document showUserInfoInMission(Document document) {

        Document reporters = document.get("reporters", Document.class);
        Document statusChanger = document.get("statusChanger", Document.class);

        for (String kind : reporters.keySet()) {
            if (reporters.getList(kind, String.class).isEmpty()) {
                continue;
            }
            ArrayList<Document> tempDocument = new ArrayList<>();
            reporters.getList(kind, String.class).forEach(
                    (userid) -> tempDocument.add(userHelper.getUserInfoInMission("userid", userid)));
            reporters.put(kind, tempDocument);
        }
        for (String kind : statusChanger.keySet()) {
            String s = statusChanger.get(kind, String.class);
            if (s == null || s.equals("")) {
                continue;
            }
            if (s.startsWith("U")) {
                statusChanger.put(kind, userHelper.getUserInfoInMission("userid", s));
            } else {
                statusChanger.put(kind, userHelper.getUserInfoInMission("username", s));
            }

        }

        return document;
    }

    public ArrayList<Document> findSimilarMission(String... tags) {

        // TODO 改了结构,要重新写

        FindIterable<Document> documents;
        if (tags.length == 1) {
            documents = missionDao.searchMissionByInput("tag1", tags[0]);
        } else {
            documents = missionDao.searchMissionByInput("tag1", tags[0], "tag2", tags[1]);
        }
        if (documents.first() == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        return this.changeFormAndCalculate(documents);
    }
}
