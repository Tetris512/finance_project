package finance_backend.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天会话简要信息，用于 /api/history 返回列表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    /** 会话 id */
    private String conversation_id;

    /** 会话标题 */
    private String title;
}

