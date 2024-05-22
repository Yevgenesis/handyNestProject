package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Performer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PerformerRepository extends JpaRepository<Performer, Long> {

//    @Query("select p from Performer p join fetch p.categories c where p.id=:id")
//    Optional<Performer> findPerformerWithCategoriesById(Long id);


    Performer save(Performer performer);

    @EntityGraph(value = "Performer.withUserAndCategoriesAndAddress", type = EntityGraph.EntityGraphType.LOAD)
    List<Performer> findAll();

    @EntityGraph(value = "Performer.withUserAndCategoriesAndAddress", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Performer> findById(Long id);

    @Query(value =
            "SELECT (COUNT(f) FILTER (WHERE f.grade >= 4) * 100.0 / COUNT(f)) " +
                    "FROM feedback f " +
                    "JOIN task t ON f.task_id = t.id " +
                    "WHERE t.performer_id = :performerId AND f.sender_id != :performerId",
            nativeQuery = true)
    Double getRatingByPerformerId(@Param("performerId") Long performerId);
}
