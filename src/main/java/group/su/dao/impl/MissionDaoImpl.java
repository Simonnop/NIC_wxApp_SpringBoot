package group.su.dao.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import group.su.dao.MissionDao;
import group.su.dao.util.DataBaseUtil;
import group.su.pojo.Mission;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;


@Repository
public class MissionDaoImpl implements MissionDao {

    // 获取集合
    static MongoCollection<Document> missionCollection = DataBaseUtil.getMongoDB().getCollection("Mission");

    @Override
    public void addMission(Mission mission) {
        Document document = mission.changeToDocument();
        missionCollection.insertOne(document);
    }


    @Override
    public FindIterable<Document> showAll() {
        // 查询全部
        return missionCollection.find();
    }

    @Override
    public <T> FindIterable<Document> searchMissionByInput(String field, T value) {
        // 按字段查询
        Bson filter = Filters.eq(field, value);
        return missionCollection.find(filter);
    }

    @Override
    public <T> FindIterable<Document> searchMissionByInput(String field1, T value1, String field2, T value2) {
        // 按字段查询
        Bson filter = Filters.and(Filters.eq(field1,value1),Filters.eq(field2,value2));
        return missionCollection.find(filter);
    }

    public <T, K> void addToSetInMission(String filterField, T filterValue, String updateField, K updateValue) {
        // 更新字段(插入)
        Bson filter = Filters.eq(filterField, filterValue);
        Bson update = Updates.addToSet(updateField, updateValue);
        missionCollection.updateOne(filter, update);
    }

    @Override
    public <T, K> void updateInMission(String filterField, T filterValue, String updateField, K updateValue) {
        // 更新字段(插入)
        Bson filter = Filters.eq(filterField, filterValue);
        Bson update = Updates.set(updateField, updateValue);
        missionCollection.updateOne(filter, update);
    }

    @Override
    public <T> void replaceMission(String filterField, T filterValue, Document document) {
        Bson filter = Filters.eq(filterField, filterValue);
        missionCollection.findOneAndReplace(filter, document);
    }

    @Override
    public <T> void deleteMissionByInput(String field, T value) {
        Bson filter = Filters.eq(field, value);
        missionCollection.deleteOne(filter);
    }
}
