package com.example.sse.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalTime;

@RestController
public class SseController {

    @GetMapping(value = "/time-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTimeFor30Seconds() {
        // Set a timeout slightly longer than 30 seconds
        SseEmitter emitter = new SseEmitter(35_000L);

        new Thread(() -> {
            long start = System.currentTimeMillis();
            try {
                while (System.currentTimeMillis() - start < 30_000) { // 30 seconds
                    String time = LocalTime.now().toString();
                    emitter.send(SseEmitter.event()
                            .name("Present time is : ")
                            .data(time));
                    Thread.sleep(1000); // send every second
                }
                emitter.complete(); // close the stream after 30 seconds
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}