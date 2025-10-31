package finance_backend.pojo.request.difyRequest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DifyFile {

    private String type;

    @JsonProperty("transfer_method")
    private String transferMethod;

    private String url;

    public DifyFile(String type, String transferMethod, String url) {
        this.type = type;
        this.transferMethod = transferMethod;
        this.url = url;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTransferMethod() {
        return transferMethod;
    }

    public void setTransferMethod(String transferMethod) {
        this.transferMethod = transferMethod;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}