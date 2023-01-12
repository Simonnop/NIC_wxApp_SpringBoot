package group.su.service.helper;

import com.alibaba.fastjson.JSONObject;
import group.su.dao.impl.ConfigDaoImpl;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Service
public class TimeHelper {

    private final ConfigDaoImpl configDao;
    static Document firstDay;
    static Document timetable;
    boolean init = false;

    @Autowired
    public TimeHelper(ConfigDaoImpl configDao) {
        this.configDao = configDao;
    }

    private void initData(){
        if (init) {
            return;
        }
        timetable = configDao.showItemByInput("item", "timetable").first();
        firstDay = (Document) configDao.showItemByInput("item", "timetable").first().get("firstDay");
        init = true;
    }

    public int[] changeTimeToInts(String time) {

        String[] strings = time.split(":");
        int[] HourWithMinute = new int[2];
        for (int i = 0; i < strings.length; i++) {
            HourWithMinute[i] = Integer.parseInt(strings[i]);
        }
        return HourWithMinute;
    }

    public Integer[] getWeekDayByTime(Map<String, Integer> time) {
        // 根据年月日获取周数与星期
        initData();

        Calendar beginCalendar = Calendar.getInstance();
        beginCalendar.set(
                (Integer) firstDay.get("year"),
                (Integer) firstDay.get("month") - 1,
                (Integer) firstDay.get("day"));
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(
                time.get("year"),
                time.get("month") - 1,
                time.get("day"));

        int week = 1;
        int count = 0;
        while (beginCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)
               || beginCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH)
               || beginCalendar.get(Calendar.DATE) != currentCalendar.get(Calendar.DATE)
        ) {
            count++;
            beginCalendar.add(Calendar.DATE, 1);
            if (count == 7) {
                week++;
                count = 0;
            }
        }

        int weekDay = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (weekDay == 0) {
            weekDay = 7;
        }

        return new Integer[]{week, weekDay};
    }

    public boolean checkAvailable(int[] missionStartTime, int[] missionEndTime,
                                  int[] classStartTime, int[] classEndTime) {
        // 判断课程时间与任务时间的关系
        // 以下为时间线示意图
        // 任务: ......000000000000000000000000000.......
        // 课程: ...00000000.............................
        if (classEndTime[0] > missionStartTime[0] && classStartTime[0] < missionStartTime[0]) {
            return false;
        } else if (classEndTime[0] == missionStartTime[0] && classEndTime[1] > missionStartTime[1]) {
            return false;
        }
        // 任务: ......000000000000000000000000000.......
        // 课程: .............................00000000...
        if (missionEndTime[0] > classStartTime[0] && missionEndTime[0] < classEndTime[0]) {
            return false;
        } else if (missionEndTime[0] == classStartTime[0] && missionEndTime[1] > classStartTime[1]) {
            return false;
        }
        // 任务: ......000000000000000000000000000.......
        // 课程: ...............00000000.................
        if (missionEndTime[0] > classEndTime[0] && missionStartTime[0] < classStartTime[0]) {
            return false;
        }
        // 全部无冲突
        return true;
    }

    public String getSeason(Integer week) {
        initData();

        // 根据周数获取夏冬令时
        int winterWeek = (int) timetable.get("winterBegin");
        int summerWeek = (int) timetable.get("summerBegin");

        if (winterWeek > summerWeek) {
            if (week < winterWeek) {
                return "summer";
            } else {
                return "winter";
            }
        } else {
            if (week < summerWeek) {
                return "winter";
            } else {
                return "summer";
            }
        }
    }

    public JSONObject getCurrentWeekInfo() {
        // 获取当前是第几周星期几
        Integer[] weekDayByTime = getWeekDayByTime(new HashMap<String, Integer>() {{
            Calendar calendar = Calendar.getInstance();
            put("year", calendar.get(Calendar.YEAR));
            put("month", calendar.get(Calendar.MONTH) + 1);
            put("day", calendar.get(Calendar.DATE));
        }});
        return new JSONObject() {{
            put("week", weekDayByTime[0]);
            put("weekDay", weekDayByTime[1]);
        }};
    }

}
