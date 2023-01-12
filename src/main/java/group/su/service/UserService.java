package group.su.service;

import group.su.pojo.Mission;
import org.bson.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

public interface UserService {

    Boolean tryLogin(String userid, String password);

    ArrayList<Mission> showAllMission();

    ArrayList<Mission> showNeedMission();

    ArrayList<Mission> showMissionById(String missionID);

    ArrayList<Mission> showTakenMission(String userid);

    void tryGetMission(String userid, String missionID, String kind);

    void saveFile(MultipartFile file, String missionID, String userid);

    ArrayList<String> showTag(String... str);

    ArrayList<Document> showLessons(String userid, Integer... week);

}
