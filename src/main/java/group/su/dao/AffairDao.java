package group.su.dao;

import com.mongodb.client.FindIterable;
import group.su.pojo.Affair;
import org.bson.Document;

public interface AffairDao {

    void addAffair(Affair affair);

    void delete(String field, String value);

    FindIterable<Document> getAffairByInput(String field, String value);

    FindIterable<Document> getAffairByContain(String field, String value);

    <T, K> void addToSetInAffair(String filterField, T filterValue, String updateField, K updateValue);
}
