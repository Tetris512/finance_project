package finance_backend.pojo.vo;

import finance_backend.pojo.request.difyRequest.FeedbackRequest;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class DifyFeedbackVO {
    // 反馈发起的用户 ID，例如 "tetris512"
    private String user;

    // 评价结果，例如 "up" / "down"
    private String rating;

    // 本次评价对应的会话 ID
    private String conversationId;

    // 用户填写的文字反馈，例如 "回答不是很准确"
    private String feedbackText;

    // 本次评价对应的消息 ID
    private String messageId;

    public DifyFeedbackVO(@Valid FeedbackRequest request) {
        this.user = request.getUser();
        this.rating = request.getRating();
        this.conversationId = request.getConversationId();
        this.feedbackText = request.getFeedbackText();
        this.messageId = request.getMessageId();
    }
}
