package com.shop.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j @Service @RequiredArgsConstructor
public class SseService {
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30 min
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable cleanup = () -> remove(userId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());
        try { emitter.send(SseEmitter.event().name("heartbeat").data("connected")); }
        catch (IOException ignored) {}
        log.debug("SSE subscribed: userId={}", userId);
        return emitter;
    }

    public void notifyUser(UUID userId, String eventType, Object payload) {
        var userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) return;
        String json;
        try { json = objectMapper.writeValueAsString(payload); }
        catch (Exception e) { log.error("SSE serialize error: {}", e.getMessage()); return; }
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : userEmitters) {
            try { emitter.send(SseEmitter.event().name(eventType).data(json)); }
            catch (IOException e) { dead.add(emitter); }
        }
        if (!dead.isEmpty()) userEmitters.removeAll(dead);
    }

    private void remove(UUID userId, SseEmitter emitter) {
        var list = emitters.get(userId);
        if (list != null) list.remove(emitter);
    }
}
