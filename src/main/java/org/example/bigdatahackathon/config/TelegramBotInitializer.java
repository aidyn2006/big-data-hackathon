package org.example.bigdatahackathon.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bigdatahackathon.service.ComplaintTelegramBot;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotInitializer {
    
    private final ComplaintTelegramBot complaintTelegramBot;
    private final TelegramBotConfig botConfig;
    
    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        try {
            // Check if token is configured
            if (botConfig.getToken() == null || 
                botConfig.getToken().isEmpty() || 
                botConfig.getToken().equals("YOUR_BOT_TOKEN_HERE")) {
                log.error("❌ Telegram Bot НЕ ЗАПУЩЕН - Token не настроен!");
                log.error("📝 Инструкция:");
                log.error("   1. Откройте Telegram и найдите @BotFather");
                log.error("   2. Отправьте команду: /newbot");
                log.error("   3. Следуйте инструкциям и получите токен");
                log.error("   4. Добавьте в application.properties:");
                log.error("      telegram.bot.token=ВАШ_ТОКЕН");
                log.error("      telegram.bot.username=ВАШ_БОТ_USERNAME");
                log.error("");
                log.error("⚠️  БОТ НЕ БУДЕТ РАБОТАТЬ БЕЗ ТОКЕНА!");
                return;
            }
            
            log.info("🚀 Запуск Telegram бота...");
            
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(complaintTelegramBot);
            
            log.info("✅ ✅ ✅ Telegram бот УСПЕШНО ЗАПУЩЕН! ✅ ✅ ✅");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("🤖 Имя бота: @{}", botConfig.getUsername());
            log.info("📱 Бот готов принимать сообщения!");
            log.info("💬 Откройте Telegram и найдите @{}", botConfig.getUsername());
            log.info("🎯 Отправьте /start для начала работы");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (TelegramApiException e) {
            log.error("❌ Ошибка при запуске Telegram бота: {}", e.getMessage());
            log.error("Возможные причины:");
            log.error("  1. Неверный токен бота");
            log.error("  2. Проблемы с подключением к Telegram API");
            log.error("  3. Бот уже запущен в другом месте");
            log.error("");
            log.error("Проверьте токен в application.properties");
        }
    }
}

