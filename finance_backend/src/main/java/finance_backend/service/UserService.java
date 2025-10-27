package finance_backend.service;

import finance_backend.pojo.Pair;
import finance_backend.pojo.entity.UserEntity;
import finance_backend.pojo.vo.registerVO;

public interface UserService {
    void login(String uid, String password);

    UserEntity register(registerVO registerVO);

    UserEntity findByUid(String uid);

}