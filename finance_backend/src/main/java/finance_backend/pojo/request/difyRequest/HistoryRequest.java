package finance_backend.pojo.request.difyRequest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * /api/history 请求体
 */
@Data
public class HistoryRequest {

    /**
     * 用户标识
     */
    @NotBlank
    private String user;

    /**
     * 返回的历史条数，默认为 20，可缺省
     */
    private Integer limit = 20;
}

