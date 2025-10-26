package finance_backend.pojo.exception;

public class CommonResponse<T> {
    private int code;
    private String message;
    private T data;

    public CommonResponse() {
    }

    public CommonResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Static factories
    public static <T> CommonResponse<T> success() {
        return new CommonResponse<T>();
    }

    public static <T> CommonResponse<T> success(T data) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setData(data);
        return response;
    }

    public static <T> CommonResponse<T> success(int code) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setCode(code);
        return response;
    }

    public static <T> CommonResponse<T> failure() {
        return new CommonResponse<T>();
    }

    public static <T> CommonResponse<T> failure(T data) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setData(data);
        return response;
    }

    public static <T> CommonResponse<T> failure(int code) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setCode(code);
        return response;
    }

    // Explicit getters/setters to avoid relying on Lombok
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CommonResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
