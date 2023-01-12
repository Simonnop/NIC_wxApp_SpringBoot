package group.su.dao;

import group.su.pojo.Mission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MissionDaoTest extends MongoRepository<Mission, String> {

    // 更新任务
    @Override
    <S extends Mission> S save(S entity);

    // 添加任务
    @Override
    <S extends Mission> S insert(S entity);

    // 根据 missionID 查询
    Mission findMissionByMissionID(String missionID);

    // 查找全部
    @Override
    List<Mission> findAll();





}
