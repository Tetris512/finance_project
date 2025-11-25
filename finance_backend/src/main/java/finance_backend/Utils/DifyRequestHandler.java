package finance_backend.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import finance_backend.pojo.response.difyResponse.DifyChatResponse;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DifyRequestHandler {

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
    public Response sendRequest(String urlString, String apiKey, String type, String body) throws IOException {
        System.out.println("\n--- Preparing to send Dify request ---");
        System.out.println("URL: " + urlString);
        System.out.println("Request Body: " + body);

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlString)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", "PostmanRuntime/7.49.1")
                .addHeader("Accept", "text/event-stream")
                .addHeader("Accept-Encoding", "gzip, deflate")
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
        if (!response.isSuccessful()) {
            ResponseBody errBody = response.body();
            String err = errBody != null ? errBody.string() : "";
            response.close();
            throw new IOException("Request failed with response code " + response.code() + ". Error: " + err);
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
}