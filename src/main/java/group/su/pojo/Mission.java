package group.su.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
@Document("Mission")
public class Mission {
    @Transient
    private static int count = 1;
    String missionID;
    int element;  // 任务属性
    Map<String, Integer> time;
    String place;
    String title;
    String description;
    Map<String, List<String>> tags = new HashMap<>();
    Map<String, String> status;
    Map<String, String> statusChanger;
    Map<String, Integer> reporterNeeds;
    Map<String, List<String>> reporters;
    ArrayList<String> files = new ArrayList<>();

    // 审核部分
    ArrayList<String> layoutFiles = new ArrayList<>();  // 排版文件
    Map<String, Integer> score = new HashMap<>();  // 分数
    ArrayList<String> draftTags = new ArrayList<>();  // 稿件的tag
    Map<String, String> comments = new HashMap<>(); // 评论
    Map<String, String> postscript = new HashMap<>(); // 备注
    String deadline;  // 下一任务的ddl

    String articleURL;


    public void initializeMission() {
        this.status = new HashMap<String, String>() {{
            put("发布任务", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            if (element == 0) {
                put("接稿", "未达成");
                put("写稿", "未达成");
                put("编辑部审稿", "未达成");
                put("辅导员审核", "未达成");
                put("排版", "未达成");
            } else if (element == 1) {
                put("接稿", "跳过");
                put("写稿", "跳过");
                put("编辑部审稿", "未达成");
                put("辅导员审核", "未达成");
                put("排版", "未达成");
            } else if (element == 2) {
                put("接稿", "跳过");
                put("写稿", "跳过");
                put("编辑部审稿", "跳过");
                put("辅导员审核", "未达成");
                put("排版", "未达成");
            } else if (element == 3) {
                put("接稿", "跳过");
                put("写稿", "跳过");
                put("编辑部审稿", "跳过");
                put("辅导员审核", "跳过");
                put("排版", "未达成");
            } else if (element == 4) {
                put("接稿", "跳过");
                put("写稿", "跳过");
                put("编辑部审稿", "跳过");
                put("辅导员审核", "跳过");
                put("排版", "跳过");
            }
        }};
        this.statusChanger = new HashMap<String, String>() {{
            put("发布任务", null);
            put("写稿", null);
            put("编辑部审稿", null);
            put("辅导员审核", null);
            put("排版", null);
        }};
        reporters = new HashMap<>();
        if (!(reporterNeeds == null)) {
            for (String str : reporterNeeds.keySet()
            ) {
                reporters.put(str, new ArrayList<>());
            }
        } else {
            reporterNeeds = new HashMap<>();
        }
        // 设置任务号
        if (count < 10) {
            missionID = initDataCode() + "0" + count;
        } else {
            missionID = initDataCode() + count;
        }
        // count累加
        count++;
        // 控制在两位数内
        if (count == 99) {
            count = 1;
        }
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
            doc.remove("count");
            doc.remove("publisher");
        }

        return doc;
    }

    public static Mission changeToMission(org.bson.Document document) {

        Mission mission = new Mission();
        try {
            document.remove("_id");
            for (String key : document.keySet()) {
                Field field = mission.getClass().getDeclaredField(key);
                field.setAccessible(true);
                field.set(mission, document.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mission;
    }

    public String initDataCode() {
        String gap1 = "";
        String gap2 = "";
        String gap3 = "";
        if (time == null) {
            Calendar calendar = Calendar.getInstance();
            time = new HashMap<>();
            time.put("year", calendar.get(Calendar.YEAR));
            time.put("month", calendar.get(Calendar.MONTH) + 1);
            time.put("day", calendar.get(Calendar.DATE));
            time.put("hour", calendar.get(Calendar.HOUR_OF_DAY));
            time.put("minute", calendar.get(Calendar.MINUTE));
        }
        if (time.get("month") < 10) {
            gap1 = "0";
        }
        if (time.get("day") < 10) {
            gap2 = "0";
        }
        if (time.get("hour") < 10) {
            gap3 = "0";
        }
        return "" + time.get("year") + gap1 + time.get("month") + gap2 +
                time.get("day") + gap3 + time.get("hour") + time.get("minute");
    }
}
