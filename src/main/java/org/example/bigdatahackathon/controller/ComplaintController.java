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
}


