package finance_backend.pojo.vo;

import finance_backend.Utils.CommonValue;
import finance_backend.pojo.request.difyRequest.DifyChatRequest;
import finance_backend.pojo.request.difyRequest.DifyFile;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RequestData {

    private Map<String, Object> inputs;

    private String query;

    private String responseMode;

    private String conversationId;

    private String user;

    private List<DifyFile> files;

    public RequestData(DifyChatRequest request) {
        this.query = request.getQuery();
        this.inputs = request.getInputs();
        this.responseMode = request.getResponseMode() == null ? CommonValue.RESPONSE_MODE_STREAM : request.getResponseMode();
        this.conversationId = request.getConversationId();
        this.user = request.getUser();
        this.files = request.getFiles();
    }
}
