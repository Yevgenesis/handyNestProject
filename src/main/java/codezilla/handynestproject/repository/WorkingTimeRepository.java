package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.WorkingTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkingTimeRepository extends JpaRepository<WorkingTime, UUID> {

    WorkingTime getById(Long workingTimeId);


}
