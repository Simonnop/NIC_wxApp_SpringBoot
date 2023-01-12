package group.su.dao;

import group.su.pojo.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserDaoTest extends MongoRepository<User, String> {

    // 通过 userid 寻找
    User findByUserid(String userid);

    // 更新
    @Override
    <S extends User> S save(S entity);

    // 插入
    @Override
    <S extends User> S insert(S entity);
}
