package finance_backend.pojo.vo;

import finance_backend.pojo.entity.FeedbackEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackConversation {
    private FeedbackEntity feedback;
    private MessageItem message;
}
