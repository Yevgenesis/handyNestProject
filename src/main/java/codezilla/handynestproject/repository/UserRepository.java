package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);
    User save(User user);

    @Query("SELECT AVG(f.grade) FROM Feedback f join Task t on f.task.id=t.id WHERE t.user.id = :userId")
    Double findAverageRatingByUserId(@Param("userId") Long userId);
}
