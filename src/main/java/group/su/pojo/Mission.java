package group.su.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.SimpleDateFormat;
import java.util.*;

@Data
@Document("Mission")
public class Mission {
    @Transient
    private static int count = 1;
    @Id
    String missionID;
    int element;  // 任务属性
    Map<String, Integer> time;
    String place;
    String title;
    String description;
    Map<String, String> status;
    Map<String, String> statusChanger;
    Map<String, Integer> reporterNeeds;
    Map<String, List<String>> reporters;
    ArrayList<String> files;

    public void initializeMission() {
        this.status = new HashMap<String, String>() {{
            put("发布任务", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            System.out.println(element);
            if (element == 0) {
                put("接稿", "未达成");
                put("写稿", "未达成");
                put("编辑部审稿", "未达成");
            } else {
                put("接稿", "跳过");
                put("写稿", "跳过");
                put("编辑部审稿", "跳过");
            }
            put("辅导员审核", "未达成");
            put("排版", "未达成");
        }};
        this.statusChanger = new HashMap<String, String>() {{
            put("发布任务", null);
            put("写稿", null);
            put("编辑部审稿", null);
            put("辅导员审核", null);
            put("排版", null);
        }};
        reporters = new HashMap<>();
        for (String str : reporterNeeds.keySet()
        ) {
            reporters.put(str, new ArrayList<>());
        }
        // 设置任务号
        if (count < 10) {
            missionID = initDataCode() + "0" + count;
        } else {
            missionID = initDataCode() + count;
        }
        files = new ArrayList<>();
        // count累加
        count++;
        // 控制在两位数内
        if (count == 99) {
            count = 1;
        }
    }

    public String initDataCode() {
        String gap1 = "";
        String gap2 = "";
        if (time.get("month") < 10) {
            gap1 = "0";
        }
        if (time.get("day") < 10) {
            gap2 = "0";
        }
        return "" + time.get("year") + gap1 + time.get("month") + gap2 + time.get("day");
    }
}
