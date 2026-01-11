package uz.pdp.interlex.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.Base64;

@Component
public class StartupChecks {

    private final Logger log = LoggerFactory.getLogger(StartupChecks.class);

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${server.ssl.key-store:}")
    private String keyStorePath;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        // SSL checks
        if (sslEnabled) {
            log.info("SSL is enabled (server.ssl.enabled=true). Verifying keystore: {}", keyStorePath);
            if (keyStorePath == null || keyStorePath.isBlank()) {
                throw new IllegalStateException("SSL is enabled but 'server.ssl.key-store' is not configured.");
            }

            try {
                ClassPathResource res = new ClassPathResource(keyStorePath.replace("classpath:", ""));
                if (!res.exists()) {
                    throw new IllegalStateException("SSL is enabled but keystore not found at: " + keyStorePath);
                }
            } catch (Exception ex) {
                throw new IllegalStateException("SSL keystore validation failed: " + ex.getMessage(), ex);
            }
        }

        // JWT secret checks
        if (jwtSecret == null || jwtSecret.isBlank()) {
            log.warn("JWT secret is empty. The application will not start in production without a secure jwt.secret");
        } else {
            try {
                byte[] decoded = Base64.getDecoder().decode(jwtSecret);
                if (decoded.length < 32) {
                    log.warn("JWT secret decodes to less than 256 bits ({} bytes). Consider using a stronger secret.", decoded.length);
                }
            } catch (IllegalArgumentException ex) {
                // Not valid base64 - still warn but allow startup
                if (jwtSecret.length() < 32) {
                    log.warn("JWT secret looks weak (short plain string). Consider replacing with a Base64-encoded 256+ bit key.");
                } else {
                    log.info("JWT secret is present (non-base64). Consider using Base64-encoded secret for stronger key handling.");
                }
            }
        }

        log.info("Startup checks completed.");
    }
}
