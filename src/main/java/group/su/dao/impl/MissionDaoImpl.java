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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class MissionDaoImpl implements MissionDao {

    @Resource
    private MongoTemplate mongoTemplate;

    // 获取集合
    MongoCollection<Document> missionCollection = mongoTemplate.getCollection("Mission");

    @Override
    public void addMission(Mission mission) {
        mongoTemplate.save(mission);
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
}
