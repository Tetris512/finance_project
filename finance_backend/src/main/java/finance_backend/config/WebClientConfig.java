package finance_backend.config;

import finance_backend.Utils.CommonValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient difyWebClient() {
        return WebClient.builder()
                .baseUrl(CommonValue.BASE_DIFY_URL)
                .defaultHeader(HttpHeaders.USER_AGENT, "PostmanRuntime/7.49.1")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")
                .defaultHeader(HttpHeaders.CONNECTION, "keep-alive")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + CommonValue.apiKey)
                .build();
    }
}

