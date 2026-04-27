package com.robocare.notification.sender;

import com.robocare.notification.dto.NotificationRequest;

public interface NotificationSender {

    boolean send(NotificationRequest request) throws Exception;

    String getChannel();
}
