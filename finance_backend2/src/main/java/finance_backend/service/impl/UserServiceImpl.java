package finance_backend.service.impl;

import finance_backend.dao.UserDao;
import finance_backend.pojo.entity.UserEntity;
import finance_backend.pojo.exception.BizException;
import finance_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import finance_backend.pojo.exception.BizError;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final Long base = 100000L;
    private Long momentCounter = 0L;

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     */
    @Override
    public UserEntity register(String username, String password) {
        long uid_long = userDao.findAll().size() + base;
        String uid = Long.toString(uid_long);

        UserEntity userEntity = UserEntity.builder().username(username).password(password)
                .uid(uid).build();
        userDao.save(userEntity);
        return userEntity;
    }

    /**
     * 根据 uid 查找用户
     * @param uid 账号
     * @return         用户实体
     */
    @Override
    public UserEntity findByUid(String uid) {
        return userDao.findByUid(uid);
    }

    /**
     * 用户登录
     * @param uid 账号
     * @param password 密码
     */
    @Override
    public void login(String uid, String password) {
        UserEntity user = userDao.findByUid(uid);
        if (user == null || !password.equals(user.getPassword())) {
            throw new BizException(BizError.INVALID_CREDENTIAL);
        }
    }
}