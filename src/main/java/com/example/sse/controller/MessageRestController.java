package com.example.sse.controller;


import com.example.sse.handler.ChatWebSocketHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageRestController {

    private final ChatWebSocketHandler chatWebSocketHandler;

    public MessageRestController(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @PostMapping("/broadcast")
    public String broadcast(@RequestParam String message) {
        chatWebSocketHandler.broadcast(message);
        return "Message broadcasted: " + message;
    }
}