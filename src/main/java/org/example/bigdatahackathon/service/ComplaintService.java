package org.example.bigdatahackathon.service;

import org.example.bigdatahackathon.entity.Complaint;
import org.example.bigdatahackathon.repository.ComplaintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public ComplaintService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public List<Complaint> list(Optional<String> route,
                                Optional<String> priority,
                                Optional<String> actor,
                                Optional<String> place,
                                Optional<Integer> limit) {
        List<Complaint> all = complaintRepository.findAll();
        List<Complaint> filtered = new ArrayList<>();
        for (Complaint c : all) {
            if (route.isPresent() && (c.getRoute() == null || !c.getRoute().equalsIgnoreCase(route.get()))) continue;
            if (priority.isPresent() && (c.getPriority() == null || !c.getPriority().equalsIgnoreCase(priority.get()))) continue;
            if (actor.isPresent() && (c.getActor() == null || !c.getActor().equalsIgnoreCase(actor.get()))) continue;
            if (place.isPresent() && (c.getPlace() == null || !c.getPlace().equalsIgnoreCase(place.get()))) continue;
            filtered.add(c);
            if (limit.isPresent() && filtered.size() >= limit.get()) break;
        }
        return filtered;
    }

    public Map<String, Object> summary() {
        List<Complaint> all = complaintRepository.findAll();
        Map<String, Long> byPriority = new LinkedHashMap<>();
        Map<String, Long> byRoute = new LinkedHashMap<>();
        double avgConfidence = 0.0;
        for (Complaint c : all) {
            String pr = c.getPriority() == null ? "(нет)" : c.getPriority();
            byPriority.put(pr, byPriority.getOrDefault(pr, 0L) + 1);
            String route = c.getRoute() == null ? "(нет)" : c.getRoute();
            byRoute.put(route, byRoute.getOrDefault(route, 0L) + 1);
            if (c.getConfidence() != null) avgConfidence += c.getConfidence();
        }
        int n = all.size();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("total", n);
        resp.put("byPriority", byPriority);
        resp.put("byRoute", byRoute);
        resp.put("avgConfidence", n > 0 ? avgConfidence / n : 0.0);
        return resp;
    }

    public long count() {
        return complaintRepository.count();
    }

    public Map<String, Object> bulkImportFromText(String body) {
        List<String> lines = body == null ? List.of() : body.strip().lines().toList();
        int imported = 0;
        int skipped = 0;
        for (String line : lines) {
            if (line.isBlank()) continue;
            try {
                List<String> fields = smartCsvSplit(line);
                if (fields.size() < 13) {
                    skipped++;
                    continue;
                }
                Complaint c = new Complaint();
                // 0 id
                String idStr = nullIfEmpty(stripQuotes(fields.get(0)));
                if (idStr != null) {
                    c.setId(UUID.fromString(idStr));
                }
                // 1 raw_text
                c.setRawText(nullIfEmpty(stripQuotes(fields.get(1))));
                // 2 route
                c.setRoute(nullIfEmpty(stripQuotes(fields.get(2))));
                // 3 object
                c.setObject(nullIfEmpty(stripQuotes(fields.get(3))));
                // 4 time
                c.setTime(parseOffsetDateTime(nullIfEmpty(stripQuotes(fields.get(4)))));
                // 5 place
                c.setPlace(nullIfEmpty(stripQuotes(fields.get(5))));
                // 6 actor
                c.setActor(nullIfEmpty(stripQuotes(fields.get(6))));
                // 7 aspect (TEXT[])
                c.setAspect(parseArrayToStrArray(nullIfEmpty(stripQuotes(fields.get(7)))));
                // 8 priority
                c.setPriority(nullIfEmpty(stripQuotes(fields.get(8))));
                // 9 evidence (TEXT[])
                c.setEvidence(parseArrayToStrArray(nullIfEmpty(stripQuotes(fields.get(9)))));
                // 10 confidence
                c.setConfidence(parseDouble(nullIfEmpty(stripQuotes(fields.get(10)))));
                // 11 created_at
                c.setCreatedAt(parseOffsetDateTime(nullIfEmpty(stripQuotes(fields.get(11)))));
                // 12 updated_at
                c.setUpdatedAt(parseOffsetDateTime(nullIfEmpty(stripQuotes(fields.get(12)))));

                complaintRepository.save(c);
                imported++;
            } catch (Exception e) {
                skipped++;
            }
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("imported", imported);
        resp.put("skipped", skipped);
        return resp;
    }

    public Complaint createFromChat(String message) {
        Complaint c = new Complaint();
        c.setRawText(message);
        // simple route extraction (digits)
        String digits = message == null ? null : message.replaceAll("[^0-9]", "");
        if (digits != null && !digits.isBlank()) {
            c.setRoute(digits);
        }
        c.setAspect(new String[]{});
        c.setEvidence(new String[]{});
        return complaintRepository.save(c);
    }

    private static String stripQuotes(String s) {
        if (s == null) return null;
        String t = s.strip();
        if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
            return t.substring(1, t.length() - 1);
        }
        return t;
    }

    private static String nullIfEmpty(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static Double parseDouble(String s) {
        if (s == null) return null;
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private static OffsetDateTime parseOffsetDateTime(String s) {
        if (s == null) return null;
        // Accept formats like: 2025-11-05 22:56:31.624924 +00:00
        String pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS xxx";
        try {
            return OffsetDateTime.parse(s.replace('T', ' '), DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            // Try without micros
            String pattern2 = "yyyy-MM-dd HH:mm:ss xxx";
            try { return OffsetDateTime.parse(s.replace('T', ' '), DateTimeFormatter.ofPattern(pattern2)); }
            catch (Exception ignored) { return null; }
        }
    }

    private static String[] parseArrayToStrArray(String literal) {
        if (literal == null) return new String[]{};
        String t = literal.strip();
        if (t.startsWith("{") && t.endsWith("}")) {
            String inner = t.substring(1, t.length() - 1);
            if (inner.isBlank()) return new String[]{};
            String[] parts = inner.split(",");
            List<String> out = new ArrayList<>();
            for (String p : parts) {
                String v = p.strip();
                if (v.equals("NULL")) continue;
                out.add(v);
            }
            return out.toArray(String[]::new);
        }
        return new String[]{ t };
    }

    private static List<String> smartCsvSplit(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        int braceDepth = 0;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                cur.append(ch);
                continue;
            }
            if (!inQuotes) {
                if (ch == '{') braceDepth++;
                if (ch == '}') braceDepth--;
                if (ch == ',' && braceDepth == 0) {
                    fields.add(cur.toString());
                    cur.setLength(0);
                    continue;
                }
            }
            cur.append(ch);
        }
        fields.add(cur.toString());
        return fields;
    }
}


