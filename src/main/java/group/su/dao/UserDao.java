package group.su.dao;

import com.mongodb.client.FindIterable;
import group.su.pojo.User;
import org.bson.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserDao {

    // 通过用户名查找用户

    <T> FindIterable<Document> searchUserByInputEqual(String field, T value);

    <T> FindIterable<Document> searchUserByInputContain(String field, T value);

    <T, K> void addToSetInUser(String filterField, T filterValue, String updateField, K updateValue);

    <T, K> void updateInUser(String filterField, T filterValue, String updateField, K updateValue);
}
