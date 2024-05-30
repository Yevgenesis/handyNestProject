package codezilla.handynestproject.service;

import codezilla.handynestproject.model.entity.Notification;

import java.util.List;

public interface NotificationService {


    Notification save(Notification notification);
//    List<Notification> findUnread(Long userId);
    void markAsRead(Long id);
}
