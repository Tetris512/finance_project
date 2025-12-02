// Java
package finance_backend.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class TomcatThreadLogConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers((Connector connector) -> {
            ProtocolHandler handler = connector.getProtocolHandler();
            if (handler instanceof AbstractHttp11Protocol<?> protocol) {
                int maxThreads = protocol.getMaxThreads();
                int minSpareThreads = protocol.getMinSpareThreads();
                System.out.println("[TOMCAT] maxThreads = " + maxThreads);
                System.out.println("[TOMCAT] minSpareThreads = " + minSpareThreads);
            }
        });
    }
}