package com.renanbarhbosa.dscatalog.services;

import com.renanbarhbosa.dscatalog.dto.ProductDTO;
import com.renanbarhbosa.dscatalog.repositories.ProductRepository;
import com.renanbarhbosa.dscatalog.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ProductServiceIT {

    @Autowired
    private ProductService service;

    @Autowired
    private ProductRepository repository;

    private Long existingId;
    private Long nonExistingId;
    private Long countTotalProducts;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        countTotalProducts = 25L;
    }

    @Test
    public void findAllPagedShouldReturnPageWhenPage0Size10() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ProductDTO> res = service.findAllPaged(pageRequest);
        Assertions.assertFalse(res.isEmpty());
        Assertions.assertEquals(0, res.getNumber());
        Assertions.assertEquals(10, res.getSize());
        Assertions.assertEquals(countTotalProducts, res.getTotalElements());
    }

    @Test
    public void findAllPagedShouldReturnEmptyPagePageWhenPageDoesNotExist() {
        PageRequest pageRequest = PageRequest.of(50, 10);
        Page<ProductDTO> res = service.findAllPaged(pageRequest);
        Assertions.assertTrue(res.isEmpty());
    }

    @Test
    public void findAllPagedShouldReturnSortedPageWhenSortByName() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name"));
        Page<ProductDTO> res = service.findAllPaged(pageRequest);
        Assertions.assertFalse(res.isEmpty());
        Assertions.assertEquals("Macbook Pro", res.getContent().get(0).getName());
        Assertions.assertEquals("PC Gamer", res.getContent().get(1).getName());
        Assertions.assertEquals("PC Gamer Alfa", res.getContent().get(2).getName());
    }

    @Test
    public void deleteShouldDeleteResourceWhenIdExists() {
        service.delete(existingId);
        Assertions.assertEquals(countTotalProducts - 1, repository.count());
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingId);
        });
    }
}
