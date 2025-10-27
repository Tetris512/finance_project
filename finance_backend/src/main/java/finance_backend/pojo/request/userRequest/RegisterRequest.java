package finance_backend.pojo.request.userRequest;

import finance_backend.pojo.entity.Department;
import finance_backend.pojo.entity.MajorName;
import finance_backend.pojo.entity.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "注册请求")
public class RegisterRequest {

    @Schema(description = "用户名", required = true)
    @NotNull
    @Size(min = 4, max = 16, message = "用户名长度必须在 4-16 之间")
    @Pattern(regexp = "^[A-Za-z\\d-_]*$", message = "用户名只能包含字母,数字,下划线和连字符")
    private String username;

    @Schema(description = "密码", required = true)
    @NotNull
    @Size(min = 8, max = 56, message = "密码长度必须在 8-56 之间")
    @Pattern.List({
            @Pattern(regexp = "^[\\x21-\\x7e]*$", message = "密码只能包含字母,数字和符号"),
    })
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role; // STUDENT or TEACHER

    @Enumerated(EnumType.STRING)
    private Department department;

    @Enumerated(EnumType.STRING)
    private MajorName majorName;
}
