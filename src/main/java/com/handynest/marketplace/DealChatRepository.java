package com.handynest.marketplace;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DealChatRepository extends JpaRepository<DealChat, Long> {

  long countByTaskPublicId(String taskPublicId);

  @EntityGraph(attributePaths = {"deal", "task", "customer", "performer", "performer.user"})
  Optional<DealChat> findByDealId(Long dealId);

  @EntityGraph(
      attributePaths = {
        "deal",
        "deal.acceptedOffer",
        "task",
        "task.customer",
        "customer",
        "performer",
        "performer.user"
      })
  Optional<DealChat> findByPublicId(String publicId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
            select chat from DealChat chat
            join fetch chat.deal deal
            join fetch deal.acceptedOffer
            join fetch chat.task task
            join fetch task.customer
            join fetch chat.customer
            join fetch chat.performer performer
            join fetch performer.user
            where chat.publicId = :publicId
            """)
  Optional<DealChat> findByPublicIdForUpdate(@Param("publicId") String publicId);

  @Query(
      value =
          """
          select
            dc.public_id as "publicId",
            d.public_id as "dealId",
            t.public_id as "taskId",
            t.title as "taskTitle",
            customer.public_id as "customerId",
            coalesce(nullif(trim(concat_ws(' ', customer.first_name, customer.last_name)), ''), customer.email) as "customerDisplayName",
            performer.public_id as "performerId",
            performer.display_name as "performerDisplayName",
            case when dc.customer_id = :userId then 'CUSTOMER' else 'PERFORMER' end as "participantRole",
            dc.status as "status",
            latest.text as "lastMessageText",
            latest.message_type as "lastMessageType",
            latest.created_at as "lastMessageAt",
            coalesce(unread.count, 0) as "unreadCount",
            dc.closed_at as "closedAt",
            dc.created_at as "createdAt",
            dc.updated_at as "updatedAt"
          from deal_chat dc
          join deal d on d.id = dc.deal_id
          join marketplace_task t on t.id = dc.task_id
          join handy_user customer on customer.id = dc.customer_id
          join performer_profile performer on performer.id = dc.performer_id
          left join lateral (
            select message.text, message.message_type, message.created_at
            from chat_message message
            where message.chat_id = dc.id and message.deleted_at is null
            order by message.id desc
            limit 1
          ) latest on true
          left join lateral (
            select count(*) as count
            from chat_message message
            where message.chat_id = dc.id
              and message.sender_id <> :userId
              and message.read_at is null
              and message.deleted_at is null
          ) unread on true
          where dc.customer_id = :userId or performer.user_id = :userId
          order by coalesce(latest.created_at, dc.created_at) desc
          """,
      nativeQuery = true)
  List<DealChatSummaryProjection> findSummariesForParticipant(@Param("userId") Long userId);

  @Query(
      value =
          """
          select
            dc.public_id as "publicId",
            d.public_id as "dealId",
            t.public_id as "taskId",
            t.title as "taskTitle",
            customer.public_id as "customerId",
            coalesce(nullif(trim(concat_ws(' ', customer.first_name, customer.last_name)), ''), customer.email) as "customerDisplayName",
            performer.public_id as "performerId",
            performer.display_name as "performerDisplayName",
            case when dc.customer_id = :userId then 'CUSTOMER' else 'PERFORMER' end as "participantRole",
            dc.status as "status",
            latest.text as "lastMessageText",
            latest.message_type as "lastMessageType",
            latest.created_at as "lastMessageAt",
            coalesce(unread.count, 0) as "unreadCount",
            dc.closed_at as "closedAt",
            dc.created_at as "createdAt",
            dc.updated_at as "updatedAt"
          from deal_chat dc
          join deal d on d.id = dc.deal_id
          join marketplace_task t on t.id = dc.task_id
          join handy_user customer on customer.id = dc.customer_id
          join performer_profile performer on performer.id = dc.performer_id
          left join lateral (
            select message.text, message.message_type, message.created_at
            from chat_message message
            where message.chat_id = dc.id and message.deleted_at is null
            order by message.id desc
            limit 1
          ) latest on true
          left join lateral (
            select count(*) as count
            from chat_message message
            where message.chat_id = dc.id
              and message.sender_id <> :userId
              and message.read_at is null
              and message.deleted_at is null
          ) unread on true
          where dc.public_id = :chatId
            and (dc.customer_id = :userId or performer.user_id = :userId)
          """,
      nativeQuery = true)
  Optional<DealChatSummaryProjection> findSummaryForParticipant(
      @Param("chatId") String chatId, @Param("userId") Long userId);
}
