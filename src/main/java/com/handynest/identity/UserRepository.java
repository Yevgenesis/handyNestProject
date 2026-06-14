package com.handynest.identity;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByPublicId(String publicId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select user from User user where user.id = :id")
  Optional<User> findByIdForUpdate(@Param("id") Long id);

  List<User> findAllByStatusAndDeletedAtBefore(
      UserStatus status, Instant deletedAtBefore, Pageable pageable);
}
