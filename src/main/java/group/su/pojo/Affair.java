package group.su.pojo;

import lombok.Data;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
public class Affair {
    String publisher;
    String affairID;
    String affairName;
    String place;
    String date;
    String beginTime;
    String endTime;
    List<String> involveUsers;

    public Affair() {
    }

    public Affair(String affairName, String place, String date, String beginTime, String endTime) {
        this.affairName = affairName;
        this.place = place;
        this.date = date;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }

    public Affair(Mission mission) {
        this.affairName = mission.getDescription();
        this.place = mission.getPlace();
        // 日期转成 yyyy-MM-dd
        this.date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(
                mission.getTime().get("year") - 1900,
                mission.getTime().get("month") - 1,
                mission.getTime().get("day")));
        // 时分转成 hh:mm
        this.beginTime = String.format("%02d:%02d",
                mission.getTime().get("beginHour"),
                mission.getTime().get("beginMinute"));
        this.endTime = String.format("%02d:%02d",
                mission.getTime().get("endHour"),
                mission.getTime().get("endMinute"));
        // 初始化列表
        this.involveUsers = new ArrayList<>();
        // 生成 id
        this.affairID = generateRandomID();
    }

    public String generateRandomID() {

        String CHARACTERS = "0123456789";
        int ID_LENGTH = 6;
        // 调整日期字符串格式
        Date date1;
        try {
            date1 = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(date1);
        // 根据输入的字符串生成 hashcode, 作为 seed
        int hashCode = beginTime.hashCode() + endTime.hashCode() + affairName.hashCode();
        System.out.println(endTime + "  " + endTime.hashCode());
        StringBuilder sb = new StringBuilder();
        Random random = new Random(hashCode);
        for (int i = 0; i < ID_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(index);
            sb.append(randomChar);
        }
        return yyyyMMdd + sb;
    }

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

    public static Affair changeToAffair(org.bson.Document document) {

        Affair affair = new Affair();
        try {
            document.remove("_id");
            for (String key : document.keySet()) {
                Field field = affair.getClass().getDeclaredField(key);
                field.setAccessible(true);
                field.set(affair, document.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return affair;
    }
}
