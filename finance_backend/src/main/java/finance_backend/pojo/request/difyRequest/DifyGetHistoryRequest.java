package finance_backend.pojo.request.difyRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "获取历史聊天记录请求")
public class DifyGetHistoryRequest {

    @JsonProperty("conversationId")
    private String conversationId;

    @NotNull
    private String user;

    @JsonProperty("firstId")
    private String firstId;

    private String limit;

    public DifyGetHistoryRequest(Map<String, Object> inputs, String query, String responseMode, String conversationId, String user, List<DifyFile> files) {
        this.conversationId = conversationId;
        this.user = user;
        this.firstId = firstId;
        this.limit = limit;
    }
}


