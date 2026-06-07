package com.handynest.marketplace;

import com.handynest.common.error.InvalidStatusTransitionException;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MarketplaceStateMachineTest {

    @Test
    void taskLifecycleAllowsApprovedFlowAndRejectsTerminalTransitions() {
        MarketplaceTask task = new MarketplaceTask(null, null, null);
        assertEquals(TaskStatus.DRAFT, task.getStatus());
        assertEquals(PublicationStatus.UNPUBLISHED, task.getPublicationStatus());

        task.submitForModeration();
        assertEquals(TaskStatus.MODERATION, task.getStatus());

        task.publish();
        assertEquals(TaskStatus.OPEN, task.getStatus());
        assertEquals(PublicationStatus.PUBLISHED, task.getPublicationStatus());

        TaskOffer offer = new TaskOffer(task, null);
        task.acceptOffer(offer, Instant.now());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());

        task.submitWork(Instant.now());
        assertEquals(TaskStatus.WORK_SUBMITTED, task.getStatus());

        task.requestRevision();
        assertEquals(TaskStatus.REVISION_REQUESTED, task.getStatus());
        assertEquals(1, task.getRevisionCount());

        task.resumeAfterRevision();
        task.submitWork(Instant.now());
        task.completeWork(Instant.now());
        assertEquals(TaskStatus.COMPLETED, task.getStatus());

        assertThrows(InvalidStatusTransitionException.class, () -> task.acceptOffer(offer, Instant.now()));
        assertThrows(InvalidStatusTransitionException.class, () -> task.cancelOpen(Instant.now()));
    }

    @Test
    void taskOfferLifecycleKeepsAcceptedOfferTerminal() {
        TaskOffer offer = new TaskOffer(null, null);
        offer.setMessage("Ready");
        offer.setProposedPrice(BigDecimal.TEN);
        offer.setExpiresAt(Instant.now().plusSeconds(60));

        offer.accept(Instant.now());
        assertEquals(TaskOfferStatus.ACCEPTED, offer.getStatus());
        assertThrows(InvalidStatusTransitionException.class, () -> offer.reject(Instant.now()));
        assertThrows(InvalidStatusTransitionException.class, () -> offer.cancel(Instant.now()));
        assertThrows(InvalidStatusTransitionException.class, () -> offer.expire(Instant.now()));
    }

    @Test
    void dealLifecycleSupportsRevisionResubmissionAndDisputeResolution() {
        MarketplaceTask task = new MarketplaceTask(null, null, null);
        task.publish();
        TaskOffer offer = new TaskOffer(task, null);
        Deal deal = new Deal(task, offer);

        deal.submitWork();
        assertEquals(DealStatus.WORK_SUBMITTED, deal.getStatus());

        deal.requestRevision();
        assertEquals(DealStatus.REVISION_REQUESTED, deal.getStatus());
        assertEquals(1, deal.getRevisionCount());

        deal.resumeAfterRevision();
        assertEquals(DealStatus.ACTIVE, deal.getStatus());

        deal.submitWork();
        deal.openDispute();
        assertEquals(DealStatus.DISPUTED, deal.getStatus());

        deal.resolveDisputeCompleted(Instant.now());
        assertEquals(DealStatus.COMPLETED, deal.getStatus());
        assertThrows(InvalidStatusTransitionException.class, () -> deal.submitWork());
    }
}
