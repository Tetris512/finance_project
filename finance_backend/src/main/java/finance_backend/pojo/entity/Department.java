package finance_backend.pojo.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Department placeholder. Update enum values to your actual departments.
 */
public enum Department {
    STUDENT,
    TEACHER;

    @JsonCreator
    public static Department from(String value) {
        if (value == null) return null;
        try {
            return Department.valueOf(value.trim().toUpperCase().replaceAll("\\s+","_"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown Department value: '" + value + "'");
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
