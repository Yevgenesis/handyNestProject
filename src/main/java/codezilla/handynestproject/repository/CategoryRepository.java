package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    //List<Category> findByParentId(Long parentId);
    Category findById(long id);

    //Set<Category> findCategoriesByIdIn(List<Long> ids);
}
