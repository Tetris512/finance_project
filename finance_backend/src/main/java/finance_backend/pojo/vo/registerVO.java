package finance_backend.pojo.vo;

import finance_backend.pojo.entity.Department;
import finance_backend.pojo.entity.MajorName;
import finance_backend.pojo.entity.Role;
import finance_backend.pojo.request.userRequest.RegisterRequest;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class registerVO {

    public registerVO(RegisterRequest registerRequest) {
        this.username = registerRequest.getUsername();
        this.password = registerRequest.getPassword();
        this.role = registerRequest.getRole();
        this.department = registerRequest.getDepartment();
        this.majorName = registerRequest.getMajorName();
    }

    private String username;

    private String password;
    // STUDENT or TEACHER
    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Department department;

    @Enumerated(EnumType.STRING)
    private MajorName majorName;
}
