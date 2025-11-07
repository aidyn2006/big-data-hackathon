package org.example.bigdatahackathon.service;

import lombok.extern.slf4j.Slf4j;
import org.example.bigdatahackathon.config.TelegramBotConfig;
import org.example.bigdatahackathon.entity.Complaint;
import org.example.bigdatahackathon.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ComplaintTelegramBot extends TelegramLongPollingBot {
    
    private final TelegramBotConfig botConfig;
    private final TelegramBotService telegramBotService;
    private final ComplaintRepository complaintRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    
    // Хранение состояния пользователей (ожидают ли они ввод жалобы)
    private final Map<Long, Boolean> waitingForComplaint = new java.util.concurrent.ConcurrentHashMap<>();
    
    @Autowired
    public ComplaintTelegramBot(TelegramBotConfig botConfig, 
                                TelegramBotService telegramBotService,
                                ComplaintRepository complaintRepository) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.telegramBotService = telegramBotService;
        this.complaintRepository = complaintRepository;
        
        log.info("=== Telegram Bot Starting ===");
        log.info("Bot Username: {}", botConfig.getUsername());
        log.info("Bot Token: {}", botConfig.getToken() != null ? "Configured" : "NOT CONFIGURED");
        log.info("============================");
        
        if (botConfig.getToken() == null || botConfig.getToken().isEmpty() || 
            botConfig.getToken().equals("YOUR_BOT_TOKEN_HERE")) {
            log.error("❌ TELEGRAM BOT CANNOT START - TOKEN NOT SET!");
            log.error("Please follow these steps:");
            log.error("1. Open Telegram and find @BotFather");
            log.error("2. Send /newbot command");
            log.error("3. Follow instructions and get your token");
            log.error("4. Add to application.properties:");
            log.error("   telegram.bot.token=YOUR_TOKEN_HERE");
            log.error("   telegram.bot.username=YOUR_BOT_USERNAME");
        } else {
            log.info("✅ Telegram bot initialized successfully: @{}", botConfig.getUsername());
        }
    }
    
    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();
                String username = update.getMessage().getFrom().getUserName();
                String firstName = update.getMessage().getFrom().getFirstName();
                
                log.info("📨 Received message from {} (@{}): {}", firstName, username, messageText);
                
                if (messageText.startsWith("/")) {
                    handleCommand(messageText, chatId, username);
                } else {
                    // Проверяем, ожидается ли ввод жалобы
                    if (waitingForComplaint.getOrDefault(chatId, false)) {
                        handleComplaintText(messageText, chatId, username);
                        waitingForComplaint.remove(chatId); // Убираем состояние ожидания
                    } else {
                        // Если не в режиме жалобы, показываем подсказку
                        sendMessage(chatId, """
                            💡 <b>Жалоба жіберу үшін:</b>
                            
                            Төмендегі батырманы басыңыз немесе команданы жіберіңіз:
                            • /complaint - жалоба жіберу
                            • /mycomplaints - менің жалобаларым
                            • /help - анықтама
                            """);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }
    
    private void handleCommand(String command, long chatId, String username) {
        String cmd = command.toLowerCase().trim();
        
        switch (cmd) {
            case "/start":
                waitingForComplaint.remove(chatId); // Сброс состояния
                sendWelcomeMessage(chatId);
                break;
            case "/help", "/help@":
                sendHelpMessage(chatId);
                break;
            case "/complaint", "/zhaloba", "/жалоба":
                waitingForComplaint.put(chatId, true);
                sendMessage(chatId, """
                    📝 <b>Жалоба жіберу режимі</b>
                    
                    Енді қоғамдық көліктегі мәселені жазыңыз.
                    
                    <i>Мысалы:</i>
                    • 65 автобус өте ескі, жүргізуші дөрекі
                    • 12 маршрут 30 минутқа кешігеді
                    • Троллейбус 4 кондиционер істемейді
                    
                    Болдырмау үшін: /cancel
                    """);
                break;
            case "/cancel", "/болдырмау":
                if (waitingForComplaint.containsKey(chatId)) {
                    waitingForComplaint.remove(chatId);
                    sendMessage(chatId, "✅ Жалоба жіберу болдырылды");
                } else {
                    sendMessage(chatId, "Болдырмайтын ештеңе жоқ");
                }
                break;
            case "/mycomplaints", "менің жалобаларым", "мои жалобы":
                sendUserComplaints(chatId, username);
                break;
            default:
                sendMessage(chatId, "❓ Белгісіз команда. /help командасын қараңыз");
        }
    }
    
    private void sendWelcomeMessage(long chatId) {
        String welcomeText = """
            👋 <b>Қош келдіңіз!</b>
            
            Мен қоғамдық көліктегі мәселелерді тіркейтін AI боттың.
            
            🤖 <b>Мүмкіндіктер:</b>
            • Жалоба жіберу
            • Маршрут, орын және басымдықты автоматты анықтау
            • Жалобаларыңызды қадағалау
            
            📝 <b>Жалоба жіберу үшін:</b>
            Төмендегі батырманы басыңыз немесе /complaint командасын жіберіңіз.
            
            <i>Мысалы:</i>
            "65 автобус өте ескі, жүргізуші дөрекі"
            
            Қосымша мәліметтер үшін /help командасын қараңыз.
            """;
        
        sendMessageWithKeyboard(chatId, welcomeText);
    }
    
    private void sendHelpMessage(long chatId) {
        String helpText = """
            📚 <b>Анықтама</b>
            
            <b>Қолжетімді командалар:</b>
            /start - Ботты қайта іске қосу
            /complaint - Жалоба жіберу
            /mycomplaints - Менің жалобаларым
            /cancel - Жалоба жіберуді болдырмау
            /help - Анықтама
            
            <b>Жалоба қалай жіберу керек?</b>
            1. /complaint командасын жіберіңіз немесе "Жалоба жіберу" батырмасын басыңыз
            2. Мәселені жазыңыз
            
            AI автоматты түрде:
            • Маршрутты анықтайды
            • Орынды табады
            • Басымдықты белгілейді
            • Жауапты тұлғаны анықтайды
            
            <b>Мысалдар:</b>
            ✅ "12 автобус 30 минутқа кешігеді"
            ✅ "95 маршрут жүргізушісі өте тез жүреді"
            ✅ "Троллейбус 4 кондиционер істемейді"
            
            💡 Егер сұрақтарыңыз болса, техникалық қолдау қызметіне хабарласыңыз.
            """;
        
        sendMessage(chatId, helpText);
    }
    
    private void handleComplaintText(String text, long chatId, String username) {
        // Отправляем сообщение о обработке
        sendMessage(chatId, "⏳ Жалоба өңделуде...");
        
        // Отправляем на webhook асинхронно
        telegramBotService.processComplaintText(text, username)
            .subscribe(
                response -> {
                    String formattedResponse = telegramBotService.formatResponse(response);
                    sendMessage(chatId, formattedResponse);
                },
                error -> {
                    log.error("Error processing complaint", error);
                    sendMessage(chatId, "❌ Қате орын алды. Кейінірек қайталап көріңіз.");
                }
            );
    }
    
    private void sendUserComplaints(long chatId, String username) {
        if (username == null || username.isEmpty()) {
            sendMessage(chatId, "❌ Жалобаларды көру үшін Telegram username-ңіз керек.");
            return;
        }
        
        try {
            List<Complaint> complaints = complaintRepository.findByCreatedByOrderByCreatedAtDesc(username);
            
            if (complaints.isEmpty()) {
                sendMessage(chatId, """
                    📭 <b>Сіздің жалобаларыңыз жоқ</b>
                    
                    Жаңа жалоба жіберу үшін, мәселені осы чатқа жазыңыз.
                    """);
                return;
            }
            
            StringBuilder message = new StringBuilder();
            message.append(String.format("📋 <b>Сіздің жалобаларыңыз (%d):</b>\n\n", complaints.size()));
            
            int count = 0;
            for (Complaint c : complaints) {
                if (count >= 10) { // Показываем только последние 10
                    message.append(String.format("\n<i>... және тағы %d жалоба</i>", complaints.size() - 10));
                    break;
                }
                
                count++;
                message.append(String.format("<b>%d.</b> ", count));
                
                if (c.getRoute() != null) {
                    message.append(String.format("🚌 <b>%s</b> маршрут\n", c.getRoute()));
                }
                
                if (c.getPlace() != null) {
                    message.append(String.format("📍 %s\n", c.getPlace()));
                }
                
                String priority = c.getPriority();
                if (priority != null) {
                    String emoji = getPriorityEmoji(priority);
                    message.append(String.format("%s %s\n", emoji, priority));
                }
                
                message.append(String.format("📅 %s\n", 
                    c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_FORMATTER) : "—"));
                
                message.append(String.format("📊 Статус: %s\n", 
                    formatStatus(c.getStatus())));
                
                // Показываем краткий текст жалобы
                String shortText = c.getRawText();
                if (shortText != null && shortText.length() > 80) {
                    shortText = shortText.substring(0, 77) + "...";
                }
                if (shortText != null) {
                    message.append(String.format("<i>%s</i>\n", shortText));
                }
                
                message.append("\n");
            }
            
            message.append("\n💡 Жалобалар тізімі соңғы өзгерістер бойынша көрсетілген.");
            
            sendMessage(chatId, message.toString());
            
        } catch (Exception e) {
            log.error("Error retrieving user complaints", e);
            sendMessage(chatId, "❌ Жалобаларды алу кезінде қате орын алды.");
        }
    }
    
    private String formatStatus(String status) {
        if (status == null) return "❓ Белгісіз";
        
        return switch (status.toUpperCase()) {
            case "NEW" -> "🆕 Жаңа";
            case "IN_PROGRESS" -> "⏳ Өңделуде";
            case "RESOLVED" -> "✅ Шешілді";
            case "REJECTED" -> "❌ Қабылданбады";
            default -> status;
        };
    }
    
    private String getPriorityEmoji(String priority) {
        if (priority == null) return "ℹ️";
        String p = priority.toLowerCase();
        if (p.contains("өте жоғары") || p.contains("критическ") || p.contains("очень высок")) {
            return "🔴";
        } else if (p.contains("жоғары") || p.contains("высок")) {
            return "🟠";
        } else if (p.contains("орташа") || p.contains("средн")) {
            return "🟡";
        } else if (p.contains("төмен") || p.contains("низк")) {
            return "🟢";
        }
        return "ℹ️";
    }
    
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setParseMode(ParseMode.HTML);
        message.disableWebPagePreview();
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message to {}: {}", chatId, e.getMessage());
        }
    }
    
    private void sendMessageWithKeyboard(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setParseMode(ParseMode.HTML);
        message.disableWebPagePreview();
        
        // Создаем клавиатуру
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("/complaint"));
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Менің жалобаларым"));
        row2.add(new KeyboardButton("/help"));
        
        keyboard.add(row1);
        keyboard.add(row2);
        
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message with keyboard to {}: {}", chatId, e.getMessage());
        }
    }
}

