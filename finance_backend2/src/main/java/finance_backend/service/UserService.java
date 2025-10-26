package finance_backend.service;

import finance_backend.pojo.Pair;
import finance_backend.pojo.entity.UserEntity;

public interface UserService {
    void login(String uid, String password);

    UserEntity register(String username, String password);

    UserEntity findByUid(String uid);

}