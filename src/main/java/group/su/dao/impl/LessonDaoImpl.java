package group.su.dao.impl;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import group.su.dao.LessonDao;
import group.su.dao.util.DataBaseUtil;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionKind;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;

@Repository
public class LessonDaoImpl implements LessonDao {

    // 获取集合
    static MongoCollection<Document> lessonDocument = DataBaseUtil.getMongoDB().getCollection("Lesson");

    @Override
    public void addLessonInfo(String userid, String lessonStr) {

        Document insertDoc = new Document();
        insertDoc.put("userid", userid);
        insertDoc.put("lessons", JSON.parseArray(lessonStr));

        lessonDocument.insertOne(insertDoc);
    }

    @Override
    public <T> ArrayList<Document> showLessonsByInput(String field, T value) {

        Document document = lessonDocument.find(Filters.eq(field, value)).first();
        if (document == null) {
            throw new AppRuntimeException(ExceptionKind.NO_LESSONS_INFO);
        }

        return (ArrayList<Document>) document.getList("lessons", Document.class);
    }

    @Override
    public FindIterable<Document> showAll() {
        return lessonDocument.find();
    }
}
