package com.handynest.identity;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

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

    Optional<User> findByPublicId(String publicId);

    List<User> findAllByStatusAndDeletedAtBefore(UserStatus status, Instant deletedAtBefore, Pageable pageable);
}
