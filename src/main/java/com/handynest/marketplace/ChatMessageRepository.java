package com.handynest.marketplace;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Optional<ChatMessage> findByPublicIdAndDeletedAtIsNull(String publicId);

    @EntityGraph(attributePaths = {
            "chat",
            "sender"
    })
    Page<ChatMessage> findAllByChatPublicIdAndDeletedAtIsNull(String chatId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ChatMessage message
            set message.readAt = :readAt
            where message.chat.publicId = :chatId
              and message.sender.id <> :readerId
              and message.readAt is null
              and message.deletedAt is null
            """)
    int markUnreadMessagesAsRead(
            @Param("chatId") String chatId,
            @Param("readerId") Long readerId,
            @Param("readAt") Instant readAt
    );
}
