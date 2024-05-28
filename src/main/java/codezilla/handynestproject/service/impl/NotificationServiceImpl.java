package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.exception.NotificationNotFoundException;
import codezilla.handynestproject.model.entity.Notification;
import codezilla.handynestproject.repository.NotificationRepository;
import codezilla.handynestproject.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {


    private final NotificationRepository notificationRepository;


    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> findUnread(Long userId) {
        return notificationRepository.findByUserIdAndRead(userId, false);
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);

    }
}
