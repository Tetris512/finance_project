package finance_backend.controller;

import finance_backend.pojo.entity.UserEntity;
import finance_backend.pojo.exception.CommonResponse;
import finance_backend.pojo.request.difyRequest.DifyChatRequest;
import finance_backend.pojo.vo.RequestData;
import finance_backend.service.DifyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/dify/")
@RequiredArgsConstructor
public class DifyController {
    private final DifyService difyService;

    @PostMapping("chat-messages")
    public CommonResponse<?> chat(@Valid @RequestBody DifyChatRequest request) {
        // Throws BizException if auth failed.
        RequestData requestData = new RequestData(request);
        if(userEntity == null) {
            return CommonResponse.failure(400);
        } else{
            CommonResponse<UserEntity> commonResponse = CommonResponse.success(userEntity);
            commonResponse.setCode(200);
            return commonResponse;
        }
    }
}
