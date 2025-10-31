package finance_backend.service;

import finance_backend.pojo.request.difyRequest.DifyChatRequest;
import finance_backend.pojo.response.difyResponse.DifyChatResponse;

public interface DifyService {
    DifyChatResponse chatMessage(DifyChatRequest request);

}
