package finance_backend.controller;

import finance_backend.pojo.exception.CommonResponse;
import finance_backend.pojo.request.difyRequest.ChatStreamRequest;
import finance_backend.pojo.request.difyRequest.DifyChatRequest;
import finance_backend.pojo.request.difyRequest.FeedbackRequest;
import finance_backend.pojo.request.difyRequest.HistoryRequest;
import finance_backend.pojo.response.difyResponse.DifyChatResponse;
import finance_backend.pojo.vo.DifyChatVO;
import finance_backend.pojo.vo.DifyFeedbackVO;
import finance_backend.service.DifyService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import finance_backend.pojo.vo.Conversation;
import finance_backend.pojo.vo.MessageItem;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DifyController {
    private final DifyService difyService;

    @PostMapping("/chatBlocked")
    public CommonResponse<?> chat(@Valid @RequestBody DifyChatRequest request) {
        // Throws BizException if auth failed.
        DifyChatVO difyChatVO = new DifyChatVO(request);
        DifyChatResponse difyChatResponse = difyService.chatBlocked(difyChatVO);
        if (difyChatResponse == null) {
            return CommonResponse.failure(400);
        }

        CommonResponse<?> commonResponse = CommonResponse.success(difyChatResponse);
        commonResponse.setCode(200);
        return commonResponse;
    }

    /**
     * /api/chat：从 DifyService 获得原始 SSE 文本流，只转发特定几种事件：
     * message / message_end / error / ping / message_replace，其它事件类型直接丢弃。
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void chatStream(@Valid @RequestBody ChatStreamRequest request,
                           HttpServletResponse servletResponse) throws IOException {
        // 拼接 history + 当前 prompt 为最终 query
        StringBuilder sb = new StringBuilder();
        if (request.getHistory() != null) {
            request.getHistory().forEach(h -> {
                sb.append(h.getRole()).append(": ").append(h.getContent()).append("\n");
            });
        }
        sb.append("user: ").append(request.getPrompt());

        DifyChatVO vo = new DifyChatVO();
        vo.setQuery(sb.toString());
        vo.setUser(request.getUser());
        vo.setInputs(null);
        vo.setFiles(null);
        vo.setConversationId(request.getConversationId());

        // 直接由 service 完成：向 Dify 发起请求，读取上游 SSE，解析并只把允许的事件写回 servletResponse
        // 允许的事件：message, message_end, error, ping, message_replace
        difyService.chatStream(vo, servletResponse);
    }

    /**
     * 新增：/api/feedback
     * 接收用户对某条 AI 回复的评价和反馈意见
     */
    @PostMapping("/feedback")
    public CommonResponse<?> feedback(@Valid @RequestBody FeedbackRequest request) {
        // 这里先简单打印日志，后续你可以接入数据库或日志系统
        System.out.println("[FEEDBACK] rating=" + request.getRating());
        System.out.println("[FEEDBACK] conversation_id=" + request.getConversationId());
        if (request.getFeedbackText() != null) {
            System.out.println("[FEEDBACK] feedbackText=" + request.getFeedbackText());
        }
        DifyFeedbackVO feedbackVO = new DifyFeedbackVO(request);
        difyService.feedback(feedbackVO);

        // 按文档，成功时返回标准 JSON
        return CommonResponse.success(
                java.util.Map.of(
                        "status", "success",
                        "message", "Feedback received successfully")
        );
    }

    @GetMapping("/history/conversations" )
    public CommonResponse<?> getConversations(@RequestParam String user) {
        List<Conversation> conversations = difyService.getConversations(user);
        if (conversations == null) {
            return CommonResponse.failure(404);
        }
        return CommonResponse.success(conversations);
    }

    // 3.2 Get a Specific Conversation: /api/history/conversations/{conversationId}?user=xxx
    @GetMapping("/history/conversations/{conversationId}")
    public CommonResponse<?> getConversationMessages(@PathVariable("conversationId") String conversationId,
                                                     @RequestParam("user") String user) {
        List<MessageItem> messages = difyService.getConversationMessages(user, conversationId);
        return CommonResponse.success(messages);
    }
}
