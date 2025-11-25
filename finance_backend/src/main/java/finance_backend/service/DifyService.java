package finance_backend.service;

import finance_backend.pojo.response.difyResponse.DifyChatResponse;
import finance_backend.pojo.vo.DifyChatVO;

public interface DifyService {
    DifyChatResponse chatMessage(DifyChatVO requestData);

}
