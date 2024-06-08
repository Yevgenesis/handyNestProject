package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);

    User save(User user);

    // Returns the percentage of positive feedbacks with a grade of 4 or 5
    // and rounds the response to one decimal place.
    @Query(value = """
            SELECT COALESCE(
                ROUND(
                    COUNT(f) FILTER (WHERE f.grade >= 4) * 100.0 / NULLIF(COUNT(f), 0)
                ,1)
            ,100)
            FROM feedback f
            JOIN task t ON f.task_id = t.id
            WHERE t.user_id = :userId AND f.sender_id != :userId
            """, nativeQuery = true)
    Double getRatingByUserId(@Param("userId") Long userId);

    Optional<User> findByEmail(String email);
}
