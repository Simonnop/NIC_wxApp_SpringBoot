package group.su.dao.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import group.su.dao.ConfigDao;
import group.su.dao.util.DataBaseUtil;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class ConfigDaoImpl implements ConfigDao {

    // 获取集合
    static MongoCollection<Document> ConfigCollection = DataBaseUtil.getMongoDB().getCollection("Config");

    @Override
    public <T> FindIterable<Document> showItemByInput(String field, T value) {
        // 指定查询过滤器
        Bson filter = Filters.eq(field, value);
        // 根据查询过滤器查询
        return ConfigCollection.find(filter);
    }
}
