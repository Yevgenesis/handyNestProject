package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Performer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PerformerRepository extends JpaRepository<Performer, Long> {

    Performer findPerformerById(Long id);
}
