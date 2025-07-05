package uz.pdp.interlex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")// Barcha frontend domenlarga ruxsat beradi
                .allowedMethods("*")             // GET, POST, PUT, DELETE va h.k.
                .allowedHeaders("*")             // Har qanday headerga ruxsat
                .allowCredentials(true);         // Keyinchalik cookie yoki JWT yuborish uchun kerak bo‘ladi
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
