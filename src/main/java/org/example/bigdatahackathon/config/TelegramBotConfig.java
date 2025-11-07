package org.example.bigdatahackathon.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotConfig {
    private String token;
    private String username;
    
    @PostConstruct
    public void init() {
        log.info("=== Telegram Bot Configuration ===");
        log.info("Token configured: {}", token != null && !token.isEmpty() ? "YES (hidden)" : "NO - Please set telegram.bot.token in application.properties");
        log.info("Username: {}", username != null ? username : "NOT SET");
        log.info("==================================");
        
        if (token == null || token.isEmpty() || token.equals("YOUR_BOT_TOKEN_HERE")) {
            log.error("⚠️  TELEGRAM BOT TOKEN NOT CONFIGURED!");
            log.error("⚠️  Please set telegram.bot.token in application.properties");
            log.error("⚠️  Get your token from @BotFather in Telegram");
        }
    }
}

