package finance_backend.pojo.response.difyResponse;

import lombok.Data;

@Data
public class DifyChatResponse {
    private String answer;

    private String conversation_id;

    private String message_id;

    //TODO: add other fields if needed
}
