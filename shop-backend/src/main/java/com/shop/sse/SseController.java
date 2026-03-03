package com.shop.sse;

import com.shop.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController @RequestMapping("/events") @RequiredArgsConstructor
public class SseController {
    private final SseService sseService;

    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@AuthenticationPrincipal User user) {
        return sseService.subscribe(user.getId());
    }
}
