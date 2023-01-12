package group.su.service.impl;

import com.mongodb.client.FindIterable;
import group.su.dao.*;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionKind;
import group.su.pojo.Mission;
import group.su.service.ManagerService;
import group.su.service.helper.MissionHelper;
import group.su.service.helper.TimeHelper;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
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
    final TimeHelper timeHelper;

    @Autowired
    UserDaoTest userDaoTest;

    @Autowired
    MissionDaoTest missionDaoTest;

    @Autowired
    public ManagerServiceImpl(UserDao userDao, MissionDao missionDao, ConfigDao configDao,
                              LessonDao lessonDao, MissionHelper missionManager, TimeHelper timeHelper) {
        this.userDao = userDao;
        this.missionDao = missionDao;
        this.configDao = configDao;
        this.lessonDao = lessonDao;
        this.missionManager = missionManager;
        this.timeHelper = timeHelper;
    }

    @Override
    public void addMission(Mission mission, String userid) {
        // 初始化任务id与状态
        mission.initializeMission();
        Map<String, String> statusChanger = mission.getStatusChanger();
        statusChanger.put("发布任务", userDaoTest.findByUserid(userid).getUsername());
        mission.setStatusChanger(statusChanger);
        // 添加任务
        missionDaoTest.insert(mission);
    }

    @Override
    public ArrayList<Mission> showMissionGotDraft() {

        List<Mission> missionList = missionDaoTest.findAll();
        if (missionList.isEmpty()) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        ArrayList<Mission> missionsWithLack = missionManager.changeFormAndCalculate(missionList);

        // 判断是否缺人
        missionsWithLack.removeIf(mission -> (mission
                .getStatus()
                .get("写稿")
                .equals("未达成")));

        return missionsWithLack;
    }

    @Override
    public ArrayList<String> findAvailableReporters(String missionID, Integer... intervals) {

        ArrayList<String> reportersList = new ArrayList<>();

        // 拿任务的时间
        Mission mission = missionDaoTest.findMissionByMissionID(missionID);
        if (mission == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        Map<String, Integer> time = mission.getTime();

        // 查询第几周星期几
        Integer[] weekDayByTime = timeHelper.getWeekDayByTime(time);
        // 查询夏令时或冬令时
        String season = timeHelper.getSeason(weekDayByTime[0]);
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
                    int[] classStartTime = timeHelper.changeTimeToInts(
                            singleLesson.get("startTime", String.class));
                    // 获取结束时间(时+分)
                    int[] classEndTime = timeHelper.changeTimeToInts(
                            singleLesson.get("endTime", String.class));
                    // 判断与任务时间是否冲突
                    boolean checkAvailable = timeHelper.checkAvailable(
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
