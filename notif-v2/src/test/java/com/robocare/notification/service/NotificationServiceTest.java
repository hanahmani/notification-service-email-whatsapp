package com.robocare.notification.service;

import com.robocare.notification.dto.NotificationRequest;
import com.robocare.notification.dto.NotificationResponse;
import com.robocare.notification.sender.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService service;

    @BeforeEach
    void setUp() {
        // Fake sender qui simule un envoi reussi
        NotificationSender fakeSender = new NotificationSender() {
            @Override
            public boolean send(NotificationRequest request) {
                return true;
            }
            @Override
            public String getChannel() {
                return "EMAIL";
            }
        };
        service = new NotificationService(List.of(fakeSender));
    }

    @Test
    @DisplayName("Envoi email reussi = status SENT")
    void send_success() {
        NotificationRequest req = NotificationRequest.builder()
                .type("ALERT_TEMP")
                .recipient("test@gmail.com")
                .message("Test alerte")
                .build();

        NotificationResponse resp = service.send(req);

        assertEquals("SENT", resp.getStatus());
        assertEquals("EMAIL", resp.getChannel());
        assertEquals("test@gmail.com", resp.getRecipient());
        assertNotNull(resp.getId());
        assertNull(resp.getErrorDetails());
    }

    @Test
    @DisplayName("Historique contient la notification")
    void history_works() {
        NotificationRequest req = NotificationRequest.builder()
                .type("TEST")
                .recipient("a@b.com")
                .message("Hello")
                .build();

        NotificationResponse sent = service.send(req);
        List<NotificationResponse> all = service.getAll();

        assertEquals(1, all.size());
        assertEquals(sent.getId(), all.get(0).getId());
    }

    @Test
    @DisplayName("getById retourne la bonne notification")
    void getById_found() {
        NotificationRequest req = NotificationRequest.builder()
                .type("TEST")
                .recipient("a@b.com")
                .message("Hello")
                .build();

        NotificationResponse sent = service.send(req);
        NotificationResponse found = service.getById(sent.getId());

        assertNotNull(found);
        assertEquals(sent.getId(), found.getId());
    }

    @Test
    @DisplayName("getById retourne null si inexistant")
    void getById_not_found() {
        assertNull(service.getById("zzzzz"));
    }

    @Test
    @DisplayName("Stats comptent correctement")
    void stats_work() {
        service.send(NotificationRequest.builder()
                .type("T1").recipient("a@b.com").message("1").build());
        service.send(NotificationRequest.builder()
                .type("T2").recipient("c@d.com").message("2").build());

        Map<String, Object> stats = service.getStats();

        assertEquals(2L, stats.get("total"));
        assertEquals(2L, stats.get("sent"));
        assertEquals(0L, stats.get("failed"));
        assertEquals(100L, stats.get("success_rate"));
    }
}
