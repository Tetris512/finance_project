package finance_backend.service.impl;

import finance_backend.dao.UserDao;
import finance_backend.pojo.entity.UserEntity;
import finance_backend.pojo.exception.BizException;
import finance_backend.pojo.vo.Conversation;
import finance_backend.pojo.vo.registerVO;
import finance_backend.service.DifyService;
import finance_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import finance_backend.pojo.exception.BizError;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String USERNAME_PREFIX = "dify_";
    private final UserDao userDao;

    private final DifyService difyService;

    @Override
    public UserEntity register(registerVO registerVO) {
        String rawUsername = registerVO.getUsername();
        String prefixedUsername = applyPrefix(rawUsername);
        // 检查mysql数据库是否存在用户
        UserEntity user = userDao.findByUsername(prefixedUsername);
        if (user != null) {
            throw new BizException(BizError.USERNAME_EXISTS_IN_MYSQL);
        }
        // 检查dify数据库是否存在用户
        List<Conversation> res = difyService.getConversations(prefixedUsername);
        if (res != null && !res.isEmpty()) {
            throw new BizException(BizError.USERNAME_EXISTS_IN_DIFY);
        }

        UserEntity userEntity = UserEntity.builder()
                .username(prefixedUsername)
                .password(registerVO.getPassword())
                .role(registerVO.getRole())
                .department(registerVO.getDepartment())
                .majorName(registerVO.getMajorName())
                .build();
        userDao.save(userEntity);
        return userEntity;
    }


    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     */
    @Override
    public UserEntity login(String username, String password) {
        UserEntity user = userDao.findByUsername(applyPrefix(username));
        if (user == null || !password.equals(user.getPassword())) {
            throw new BizException(BizError.INVALID_CREDENTIAL);
        }
        return user;
    }

    private String applyPrefix(String username) {
        if (username == null) {
            return null;
        }
        return username.startsWith(USERNAME_PREFIX) ? username : USERNAME_PREFIX + username;
    }

    public boolean exists(String username) {
        return userDao.findByUsername(applyPrefix(username)) != null;
    }
}