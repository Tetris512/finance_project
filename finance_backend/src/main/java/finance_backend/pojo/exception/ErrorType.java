package finance_backend.pojo.exception;

public interface ErrorType {
    int getCode();
    String getMessage();
    int getHttpCode();
}
