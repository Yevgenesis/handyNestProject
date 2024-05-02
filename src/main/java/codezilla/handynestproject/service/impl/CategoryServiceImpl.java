package codezilla.handynestproject.service.impl;

import codezilla.handynestproject.model.entity.Category;
import codezilla.handynestproject.repository.CategoryRepository;
import codezilla.handynestproject.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;


    @Override
    public List<Category> getCategories() {

        return categoryRepository.findAll();

    }
}
