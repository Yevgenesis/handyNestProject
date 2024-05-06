package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Performer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PerformerRepository extends JpaRepository<Performer, Long> {

    @Query("select p from Performer p join fetch p.categories c where p.id=:id")
    Optional<Performer> findPerformerWithCategoriesById(Long id);
}
