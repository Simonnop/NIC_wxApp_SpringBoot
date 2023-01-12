package group.su.service;

import group.su.pojo.Mission;
import org.bson.Document;

import java.util.ArrayList;

public interface ManagerService {

    void addMission(Mission mission, String publisher);

    ArrayList<Document> showMissionGotDraft();

    ArrayList<String> findAvailableReporters(String missionID, Integer... intervals);

}
