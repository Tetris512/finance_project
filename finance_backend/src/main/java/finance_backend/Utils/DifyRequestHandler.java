package finance_backend.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import finance_backend.pojo.response.difyResponse.DifyChatResponse;
import finance_backend.pojo.vo.Conversation;
import finance_backend.pojo.vo.MessageItem;
import lombok.Getter;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DifyRequestHandler {

    /**
     * -- GETTER --
     *  暴露内部 OkHttpClient，便于其他 Service 复用同一个客户端实例。
     */
    @Getter
    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DifyRequestHandler() {
        this.client = new OkHttpClient.Builder()
                .protocols(java.util.Arrays.asList(Protocol.HTTP_1_1))
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .callTimeout(5, TimeUnit.MINUTES)
                .build();
    }

    /**
     * 发送请求到 Dify，不在这里关闭 Response，由调用方负责关闭或交给 handleChatResponse 处理。
     */
    public Response postRequest(String urlString, String apiKey, String type, String body) throws IOException {
        System.out.println("\n--- Preparing to send Dify request ---");
        System.out.println("URL: " + urlString);
        System.out.println("Request Body: " + body);

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlString)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", "PostmanRuntime/7.49.1")
                .addHeader("Accept", "text/event-stream")
                // 不手动设置 Accept-Encoding，交给 OkHttp 透明解压
                .addHeader("Connection", "keep-alive");

        RequestBody requestBody = RequestBody.create(body, MediaType.get("application/json; charset=utf-8"));
        if (CommonValue.REQUEST_POST.equals(type)) {
            requestBuilder.post(requestBody);
        } else {
            throw new IllegalArgumentException("Unsupported request type: " + type);
        }

        Request request = requestBuilder.build();

        System.out.println("Headers:");
        request.headers().toMultimap().forEach((key, value) -> System.out.println("  " + key + ": " + value));
        System.out.println("--- End of request details ---\n");

        Response response = client.newCall(request).execute();
        System.out.println("[HTTP] Response code=" + response.code() + ", Content-Type=" + response.header("Content-Type") + ", Content-Encoding=" + response.header("Content-Encoding"));
        if (!response.isSuccessful()) {
            ResponseBody errBody = response.body();
            String err = errBody != null ? errBody.string() : "";
            response.close();
            throw new IOException("Request failed with response code " + response.code() + ". Error: " + err);
        }

        return response;
    }

    /**
     * 仿造 postRequest 的 GET 版本：
     * - 使用同样的通用请求头（Authorization / User-Agent / Accept 等）
     * - 支持通过可变参数追加查询参数，形式为 key1, value1, key2, value2 ...
     *   示例：getRequest(url, apiKey, "user", "tetris512", "limit", 20)
     */
    public Response getRequest(String urlString, String apiKey, Object... queryParams) throws IOException {
        System.out.println("\n--- Preparing to send Dify GET request ---");

        // 使用 HttpUrl 安全构造带 query 的 URL
        HttpUrl baseUrl = HttpUrl.parse(urlString);
        if (baseUrl == null) {
            throw new IllegalArgumentException("Invalid URL: " + urlString);
        }
        HttpUrl.Builder urlBuilder = baseUrl.newBuilder();

        if (queryParams != null && queryParams.length > 0) {
            if (queryParams.length % 2 != 0) {
                throw new IllegalArgumentException("queryParams length must be even: key1, value1, key2, value2, ...");
            }
            for (int i = 0; i < queryParams.length; i += 2) {
                Object keyObj = queryParams[i];
                Object valueObj = queryParams[i + 1];
                if (keyObj == null || valueObj == null) {
                    continue; // 跳过 null 的键值对
                }
                String key = keyObj.toString();
                String value = valueObj.toString();
                urlBuilder.addQueryParameter(key, value);
            }
        }

        HttpUrl finalUrl = urlBuilder.build();
        System.out.println("URL: " + finalUrl);

        Request.Builder requestBuilder = new Request.Builder()
                .url(finalUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", "PostmanRuntime/7.49.1")
                .addHeader("Accept", "application/json")
                // 不手动设置 Accept-Encoding，交给 OkHttp 透明解压
                .addHeader("Connection", "keep-alive")
                .get();

        Request request = requestBuilder.build();

        System.out.println("Headers:");
        request.headers().toMultimap().forEach((key, value) -> System.out.println("  " + key + ": " + value));
        System.out.println("--- End of GET request details ---\n");

        Response response = client.newCall(request).execute();
        System.out.println("[HTTP] Response code=" + response.code() + ", Content-Type=" + response.header("Content-Type") + ", Content-Encoding=" + response.header("Content-Encoding"));
        if (!response.isSuccessful()) {
            ResponseBody errBody = response.body();
            String err = errBody != null ? errBody.string() : "";
            response.close();
            throw new IOException("GET request failed with response code " + response.code() + ". Error: " + err);
        }

        return response;
    }

    /**
     * 统一处理 Dify 响应：
     * - Content-Type 为 text/event-stream 时解析 SSE 流，拼出最终 answer
     * - 否则按 JSON 一次性解析
     */
    public DifyChatResponse handleChatResponse(Response response) throws IOException {
        try (Response res = response) {
            DifyChatResponse chatResponse = new DifyChatResponse();
            ResponseBody responseBody = res.body();
            if (responseBody == null) {
                chatResponse.setAnswer("");
                return chatResponse;
            }

            String contentType = res.header("Content-Type", "");
            if (contentType != null && contentType.contains("text/event-stream")) {
                StringBuilder finalAnswer = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(responseBody.charStream())) {
                    String line;
                    while (true) {
                        try {
                            line = reader.readLine();
                        } catch (IOException | IllegalStateException e) {
                            // 上游主动断开或底层 source 已关闭，视为流结束
                            break;
                        }

                        if (line == null) {
                            // 正常读到 EOF
                            break;
                        }

                        String raw = line.trim();
                        if (raw.isEmpty()) continue;

                        // 忽略 event: ping 等事件类型行
                        if (raw.startsWith("event:")) continue;

                        String payload = raw.startsWith("data:") ? raw.substring(5).trim() : raw;

                        Map<String, Object> eventData;
                        try {
                            eventData = objectMapper.readValue(payload, Map.class);
                        } catch (Exception ignore) {
                            continue;
                        }

                        // 1) 顶层 answer
                        Object topAnswer = eventData.get("answer");
                        if (topAnswer instanceof String) {
                            finalAnswer.append((String) topAnswer);
                        }
                        // 2) data.outputs.answer 或 data.outputs.text
                        Object dataObj = eventData.get("data");
                        if (dataObj instanceof Map) {
                            Object outputs = ((Map<?, ?>) dataObj).get("outputs");
                            if (outputs instanceof Map) {
                                Object ans = ((Map<?, ?>) outputs).get("answer");
                                if (ans instanceof String) finalAnswer.append((String) ans);
                                Object text = ((Map<?, ?>) outputs).get("text");
                                if (text instanceof String) finalAnswer.append((String) text);
                            }
                        }

                        Object convId = eventData.get("conversation_id");
                        if (convId != null) {
                            chatResponse.setConversation_id(convId.toString());
                        }
                        Object msgId = eventData.get("message_id");
                        if (msgId != null) {
                            chatResponse.setMessage_id(msgId.toString());
                        }

                        Object evt = eventData.get("event");
                        if (evt != null && "message_end".equals(evt.toString())) {
                            break;
                        }
                    }
                }

                chatResponse.setAnswer(finalAnswer.toString());
                return chatResponse;
            } else {
                // 非 SSE：一次性读取 body 并尝试按 JSON 解析
                String bodyStr = responseBody.string();
                try {
                    Map<String, Object> root = objectMapper.readValue(bodyStr, Map.class);
                    Object dataObj = root.get("data");
                    if (dataObj instanceof Map) {
                        Object answer = ((Map<?, ?>) dataObj).get("answer");
                        if (answer instanceof String) {
                            chatResponse.setAnswer((String) answer);
                        }
                        Object convId = ((Map<?, ?>) dataObj).get("conversation_id");
                        if (convId != null) chatResponse.setConversation_id(convId.toString());
                        Object msgId = ((Map<?, ?>) dataObj).get("message_id");
                        if (msgId != null) chatResponse.setMessage_id(msgId.toString());
                    }
                } catch (Exception e) {
                    // 如果不是预期 JSON 结构，就直接返回原始 body
                    chatResponse.setAnswer(bodyStr);
                }

                return chatResponse;
            }
        }
    }

    public List<Conversation> getConversationsByUser(String user, String apiKey, String url) throws IOException {
        Response response = getRequest(url, apiKey, "user", user);
        try (Response res = response) {
            ResponseBody responseBody = res.body();
            if (responseBody == null) {
                System.out.println("[GET CONVS] empty body");
                return List.of();
            }

            String bodyStr = responseBody.string();
            System.out.println("[GET CONVS] raw body: " + bodyStr);
            try {
                Map<String, Object> root = objectMapper.readValue(bodyStr, Map.class);
                Object dataObj = root.get("data");

                List<Conversation> result = new ArrayList<>();

                // data 是数组：遍历并构造 Conversation 列表
                if (dataObj instanceof List<?> list) {
                    for (Object elem : list) {
                        if (!(elem instanceof Map<?, ?> item)) continue;
                        String convId = toStringSafe(item.get("conversation_id"));
                        if (convId == null || convId.isEmpty()) convId = toStringSafe(item.get("id"));
                        if (convId == null || convId.isEmpty()) continue; // 跳过无 id 的项

                        String title = toStringSafe(item.get("name"));
                        if (title == null) title = toStringSafe(item.get("title"));

                        Conversation conversation = new Conversation();
                        conversation.setConversation_id(convId);
                        conversation.setTitle(title == null ? "" : title);
                        result.add(conversation);
                    }
                    return result;
                }

                // data 是单个对象：构造单元素列表
                if (dataObj instanceof Map<?, ?> dataMap) {
                    String convId = toStringSafe(dataMap.get("conversation_id"));
                    if (convId == null || convId.isEmpty()) convId = toStringSafe(dataMap.get("id"));
                    if (convId != null && !convId.isEmpty()) {
                        String title = toStringSafe(dataMap.get("name"));
                        if (title == null) title = toStringSafe(dataMap.get("title"));

                        Conversation conversation = new Conversation();
                        conversation.setConversation_id(convId);
                        conversation.setTitle(title == null ? "" : title);
                        return List.of(conversation);
                    }
                }

                System.out.println("[GET CONVS] unrecognized response shape");
                return List.of();
            } catch (Exception parseEx) {
                System.out.println("[GET CONVS] parse error: " + parseEx.getMessage());
                return List.of();
            }
        }
    }

    public List<MessageItem> getMessagesByConversation(String user, String conversationId, String apiKey, String url) throws IOException {
        // GET /messages?user=...&conversation_id=...
        Response response = getRequest(url, apiKey, "user", user, "conversation_id", conversationId);
        try (Response res = response) {
            ResponseBody responseBody = res.body();
            if (responseBody == null) {
                System.out.println("[GET MSGS] empty body");
                return List.of();
            }
            String bodyStr = responseBody.string();
            System.out.println("[GET MSGS] raw body: " + bodyStr);
            try {
                Map<String, Object> root = objectMapper.readValue(bodyStr, Map.class);
                Object dataObj = root.get("data");

                List<MessageItem> result = new ArrayList<>();
                if (dataObj instanceof List<?> list) {
                    for (Object elem : list) {
                        if (!(elem instanceof Map<?, ?> m)) continue;
                        String id = toStringSafe(m.get("id"));
                        String userText = toStringSafe(m.get("query"));
                        String answerText = toStringSafe(m.get("answer"));

                        MessageItem item = new MessageItem();
                        item.setId(id != null ? id : UUID.randomUUID().toString());
                        item.setQuery(userText != null ? userText : "");
                        item.setAnswer(answerText != null ? answerText : "");
                        result.add(item);
                    }
                    return result;
                }
                if (dataObj instanceof Map<?, ?> m) {
                    String id = toStringSafe(m.get("id"));
                    String userText = toStringSafe(m.get("query"));
                    String answerText = toStringSafe(m.get("answer"));

                    MessageItem item = new MessageItem();
                    item.setId(id != null ? id : UUID.randomUUID().toString());
                    item.setQuery(userText != null ? userText : "");
                    item.setAnswer(answerText != null ? answerText : "");
                    result.add(item);
                    return result;
                }
                // 顶层兜底
                String id = toStringSafe(root.get("id"));
                String userText = toStringSafe(root.get("query"));
                String answerText = toStringSafe(root.get("answer"));
                if (userText != null || answerText != null) {
                    MessageItem item = new MessageItem();
                    item.setId(id != null ? id : UUID.randomUUID().toString());
                    item.setQuery(userText != null ? userText : "");
                    item.setAnswer(answerText != null ? answerText : "");
                    return List.of(item);
                }
                return List.of();
            } catch (Exception e) {
                System.out.println("[GET MSGS] parse error: " + e.getMessage());
                return List.of();
            }
        }
    }

    private String toStringSafe(Object v) {
        return v == null ? null : v.toString();
    }

}
