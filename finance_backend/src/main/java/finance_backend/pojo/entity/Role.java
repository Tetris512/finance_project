package finance_backend.pojo.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Role for a user: either STUDENT or TEACHER.
 */
public enum Role {
    STUDENT,
    TEACHER;

    @JsonCreator
    public static Role from(String value) {
        if (value == null) return null;
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown Role value: '" + value + "'");
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
