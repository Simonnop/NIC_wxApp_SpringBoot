package group.su.service;

import group.su.pojo.Affair;
import org.bson.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface UserService {

    Boolean tryLogin(String userid, String password);

    ArrayList<Document> showAllMission();

    ArrayList<Document> showNeedMission();

    ArrayList<Document> showMissionById(String missionID);

    ArrayList<Document> showMissionByTag(String tag1,String tag2);

    ArrayList<Document> showTakenMission(String field, String value);

    void tryGetMission(String userid, String missionID, String kind);

    void saveFile(MultipartFile file, String missionID, String userid);

    Map<String, String[][]> showTag();

    ArrayList<Document> showLessons(String userid, Integer... week);

    ArrayList<Document> showFinishedMission(String field, String value);

    ArrayList<Document> showMissionNeedLayout();

    List<Affair> showAffairOnDate(String userid, String date);

    void addAffair(Affair affair);

    List<Document> showAllAffairs(String userid);

    void deleteAffair(String affairID);

    Document queryUserInfoByKey(String openid);

    void updateUserKey(String s1,String s2 );

    Document getAppconfig();

}
