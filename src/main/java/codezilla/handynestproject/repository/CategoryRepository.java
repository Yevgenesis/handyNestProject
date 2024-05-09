package codezilla.handynestproject.repository;

import codezilla.handynestproject.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentId(Long parentId);

    Set<Category> findCategoriesByIdIn(List<Long> ids);
}
