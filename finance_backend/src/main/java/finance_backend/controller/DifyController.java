package finance_backend.controller;

import finance_backend.pojo.exception.CommonResponse;
import finance_backend.pojo.request.difyRequest.DifyChatRequest;
import finance_backend.pojo.response.difyResponse.DifyChatResponse;
import finance_backend.pojo.vo.DifyChatVO;
import finance_backend.service.DifyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DifyController {
    private final DifyService difyService;

    @PostMapping("/chat")
    public CommonResponse<?> chat(@Valid @RequestBody DifyChatRequest request) {
        // Throws BizException if auth failed.
        DifyChatVO difyChatVO = new DifyChatVO(request);
        DifyChatResponse difyChatResponse = difyService.chatMessage(difyChatVO);
        if (difyChatResponse == null) {
            return CommonResponse.failure(400);
        } else {
            CommonResponse<?> commonResponse = CommonResponse.success(difyChatResponse);
            commonResponse.setCode(200);
            return commonResponse;
        }
    }
}
