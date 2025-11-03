package com.example.sse.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler with typing indicators and user list
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Map<WebSocketSession, String> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sendSystemMessage(session, "Connected. Please send your username to join.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, String> msg = mapper.readValue(message.getPayload(), Map.class);
        String type = msg.get("type");

        switch (type) {
            case "join" -> {
                String username = msg.get("username");
                userSessions.put(session, username);
                broadcastSystemMessage("👋 " + username + " joined the chat.");
                broadcastUserList();
            }
            case "chat" -> {
                String username = userSessions.getOrDefault(session, "Unknown");
                broadcastChatMessage(username, msg.get("text"));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String username = userSessions.remove(session);
        if (username != null) {
            broadcastSystemMessage("👋 " + username + " left the chat.");
            broadcastUserList();
        }
    }

    /** Chat message */
    private void broadcastChatMessage(String username, String text) {
        Map<String, Object> chatMessage = Map.of(
                "type", "chat",
                "username", username,
                "text", text,
                "timestamp", LocalDateTime.now().format(timeFormat)
        );
        sendToAll(chatMessage);
    }

    /** System message */
    private void broadcastSystemMessage(String text) {
        Map<String, Object> msg = Map.of(
                "type", "system",
                "text", text,
                "timestamp", LocalDateTime.now().format(timeFormat)
        );
        sendToAll(msg);
    }

    /** Typing indicator broadcast */
    private void broadcastTyping(String username) {
        Map<String, Object> typingMsg = Map.of(
                "type", "typing",
                "username", username
        );
        sendToAll(typingMsg);
    }

    /** Online users list */
    private void broadcastUserList() {
        List<String> users = new ArrayList<>(userSessions.values());
        Map<String, Object> msg = Map.of(
                "type", "users",
                "users", users
        );
        sendToAll(msg);
    }

    /** Helpers */
    private void sendSystemMessage(WebSocketSession session, String text) {
        try {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(Map.of(
                    "type", "system",
                    "text", text,
                    "timestamp", LocalDateTime.now().format(timeFormat)
            ))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToAll(Map<String, Object> data) {
        try {
            String json = mapper.writeValueAsString(data);
            for (WebSocketSession s : userSessions.keySet()) {
                if (s.isOpen()) s.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** REST broadcast still works */
    public void broadcast(String message) {
        broadcastSystemMessage("📢 " + message);
    }
}
