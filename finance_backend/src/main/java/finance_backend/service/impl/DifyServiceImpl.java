package finance_backend.service.impl;

import finance_backend.pojo.request.difyRequest.DifyChatRequest;
import finance_backend.pojo.response.difyResponse.DifyChatResponse;
import finance_backend.service.DifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DifyServiceImpl implements DifyService {
    @Override
    public DifyChatResponse chatMessage(DifyChatRequest request) {

        return null;
    }
}
