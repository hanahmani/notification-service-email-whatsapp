# Guide Complet — RoboCare Notification Service
# Phase 1 : EMAIL | Phase 2 : WHATSAPP (plus tard)

---

# ============================================================
#  PHASE 1 : EMAIL UNIQUEMENT
# ============================================================

## ÉTAPE 1 : Ouvrir le projet dans IntelliJ

1. Extraire le dossier `notification-service`
2. IntelliJ → File → Open → sélectionner le dossier
3. Cliquer "Open as Project"
4. Attendre que Maven télécharge les dépendances (2-3 min)
5. Vérifier en bas : "Build completed successfully"


## ÉTAPE 2 : Créer le Gmail App Password

1. Aller sur https://myaccount.google.com
2. Sécurité → Validation en 2 étapes → ACTIVER
3. Retourner sur Sécurité
4. Chercher "Mots de passe des applications"
   (ou aller sur https://myaccount.google.com/apppasswords)
5. Sélectionner "Autre" → taper "RoboCare" → Générer
6. Google affiche 16 caractères : abcd efgh ijkl mnop
7. COPIER sans les espaces : abcdefghijklmnop


## ÉTAPE 3 : Configurer les variables dans IntelliJ

1. Ouvrir `NotificationApplication.java`
2. Cliquer sur le triangle vert ▶ → Edit Configurations
3. Cliquer "Modify options" → cocher "Environment variables"
4. Dans le champ Environment variables, coller :

```
MAIL_USERNAME=votre-email@gmail.com;MAIL_PASSWORD=votre-app-password-16-chars
```

   Exemple réel :
```
MAIL_USERNAME=hana.robocare@gmail.com;MAIL_PASSWORD=abcdefghijklmnop
```

5. OK → Apply


## ÉTAPE 4 : Lancer le projet

Cliquer ▶ (Run) sur NotificationApplication

Vous devez voir dans la console :
```
=== NotificationService demarre avec 1 canal(aux) : [EMAIL] ===
Started NotificationApplication in 2.xxx seconds
Tomcat started on port 8081
```

Si vous voyez ça → LE SERVEUR TOURNE ✓


## ÉTAPE 5 : Tester avec Postman

### Test 1 : Healthcheck (vérifier que ça tourne)

- Method : GET
- URL : http://localhost:8081/notifications/health
- Cliquer Send

Réponse attendue :
```json
{
    "status": "UP",
    "service": "robocare-notification-service",
    "port": 8081,
    "channels": ["EMAIL"],
    "timestamp": "2026-04-22T21:00:00"
}
```

### Test 2 : Envoyer un email simple

- Method : POST
- URL : http://localhost:8081/notifications
- Tab Body → raw → JSON
- Coller :

```json
{
    "type": "TEST",
    "recipient": "votre-email@gmail.com",
    "subject": "Test RoboCare",
    "message": "Bonjour ! Ceci est un test du microservice de notification RoboCare."
}
```

- Cliquer Send

Réponse attendue :
```json
{
    "id": "a1b2c3d4",
    "type": "TEST",
    "channel": "EMAIL",
    "recipient": "votre-email@gmail.com",
    "status": "SENT",
    "message": "Bonjour ! Ceci est un test du microservice de notification RoboCare.",
    "timestamp": "2026-04-22T21:05:00",
    "errorDetails": null
}
```

→ VÉRIFIEZ VOTRE BOÎTE EMAIL ! Le mail doit arriver.


### Test 3 : Envoyer une alerte agricole

```json
{
    "type": "ALERT_TEMP",
    "recipient": "votre-email@gmail.com",
    "subject": "Alerte température - Parcelle Nord",
    "message": "Alerte RoboCare : température 45°C détectée sur Parcelle Nord. Seuil : 35°C. Vérifiez vos cultures immédiatement."
}
```

### Test 4 : Envoyer avec données dynamiques (sans message)

```json
{
    "type": "SENSOR_OFFLINE",
    "recipient": "votre-email@gmail.com",
    "data": {
        "sensor_id": "TEMP_007",
        "field_name": "Parcelle Est",
        "duration": "30 minutes"
    }
}
```

Le système construit automatiquement le body de l'email :
```
Notification RoboCare

Type : SENSOR_OFFLINE
sensor_id : TEMP_007
field_name : Parcelle Est
duration : 30 minutes

-- Equipe RoboCare
```

### Test 5 : Voir l'historique

- Method : GET
- URL : http://localhost:8081/notifications
- Cliquer Send

→ Vous verrez la liste de tous les emails envoyés.

### Test 6 : Chercher une notification par ID

- Method : GET
- URL : http://localhost:8081/notifications/a1b2c3d4
  (remplacer a1b2c3d4 par l'ID reçu dans le Test 2)

### Test 7 : Voir les statistiques

- Method : GET
- URL : http://localhost:8081/notifications/stats

Réponse :
```json
{
    "total": 3,
    "sent": 3,
    "failed": 0,
    "success_rate": 100
}
```


## ÉTAPE 6 : Si l'email ne part pas (debug)

### Erreur "Authentication failed"
→ Le App Password est incorrect
→ Vérifier : 16 caractères, sans espaces
→ Vérifier que la validation en 2 étapes est activée

### Erreur "Connection refused" ou "Could not connect"
→ Votre réseau bloque le port 587
→ Essayer : ouvrir `application.yml` et changer :
```yaml
spring:
  mail:
    port: 465
    properties:
      mail:
        smtp:
          ssl:
            enable: true
          starttls:
            enabled: false
```

### Erreur "Mail server connection failed"
→ Vérifier la connexion internet
→ Vérifier que l'antivirus ne bloque pas


## ÉTAPE 7 : Lancer les tests unitaires

Dans IntelliJ :
- Clic droit sur le dossier `src/test` → Run All Tests

Ou en terminal :
```bash
mvn test
```

5 tests doivent passer :
```
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```


---

# ============================================================
#  PHASE 2 : AJOUTER WHATSAPP (après que l'email marche)
# ============================================================

Une fois que l'email fonctionne parfaitement, suivez ces étapes
pour ajouter WhatsApp.

## ÉTAPE 8 : Créer le compte Meta Developer

1. Aller sur https://developers.facebook.com
2. Se connecter avec Facebook
3. "Mes applications" → "Créer une application"
4. Type : Business
5. Nom : "RoboCare Notification"
6. Créer

## ÉTAPE 9 : Configurer WhatsApp API

1. Dashboard de l'app → chercher "WhatsApp" → "Configurer"
2. Section "API Setup"
3. COPIER :
   - Phone number ID : 123456789012345
   - Temporary access token : EAAxxxxxx...
4. Section "To" → "Manage phone number list"
5. Ajouter votre numéro : 216XXXXXXXX (sans le +)
6. Entrer le code reçu par WhatsApp


## ÉTAPE 10 : Créer le fichier WhatsAppSender.java

Créer un nouveau fichier dans `src/main/java/com/robocare/notification/sender/` :

Nom : `WhatsAppSender.java`

Contenu :

```java
package com.robocare.notification.sender;

import com.robocare.notification.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WhatsAppSender implements NotificationSender {

    @Value("${whatsapp.token:disabled}")
    private String token;

    @Value("${whatsapp.phone-id:disabled}")
    private String phoneId;

    @Value("${whatsapp.api-url:https://graph.facebook.com/v21.0}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean send(NotificationRequest request) throws Exception {
        if ("disabled".equals(token) || "disabled".equals(phoneId)) {
            throw new RuntimeException("WhatsApp non configure");
        }

        log.info("[WHATSAPP] Envoi vers {} | type={}",
                request.getRecipient(), request.getType());

        String url = apiUrl + "/" + phoneId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", request.getRecipient());
        body.put("type", "text");

        String msg = request.getMessage();
        if (msg == null || msg.isBlank()) {
            StringBuilder sb = new StringBuilder();
            sb.append("*RoboCare - ").append(request.getType()).append("*\n\n");
            if (request.getData() != null) {
                request.getData().forEach((k, v) ->
                    sb.append(k).append(" : ").append(v).append("\n"));
            }
            msg = sb.toString();
        }

        Map<String, Object> text = new HashMap<>();
        text.put("body", msg);
        text.put("preview_url", false);
        body.put("text", text);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> resp = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        if (resp.getStatusCode().is2xxSuccessful()) {
            log.info("[WHATSAPP] Envoye avec succes vers {}", request.getRecipient());
            return true;
        }
        return false;
    }

    @Override
    public String getChannel() {
        return "WHATSAPP";
    }
}
```


## ÉTAPE 11 : Ajouter la config WhatsApp dans application.yml

Ouvrir `src/main/resources/application.yml` et AJOUTER à la fin :

```yaml
whatsapp:
  token: ${WHATSAPP_TOKEN:disabled}
  phone-id: ${WHATSAPP_PHONE_ID:disabled}
  api-url: https://graph.facebook.com/v21.0
```


## ÉTAPE 12 : Modifier NotificationService.java

Ouvrir `NotificationService.java`.

Remplacer la méthode `send` pour supporter le choix du canal :

```java
public NotificationResponse send(NotificationRequest request) {
    String id = UUID.randomUUID().toString().substring(0, 8);

    log.info("[{}] Nouvelle notification | type={} dest={}",
            id, request.getType(), request.getRecipient());

    // Déterminer le canal
    String channel = "EMAIL"; // par défaut
    if (request.getRecipient().matches("\\d{8,15}")) {
        // Si le destinataire est un numéro de téléphone → WhatsApp
        channel = "WHATSAPP";
    }

    NotificationSender sender = senders.get(channel);
    if (sender == null) {
        log.error("[{}] Canal {} non disponible", id, channel);
        return saveAndReturn(id, request, channel, "FAILED",
                "Canal " + channel + " non disponible");
    }

    try {
        boolean ok = sender.send(request);
        if (ok) {
            log.info("[{}] SENT via {}", id, channel);
            return saveAndReturn(id, request, channel, "SENT", null);
        } else {
            return saveAndReturn(id, request, channel, "FAILED", "Echec envoi");
        }
    } catch (Exception e) {
        log.error("[{}] ERREUR {} : {}", id, channel, e.getMessage());
        return saveAndReturn(id, request, channel, "FAILED", e.getMessage());
    }
}
```

Et modifier `saveAndReturn` pour accepter le canal :

```java
private NotificationResponse saveAndReturn(String id, NotificationRequest req,
                                            String channel, String status,
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
```


## ÉTAPE 13 : Ajouter les variables WhatsApp dans IntelliJ

1. Run → Edit Configurations
2. Environment variables :

```
MAIL_USERNAME=votre-email@gmail.com;MAIL_PASSWORD=abcdefghijklmnop;WHATSAPP_TOKEN=EAAxxxxx;WHATSAPP_PHONE_ID=123456789
```

3. OK → Relancer ▶


## ÉTAPE 14 : Tester WhatsApp dans Postman

### Envoyer un WhatsApp

- Method : POST
- URL : http://localhost:8081/notifications
- Body → raw → JSON :

```json
{
    "type": "ALERT_TEMP",
    "recipient": "216XXXXXXXX",
    "message": "Alerte RoboCare : température 45°C sur Parcelle Nord"
}
```

Le système détecte que le destinataire est un numéro → envoie via WhatsApp.

→ VÉRIFIEZ VOTRE WHATSAPP !


### Envoyer un email (toujours pareil)

```json
{
    "type": "ALERT_TEMP",
    "recipient": "votre-email@gmail.com",
    "subject": "Alerte",
    "message": "Test email"
}
```

Le système détecte que c'est un email → envoie via Gmail.


## ÉTAPE 15 : Debug WhatsApp

### Erreur 401
→ Le token a expiré (24h max)
→ Aller sur Meta Developer → WhatsApp → API Setup → copier le nouveau token
→ Mettre à jour dans IntelliJ → relancer

### Erreur 400
→ Le numéro n'est pas dans la liste de test
→ Retourner étape 9 point 4-5

### Format numéro
→ Sans le + : 21612345678 (pas +21612345678)


---

# RÉSUMÉ VISUEL

```
PHASE 1 (maintenant) :                PHASE 2 (après) :
                                      
Étape 1 : Ouvrir IntelliJ             Étape 8  : Compte Meta
Étape 2 : Gmail App Password          Étape 9  : WhatsApp API Setup
Étape 3 : Config IntelliJ             Étape 10 : Créer WhatsAppSender.java
Étape 4 : Lancer ▶                    Étape 11 : Modifier application.yml
Étape 5 : Tester Postman              Étape 12 : Modifier NotificationService
Étape 6 : Debug si besoin             Étape 13 : Variables IntelliJ
Étape 7 : Tests unitaires             Étape 14 : Tester Postman
                                       Étape 15 : Debug si besoin
          ↓                                       ↓
    EMAIL MARCHE ✓                     EMAIL + WHATSAPP ✓
```


# FICHIERS DU PROJET

```
Phase 1 (Email) — 9 fichiers :
├── pom.xml
├── application.yml
├── NotificationApplication.java
├── NotificationRequest.java
├── NotificationResponse.java
├── NotificationSender.java          ← interface
├── EmailSender.java                 ← canal email
├── NotificationService.java         ← logique métier
├── NotificationController.java      ← API REST (5 endpoints)
└── NotificationServiceTest.java     ← 5 tests

Phase 2 (+ WhatsApp) — ajouter 1 fichier :
├── WhatsAppSender.java              ← nouveau fichier
└── (modifier application.yml + NotificationService.java)
```
