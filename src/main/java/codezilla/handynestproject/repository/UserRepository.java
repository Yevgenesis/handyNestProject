package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Performer;
import codezilla.handynestproject.model.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserInfo, Long> {

}
