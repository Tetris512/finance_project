package finance_backend.pojo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BizError implements ErrorType {

    USERNAME_EXISTS_IN_MYSQL(200001, "用户名已存在", 400),
    INVALID_CREDENTIAL(200002, "用户名或密码错误", 400),
    USERNAME_EXISTS_IN_DIFY(200003, "Dify中用户名已存在", 400)
    ;

    final int code;
    final String message;
    final int httpCode;
}
