package finance_backend.controller;

import finance_backend.pojo.exception.BizError;
import finance_backend.pojo.exception.BizException;
import finance_backend.pojo.exception.CommonResponse;
import finance_backend.pojo.request.difyRequest.ChatStreamRequest;
import finance_backend.pojo.request.difyRequest.DifyChatRequest;
import finance_backend.pojo.request.difyRequest.FeedbackRequest;
import finance_backend.pojo.request.difyRequest.HistoryRequest;
import finance_backend.pojo.response.difyResponse.DifyChatResponse;
import finance_backend.pojo.vo.DifyChatVO;
import finance_backend.pojo.vo.DifyFeedbackVO;
import finance_backend.service.DifyService;
import finance_backend.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import finance_backend.pojo.vo.Conversation;
import finance_backend.pojo.vo.MessageItem;
import finance_backend.pojo.vo.FeedbackConversation;
import finance_backend.pojo.vo.FeedbackSimple;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DifyController {
    private final DifyService difyService;
    private final UserService userService;

    private void ensureUserExists(String username) {
        if (username == null || !userService.exists(username)) {
            throw new BizException(BizError.USER_NOT_FOUND);
        }
    }

    @PostMapping("/chatBlocked")
    public CommonResponse<?> chat(@Valid @RequestBody DifyChatRequest request) {
        ensureUserExists(request.getUser());
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
     * 基于 WebFlux 的非阻塞 /api/chat：直接返回 Flux<ServerSentEvent<String>>
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@Valid @RequestBody ChatStreamRequest request) {
        ensureUserExists(request.getUser());
        // 拼接 history + 当前 prompt 为最终 query
        StringBuilder sb = new StringBuilder();
        sb.append("user: ").append(request.getPrompt());

        DifyChatVO vo = new DifyChatVO();
        vo.setQuery(sb.toString());
        vo.setUser(request.getUser());
        vo.setInputs(null);
        vo.setFiles(null);
        vo.setConversationId(request.getConversationId());

        return difyService.chatStreamReactive(vo);
    }

    /**
     * 新增：/api/feedback
     * 接收用户对某条 AI 回复的评价和反馈意见
     */
    @PostMapping("/feedback")
    public CommonResponse<?> feedback(@Valid @RequestBody FeedbackRequest request) {
        ensureUserExists(request.getUser());
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
        ensureUserExists(user);
        List<Conversation> conversations = difyService.getConversations(user);
        if (conversations == null) {
            return CommonResponse.failure(404);
        }
        return CommonResponse.success(conversations);
    }

    /**
     * 查询指定conversationId的消息列表
     */
    @GetMapping("/history/conversations/{conversationId}")
    public CommonResponse<?> getConversationMessages(@PathVariable("conversationId") String conversationId,
                                                     @RequestParam("user") String user) {
        ensureUserExists(user);
        List<MessageItem> messages = difyService.getConversationMessages(user, conversationId);
        return CommonResponse.success(messages);
    }

    @GetMapping("/history/conversations/{conversationId}/{messageId}")
    public CommonResponse<?> getConversationMessage(@PathVariable("conversationId") String conversationId,
                                                    @PathVariable("messageId") String messageId,
                                                    @RequestParam("user") String user) {
        ensureUserExists(user);
        MessageItem message = difyService.getConversationMessage(user, conversationId, messageId);
        if (message == null) {
            return CommonResponse.failure(404);
        }
        return CommonResponse.success(message);
    }

    @GetMapping("/getFeedback")
    public CommonResponse<?> getFeedback() {
        List<FeedbackConversation> feedbackList = difyService.getFeedbackWithConversations();
        return CommonResponse.success(feedbackList);
    }

    @GetMapping("/getFeedbackSimple")
    public CommonResponse<?> getFeedbackSimple() {
        List<FeedbackSimple> feedbackList = difyService.getFeedbackSimple();
        return CommonResponse.success(feedbackList);
    }
}
