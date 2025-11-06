package org.example.bigdatahackathon.controller;

import org.example.bigdatahackathon.entity.Complaint;
import org.example.bigdatahackathon.service.ComplaintService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = {"*"})
public class ComplaintController {

    private final ComplaintService complaintService;
    private final RestTemplate restTemplate;
    @Value("${app.webhook.url:}")
    private String webhookUrl;
    @Value("${app.webhook.voice.url:}")
    private String voiceWebhookUrl;
    private static final Logger log = LoggerFactory.getLogger(ComplaintController.class);

    public ComplaintController(ComplaintService complaintService, RestTemplate restTemplate) {
        this.complaintService = complaintService;
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public ResponseEntity<List<Complaint>> list(@RequestParam Optional<String> route,
                                                @RequestParam Optional<String> priority,
                                                @RequestParam Optional<String> actor,
                                                @RequestParam Optional<String> place,
                                                @RequestParam Optional<Integer> limit) {
        return ResponseEntity.ok(complaintService.list(route, priority, actor, place, limit));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(complaintService.summary());
    }

    @PostMapping(value = "/bulk-text", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Map<String, Object>> bulkImportText(@RequestBody String body) {
        return ResponseEntity.ok(complaintService.bulkImportFromText(body));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> dbHealth() {
        long count = complaintService.count();
        return ResponseEntity.ok(Map.of("status", "OK", "complaintsCount", count));
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> chat(@RequestBody Map<String, String> req) {
        String message = req.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body("{\"error\":\"Пустое сообщение\"}");
        }
        try {
            if (webhookUrl != null && !webhookUrl.isBlank()) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("message", message);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                var resp = restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, String.class);
                log.info("Webhook sent to {} with status {}", webhookUrl, resp.getStatusCode());
                HttpHeaders out = new HttpHeaders();
                out.setContentType(resp.getHeaders().getContentType() != null ? resp.getHeaders().getContentType() : MediaType.APPLICATION_JSON);
                return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("Webhook sending failed: {}", e.toString());
            return ResponseEntity.status(502).body("{\"error\":\"Webhook failed\"}");
        }
        return ResponseEntity.status(503).body("{\"error\":\"Webhook URL is empty\"}");
    }

    @PostMapping(value = "/chat-voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> chatVoice(@RequestPart("file") MultipartFile file,
                                            @RequestPart(value = "text", required = false) String text) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\":\"Файл пустой\"}");
        }
        try {
            if (voiceWebhookUrl != null && !voiceWebhookUrl.isBlank()) {
                ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() { return file.getOriginalFilename() != null ? file.getOriginalFilename() : "voice.webm"; }
                };
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", resource);
                if (text != null) { body.add("text", text); }
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
                var resp = restTemplate.exchange(voiceWebhookUrl, HttpMethod.POST, entity, String.class);
                log.info("Voice webhook sent to {} ({} bytes), status {}", voiceWebhookUrl, file.getSize(), resp.getStatusCode());
                HttpHeaders out = new HttpHeaders();
                out.setContentType(resp.getHeaders().getContentType() != null ? resp.getHeaders().getContentType() : MediaType.APPLICATION_JSON);
                return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("Voice webhook sending failed: {}", e.toString());
            return ResponseEntity.status(502).body("{\"error\":\"Webhook failed\"}");
        }
        return ResponseEntity.status(503).body("{\"error\":\"Webhook URL is empty\"}");
    }

    // Write endpoints disabled per requirement: do not store chat messages in DB
}


