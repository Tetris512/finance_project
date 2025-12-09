package finance_backend.controller;

import finance_backend.pojo.entity.UserEntity;
import finance_backend.pojo.exception.BizException;
import finance_backend.pojo.exception.CommonResponse;
import finance_backend.pojo.request.userRequest.LoginRequest;
import finance_backend.pojo.request.userRequest.RegisterRequest;
import finance_backend.pojo.vo.registerVO;
import finance_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/user/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("login")
    public CommonResponse<?> login(@Valid @RequestBody LoginRequest request) {
        // Throws BizException if auth failed.
        try {
            UserEntity userEntity = userService.login(request.getUsername(), request.getPassword());
            userEntity.setPassword(null); // 不返回密码
            CommonResponse<UserEntity> commonResponse = CommonResponse.success(userEntity);
            commonResponse.setCode(200);
            commonResponse.setMessage("登录成功");
            return commonResponse;
        } catch (BizException e) {
            CommonResponse<String> commonResponse = CommonResponse.failure(400);
            commonResponse.setMessage(e.getMessage());
            return commonResponse;
        }
    }

    @PostMapping("register")
    public CommonResponse<String> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        // Throws BizException if register failed.
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            StringBuilder sb = new StringBuilder();
            for (FieldError fieldError : fieldErrors)
                sb.append(fieldError.getDefaultMessage()).append(";");

            CommonResponse<String> commonResponse = CommonResponse.failure(400);
            commonResponse.setMessage(sb.toString());
            return commonResponse;
        }
        else
        {
            try {
                registerVO registerVO = new registerVO(request);
                UserEntity userEntity = userService.register(registerVO);
                CommonResponse<String> commonResponse = CommonResponse.success(userEntity.getUsername());
                commonResponse.setCode(200);
                commonResponse.setMessage("注册成功");
                return commonResponse;
            } catch (BizException e) {
                CommonResponse<String> commonResponse = CommonResponse.failure(400);
                commonResponse.setMessage(e.getMessage());
                return commonResponse;
            }
        }
    }

    @PostMapping("logout")
    public CommonResponse<?> logout() {
        return CommonResponse.success(200);
    }


}