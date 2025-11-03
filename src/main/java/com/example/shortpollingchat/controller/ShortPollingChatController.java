package com.example.shortpollingchat.controller;

import com.example.shortpollingchat.model.ShortPollingMessage;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/shortpoll/messages")
@CrossOrigin(origins = "*")
public class ShortPollingChatController {

    private final List<ShortPollingMessage> chatMessages = new CopyOnWriteArrayList<>();

    @PostMapping
    public void postMessage(@RequestBody ShortPollingMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        chatMessages.add(message);
    }

    @GetMapping
    public List<ShortPollingMessage> getMessages(@RequestParam(required = false) Long since) {
        if (since == null) return chatMessages;
        return chatMessages.stream()
                .filter(m -> m.getTimestamp() > since)
                .toList();
    }
}
