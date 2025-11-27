package finance_backend.pojo.request.difyRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * /api/feedback 请求体
 */
@Data
public class FeedbackRequest {

    @NotNull
    private String user;

    /**
     * 评价结果，"up" 表示点赞，"down" 表示点踩
     */
    @NotBlank
    private String rating;

    /**
     * 机器人的回答完整文本
     */
    @NotNull
    @JsonProperty("conversationId")
    private String conversationId;

    /**
     * 可选，仅当 rating = "down" 时前端才会传，包含用户的具体反馈意见
     */
    private String feedbackText;

    @NotNull
    @JsonProperty("messageId")
    private String messageId;
}

