package org.example.bigdatahackathon.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bigdatahackathon.entity.Complaint;
import org.example.bigdatahackathon.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final ComplaintRepository complaintRepository;
    
    @Value("${app.webhook.url}")
    private String webhookUrl;
    
    /**
     * Отправляет текст жалобы на webhook n8n, получает обработанный ответ и сохраняет в БД
     */
    public Mono<String> processComplaintText(String text, String username) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text", text);
        requestBody.put("source", "telegram");
        requestBody.put("username", username);
        
        log.info("📤 Sending complaint to webhook from Telegram user: {}", username);
        
        return webClientBuilder.build()
            .post()
            .uri(webhookUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .doOnSuccess(response -> {
                log.info("✅ Webhook response received, saving to database...");
                saveComplaintFromWebhook(text, response, username);
            })
            .doOnError(error -> log.error("❌ Webhook error: {}", error.getMessage()))
            .onErrorResume(e -> Mono.just("{\"error\": \"" + e.getMessage() + "\"}"));
    }
    
    /**
     * Сохраняет жалобу в базу данных после обработки webhook
     */
    private void saveComplaintFromWebhook(String originalText, String webhookResponse, String username) {
        try {
            JsonNode responseData = objectMapper.readTree(webhookResponse);
            
            Complaint complaint = new Complaint();
            complaint.setRawText(originalText);
            complaint.setCreatedBy(username);
            complaint.setStatus("NEW");
            complaint.setCreatedAt(OffsetDateTime.now());
            complaint.setUpdatedAt(OffsetDateTime.now());
            
            // Извлекаем данные из webhook ответа
            if (responseData.has("route")) {
                complaint.setRoute(responseData.get("route").asText());
            }
            
            if (responseData.has("object")) {
                complaint.setObject(responseData.get("object").asText());
            }
            
            if (responseData.has("place")) {
                complaint.setPlace(responseData.get("place").asText());
            }
            
            if (responseData.has("actor")) {
                complaint.setActor(responseData.get("actor").asText());
            }
            
            if (responseData.has("priority")) {
                complaint.setPriority(responseData.get("priority").asText());
            }
            
            if (responseData.has("confidence")) {
                complaint.setConfidence(responseData.get("confidence").asDouble());
            }
            
            // Обработка массива аспектов
            if (responseData.has("aspects") && responseData.get("aspects").isArray()) {
                List<String> aspects = new ArrayList<>();
                responseData.get("aspects").forEach(aspect -> aspects.add(aspect.asText()));
                complaint.setAspect(aspects.toArray(new String[0]));
            } else if (responseData.has("aspect") && responseData.get("aspect").isArray()) {
                List<String> aspects = new ArrayList<>();
                responseData.get("aspect").forEach(aspect -> aspects.add(aspect.asText()));
                complaint.setAspect(aspects.toArray(new String[0]));
            }
            
            // Координаты (если есть)
            if (responseData.has("latitude")) {
                complaint.setLatitude(responseData.get("latitude").asDouble());
            }
            if (responseData.has("longitude")) {
                complaint.setLongitude(responseData.get("longitude").asDouble());
            }
            
            // Время жалобы
            if (responseData.has("time")) {
                try {
                    complaint.setTime(OffsetDateTime.parse(responseData.get("time").asText()));
                } catch (Exception e) {
                    complaint.setTime(OffsetDateTime.now());
                }
            } else {
                complaint.setTime(OffsetDateTime.now());
            }
            
            // Сохраняем в БД
            Complaint saved = complaintRepository.save(complaint);
            log.info("💾 Complaint saved to database with ID: {} from Telegram user: {}", 
                    saved.getId(), username);
            log.info("📊 Route: {}, Place: {}, Priority: {}", 
                    saved.getRoute(), saved.getPlace(), saved.getPriority());
            
        } catch (Exception e) {
            log.error("❌ Error saving complaint from Telegram to database: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Форматирует JSON ответ от webhook в красивое сообщение
     */
    public String formatResponse(String jsonResponse) {
        try {
            JsonNode node = objectMapper.readTree(jsonResponse);
            
            if (node.has("error")) {
                return "❌ Қате орын алды: " + node.get("error").asText();
            }
            
            StringBuilder message = new StringBuilder();
            message.append("✅ <b>Жалоба қабылданды!</b>\n\n");
            
            if (node.has("route")) {
                message.append("🚌 <b>Маршрут:</b> ").append(node.get("route").asText()).append("\n");
            }
            
            if (node.has("object")) {
                message.append("🎯 <b>Объект:</b> ").append(node.get("object").asText()).append("\n");
            }
            
            if (node.has("place")) {
                message.append("📍 <b>Орын:</b> ").append(node.get("place").asText()).append("\n");
            }
            
            if (node.has("actor")) {
                message.append("👤 <b>Жауапты:</b> ").append(node.get("actor").asText()).append("\n");
            }
            
            if (node.has("priority")) {
                String priority = node.get("priority").asText();
                String emoji = getPriorityEmoji(priority);
                message.append("\n").append(emoji).append(" <b>Басымдық:</b> ").append(priority).append("\n");
            }
            
            if (node.has("aspects") && node.get("aspects").isArray()) {
                message.append("\n🏷 <b>Аспектілер:</b>\n");
                node.get("aspects").forEach(aspect -> 
                    message.append("  • ").append(aspect.asText()).append("\n")
                );
            }
            
            message.append("\n📊 Жалоба жүйеде тіркелді және өңделуде.");
            
            return message.toString();
            
        } catch (Exception e) {
            log.error("Error formatting response", e);
            return "✅ Жалоба қабылданды!\n\n" + jsonResponse;
        }
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
}

