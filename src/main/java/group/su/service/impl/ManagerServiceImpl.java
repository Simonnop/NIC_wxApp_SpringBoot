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
import org.omg.CORBA.INTERNAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
    public void deleteMission(String missionID) {

        missionDao.deleteMissionByInput("missionID", missionID);
    }

    @Override
    public void alterMission(String missionID, Mission mission, String publisher) {
        mission.initializeMission();
        Map<String, String> statusChanger = mission.getStatusChanger();
        statusChanger.put("发布任务", (String) userDao
                .searchUserByInputEqual("userid", publisher).first()
                .get("username"));
        mission.setStatusChanger(statusChanger);
        mission.setMissionID(missionID);

        // 添加任务
        missionDao.replaceMission("missionID", mission.getMissionID(), mission.changeToDocument());
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
                .equals("未达成")
                ||!((Document) document
                .get("status"))
                .get("编辑部审稿")
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

    @Override
    public Map<String, Integer> findAvailableTime(int week) {
        Map<String, Integer> map = new HashMap<>();
        // 看一天有多少节课
        int size = configDao.showItemByInput("item", "timetable")
                .first()
                .getList(TimeUtil.getSeason(week), Document.class)
                .size();
        // 初始化 map
        for (int day = 0; day < 7; day++) {
            for (int i = 0; i < size; i++) {
                map.put((day + 1) + "-" + (i + 1), 0);
            }
        }
        // 遍历每个人的课表
        for (Document document : lessonDao.showAll()) {
            // 看每一天的课
            for (int day = 0; day < 7; day++) {
                List<Document> lessonOfDay = document.getList("lessons", Document.class)
                        .get(week - 1)
                        .getList("time", Document.class)
                        .get(day)
                        .getList("lesson", Document.class);
                for (Document lessons : lessonOfDay) {
                    // 获取课程是第几节
                    String[] times = ((String) lessons.get("time")).split("-");
                    for (int i = Integer.parseInt(times[0]); i <= Integer.parseInt(times[1]); i++) {
                        String key = (day + 1) + "-" + i;
                        map.put(key, map.get(key) + 1);
                    }
                }
            }
        }
        return map;
    }

    @Override
    public void examineDraftByEditor(String missionID, String userid, String score,
                                     String comment,  String... tags) {
        Document missionDoc = missionDao.searchMissionByInput("missionID", missionID).first();
        Mission mission = Mission.changeToMission(missionDoc);
        /*// 判断日期
        Date oldDate;
        Date newDate;
        try {
            oldDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(mission.getDeadline());
            newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(ddl);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        if (newDate.compareTo(oldDate) < 0) {
            mission.setDeadline(ddl);
        }
        mission.setDeadline(ddl);*/
        mission.getStatusChanger().put("编辑部审稿",
                userDao.searchUserByInputEqual("userid", userid)
                        .first()
                        .get("username", String.class));

        mission.getComments().put(userid, comment);
        mission.getDraftTags().addAll(Arrays.asList(tags));
        mission.getScore().put(userid, Integer.valueOf(score));
        mission.getStatus().put("编辑部审稿",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        Document document = mission.changeToDocument();
        missionDao.replaceMission("missionID", missionID, document);
    }

    @Override
    public void saveLayoutFiles(MultipartFile file, String missionID, String userid) {

        String fileName = missionID+"_layout_"+file.getOriginalFilename(); //获取上传文件原来的名称
        String filePath = "C:\\ProgramData\\NIC\\work_files";
        File temp = new File(filePath);
        if (!temp.exists()) {
            temp.mkdirs();
        }

        File localFile = new File(filePath + File.separator + fileName);
        try {
            file.transferTo(localFile); //把上传的文件保存至本地
        } catch (IOException e) {
            throw new AppRuntimeException(ExceptionKind.SAME_FILE_ERROR);
        }
        new Thread(() -> {
            // 将文件名保存到对应的任务下
            missionDao.addToSetInMission(
                    "missionID", missionID,
                    "layoutFiles", fileName);

        });
    }

    @Override
    public Map<String, ArrayList<Document>> getTotalStuffSortedByInput(String sortItem) {

        FindIterable<Document> documents = userDao.searchAllUsers();
        Comparator cmp;
        String[] fields = {"username", "classStr", "tel", "QQ", "userid", "department"};
        Class clazz;

        switch (sortItem) {
            case "username":
                cmp = Collator.getInstance(Locale.CHINA);
                clazz = String.class;
                break;
            case "innerId":
                cmp = new Comparator<String>() {
                    @Override
                    public int compare(String code1, String code2) {
                        return Integer.valueOf(code1).compareTo(Integer.valueOf(code2));
                    }
                };
                clazz = Integer.class;
                break;
            default:
                throw new AppRuntimeException(ExceptionKind.NO_SORT_KEY);
        }

        Map<String, ArrayList<Document>> NameIdMap = new TreeMap<String, ArrayList<Document>>(cmp) {{
            for (Document document : documents) {

                String item = String.valueOf(document.get(sortItem, clazz));

                if (!containsKey(item)) {
                    put(item, new ArrayList<>());
                }
                Document info = new Document() {{
                    for (String field : fields
                    ) {
                        put(field, document.get(field));
                    }
                }};
                get(item).add(info);
            }
        }};

        return NameIdMap;
    }

    @Override
    public void thrashBack(String missionID) {
        Document document = missionDao.searchMissionByInput("missionID", missionID).first();
        if (document == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        Map<String, String> status = document.get("status", Map.class);
        String undo = "未达成";
        if (status.get("编辑部审稿").equals(undo)) {
            missionDao.updateInMission(
                    "missionID", missionID,
                    "status.写稿","未达成");
        } else if (status.get("辅导员审核").equals(undo)) {
            missionDao.updateInMission(
                    "missionID", missionID,
                    "status.编辑部审稿","未达成");
        }
    }

    @Override
    public void uploadArticleURL(String missionID, String userid, String url) {

        Document document = missionDao.searchMissionByInput("missionID", missionID).first();
        if (document == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        document.put("articleURL", url);
        document.get("statusChanger", Document.class).put("排版",
                userDao.searchUserByInputEqual("userid", userid)
                        .first()
                        .get("username", String.class));
        document.get("status", Document.class).put("排版",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    @Override
    public Map<String, ArrayList<Map<String, String>>> getTotalStuffGroupedByInput(String groupItem) {

        FindIterable<Document> documents = userDao.searchAllUsers();

        Map<String, ArrayList<Map<String, String>>> map = new HashMap<>();

        for (Document document : documents
        ) {
            String item = document.get(groupItem, String.class);
            if (!map.containsKey(item)) {
                map.put(item, new ArrayList<>());
            }
            map.get(item).add(new HashMap<String, String>() {{
                put("username", document.get("username", String.class));
                put("userid", document.get("userid", String.class));
                put("class", document.get("classStr", String.class));
                put("identity", document.get("identity", String.class));
                put("department", document.get("department", String.class));
            }});
        }

        return map;
    }
}
