package group.su.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

@Data
@Document("User")
public class User {

    String userid;
    String username;
    String password;
    String classStr;
    String tel;
    String QQ;
    String identity;
    ArrayList<Integer> authorityLevel;
    ArrayList<String> missionTaken;
    ArrayList<Map<String, String>> missionCompleted;


    int gender;
    int month_performance;
    int total_performance;
    String department;
    int innerId;
    String head = "head_xiaotian.png";

//    以下为微信用户属性
    String openid;          //微信用户唯一标示
    String session_key;
//    String session_key;
//    String nickName;
//    String province;
//    String city;
//    String country;





    public org.bson.Document changeToDocument() {
        org.bson.Document doc = new org.bson.Document();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field :
                fields) {
            field.setAccessible(true);
            try {
                doc.put(field.getName(), field.get(this));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return doc;
    }

    public static User changeToUser(org.bson.Document document) {

        User user = new User();
        try {
            document.remove("_id");
            for (String key : document.keySet()) {
                Field field = user.getClass().getDeclaredField(key);
                field.setAccessible(true);
                field.set(user, document.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }



}
