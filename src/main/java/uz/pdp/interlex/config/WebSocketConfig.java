package uz.pdp.interlex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // /topic prefiksi bilan boshlanadigan xabarlar brokerga yuboriladi
        config.setApplicationDestinationPrefixes("/app"); // /app prefiksi bilan boshlanadigan xabarlar controllerga yuboriladi
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*") // to'g'ri ishlaydi, chunki credentials ishlamayapti
                .withSockJS();
    }

}
