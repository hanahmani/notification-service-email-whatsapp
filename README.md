# RoboCare Notification Service

Service Spring Boot pour envoyer des notifications par email, WhatsApp et SMS.

## Fonctionnalites

- Envoi d'emails a un ou plusieurs destinataires
- Envoi d'emails avec piece jointe PDF depuis une URL
- Envoi de messages WhatsApp texte
- Envoi de documents PDF via WhatsApp
- Envoi de SMS via TunisieSMS
- Endpoint de verification de sante du service
- Execution locale avec Maven ou via Docker

## Stack technique

- Java 17
- Spring Boot 3.2.4
- Spring Web
- Spring Mail
- Spring Validation
- Maven
- Docker / Docker Compose

## Prerequis

- Java 17+
- Maven 3.9+
- Docker et Docker Compose, optionnel
- Un compte SMTP pour l'envoi d'emails
- Un token WhatsApp Cloud API, optionnel
- Un identifiant TunisieSMS, optionnel

## Configuration

Le service utilise les variables d'environnement suivantes :

| Variable | Description | Obligatoire |
| --- | --- | --- |
| `MAIL_USERNAME` | Adresse email SMTP | Oui pour email |
| `MAIL_PASSWORD` | Mot de passe ou app password SMTP | Oui pour email |
| `WHATSAPP_TOKEN` | Token WhatsApp Cloud API | Oui pour WhatsApp |
| `WHATSAPP_PHONE_ID` | Phone Number ID WhatsApp | Oui pour WhatsApp |
| `TUNISIESMS_ID` | Identifiant API TunisieSMS | Oui pour SMS |
| `TUNISIESMS_URL` | URL API TunisieSMS | Non |
| `SMS_SENDER_NAME` | Nom de l'expediteur SMS | Non |

Exemple de fichier `.env` :

```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

WHATSAPP_TOKEN=your-whatsapp-token
WHATSAPP_PHONE_ID=your-phone-number-id

TUNISIESMS_ID=your-tunisiesms-id
TUNISIESMS_URL=https://api.tunisiesms.tn/sms/send
SMS_SENDER_NAME=RoboCare
```

Par defaut, l'application ecoute sur le port `8082`.

## Lancement en local

Installer les dependances et compiler :

```bash
mvn clean package
```

Demarrer l'application :

```bash
mvn spring-boot:run
```

Ou lancer le JAR genere :

```bash
java -jar target/notification-service-1.0.0.jar
```

## Lancement avec Docker

Construire et demarrer le service :

```bash
docker compose up --build
```

Arreter le service :

```bash
docker compose down
```

## Endpoints

Base URL locale :

```text
http://localhost:8082
```

### Health check

```http
GET /api/notifications/health
```

Exemple :

```bash
curl http://localhost:8082/api/notifications/health
```

### Envoyer un email

```http
POST /api/notifications/email
```

Payload :

```json
{
  "to": ["user@example.com"],
  "subject": "Alerte RoboCare",
  "message": "Votre notification a ete envoyee avec succes."
}
```

Exemple :

```bash
curl -X POST http://localhost:8082/api/notifications/email \
  -H "Content-Type: application/json" \
  -d '{
    "to": ["user@example.com"],
    "subject": "Alerte RoboCare",
    "message": "Votre notification a ete envoyee avec succes."
  }'
```

Email avec PDF :

```json
{
  "to": ["user@example.com"],
  "subject": "Rapport RoboCare",
  "message": "Veuillez trouver le rapport en piece jointe.",
  "fileUrl": "https://example.com/report.pdf"
}
```

### Envoyer un message WhatsApp

```http
POST /api/notifications/whatsapp
```

Payload :

```json
{
  "to": ["+21612345678"],
  "message": "Alerte RoboCare"
}
```

Exemple :

```bash
curl -X POST http://localhost:8082/api/notifications/whatsapp \
  -H "Content-Type: application/json" \
  -d '{
    "to": ["+21612345678"],
    "message": "Alerte RoboCare"
  }'
```

WhatsApp avec PDF :

```json
{
  "to": ["+21612345678"],
  "message": "Veuillez consulter le document.",
  "type": "PDF",
  "fileUrl": "https://example.com/document.pdf"
}
```

### Envoyer un SMS

```http
POST /api/notifications/sms
```

Payload :

```json
{
  "to": ["+21612345678"],
  "message": "Votre code OTP est 1234"
}
```

Exemple :

```bash
curl -X POST http://localhost:8082/api/notifications/sms \
  -H "Content-Type: application/json" \
  -d '{
    "to": ["+21612345678"],
    "message": "Votre code OTP est 1234"
  }'
```

## Format de reponse

Les endpoints de notification retournent une reponse similaire :

```json
{
  "id": "abc12345",
  "channel": "EMAIL",
  "to": ["user@example.com"],
  "status": "SENT",
  "successCount": 1,
  "failCount": 0,
  "message": "Votre notification a ete envoyee avec succes.",
  "timestamp": "2026-04-29T12:00:00",
  "errorDetails": null
}
```

Statuts possibles :

- `SENT` : tous les messages ont ete envoyes
- `FAILED` : aucun message n'a ete envoye
- `PARTIAL` : certains messages ont echoue

## Tests

Lancer les tests :

```bash
mvn test
```

## Structure du projet

```text
src/main/java/com/robocare/notification
├── controller
│   ├── EmailController.java
│   ├── HealthController.java
│   ├── SmsController.java
│   └── WhatsappController.java
├── dto
│   ├── EmailRequest.java
│   ├── NotificationResponse.java
│   ├── SmsRequest.java
│   └── WhatsappRequest.java
├── service
│   ├── EmailService.java
│   ├── SmsService.java
│   └── WhatsappService.java
└── NotificationApplication.java
```

## Notes

- Pour Gmail, utilisez un app password au lieu du mot de passe principal du compte.
- Pour WhatsApp, les numeros sont envoyes a l'API sans le signe `+`.
- Pour les pieces jointes PDF, l'URL doit etre accessible publiquement par le service.
- Si une integration n'est pas configuree, le service retourne une reponse `FAILED` avec le detail dans `errorDetails`.
