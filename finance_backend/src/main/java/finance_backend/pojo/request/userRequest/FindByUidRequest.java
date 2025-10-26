package finance_backend.pojo.request.userRequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "获取好友请求的请求")
public class FindByUidRequest {
    @Schema(description = "账号", required = true)
    @NotNull
    @Size(min = 4, max = 16, message = "账号长度必须在 4-16 之间")
    @Pattern(regexp = "^[0-9]*$", message = "账号只能包含数字")
    private String uid;
}
