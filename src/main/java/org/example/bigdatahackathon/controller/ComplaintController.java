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
import java.security.Principal;
import java.util.UUID;
import org.example.bigdatahackathon.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = {"*"})
public class ComplaintController {

    private final ComplaintService complaintService;
    private final RestTemplate restTemplate;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    @Value("${app.webhook.url:}")
    private String webhookUrl;
    @Value("${app.webhook.voice.url:}")
    private String voiceWebhookUrl;
    @Value("${app.webhook.photo.url:}")
    private String photoWebhookUrl;
    @Value("${app.webhook.admin.url:}")
    private String adminWebhookUrl;
    private static final Logger log = LoggerFactory.getLogger(ComplaintController.class);

    public ComplaintController(ComplaintService complaintService, RestTemplate restTemplate, UserService userService, ObjectMapper objectMapper) {
        this.complaintService = complaintService;
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.objectMapper = objectMapper;
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

    // CSV import disabled per requirements

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> dbHealth() {
        long count = complaintService.count();
        return ResponseEntity.ok(Map.of("status", "OK", "complaintsCount", count));
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> chat(@RequestBody Map<String, Object> req, Principal principal) {
        String message = (String) req.get("message");
        Double lat = null; Double lng = null;
        try { if (req.get("lat") != null) lat = Double.valueOf(req.get("lat").toString()); } catch (Exception ignored) {}
        try { if (req.get("lng") != null) lng = Double.valueOf(req.get("lng").toString()); } catch (Exception ignored) {}
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body("{\"error\":\"Пустое сообщение\"}");
        }
        try {
            if (webhookUrl != null && !webhookUrl.isBlank()) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("message", message);
                if (lat != null) { payload.put("lat", lat); payload.put("latitude", lat); }
                if (lng != null) { payload.put("lng", lng); payload.put("longitude", lng); }
                if (principal != null) {
                    try {
                        Long userId = userService.findByUsername(principal.getName()).getId();
                        payload.put("userId", userId);
                        payload.put("username", principal.getName());
                    } catch (Exception ignored) {}
                }
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

    @GetMapping("/mine")
    public ResponseEntity<List<Complaint>> mine(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(complaintService.getMine(principal.getName()));
    }

    @PostMapping(value = "/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> submit(@RequestBody Map<String, Object> req, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        String message = (String) req.get("message");
        Double lat = null; Double lng = null;
        try { if (req.get("lat") != null) lat = Double.valueOf(req.get("lat").toString()); } catch (Exception ignored) {}
        try { if (req.get("lng") != null) lng = Double.valueOf(req.get("lng").toString()); } catch (Exception ignored) {}
        if (message == null || message.isBlank()) return ResponseEntity.badRequest().build();
        Long userId = null;
        try { userId = userService.findByUsername(principal.getName()).getId(); } catch (Exception ignored) {}
        Complaint saved = complaintService.submit(message, principal.getName(), lat, lng, userId);
        try {
            if (webhookUrl != null && !webhookUrl.isBlank()) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("message", message);
                payload.put("complaintId", saved.getId());
                if (lat != null) payload.put("lat", lat);
                if (lng != null) payload.put("lng", lng);
                if (userId != null) { payload.put("userId", userId); payload.put("username", principal.getName()); }
                HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
                var resp = restTemplate.exchange(webhookUrl, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
                
                // Parse AI response and update complaint
                try {
                    JsonNode aiResponse = objectMapper.readTree(resp.getBody());
                    updateComplaintFromAI(saved, aiResponse);
                } catch (Exception parseEx) {
                    log.warn("Failed to parse AI response: {}", parseEx.getMessage());
                }
                
                HttpHeaders out = new HttpHeaders();
                out.setContentType(resp.getHeaders().getContentType() != null ? resp.getHeaders().getContentType() : MediaType.APPLICATION_JSON);
                return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("Webhook (submit) failed: {}", e.toString());
            return ResponseEntity.status(502).body("{\"error\":\"Webhook failed\"}");
        }
        return ResponseEntity.ok("{\"status\":\"saved\"}");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Complaint> updateStatus(@PathVariable("id") UUID id, @RequestBody Map<String, String> req) {
        String status = req.get("status");
        if (status == null || status.isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(complaintService.updateStatus(id, status));
    }

    @PostMapping(value = "/chat-voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> chatVoice(@RequestPart(value = "audio", required = false) MultipartFile audioFile,
                                            @RequestPart(value = "file", required = false) MultipartFile file,
                                            @RequestPart(value = "text", required = false) String text,
                                            @RequestPart(value = "lat", required = false) Double lat,
                                            @RequestPart(value = "lng", required = false) Double lng,
                                            Principal principal) {
        // Accept both "audio" and "file" parameter names
        MultipartFile voiceFile = audioFile != null ? audioFile : file;
        
        if (voiceFile == null || voiceFile.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\":\"Аудио файл отсутствует\"}");
        }
        
        log.info("Received voice file: {} ({} bytes, type: {})", 
                 voiceFile.getOriginalFilename(), 
                 voiceFile.getSize(), 
                 voiceFile.getContentType());
        
        try {
            if (voiceWebhookUrl != null && !voiceWebhookUrl.isBlank()) {
                ByteArrayResource resource = new ByteArrayResource(voiceFile.getBytes()) {
                    @Override
                    public String getFilename() { 
                        return voiceFile.getOriginalFilename() != null ? voiceFile.getOriginalFilename() : "voice.webm"; 
                    }
                };
                
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("audio", resource);
                body.add("file", resource); // Send as both for compatibility
                
                if (text != null && !text.isBlank()) { body.add("text", text); }
                if (lat != null) { 
                    body.add("lat", String.valueOf(lat)); 
                    body.add("latitude", String.valueOf(lat)); 
                }
                if (lng != null) { 
                    body.add("lng", String.valueOf(lng)); 
                    body.add("longitude", String.valueOf(lng)); 
                }
                if (principal != null) {
                    try {
                        Long userId = userService.findByUsername(principal.getName()).getId();
                        body.add("userId", String.valueOf(userId));
                        body.add("username", principal.getName());
                    } catch (Exception ignored) {}
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
                
                log.info("Sending voice to webhook: {}", voiceWebhookUrl);
                var resp = restTemplate.exchange(voiceWebhookUrl, HttpMethod.POST, entity, String.class);
                log.info("Voice webhook response: status={}, body length={}", resp.getStatusCode(), resp.getBody() != null ? resp.getBody().length() : 0);
                
                HttpHeaders out = new HttpHeaders();
                out.setContentType(resp.getHeaders().getContentType() != null ? resp.getHeaders().getContentType() : MediaType.APPLICATION_JSON);
                return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Voice webhook sending failed", e);
            return ResponseEntity.status(502).body("{\"error\":\"Ошибка отправки на сервер: " + e.getMessage() + "\"}");
        }
        return ResponseEntity.status(503).body("{\"error\":\"URL вебхука не настроен\"}");
    }
    
    @PostMapping(value = "/chat-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> chatPhoto(@RequestPart(value = "photo", required = false) MultipartFile photoFile,
                                           @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        // Accept both "photo" and "image" parameter names
        MultipartFile photo = photoFile != null ? photoFile : imageFile;
        
        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\":\"Фото отсутствует\"}");
        }
        
        log.info("Received photo: {} ({} bytes, type: {})", 
                 photo.getOriginalFilename(), 
                 photo.getSize(), 
                 photo.getContentType());
        
        try {
            if (photoWebhookUrl != null && !photoWebhookUrl.isBlank()) {
                ByteArrayResource resource = new ByteArrayResource(photo.getBytes()) {
                    @Override
                    public String getFilename() { 
                        return photo.getOriginalFilename() != null ? photo.getOriginalFilename() : "photo.jpg"; 
                    }
                };
                
                // ONLY send the image, nothing else
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("photo", resource);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
                
                log.info("Sending photo to webhook: {}", photoWebhookUrl);
                var resp = restTemplate.exchange(photoWebhookUrl, HttpMethod.POST, entity, String.class);
                log.info("Photo webhook response: status={}, body length={}", resp.getStatusCode(), resp.getBody() != null ? resp.getBody().length() : 0);
                
                HttpHeaders out = new HttpHeaders();
                out.setContentType(resp.getHeaders().getContentType() != null ? resp.getHeaders().getContentType() : MediaType.APPLICATION_JSON);
                return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Photo webhook sending failed", e);
            return ResponseEntity.status(502).body("{\"error\":\"Ошибка отправки на сервер: " + e.getMessage() + "\"}");
        }
        return ResponseEntity.status(503).body("{\"error\":\"URL вебхука не настроен\"}");
    }
    
    @PostMapping(value = "/submit-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> submitPhoto(@RequestPart(value = "photo", required = false) MultipartFile photoFile,
                                             @RequestPart(value = "image", required = false) MultipartFile imageFile,
                                             Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        
        MultipartFile photo = photoFile != null ? photoFile : imageFile;
        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\":\"Фото отсутствует\"}");
        }
        
        log.info("Received photo from user {}: {} ({} bytes)", 
                 principal.getName(), 
                 photo.getOriginalFilename(), 
                 photo.getSize());
        
        // Save complaint to DB
        Long userId = null;
        try { userId = userService.findByUsername(principal.getName()).getId(); } catch (Exception ignored) {}
        Complaint saved = complaintService.submit("Жалоба с фото", principal.getName(), null, null, userId);
        
        try {
            if (photoWebhookUrl != null && !photoWebhookUrl.isBlank()) {
                ByteArrayResource resource = new ByteArrayResource(photo.getBytes()) {
                    @Override
                    public String getFilename() { 
                        return photo.getOriginalFilename() != null ? photo.getOriginalFilename() : "photo.jpg"; 
                    }
                };
                
                // ONLY send the image, nothing else
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("photo", resource);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
                
                var resp = restTemplate.exchange(photoWebhookUrl, HttpMethod.POST, entity, String.class);
                
                // Parse AI response and update complaint
                try {
                    JsonNode aiResponse = objectMapper.readTree(resp.getBody());
                    updateComplaintFromAI(saved, aiResponse);
                } catch (Exception parseEx) {
                    log.warn("Failed to parse AI response: {}", parseEx.getMessage());
                }
                
                HttpHeaders out = new HttpHeaders();
                out.setContentType(resp.getHeaders().getContentType() != null ? resp.getHeaders().getContentType() : MediaType.APPLICATION_JSON);
                return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Photo webhook (submit) failed", e);
            return ResponseEntity.status(502).body("{\"error\":\"Ошибка отправки на сервер: " + e.getMessage() + "\"}");
        }
        return ResponseEntity.ok("{\"status\":\"saved\"}");
    }

    @PostMapping(value = "/admin-chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> adminChat(@RequestBody Map<String, Object> req, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        
        String message = (String) req.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body("{\"error\":\"Пустое сообщение\"}");
        }
        
        String complaintIdStr = (String) req.get("complaintId");
        
        try {
            if (adminWebhookUrl != null && !adminWebhookUrl.isBlank()) {
                // Build context payload
                Map<String, Object> payload = new HashMap<>();
                payload.put("message", message);
                payload.put("username", principal.getName());
                
                // If specific complaint is being discussed
                if (complaintIdStr != null && !complaintIdStr.isBlank()) {
                    try {
                        UUID complaintId = UUID.fromString(complaintIdStr);
                        Optional<Complaint> complaintOpt = complaintService.findById(complaintId);
                        
                        if (complaintOpt.isPresent()) {
                            Complaint c = complaintOpt.get();
                            Map<String, Object> complaintData = new HashMap<>();
                            complaintData.put("id", c.getId().toString());
                            complaintData.put("rawText", c.getRawText());
                            complaintData.put("route", c.getRoute());
                            complaintData.put("object", c.getObject());
                            complaintData.put("time", c.getTime() != null ? c.getTime().toString() : null);
                            complaintData.put("place", c.getPlace());
                            complaintData.put("actor", c.getActor());
                            complaintData.put("aspect", c.getAspect());
                            complaintData.put("priority", c.getPriority());
                            complaintData.put("evidence", c.getEvidence());
                            complaintData.put("confidence", c.getConfidence());
                            complaintData.put("status", c.getStatus());
                            complaintData.put("createdBy", c.getCreatedBy());
                            complaintData.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
                            complaintData.put("latitude", c.getLatitude());
                            complaintData.put("longitude", c.getLongitude());
                            
                            payload.put("complaint", complaintData);
                            payload.put("context", "specific_complaint");
                        }
                    } catch (Exception e) {
                        log.warn("Invalid complaint ID: {}", complaintIdStr);
                    }
                } else {
                    // General context - provide summary and recent complaints
                    Map<String, Object> summary = complaintService.summary();
                    payload.put("summary", summary);
                    payload.put("context", "general_analysis");
                    
                    // Get recent complaints
                    List<Complaint> recentComplaints = complaintService.list(
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(10)
                    );
                    
                    // Add simplified complaints data
                    List<Map<String, Object>> complaintsData = new ArrayList<>();
                    for (Complaint c : recentComplaints) {
                        Map<String, Object> cd = new HashMap<>();
                        cd.put("id", c.getId().toString());
                        cd.put("rawText", c.getRawText());
                        cd.put("route", c.getRoute());
                        cd.put("priority", c.getPriority());
                        cd.put("place", c.getPlace());
                        cd.put("actor", c.getActor());
                        cd.put("status", c.getStatus());
                        cd.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
                        complaintsData.add(cd);
                    }
                    payload.put("recentComplaints", complaintsData);
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                
                log.info("Sending admin chat to webhook: {}", adminWebhookUrl);
                var resp = restTemplate.exchange(adminWebhookUrl, HttpMethod.POST, entity, String.class);
                log.info("Admin webhook response: status={}, body={}", resp.getStatusCode(), resp.getBody());
                
                HttpHeaders out = new HttpHeaders();
                out.setContentType(resp.getHeaders().getContentType() != null ? resp.getHeaders().getContentType() : MediaType.APPLICATION_JSON);
                return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Admin webhook sending failed", e);
            return ResponseEntity.status(502).body("{\"error\":\"Webhook failed: " + e.getMessage() + "\"}");
        }
        return ResponseEntity.status(503).body("{\"error\":\"Webhook URL is empty\"}");
    }

    // Write endpoints disabled per requirement: do not store chat messages in DB
    
    /**
     * Updates complaint with data extracted by AI agent
     */
    private void updateComplaintFromAI(Complaint complaint, JsonNode aiResponse) {
        try {
            // Extract route
            if (aiResponse.has("route") && !aiResponse.get("route").isNull()) {
                complaint.setRoute(aiResponse.get("route").asText());
            }
            
            // Extract object
            if (aiResponse.has("object") && !aiResponse.get("object").isNull()) {
                complaint.setObject(aiResponse.get("object").asText());
            }
            
            // Extract time
            if (aiResponse.has("time") && !aiResponse.get("time").isNull()) {
                try {
                    String timeStr = aiResponse.get("time").asText();
                    complaint.setTime(OffsetDateTime.parse(timeStr));
                } catch (Exception e) {
                    log.debug("Could not parse time: {}", e.getMessage());
                }
            }
            
            // Extract place
            if (aiResponse.has("place") && !aiResponse.get("place").isNull()) {
                complaint.setPlace(aiResponse.get("place").asText());
            }
            
            // Extract actor
            if (aiResponse.has("actor") && !aiResponse.get("actor").isNull()) {
                complaint.setActor(aiResponse.get("actor").asText());
            }
            
            // Extract aspects (array)
            if (aiResponse.has("aspects") && aiResponse.get("aspects").isArray()) {
                List<String> aspects = new ArrayList<>();
                aiResponse.get("aspects").forEach(node -> aspects.add(node.asText()));
                complaint.setAspect(aspects.toArray(new String[0]));
            } else if (aiResponse.has("aspect") && aiResponse.get("aspect").isArray()) {
                List<String> aspects = new ArrayList<>();
                aiResponse.get("aspect").forEach(node -> aspects.add(node.asText()));
                complaint.setAspect(aspects.toArray(new String[0]));
            }
            
            // Extract priority
            if (aiResponse.has("priority") && !aiResponse.get("priority").isNull()) {
                complaint.setPriority(aiResponse.get("priority").asText());
            }
            
            // Extract confidence
            if (aiResponse.has("confidence") && !aiResponse.get("confidence").isNull()) {
                complaint.setConfidence(aiResponse.get("confidence").asDouble());
            }
            
            // Save updated complaint
            complaintService.save(complaint);
            log.info("Updated complaint {} with AI data", complaint.getId());
        } catch (Exception e) {
            log.error("Error updating complaint from AI: {}", e.getMessage(), e);
        }
    }
}


