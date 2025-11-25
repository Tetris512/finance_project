package finance_backend.pojo.vo;

import finance_backend.pojo.request.difyRequest.DifyGetHistoryRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class DifyGetHistoryVO {

    private String conversationId;

    @NotNull
    private String user;

    private String firstId;

    private String limit;

    public DifyGetHistoryVO(DifyGetHistoryRequest request) {
        this.conversationId = conversationId;
        this.user = user;
        this.firstId = firstId;
        this.limit = limit;
    }
}
