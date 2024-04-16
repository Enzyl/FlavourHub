package pl.dlusk.api.appLinkShower;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ServerInfoLogger implements ApplicationListener<WebServerInitializedEvent> {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        System.out.println("Aplikacja dostępna pod adresem: http://localhost:" + port + contextPath);
    }
}
