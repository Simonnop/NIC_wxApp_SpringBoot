package group.su.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

}
