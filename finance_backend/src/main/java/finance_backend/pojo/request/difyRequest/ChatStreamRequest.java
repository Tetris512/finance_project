package finance_backend.pojo.request.difyRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * /api/chatstream 请求体，对应文档中的 prompt + history。
 */
@Data
public class ChatStreamRequest {

    /**
     * 用户本次最新问题
     */
    @NotBlank
    private String prompt;


    @NotNull
    private String user;

    @NotNull
    @JsonProperty("conversationId")
    private String conversationId;

}

