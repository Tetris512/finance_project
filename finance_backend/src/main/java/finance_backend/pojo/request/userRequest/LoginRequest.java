package finance_backend.pojo.request.userRequest;

import finance_backend.pojo.entity.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @Schema(description = "账号", required = true)
    @NotNull
    @Pattern(regexp = "^[0-9]*$", message = "账号只能包含数字")
    private String uid;


    @Schema(description = "密码", required = true)
    @NotNull
    @Size(min = 8, max = 56, message = "密码长度必须在 8-56 之间")
    @Pattern.List({
            @Pattern(regexp = "^[\\x21-\\x7e]*$", message = "密码只能包含字母,数字和符号"),
    })
    private String password;

}
