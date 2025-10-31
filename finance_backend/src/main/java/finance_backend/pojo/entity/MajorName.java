package finance_backend.pojo.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Major names placeholder. Update enum values to your actual majors.
 */
public enum MajorName {
    STUDENT,
    TEACHER;

    @JsonCreator
    public static MajorName from(String value) {
        if (value == null) return null;
        try {
            return MajorName.valueOf(value.trim().toUpperCase().replaceAll("\\s+","_"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown MajorName value: '" + value + "'");
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
