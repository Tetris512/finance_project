package finance_backend.service;

import finance_backend.pojo.request.difyRequest.DifyChatRequest;
import finance_backend.pojo.response.difyResponse.DifyChatResponse;
import finance_backend.pojo.vo.RequestData;

public interface DifyService {
    DifyChatResponse chatMessage(RequestData requestData);

}
