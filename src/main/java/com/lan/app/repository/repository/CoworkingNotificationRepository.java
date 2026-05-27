package com.lan.app.repository;

import com.lan.app.domain.model.CoworkingNotification;

import java.util.List;

public interface CoworkingNotificationRepository {
    List<CoworkingNotification> list();
}
