package group.su.service.impl;

import com.mongodb.client.FindIterable;
import group.su.dao.ConfigDao;
import group.su.dao.LessonDao;
import group.su.dao.MissionDao;
import group.su.dao.UserDao;
import group.su.dao.impl.ConfigDaoImpl;
import group.su.dao.impl.LessonDaoImpl;
import group.su.dao.impl.MissionDaoImpl;
import group.su.dao.impl.UserDaoImpl;
import group.su.exception.AppRuntimeException;
import group.su.exception.ExceptionKind;
import group.su.service.UserService;
import group.su.service.helper.MissionHelper;
import group.su.service.util.TimeUtil;
import org.apache.commons.fileupload.FileItem;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    final UserDao userDao;
    final MissionDao missionDao;
    final ConfigDao configDao;
    final LessonDao lessonDao;
    final MissionHelper missionManager;

    @Autowired
    public UserServiceImpl(UserDao userDao, MissionDao missionDao, ConfigDao configDao,
                           LessonDao lessonDao, MissionHelper missionManager) {
        this.userDao = userDao;
        this.missionDao = missionDao;
        this.configDao = configDao;
        this.lessonDao = lessonDao;
        this.missionManager = missionManager;
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
        // 判断是否缺人
        for (Document document : missionManager.changeFormAndCalculate(documents)) {
            if (((Document) document.get("status"))
                    .get("接稿")
                    .equals("未达成")) {
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
    public ArrayList<Document> showMissionByTag(String tag1,String tag2) {
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
                    .get("写稿")
                    .equals("未达成")) {
                documentArrayList.add(missionManager.calculateLack(document));
            }
        }
        return documentArrayList;
    }

    @Override
    public void tryGetMission(String userid, String missionID, String kind) {

        // 不带事务写法: 不验证 username 响应时间更快
        boolean success = true;
        try {
            synchronized (missionDao) {
                Document document = missionDao.searchMissionByInput("missionID", missionID).first();
                // 验证非空
                if (document == null) {
                    throw new AppRuntimeException(ExceptionKind.DATABASE_NOT_FOUND);
                }
                missionManager.calculateLack(document);
                // 验证非满员
                if (((Document) document.get("reporterLack"))
                        .get(kind)
                        .equals(0)) {
                    throw new AppRuntimeException(ExceptionKind.ENOUGH_PEOPLE);
                }
                // 验证未参与
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
            // 看这个任务是不是普通任务
            Integer element = missionDao.searchMissionByInput("missionID", missionID)
                    .first()
                    .get("element", Integer.class);
            if (element != 0) {
                return;
            }
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
    public Map<String,ArrayList<String>> showTag() {
        Document document = configDao.showItemByInput("item", "tag").first();
        return new HashMap<String, ArrayList<String>>() {{
            for (String firstLayer : document.getList("firstLayer", String.class)) {
                put(firstLayer, new ArrayList<String>() {{
                    for (String s : document.getList(firstLayer, String.class)) {
                        add(s);
                    }
                }});
            }
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
            if (!(document.get("status",Document.class))
                    .get("写稿")
                    .equals("未达成")) {
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
                    .get("排版")
                    .equals("未达成")
                    && !((Document) document.get("status"))
                    .get("辅导员审核")
                    .equals("未达成")) {
                documentArrayList.add(document);
            }
        }
        return documentArrayList;
    }
}
