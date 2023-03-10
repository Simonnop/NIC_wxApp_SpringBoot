package group.su.service.impl;

import com.mongodb.client.FindIterable;
import group.su.dao.*;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionKind;
import group.su.pojo.Affair;
import group.su.pojo.Mission;
import group.su.service.UserService;
import group.su.service.helper.MissionHelper;
import group.su.service.util.TimeUtil;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    final UserDao userDao;
    final MissionDao missionDao;
    final ConfigDao configDao;
    final LessonDao lessonDao;
    final MissionHelper missionManager;
    final AffairDao affairDao;

    @Autowired
    public UserServiceImpl(UserDao userDao, MissionDao missionDao, ConfigDao configDao,
                           LessonDao lessonDao, MissionHelper missionManager, AffairDao affairDao) {
        this.userDao = userDao;
        this.missionDao = missionDao;
        this.configDao = configDao;
        this.lessonDao = lessonDao;
        this.missionManager = missionManager;
        this.affairDao = affairDao;
    }

    @Override
    public Boolean tryLogin(String userid, String password) {

        Document user = userDao.searchUserByInputEqual("userid", userid).first();
        if (user == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        return password.equals(user.get("password"));
    }

    @Override
    public ArrayList<Document> showAllMission() {

        FindIterable<Document> documents = missionDao.showAll();
        if (documents.first() == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        return missionManager.changeFormAndCalculate(documents);
    }

    @Override
    public ArrayList<Document> showNeedMission() {

        ArrayList<Document> documentArrayList = new ArrayList<>();

        FindIterable<Document> documents = missionDao.showAll();
        if (documents.first() == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        // ??????????????????
        for (Document document : missionManager.changeFormAndCalculate(documents)) {
            if (((Document) document.get("status"))
                    .get("??????")
                    .equals("?????????")) {
                documentArrayList.add(document);
            }
        }
        return documentArrayList;

    }

    @Override
    public ArrayList<Document> showMissionById(String missionID) {

        FindIterable<Document> documents = missionDao.searchMissionByInput("missionID", missionID);
        if (documents.first() == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        return missionManager.changeFormAndCalculate(documents);

    }

    @Override
    public ArrayList<Document> showMissionByTag(String tag1, String tag2) {
        FindIterable<Document> documents;
        if (tag2 == null) {
            documents = missionDao.searchMissionByInput("tag1", tag1);
        } else {
            documents = missionDao.searchMissionByInput("tag1", tag1, "tag2", tag2);
        }
        return missionManager.changeFormAndCalculate(documents);
    }

    @Override
    public ArrayList<Document> showTakenMission(String field, String value) {

        ArrayList<Document> documentArrayList = new ArrayList<>();

        Document userInfo = userDao.searchUserByInputEqual(field, value).first();
        if (userInfo == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        for (String missionID : userInfo.getList("missionTaken", String.class)) {
            Document document = missionDao.searchMissionByInput("missionID", missionID).first();
            if (document == null) {
                continue;
            }
            if (((Document) document.get("status"))
                    .get("??????")
                    .equals("?????????")
                    ||((Document) document.get("status"))
                    .get("??????")
                    .equals("?????????")) {
                documentArrayList.add(missionManager.calculateLack(document));
            }
        }
        return documentArrayList;
    }

    @Override
    public void tryGetMission(String userid, String missionID, String kind) {

        // ??????????????????: ????????? username ??????????????????
        boolean success = true;
        Document document;
        try {
            synchronized (missionDao) {
                document = missionDao.searchMissionByInput("missionID", missionID).first();
                // ????????????
                if (document == null) {
                    throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
                }
                missionManager.calculateLack(document);
                // ???????????????
                if (((Document) document.get("reporterLack"))
                        .get(kind)
                        .equals(0)) {
                    throw new AppRuntimeException(ExceptionKind.ENOUGH_PEOPLE);
                }
                // ???????????????
                if (((ArrayList<?>) ((Document) document.get("reporters"))
                        .get(kind))
                        .contains(userid)) {
                    throw new AppRuntimeException(ExceptionKind.ALREADY_PARTICIPATE);
                }
                missionDao.addToSetInMission("missionID", missionID, "reporters." + kind, userid);
            }
        } catch (Exception e) {
            success = false;
            throw e;
        } finally {
            if (success) {
                // ?????????????????????????????????
                new Thread(() -> userDao.addToSetInUser(
                        "userid", userid, "missionTaken", missionID))
                        .start();
                // ??????????????????
                new Thread(() -> missionManager.updateMissionStatus(missionID)).start();
                // ????????????
                new Thread(() -> {
                    Mission mission = Mission.changeToMission(
                            missionDao.searchMissionByInput("missionID", missionID)
                                    .first());
                    Affair affair = new Affair(mission);
                    affairDao.addToSetInAffair("affairID", affair.getAffairID(),
                            "involveUsers", userid);
                });
            }
        }
    }

    @Override
    public void saveFile(MultipartFile file, String missionID, String userid) {

        String fileName = missionID + "_" + file.getOriginalFilename(); //?????????????????????????????????
        String filePath = "C:\\ProgramData\\NIC\\work_files";
        File temp = new File(filePath);
        if (!temp.exists()) {
            temp.mkdirs();
        }

        File localFile = new File(filePath + File.separator + fileName);
        try {
            file.transferTo(localFile); //?????????????????????????????????
        } catch (IOException e) {
            throw new AppRuntimeException(ExceptionKind.SAME_FILE_ERROR);
        }
        new Thread(() -> {
            // ???????????????????????????????????????
            missionDao.addToSetInMission(
                    "missionID", missionID,
                    "files", fileName);
            // ????????????????????????????????????
            Integer element = missionDao.searchMissionByInput("missionID", missionID)
                    .first()
                    .get("element", Integer.class);
            if (element != 0) {
                return;
            }
            // ??????????????????
            missionDao.updateInMission(
                    "missionID", missionID,
                    "status.??????",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            // ?????????????????????
            missionDao.updateInMission(
                    "missionID", missionID,
                    "statusChanger.??????", userid);
            // ??? missionID ?????? user ??? missionCompleted ???
                        /*for (Document document : userDao.searchUserByInputContain("missionTaken", missionID)) {
                            userDao.addToSetInUser(
                                    "userid", document.get("userid"),
                                    "missionCompleted", missionID);
                        }*/
        }).start();
    }

    @Override
    public Map<String, String[][]> showTag() {
        Document document = configDao.showItemByInput("item", "tag").first();

        String[] firstLayers = new ArrayList<>(document.getList("firstLayer", String.class)).toArray(new String[0]);
        String[][] items = new String[firstLayers.length][];

        for (int i = 0; i < firstLayers.length; i++) {
            items[i] = document.getList(firstLayers[i], String.class).toArray(new String[0]);
        }

        return new HashMap<String, String[][]>() {{
            put("list1", new String[][]{firstLayers});
            put("list2", items);
        }};
    }

    @Override
    public ArrayList<Document> showLessons(String userid, Integer... week) {

        ArrayList<Document> documents = lessonDao.showLessonsByInput("userid", userid);

        for (Document document : documents) {
            document.put("season", TimeUtil.getSeason((Integer) document.get("week")));
        }

        return documents;
    }

    @Override
    public ArrayList<Document> showFinishedMission(String field, String value) {

        ArrayList<Document> documentArrayList = new ArrayList<>();

        Document userInfo = userDao.searchUserByInputEqual(field, value).first();
        if (userInfo == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        for (String missionID : userInfo.getList("missionTaken", String.class)) {
            Document document = missionDao.searchMissionByInput("missionID", missionID).first();
            if (document == null) {
                continue;
            }
            if (!(document.get("status", Document.class))
                    .get("??????")
                    .equals("?????????")) {
                documentArrayList.add(missionManager.calculateLack(document));
            }
        }
        return documentArrayList;
    }

    @Override
    public ArrayList<Document> showMissionNeedLayout() {

        ArrayList<Document> documentArrayList = new ArrayList<>();

        FindIterable<Document> documents = missionDao.showAll();
        if (documents.first() == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        for (Document document : documents) {
            if (((Document) document.get("status"))
                    .get("??????")
                    .equals("?????????")
                    && !((Document) document.get("status"))
                    .get("???????????????")
                    .equals("?????????")) {
                documentArrayList.add(document);
            }
        }
        return documentArrayList;
    }

    @Override
    public List<Affair> showAffairOnDate(String userid, String date) {

        Comparator<Affair> cmp = new Comparator<Affair>() {
            @Override
            public int compare(Affair a1, Affair a2) {
                return a1.getBeginTime().compareTo(a2.getBeginTime());
            }
        };

        List<Affair> todayAffairs = new ArrayList<Affair>(){{
            affairDao.getAffairByInput("userid", userid).forEach(
                    document -> add(Affair.changeToAffair(document))
            );
            affairDao.getAffairByContain("involveUsers", userid).forEach(
                    document -> add(Affair.changeToAffair(document))
            );
        }}.stream().distinct().collect(Collectors.toList()); // ??????

        Integer[] weekDayByTime = TimeUtil.getWeekDayByTime(new HashMap<String, Integer>() {{
            String[] split = date.split("-");
            String[] keys = {"year", "month", "day"};
            for (int i = 0; i < keys.length; i++) {
                put(keys[i], Integer.valueOf(split[i]));
            }
        }});

        int week = weekDayByTime[0];
        int day = weekDayByTime[1];
        // System.out.println(week + " " + day);

        Optional<Document> weekLesson = lessonDao.showLessonsByInput("userid", userid).stream()
                .filter(document -> document.get("week", Integer.class) == week)
                .findFirst();
        if (!weekLesson.isPresent()) {
            return null;
        }

        Optional<Document> dayLesson = weekLesson.get()
                .getList("time", Document.class).stream()
                .filter(document1 -> document1.get("weekday", Integer.class) == day)
                .findFirst();
        if (!dayLesson.isPresent()) {
            return null;
        }
        // ?????????????????????
        List<Document> lessonList = dayLesson.get().getList("lesson", Document.class);

        // ???????????????
        Map<Integer, List<Document>> node = TimeUtil.timetable
                .getList(TimeUtil.getSeason(week), Document.class)
                .stream()
                .collect(Collectors.groupingBy(document -> document.get("node", Integer.class)));
        // {1=[Document{{node=1, startTime=08:00, endTime=08:45}}], 2=[Document{{node=2, startTime=08:55, endTime=09:40}}],

        for (Document lesson : lessonList) {
            String[] times = lesson.get("time", String.class).split("-");
            String beginTime = node.get(Integer.valueOf(times[0])).get(0)
                    .get("startTime", String.class);
            String endTime = node.get(Integer.valueOf(times[1])).get(0)
                    .get("endTime", String.class);
            todayAffairs.add(new Affair(
                    lesson.get("name", String.class),
                    lesson.get("location", String.class),
                    date, beginTime, endTime
            ));
        }

        return todayAffairs.stream()
                .sorted(cmp)
                .collect(Collectors.toList());
    }

    @Override
    public void addAffair(Affair affair) {
        affairDao.addAffair(affair);
    }

    @Override
    public List<Document> showAllAffairs(String userid) {
        return new ArrayList<Document>(){{
            affairDao.getAffairByInput("userid", userid).forEach(this::add);
            affairDao.getAffairByContain("involveUsers", userid).forEach(this::add);
        }}.stream().distinct().collect(Collectors.toList()); // ??????
    }

    @Override
    public void deleteAffair(String affairID) {
        affairDao.delete("affairID", affairID);
    }

    //??????openid?????? user????????????
    public Document queryUserInfoByKey(String openid){
        Document userInfo = userDao.searchUserByInputEqual("openid",openid).first();
        return userInfo;
    }
    //??????
    public void updateUserKey(String s1,String s2 ){
        userDao.updateInUser(s1,s1,s2, s2);
    }

    public Document getAppconfig(){
        return configDao.showItemByInput("appname", "lyp").first();
    }
}
