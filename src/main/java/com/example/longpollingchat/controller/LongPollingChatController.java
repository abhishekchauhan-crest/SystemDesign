package com.example.longpollingchat.controller;

import com.example.longpollingchat.model.LongPollingMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/longpoll/messages")
@CrossOrigin(origins = "*")
public class LongPollingChatController {

    // Thread-safe list of all chat messages
    private final List<LongPollingMessage> messages = new CopyOnWriteArrayList<>();

    // List of clients currently waiting for updates
    private final List<DeferredResult<ResponseEntity<List<LongPollingMessage>>>> waitingClients =
            new CopyOnWriteArrayList<>();

    /**
     * POST /api/longpoll/messages
     * Adds a new message and notifies all waiting clients.
     */
    @PostMapping
    public ResponseEntity<Void> postMessage(@RequestBody LongPollingMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        messages.add(message);
        notifyClients();
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/longpoll/messages?since={timestamp}
     * If no new messages are available, keeps the connection open until new data arrives
     * or the timeout (30 seconds) is reached.
     */
    @GetMapping
    public DeferredResult<ResponseEntity<List<LongPollingMessage>>> getMessages(
            @RequestParam(required = false) Long since) {

        long lastSeen = since != null ? since : 0L;

        // Collect messages newer than the client's timestamp
        List<LongPollingMessage> newMessages = messages.stream()
                .filter(m -> m.getTimestamp() > lastSeen)
                .collect(Collectors.toList());

        // Respond immediately if new messages exist
        if (!newMessages.isEmpty()) {
            return immediateResponse(newMessages);
        }

        // Otherwise create a DeferredResult that will complete when new messages arrive
        DeferredResult<ResponseEntity<List<LongPollingMessage>>> deferredResult =
                new DeferredResult<>(30_000L, ResponseEntity.ok(Collections.emptyList())); // 30 s timeout

        waitingClients.add(deferredResult);

        // When the request finishes (either completed or timed out), remove it
        deferredResult.onCompletion(() -> waitingClients.remove(deferredResult));

        return deferredResult;
    }

    /** Notify all clients waiting for updates */
    private void notifyClients() {
        if (waitingClients.isEmpty()) return;

        List<DeferredResult<ResponseEntity<List<LongPollingMessage>>>> clients =
                new ArrayList<>(waitingClients);
        waitingClients.clear();

        // Send only the latest message(s)
        List<LongPollingMessage> latest = Collections.singletonList(
                messages.get(messages.size() - 1)
        );

        for (DeferredResult<ResponseEntity<List<LongPollingMessage>>> client : clients) {
            client.setResult(ResponseEntity.ok(latest));
        }
    }


    /** Helper for immediate responses */
    private DeferredResult<ResponseEntity<List<LongPollingMessage>>> immediateResponse(
            List<LongPollingMessage> msgs) {

        DeferredResult<ResponseEntity<List<LongPollingMessage>>> result = new DeferredResult<>();
        result.setResult(ResponseEntity.ok(msgs));
        return result;
    }
}
