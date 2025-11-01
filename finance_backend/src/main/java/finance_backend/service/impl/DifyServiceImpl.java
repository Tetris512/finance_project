package finance_backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import finance_backend.Utils.CommonValue;
import finance_backend.Utils.DifyRequestHandler;
import finance_backend.pojo.request.difyRequest.DifyChatRequest;
import finance_backend.pojo.response.difyResponse.DifyChatResponse;
import finance_backend.pojo.vo.RequestData;
import finance_backend.service.DifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DifyServiceImpl implements DifyService {

    private final DifyRequestHandler difyRequestHandler;

    @Override
    public DifyChatResponse chatMessage(RequestData request) {
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
            // files: if null -> "", else keep list
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
            String response = difyRequestHandler.sendRequest(url, CommonValue.apiKey, CommonValue.REQUEST_POST, jsonBody);

            if (response == null || response.trim().isEmpty()) {
                return null;
            }

            DifyChatResponse difyChatResponse = new DifyChatResponse();
            difyChatResponse.setAnswer(response);
            return difyChatResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
