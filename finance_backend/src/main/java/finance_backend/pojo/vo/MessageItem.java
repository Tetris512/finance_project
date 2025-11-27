package finance_backend.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageItem {
    private String id;      // 消息唯一 ID
    private String query;   // 显示用的消息文本内容
    private String answer;  // 角色：user / bot
}

