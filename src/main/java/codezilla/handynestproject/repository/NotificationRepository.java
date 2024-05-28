package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findById(Long id);
    List<Notification> findByUserIdAndIsRead(Long userId, boolean isRead);
}
