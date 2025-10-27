package finance_backend.pojo.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Role for a user: either STUDENT or TEACHER.
 */
public enum Role {
    STUDENT,
    TEACHER
}

