package group.su.dao.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import group.su.dao.UserDao;
import group.su.dao.util.DataBaseUtil;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class UserDaoImpl implements UserDao {

    @Resource
    private MongoTemplate mongoTemplate;

    // 获取集合
    MongoCollection<Document> userCollection = DataBaseUtil.getMongoDB().getCollection("User");

    @Override
    public <T> FindIterable<Document> searchUserByInputEqual(String field, T value) {
        // 指定查询过滤器
        Bson filter = Filters.eq(field, value);
        // 根据查询过滤器查询
        return userCollection.find(filter);
    }

    @Override
    public <T, K> void addToSetInUser(String filterField, T filterValue, String updateField, K updateValue) {
        // 集合中插入
        Bson filter = Filters.eq(filterField, filterValue);
        Bson update = Updates.addToSet(updateField, updateValue);

        userCollection.updateOne(filter, update);
    }


}
