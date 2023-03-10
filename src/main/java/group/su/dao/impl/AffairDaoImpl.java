package group.su.dao.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import group.su.dao.AffairDao;
import group.su.dao.util.DataBaseUtil;
import group.su.pojo.Affair;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;

@Repository
public class AffairDaoImpl implements AffairDao {

    static MongoCollection<Document> affairCollection = DataBaseUtil.getMongoDB().getCollection("Affair");

    @Override
    public void addAffair(Affair affair) {

        Document document = affair.changeToDocument();
        affairCollection.insertOne(document);
    }

    @Override
    public void delete(String field, String value) {

        Bson filter = Filters.eq(field, value);
        affairCollection.deleteOne(filter);
    }

    @Override
    public FindIterable<Document> getAffairByInput(String field, String value) {

        Bson filter = Filters.eq(field, value);
        return affairCollection.find(filter);
    }

    @Override
    public FindIterable<Document> getAffairByContain(String field, String value) {

        Bson filter = Filters.in(field, value);
        return affairCollection.find(filter);
    }

    @Override
    public <T, K> void addToSetInAffair(String filterField, T filterValue, String updateField, K updateValue) {

        Bson filter = Filters.eq(filterField, filterValue);
        Bson update = Updates.addToSet(updateField, updateValue);
        affairCollection.updateOne(filter, update);
    }
}
