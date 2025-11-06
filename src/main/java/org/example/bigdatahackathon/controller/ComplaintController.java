package org.example.bigdatahackathon.controller;

import org.example.bigdatahackathon.entity.Complaint;
import org.example.bigdatahackathon.service.ComplaintService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = {"*"})
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
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
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> req) {
        String message = req.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пустое сообщение"));
        }
        var saved = complaintService.createFromChat(message);
        String reply = "Спасибо! Жалоба сохранена. Номер маршрута: " + (saved.getRoute() == null ? "—" : saved.getRoute());
        return ResponseEntity.ok(Map.of("reply", reply, "complaint", saved));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Complaint> create(@RequestBody Complaint complaint) {
        return ResponseEntity.ok(complaintService.save(complaint));
    }

    @PostMapping(value = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Complaint>> bulkCreate(@RequestBody List<Complaint> complaints) {
        return ResponseEntity.ok(complaintService.saveAll(complaints));
    }
}


