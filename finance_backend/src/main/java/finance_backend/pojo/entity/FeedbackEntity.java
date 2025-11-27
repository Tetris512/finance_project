package finance_backend.pojo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Table(name = "feedback_entity")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FeedbackEntity {
    // 自增主键，避免在 varchar 上使用 auto_increment 的错误
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 反馈发起的用户 ID，例如 "tetris512"
    @Column(name = "user")
    private String user;

    // 评价结果，例如 "up" / "down"
    @Column(name = "rating")
    private String rating;

    // 本次评价对应的会话 ID（保留为 varchar，不参与自增）
    @Column(name = "conversation_id")
    private String conversationId;

    // 用户填写的文字反馈，例如 "回答不是很准确"
    @Column(name = "feedback_text")
    private String feedbackText;

    // 本次评价对应的消息 ID
    @Column(name = "message_id")
    private String messageId;

}
