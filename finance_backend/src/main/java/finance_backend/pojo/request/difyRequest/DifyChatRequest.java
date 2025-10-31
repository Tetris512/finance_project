package finance_backend.pojo.request.difyRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "chat请求")
public class DifyChatRequest {

    private Map<String, Object> inputs;
    @NotNull
    private String query;

    @JsonProperty("response_mode")
    private String responseMode;

    @JsonProperty("conversation_id")
    private String conversationId;

    @NotNull
    private String user;

    private List<DifyFile> files;

    public DifyChatRequest(Map<String, Object> inputs, String query, String responseMode, String conversationId, String user, List<DifyFile> files) {
        this.inputs = inputs;
        this.query = query;
        this.responseMode = responseMode;
        this.conversationId = conversationId;
        this.user = user;
        this.files = files;
    }
}


