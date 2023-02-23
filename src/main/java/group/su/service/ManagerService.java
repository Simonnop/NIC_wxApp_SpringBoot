package group.su.service;

import group.su.pojo.Mission;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Map;

public interface ManagerService {

    void addMission(Mission mission, String publisher);

    void deleteMission(String missionID);

    void alterMission(String missionID, Mission mission, String publisher);

    ArrayList<Document> showMissionGotDraft();

    ArrayList<String> findAvailableReporters(String missionID, Integer... intervals);

    Map<String, Integer> findAvailableTime(int week);

    void examineDraft(String missionID, String userid, String score,
                      String comment, String postscript, String ddl, String... tags);

    Map<String, ArrayList<Map<String, String>>> getTotalStuffByDepartment();
}
