package finance_backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finance_backend.Utils.CommonValue;
import finance_backend.Utils.DifyRequestHandler;
import finance_backend.dao.FeedbackDao;
import finance_backend.pojo.entity.FeedbackEntity;
import finance_backend.pojo.response.difyResponse.DifyChatResponse;
import finance_backend.pojo.vo.DifyChatVO;
import finance_backend.pojo.vo.DifyFeedbackVO;
import finance_backend.pojo.vo.MessageItem;
import finance_backend.service.DifyService;
import finance_backend.pojo.vo.Conversation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class DifyServiceImpl implements DifyService {

    private final DifyRequestHandler difyRequestHandler;
    private final FeedbackDao feedbackDao;

    // 复用一个 ObjectMapper 实例
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final WebClient difyWebClient;

    public DifyChatResponse chatBlocked (DifyChatVO request) {
        try {
            // Build JSON body to match Postman payload exactly
            ObjectMapper objectMapper = new ObjectMapper();
            com.fasterxml.jackson.databind.node.ObjectNode root = objectMapper.createObjectNode();
            root.put("query", request.getQuery());
            root.put("user", request.getUser());
            root.put("response_mode", finance_backend.Utils.CommonValue.RESPONSE_MODE_STREAM);
            // inputs: if null -> "", else keep original structure
            if (request.getInputs() == null) {
                root.put("inputs", "");
            } else {
                root.set("inputs", objectMapper.valueToTree(request.getInputs()));
            }
            if (request.getFiles() == null || request.getFiles().isEmpty()) {
                root.put("files", "");
            } else {
                root.set("files", objectMapper.valueToTree(request.getFiles()));
            }
            // conversation_id: if null/blank -> ""
            String convId = request.getConversationId();
            root.put("conversation_id", (convId == null || convId.isEmpty()) ? "" : convId);

            String jsonBody = objectMapper.writeValueAsString(root);

            String url = CommonValue.BASE_DIFY_URL + "/chat-messages";
            System.out.println("Final JSON body: " + jsonBody);
            Response clientResponse = difyRequestHandler.postRequest(url, CommonValue.apiKey, CommonValue.REQUEST_POST, jsonBody);
            DifyChatResponse response = difyRequestHandler.handleChatResponse(clientResponse);
            // 对返回的 answer 做清洗：
            // 1. 去掉开头的“流程”两个字（如果有）；
            // 2. 删除所有 <think> ... </think> 或从 <think> 到下一个 </think> 之间的内容。
            String answer = response.getAnswer();
            if (answer != null && !answer.isEmpty()) {
                String cleaned = answer;
                // 去掉开头的“流程”
                cleaned = cleaned.stripLeading();
                if (cleaned.startsWith("流程")) {
                    cleaned = cleaned.substring("流程".length());
                }
                // 去掉所有 <think>...</think> 块；如果没有 </think>，则从 <think> 起直到结尾
                cleaned = cleaned.replaceAll("<think>[\\s\\S]*?</think>", "");
                int idx = cleaned.indexOf("<think>");
                if (idx >= 0) {
                    cleaned = cleaned.substring(0, idx);
                }
                response.setAnswer(cleaned.trim());
            }

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void chatStream(DifyChatVO vo, HttpServletResponse response) {
        String jsonBody;
        try {
            jsonBody = buildStreamingJsonBody(vo);
        } catch (JsonProcessingException e) {
            try {
                writeErrorEvent(response, "build body error: " + e.getMessage());
            } catch (IOException ignored) {}
            return;
        }

        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(CommonValue.BASE_DIFY_URL + "/chat-messages")
                .addHeader("Authorization", "Bearer " + CommonValue.apiKey)
                .addHeader("User-Agent", "PostmanRuntime/7.49.1")
                .addHeader("Accept", "text/event-stream")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Connection", "keep-alive")
                .post(requestBody)
                .build();

        OkHttpClient client = difyRequestHandler.getClient();

        response.setContentType("text/event-stream;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try (Response upstream = client.newCall(request).execute()) {
            if (!upstream.isSuccessful() || upstream.body() == null) {
                String errMsg = "Upstream error: " + upstream.code();
                writeErrorEvent(response, errMsg);
                return;
            }

            InputStream inputStream = upstream.body().byteStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();

            String line;
            String currentEvent = null; // 外层 event 行（如果有）
            StringBuilder dataBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("event:")) {
                    currentEvent = line.substring("event:".length()).trim();
                } else if (line.startsWith("data:")) {
                    if (dataBuilder.length() > 0) {
                        dataBuilder.append("\n");
                    }
                    dataBuilder.append(line.substring("data:".length()).trim());
                } else if (line.isEmpty()) {
                    // 一个 SSE 事件结束
                    String rawData = dataBuilder.toString();

                    // 1. 从 data JSON 里优先取 innerEvent
                    String innerEvent = null;
                    if (rawData != null && !rawData.isEmpty()) {
                        try {
                            JsonNode node = MAPPER.readTree(rawData);
                            if (node.hasNonNull("event")) {
                                innerEvent = node.get("event").asText("");
                            }
                        } catch (Exception ignore) {
                            // 解析失败就当没有 innerEvent
                        }
                    }

                    // 2. effectiveEvent：优先用 innerEvent，没有就用外层 currentEvent
                    String effectiveEvent = (innerEvent != null && !innerEvent.isEmpty())
                            ? innerEvent
                            : currentEvent;

                    if (effectiveEvent != null && isWhitelisted(effectiveEvent)) {
                        String trimmedData = trimDataPayload(rawData); // 只保留 event/answer/message_id/conversation_id
                        writer.write("event: " + effectiveEvent + "\n");
                        if (!trimmedData.isEmpty()) {
                            writer.write("data: " + trimmedData + "\n\n");
                        } else {
                            writer.write("data: {}\n\n");
                        }
                        writer.flush();
                    }

                    // 重置
                    currentEvent = null;
                    dataBuilder.setLength(0);
                }
            }
        } catch (Exception e) {
            try {
                writeErrorEvent(response, e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    /**
     * 事件白名单：只允许这几种类型向前端转发。
     */
    private boolean isWhitelisted(String event) {
        return "message".equals(event)
                || "message_end".equals(event)
                || "error".equals(event)
                || "ping".equals(event)
                || "message_replace".equals(event);
    }

    /**
     * 将上游 data JSON 精简为只包含 event / answer / message_id / conversation_id 四个字段。
     * 如果不是合法 JSON，则原样返回。
     */
    private String trimDataPayload(String rawData) {
        if (rawData == null || rawData.isEmpty()) {
            return "";
        }
        try {
            JsonNode node = MAPPER.readTree(rawData);
            if (!node.isObject()) {
                return rawData; // 非对象，直接返回
            }
            ObjectNode obj = (ObjectNode) node;
            ObjectNode trimmed = MAPPER.createObjectNode();
            if (obj.has("event")) {
                trimmed.set("event", obj.get("event"));
            }
            if (obj.has("answer")) {
                trimmed.set("answer", obj.get("answer"));
            }
            if (obj.has("message_id")) {
                trimmed.set("message_id", obj.get("message_id"));
            }
            if (obj.has("conversation_id")) {
                trimmed.set("conversation_id", obj.get("conversation_id"));
            }
            return MAPPER.writeValueAsString(trimmed);
        } catch (Exception e) {
            // 解析失败，原样返回
            return rawData;
        }
    }

    private void writeErrorEvent(HttpServletResponse response, String message) throws IOException {
        PrintWriter writer = response.getWriter();
        String safeMsg = message == null ? "unknown error" : message.replace("\n", " ");
        String payload = "{\"event\":\"error\",\"message\":\"" + safeMsg.replace("\"", "\\\"") + "\"}";
        writer.write("event: error\n");
        writer.write("data: " + payload + "\n\n");
        writer.flush();
    }

    // 使用与 chatBlocked 相同逻辑构造 JSON，但强制 response_mode=streaming
    private String buildStreamingJsonBody(DifyChatVO request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode root = objectMapper.createObjectNode();
        root.put("query", request.getQuery());
        root.put("user", request.getUser());
        // 这里强制是 streaming
        root.put("response_mode", CommonValue.RESPONSE_MODE_STREAM);

        if (request.getInputs() == null) {
            root.put("inputs", "");
        } else {
            root.set("inputs", objectMapper.valueToTree(request.getInputs()));
        }
        if (request.getFiles() == null || request.getFiles().isEmpty()) {
            root.put("files", "");
        } else {
            root.set("files", objectMapper.valueToTree(request.getFiles()));
        }
        String convId = request.getConversationId();
        root.put("conversation_id", (convId == null || convId.isEmpty()) ? "" : convId);

        String jsonBody = objectMapper.writeValueAsString(root);
        System.out.println("[chatStream] Final JSON body: " + jsonBody);
        return jsonBody;
    }

    @Override
    public void feedback(DifyFeedbackVO difyFeedbackVO) {
        FeedbackEntity feedbackEntity = feedbackDao.findByUserAndConversationIdAndMessageId(difyFeedbackVO.getUser(), difyFeedbackVO.getConversationId(), difyFeedbackVO.getMessageId());
        if (feedbackEntity != null) {
            // 已存在相同用户和会话ID的反馈，覆盖
            feedbackEntity.setRating(difyFeedbackVO.getRating());
            feedbackEntity.setFeedbackText(difyFeedbackVO.getFeedbackText());
            feedbackDao.save(feedbackEntity);

            return ;
        }


        feedbackEntity = new FeedbackEntity();
        feedbackEntity.setConversationId(difyFeedbackVO.getConversationId());
        feedbackEntity.setRating(difyFeedbackVO.getRating());
        feedbackEntity.setFeedbackText(difyFeedbackVO.getFeedbackText());
        feedbackEntity.setUser(difyFeedbackVO.getUser());
        feedbackEntity.setMessageId(difyFeedbackVO.getMessageId());

        feedbackDao.save(feedbackEntity);
    }

    // 从 Dify SSE 的事件 JSON 中提取一小段文本片段
    private String extractFragmentFromDifySse(JsonNode eventNode) {
        // 你可以根据实际 Dify 事件结构微调这里，目前先按最常见模式提取
        // 1) 顶层 answer 追加
        if (eventNode.hasNonNull("answer")) {
            return eventNode.get("answer").asText("");
        }
        // 2) data.outputs.text 或 data.outputs.answer
        JsonNode data = eventNode.get("data");
        if (data != null && data.isObject()) {
            JsonNode outputs = data.get("outputs");
            if (outputs != null && outputs.isObject()) {
                if (outputs.hasNonNull("text")) {
                    return outputs.get("text").asText("");
                }
                if (outputs.hasNonNull("answer")) {
                    return outputs.get("answer").asText("");
                }
            }
        }
        return null;
    }

    @Override
    public List<Conversation> getConversations(String user) {
        String url = CommonValue.BASE_DIFY_URL + "/conversations";
        String apiKey = CommonValue.apiKey;
        try {
            return difyRequestHandler.getConversationsByUser(user, apiKey, url);
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public List<MessageItem> getConversationMessages(String user, String conversationId) {
        String url = CommonValue.BASE_DIFY_URL + "/messages";
        try {
            return difyRequestHandler.getMessagesByConversation(user, conversationId, CommonValue.apiKey, url);
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // 新增：基于 WebFlux 的非阻塞 SSE 转发方法
    public Flux<ServerSentEvent<String>> chatStreamReactive(DifyChatVO vo) {
        try {
            String jsonBody = buildStreamingJsonBody(vo);

            // Use WebClient to perform non-blocking request and stream processing
            return difyWebClient.post()
                    .uri("/chat-messages")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .accept(org.springframework.http.MediaType.TEXT_EVENT_STREAM)
                    .bodyValue(jsonBody)
                    .exchangeToFlux((ClientResponse clientResponse) -> {
                        if (!clientResponse.statusCode().is2xxSuccessful()) {
                            String payload = "{\"event\":\"error\",\"message\":\"Upstream error: " + clientResponse.statusCode().value() + "\"}";
                            return Flux.just(ServerSentEvent.<String>builder().event("error").data(payload).build());
                        }
                        // Extract body as stream of String (raw SSE chunks)
                        return clientResponse.bodyToFlux(String.class)
                                .publishOn(Schedulers.boundedElastic())
                                .flatMap(raw -> {
                                    // raw may contain fragments; split by SSE event boundary 'data:' / 'event:' lines
                                    // Simplified approach: handle when raw contains JSON object (data payload)
                                    try {
                                        String text = raw.trim();
                                        if (text.isEmpty()) return Flux.empty();
                                        // Attempt to parse as JSON; if parseable -> process
                                        JsonNode node;
                                        try {
                                            node = MAPPER.readTree(text);
                                        } catch (Exception ex) {
                                            // Not a JSON payload; try to extract data: prefix
                                            String maybe = text;
                                            if (maybe.startsWith("data:")) {
                                                maybe = maybe.substring("data:".length()).trim();
                                            }
                                            try {
                                                node = MAPPER.readTree(maybe);
                                            } catch (Exception ex2) {
                                                return Flux.empty();
                                            }
                                        }

                                        String eventName = null;
                                        if (node.hasNonNull("event")) eventName = node.get("event").asText(null);
                                        if (eventName == null) eventName = "message"; // default fallback

                                        if (!isWhitelisted(eventName)) return Flux.empty();

                                        String trimmed = trimDataPayload(node.toString());
                                        String payload = trimmed.isEmpty() ? "{}" : trimmed;
                                        ServerSentEvent<String> sse = ServerSentEvent.<String>builder()
                                                .event(eventName)
                                                .data(payload)
                                                .build();
                                        return Flux.just(sse);
                                    } catch (Exception e) {
                                        return Flux.just(ServerSentEvent.<String>builder()
                                                .event("error")
                                                .data("{\"event\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                                                .build());
                                    }
                                });
                    });
        } catch (Exception e) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data("{\"event\":\"error\",\"message\":\"build body error: " + e.getMessage() + "\"}")
                    .build());
        }
    }
}
