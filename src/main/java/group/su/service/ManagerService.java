package group.su.service;

import group.su.pojo.Mission;
import org.bson.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Map;

public interface ManagerService {

    void addMission(Mission mission, String publisher);

    void deleteMission(String missionID);

    void alterMission(String missionID, Mission mission, String publisher);

    ArrayList<Document> showMissionGotDraft();

    ArrayList<String> findAvailableReporters(String missionID, Integer... intervals);

    Map<String, Integer> findAvailableTime(int week);

    void examineDraftByEditor(String missionID, String userid, String score,
                              String comment, String... tags);

    void saveLayoutFiles(MultipartFile file, String missionID, String userid);

    Map<String, ArrayList<Map<String, String>>> getTotalStuffGroupedByInput(String groupItem);

    Map<String, ArrayList<Document>> getTotalStuffSortedByInput(String sortItem);

    void thrashBack(String missionID);

    void uploadArticleURL(String missionID, String userid, String url);
}
