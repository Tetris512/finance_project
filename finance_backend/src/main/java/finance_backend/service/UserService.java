package finance_backend.service;

import finance_backend.pojo.Pair;
import finance_backend.pojo.entity.UserEntity;
import finance_backend.pojo.vo.registerVO;

public interface UserService {
    UserEntity login(String username, String password);

    UserEntity register(registerVO registerVO);

}