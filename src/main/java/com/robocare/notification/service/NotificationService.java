package com.robocare.notification.service;

import com.robocare.notification.dto.NotificationRequest;
import com.robocare.notification.dto.NotificationResponse;
import com.robocare.notification.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private final Map<String, NotificationSender> senders;
    private final Map<String, NotificationResponse> history = new ConcurrentHashMap<>();

    public NotificationService(List<NotificationSender> senderList) {
        this.senders = senderList.stream()
                .collect(Collectors.toMap(
                        NotificationSender::getChannel,
                        Function.identity()
                ));
        log.info("=== NotificationService demarre avec {} canal(aux) : {} ===",
                senders.size(), senders.keySet());
    }

    /**
     * Envoie une notification via le canal choisi par l'utilisateur.
     * Le champ "channel" dans la requete determine le canal : EMAIL ou WHATSAPP
     */
    public NotificationResponse send(NotificationRequest request) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String channel = request.getChannel().toUpperCase();

        log.info("[{}] Nouvelle notification | type={} canal={} dest={} pdf={}",
                id, request.getType(), channel, request.getRecipient(),
                request.getAttachmentUrl() != null ? "oui" : "non");

        // Trouver le sender du canal demande
        NotificationSender sender = senders.get(channel);
        if (sender == null) {
            log.error("[{}] Canal {} non disponible. Canaux actifs : {}",
                    id, channel, senders.keySet());
            return saveAndReturn(id, request, channel, "FAILED",
                    "Canal " + channel + " non disponible. "
                            + "Canaux disponibles : " + senders.keySet());
        }

        // Envoyer
        try {
            boolean ok = sender.send(request);
            if (ok) {
                log.info("[{}] SENT via {} avec succes", id, channel);
                return saveAndReturn(id, request, channel, "SENT", null);
            } else {
                log.error("[{}] Echec envoi via {}", id, channel);
                return saveAndReturn(id, request, channel, "FAILED",
                        "Echec envoi " + channel);
            }
        } catch (Exception e) {
            log.error("[{}] ERREUR {} : {}", id, channel, e.getMessage());
            return saveAndReturn(id, request, channel, "FAILED",
                    e.getMessage());
        }
    }

    public List<NotificationResponse> getAll() {
        return new ArrayList<>(history.values());
    }

    public NotificationResponse getById(String id) {
        return history.get(id);
    }

    public Map<String, Object> getStats() {
        long total = history.size();
        long sent = history.values().stream()
                .filter(n -> "SENT".equals(n.getStatus())).count();
        long failed = history.values().stream()
                .filter(n -> "FAILED".equals(n.getStatus())).count();

        Map<String, Long> byChannel = history.values().stream()
                .collect(Collectors.groupingBy(
                        NotificationResponse::getChannel,
                        Collectors.counting()
                ));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("sent", sent);
        stats.put("failed", failed);
        stats.put("success_rate", total > 0
                ? Math.round(sent * 100.0 / total) : 0);
        stats.put("by_channel", byChannel);
        return stats;
    }

    private NotificationResponse saveAndReturn(String id,
                                               NotificationRequest req,
                                               String channel,
                                               String status,
                                               String error) {
        NotificationResponse resp = NotificationResponse.builder()
                .id(id)
                .type(req.getType())
                .channel(channel)
                .recipient(req.getRecipient())
                .status(status)
                .message(req.getMessage())
                .timestamp(LocalDateTime.now())
                .errorDetails(error)
                .build();
        history.put(id, resp);
        return resp;
    }
}