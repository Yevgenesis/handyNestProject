package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findAllByPerformerId(Long projectId);
    Attachment save(Attachment attachment);
}
