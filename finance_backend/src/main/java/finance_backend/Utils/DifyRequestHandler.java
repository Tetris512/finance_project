package finance_backend.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import finance_backend.pojo.request.difyRequest.DifyChatRequest;
import finance_backend.pojo.request.difyRequest.DifyFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DifyRequestHandler {

    // 发送HTTP请求并返回响应,type为请求类型(GET/POST)。
    // 当为POST时, 可通过body参数传入JSON字符串; body可为null表示无请求体。
    public static String sendRequest(String urlString, String apiKey, String type, String body) throws IOException {
        // 创建 URL 对象
        URL url = new URL(urlString);

        // 创建连接对象
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置连接和读取超时
        connection.setConnectTimeout(25000); // 设置连接超时时间为15秒
        connection.setReadTimeout(120000);  // 设置读取超时时间为120秒

        // 设置请求方法 (GET/POST等)
        connection.setRequestMethod(type);

        // 设置 Authorization Header
        if (apiKey != null && !apiKey.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        }

        // 如果有请求体, 则设置Content-Type并写入请求体
        if (body != null && !body.isEmpty()) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            try (OutputStream os = connection.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                writer.write(body);
                writer.flush();
            }
        }

        // 读取响应码
        int responseCode = connection.getResponseCode();

        InputStream stream = null;
        if (responseCode >= 200 && responseCode < 300) {
            stream = connection.getInputStream();
        } else {
            // 读取错误流以便返回更有用的错误信息
            stream = connection.getErrorStream();
            if (stream == null) {
                throw new IOException("Request failed with response code " + responseCode);
            }
        }

        // 读取响应内容
        StringBuilder finalAnswer = new StringBuilder();
        ObjectMapper objectMapper = new ObjectMapper();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("data:")) {
                    String jsonData = inputLine.substring(5).trim();
                    try {
                        HashMap<String, Object> eventData = objectMapper.readValue(jsonData, HashMap.class);
                        if ("workflow_finished".equals(eventData.get("event"))) {
                            if (eventData.get("data") instanceof HashMap) {
                                HashMap<String, Object> dataMap = (HashMap<String, Object>) eventData.get("data");
                                if (dataMap.get("outputs") instanceof HashMap) {
                                    HashMap<String, Object> outputsMap = (HashMap<String, Object>) dataMap.get("outputs");
                                    if (outputsMap.containsKey("answer")) {
                                        finalAnswer.append(outputsMap.get("answer"));
                                        break; // 找到最终答案，退出循环
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // 忽略无法解析的行或非预期的JSON结构
                    }
                }
            }
        }

        // 返回响应内容
        if (responseCode >= 200 && responseCode < 300) {
            if (finalAnswer.length() > 0) {
                return finalAnswer.toString();
            } else {
                throw new IOException("Could not find final answer in the stream.");
            }
        } else {
            throw new IOException("Request failed with response code " + responseCode);
        }
    }

    public static void main(String[] args) {
        try {
            // 1. Create the request body object
            HashMap<String, Object> inputs = new HashMap<>();

            DifyChatRequest requestBody = new DifyChatRequest(
                    inputs,
                    "奖学金申请流程",
                    "streaming", // 改回 streaming 模式
                    "",
                    "abc-123",
                    null
            );

            // 2. Convert the object to a JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 3. Send the request and get the final answer
            String apiKey = CommonValue.apiKey; // Replace with your actual API key
            String url = "https://dify.seec.seecoder.cn/v1/chat-messages";
            String finalAnswer = sendRequest(url, apiKey, "POST", jsonBody);

            // 4. 打印最终结果
            System.out.println("解析后的回答: " + finalAnswer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
