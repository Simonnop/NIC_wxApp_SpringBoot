package group.su.service;

import org.apache.commons.fileupload.FileItem;
import org.bson.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public interface UserService {

    Boolean tryLogin(String userid, String password);

    ArrayList<Document> showAllMission();

    ArrayList<Document> showNeedMission();

    ArrayList<Document> showMissionById(String missionID);

    ArrayList<Document> showMissionByTag(String tag1,String tag2);

    ArrayList<Document> showTakenMission(String field, String value);

    void tryGetMission(String userid, String missionID, String kind);

    void saveFile(MultipartFile file, String missionID, String userid);

    ArrayList<String> showTag(String... str);

    ArrayList<Document> showLessons(String userid, Integer... week);

    ArrayList<Document> showFinishedMission(String field, String value);

    ArrayList<Document> showMissionNeedLayout();
}
