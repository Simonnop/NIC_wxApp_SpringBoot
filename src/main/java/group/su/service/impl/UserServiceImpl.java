package group.su.service.impl;

import group.su.dao.*;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionKind;
import group.su.pojo.Mission;
import group.su.pojo.User;
import group.su.service.UserService;
import group.su.service.helper.MissionHelper;
import group.su.service.helper.TimeHelper;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    final UserDao userDao;
    final MissionDao missionDao;
    final ConfigDao configDao;
    final LessonDao lessonDao;
    final MissionHelper missionManager;
    final TimeHelper timeHelper;

    @Autowired
    MissionDaoTest missionDaoTest;

    @Autowired
    UserDaoTest userDaoTest;

    @Autowired
    public UserServiceImpl(UserDao userDao, MissionDao missionDao, ConfigDao configDao,
                           LessonDao lessonDao, MissionHelper missionManager,TimeHelper timeHelper) {
        this.userDao = userDao;
        this.missionDao = missionDao;
        this.configDao = configDao;
        this.lessonDao = lessonDao;
        this.missionManager = missionManager;
        this.timeHelper = timeHelper;
    }

    @Override
    public Boolean tryLogin(String userid, String password) {

        User user = userDaoTest.findByUserid(userid);
        if (user == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        return password.equals(user.getPassword());
    }

    @Override
    public ArrayList<Mission> showAllMission() {

        List<Mission> missionList = missionDaoTest.findAll();
        if (missionList.isEmpty()) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        return missionManager.changeFormAndCalculate(missionList);
    }

    @Override
    public ArrayList<Mission> showNeedMission() {

        ArrayList<Mission> documentArrayList = new ArrayList<>();

        List<Mission> missionList = missionDaoTest.findAll();
        if (missionList.isEmpty()) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        // 判断是否缺人
        for (Mission mission : missionManager.changeFormAndCalculate(missionList)) {
            if (mission.getStatus()
                    .get("接稿")
                    .equals("未达成")) {
                documentArrayList.add(mission);
            }
        }
        return documentArrayList;
    }

    @Override
    public ArrayList<Mission> showMissionById(String missionID) {

        List<Mission> missionList = missionDaoTest.findAll();
        if (missionList.isEmpty()) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        return missionManager.changeFormAndCalculate(missionList);
    }

    @Override
    public ArrayList<Mission> showTakenMission(String userid) {

        ArrayList<Mission> documentArrayList = new ArrayList<>();

        User user = userDaoTest.findByUserid(userid);
        if (user == null) {
            throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
        }
        for (String missionID : user.getMissionTaken()) {
            Mission mission = missionDaoTest.findMissionByMissionID(missionID);
            if (mission == null) {
                continue;
            }
            if ((mission.getStatus())
                    .get("写稿")
                    .equals("未达成")) {
                documentArrayList.add(missionManager.calculateLack(mission));
                System.out.println(missionID);
            }
        }
        return documentArrayList;
    }

    @Override
    public void tryGetMission(String userid, String missionID, String kind) {

        boolean success = true;
        try {
            synchronized (missionDaoTest) {
                Mission mission = missionDaoTest.findMissionByMissionID(missionID);
                // 验证非空
                if (mission == null) {
                    throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
                }
                missionManager.calculateLack(mission);
                // 验证非满员
                if (mission.getReporterLack()
                        .get(kind)
                        .equals(0)) {
                    throw new AppRuntimeException(ExceptionKind.ENOUGH_PEOPLE);
                }
                // 验证未参与
                if ((mission.getReporters()
                        .get(kind))
                        .contains(userid)) {
                    throw new AppRuntimeException(ExceptionKind.ALREADY_PARTICIPATE);
                }
                Map<String, List<String>> reporters = mission.getReporters();
                reporters.get(kind).add(userid);
                mission.setReporters(reporters);
                missionDaoTest.save(mission);
            }
        } catch (Exception e) {
            success = false;
            throw e;
        } finally {
            if (success) {
                new Thread(() -> userDao.addToSetInUser(
                        "userid", userid, "missionTaken", missionID))
                        .start();
                new Thread(() -> missionManager.updateMissionStatus(missionID)).start();
            }
        }
    }

    @Override
    public void saveFile(MultipartFile file, String missionID, String userid) {

        String fileName = missionID+"_"+file.getOriginalFilename(); //获取上传文件原来的名称
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
                    "files", fileName);
            // 任务写稿完成
            missionDao.updateInMission(
                    "missionID", missionID,
                    "status.写稿",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            // 存储更改者姓名
            missionDao.updateInMission(
                    "missionID", missionID,
                    "statusChanger.写稿",
                    userDao.searchUserByInputEqual("userid",userid)
                            .first()
                            .get("username"));
            // 将 missionID 加入 user 的 missionCompleted 下
                        /*for (Document document : userDao.searchUserByInputContain("missionTaken", missionID)) {
                            userDao.addToSetInUser(
                                    "userid", document.get("userid"),
                                    "missionCompleted", missionID);
                        }*/
        }).start();
    }

    @Override
    public ArrayList<String> showTag(String... str) {
        Document document = configDao.showItemByInput("item", "tag").first();
        if (str.length == 0) {
            return (ArrayList<String>) document.getList("firstLayer", String.class);
        } else {
            return (ArrayList<String>) document.getList(str[0], String.class);
        }
    }

    @Override
    public ArrayList<Document> showLessons(String userid, Integer... week) {

        ArrayList<Document> documents = lessonDao.showLessonsByInput("userid", userid);

        for (Document document : documents) {
            document.put("season", timeHelper.getSeason((Integer) document.get("week")));
        }

        return documents;
    }
}
