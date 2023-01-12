package group.su.service.impl;

import com.mongodb.client.FindIterable;
import group.su.dao.ConfigDao;
import group.su.dao.LessonDao;
import group.su.dao.MissionDao;
import group.su.dao.UserDao;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionKind;
import group.su.pojo.Mission;
import group.su.service.ManagerService;
import group.su.service.helper.MissionHelper;
import group.su.service.util.TimeUtil;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ManagerServiceImpl implements ManagerService {

    final UserDao userDao;
    final MissionDao missionDao;
    final ConfigDao configDao;
    final LessonDao lessonDao;
    final MissionHelper missionManager;

    @Autowired
    public ManagerServiceImpl(UserDao userDao, MissionDao missionDao, ConfigDao configDao,
                           LessonDao lessonDao, MissionHelper missionManager) {
        this.userDao = userDao;
        this.missionDao = missionDao;
        this.configDao = configDao;
        this.lessonDao = lessonDao;
        this.missionManager = missionManager;
    }

    @Override
    public void addMission(Mission mission, String userid) {
        // 初始化任务id与状态
        mission.initializeMission();
        Map<String, String> statusChanger = mission.getStatusChanger();
        statusChanger.put("发布任务", (String) userDao
                        .searchUserByInputEqual("userid", userid).first()
                        .get("username"));
        mission.setStatusChanger(statusChanger);
        // 添加任务
        missionDao.addMission(mission);
    }

    @Override
    public ArrayList<Document> showMissionGotDraft() {

        FindIterable<Document> documents = missionDao.showAll();
        if (documents.first() == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        ArrayList<Document> documentArrayList = missionManager.changeFormAndCalculate(documents);

        // 判断是否缺人
        documentArrayList.removeIf(document -> ((Document) document
                .get("status"))
                .get("写稿")
                .equals("未达成"));
        return documentArrayList;
    }

    @Override
    public ArrayList<String> findAvailableReporters(String missionID, Integer... intervals) {

        ArrayList<String> reportersList = new ArrayList<>();

        // 拿任务的时间
        Document mission = missionDao.searchMissionByInput("missionID", missionID).first();
        if (mission == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        Map<String, Integer> time = (Map<String, Integer>) mission.get("time");

        // 查询第几周星期几
        Integer[] weekDayByTime = TimeUtil.getWeekDayByTime(time);
        // 查询夏令时或冬令时
        String season = TimeUtil.getSeason(weekDayByTime[0]);
        // 拿时间表
        Document timetable = configDao.showItemByInput("item", "timetable").first();

        loop:
        // 遍历每个人当天的课表
        for (Document document : lessonDao.showAll()) {
            // 拿当天的课程
            List<Document> lessonOfDay = document.getList("lessons", Document.class)
                    .get(weekDayByTime[0] - 1)
                    .getList("time", Document.class)
                    .get(weekDayByTime[1] - 1)
                    .getList("lesson", Document.class);
            System.out.println(lessonOfDay);
            // 没课,加入
            if (lessonOfDay == null) {
                reportersList.add((String) document.get("userid"));
                continue;
            }
            // 遍历所有的课
            for (Document lessons : lessonOfDay) {
                // 获取课程是第几节
                String[] times = ((String) lessons.get("time")).split("-");
                for (int i = Integer.parseInt(times[0]); i <= Integer.parseInt(times[1]); i++) {
                    // 从时间表拿对应课的时间
                    Document singleLesson = timetable.getList(season, Document.class).get(i - 1);
                    // 获取开始时间(时+分)
                    int[] classStartTime = TimeUtil.changeTimeToInts(
                            singleLesson.get("startTime", String.class));
                    // 获取结束时间(时+分)
                    int[] classEndTime = TimeUtil.changeTimeToInts(
                            singleLesson.get("endTime", String.class));
                    // 判断与任务时间是否冲突
                    boolean checkAvailable = TimeUtil.checkAvailable(
                            new int[]{time.get("beginHour"), time.get("beginMinute")},
                            new int[]{time.get("endHour"), time.get("endMinute")},
                            classStartTime, classEndTime
                    );
                    // 冲突,则换下一个人
                    if (!checkAvailable) {
                        continue loop;
                    }
                }
            }
            // 遍历所有课程都没问题,加入
            reportersList.add((String) document.get("userid"));
        }

        return reportersList;
    }
}
