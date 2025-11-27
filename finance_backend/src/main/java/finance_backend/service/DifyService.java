package finance_backend.service;

import finance_backend.pojo.vo.Conversation;
import finance_backend.pojo.response.difyResponse.DifyChatResponse;
import finance_backend.pojo.vo.DifyChatVO;
import finance_backend.pojo.vo.DifyFeedbackVO;
import finance_backend.pojo.vo.MessageItem;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface DifyService {
    DifyChatResponse chatBlocked(DifyChatVO requestData);

    /**
     * 向 Dify 发起聊天请求（SSE 流式），读取上游 SSE，
     * 只把以下几种事件写回到 servletResponse：
     *   - message
     *   - message_end
     *   - error
     *   - ping
     *   - message_replace
     * 其余事件一律忽略。
     */
    void chatStream(DifyChatVO vo, HttpServletResponse response);

    void feedback(DifyFeedbackVO difyFeedbackVO);

    // 新增：查询指定用户的会话列表（仅 id、标题）
    List<Conversation> getConversations(String user);

    // 新增：获取某个会话的消息列表（按你的 UI 结构）
    List<MessageItem> getConversationMessages(String user, String conversationId);
}
