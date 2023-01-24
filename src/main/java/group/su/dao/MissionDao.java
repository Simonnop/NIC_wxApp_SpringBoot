package group.su.dao;

import com.mongodb.client.FindIterable;
import group.su.pojo.Mission;
import org.bson.Document;

public interface MissionDao {

    // 添加任务
    void addMission(Mission mission);

    // 显示所有任务
    FindIterable<Document> showAll();

    <T> FindIterable<Document> searchMissionByInput(String field, T value);

    <T> FindIterable<Document> searchMissionByInput(String field1, T value1,String field2, T value2);

    <T, K> void addToSetInMission(String filterField, T filterValue, String updateField, K updateValue);

    <T, K> void updateInMission(String filterField, T filterValue, String updateField, K updateValue);

    <T> void replaceMission(String filterField, T filterValue, Document document);
}
