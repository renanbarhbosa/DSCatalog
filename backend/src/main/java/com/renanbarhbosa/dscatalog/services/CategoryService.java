package com.renanbarhbosa.dscatalog.services;

import com.renanbarhbosa.dscatalog.entities.Category;
import com.renanbarhbosa.dscatalog.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository repository;

    public List<Category> findAll() {
        return repository.findAll();
    }
}
