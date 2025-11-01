package finance_backend.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public String sendRequest(String urlString, String apiKey, String type, String body) throws IOException {
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
        requestBuilder.post(requestBody);

        Request request = requestBuilder.build();

        System.out.println("Headers:");
        request.headers().toMultimap().forEach((key, value) -> System.out.println("  " + key + ": " + value));
        System.out.println("--- End of request details ---\n");

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                ResponseBody errBody = response.body();
                String err = errBody != null ? errBody.string() : "";
                throw new IOException("Request failed with response code " + response.code() + ". Error: " + err);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) return "";

            String contentType = response.header("Content-Type", "");
            if (contentType != null && contentType.contains("text/event-stream")) {
                StringBuilder finalAnswer = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(responseBody.charStream())) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String raw = line.trim();
                        if (raw.isEmpty()) continue;

                        // Ignore explicit event type lines like: event: ping
                        if (raw.startsWith("event:")) continue;

                        // Handle frames starting with data:
                        String payload = raw.startsWith("data:") ? raw.substring(5).trim() : raw;

                        // Try to parse JSON; skip if not JSON
                        Map<String, Object> eventData;
                        try {
                            eventData = objectMapper.readValue(payload, Map.class);
                        } catch (Exception ignore) {
                            continue;
                        }

                        // Extract text from common places
                        // 1) top-level "answer"
                        Object topAnswer = eventData.get("answer");
                        if (topAnswer instanceof String) {
                            finalAnswer.append((String) topAnswer);
                        }
                        // 2) data.outputs.answer or data.outputs.text
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

                        // Stop at message_end
                        Object evt = eventData.get("event");
                        if (evt != null && "message_end".equals(evt.toString())) {
                            break;
                        }
                    }
                }
                return finalAnswer.toString();
            } else {
                // Non-SSE path
                try (ResponseBody rb = responseBody) {
                    return rb.string();
                }
            }
        }
    }
}