package finance_backend.pojo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import finance_backend.Utils.CommonValue;
import finance_backend.pojo.request.difyRequest.DifyChatRequest;
import finance_backend.pojo.request.difyRequest.DifyFile;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class DifyChatVO {

    private Map<String, Object> inputs;

    private String query;

    private String responseMode;

    private String conversationId;

    private String user;

    private List<DifyFile> files;

    public DifyChatVO(DifyChatRequest request) {
        this.query = request.getQuery();
        this.inputs = request.getInputs() != null ? request.getInputs() : Collections.emptyMap();
        // Use blocking mode temporarily to avoid streaming-related upstream timeouts during diagnosis
        this.responseMode = CommonValue.RESPONSE_MODE_STREAM;
        this.conversationId = request.getConversationId();
        this.user = request.getUser();
        this.files = request.getFiles() != null ? request.getFiles() : java.util.Collections.emptyList();
    }
}
