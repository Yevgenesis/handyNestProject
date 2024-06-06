package codezilla.handynestproject.repository;


import codezilla.handynestproject.model.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c " +
            "WHERE c.user.id = :id OR c.performer.id = :id")
    List<Chat> findAllByUserIdOrPerformerId(Long id);
}
