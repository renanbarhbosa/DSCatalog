package com.renanbarhbosa.dscatalog.services;

import com.renanbarhbosa.dscatalog.entities.Product;
import com.renanbarhbosa.dscatalog.repositories.ProductRepository;
import com.renanbarhbosa.dscatalog.services.exceptions.DatabaseException;
import com.renanbarhbosa.dscatalog.services.exceptions.ResourceNotFoundException;
import factory.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private PageImpl<Product> page;
    private Product product;


    @BeforeEach
    void setup() throws Exception {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;

        product = Factory.createProduct();

        page = new PageImpl<>(List.of(product));

        when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

        when(repository.save(ArgumentMatchers.any())).thenReturn(product);

        when(repository.findById(existingId)).thenReturn(Optional.of(product));

        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        doNothing().when(repository).deleteById(existingId);

        doThrow(EmptyResultDataAccessException.class)
                .when(repository).deleteById(nonExistingId);

        doThrow(DataIntegrityViolationException.class)
                .when(repository).deleteById(dependentId);
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> {
            service.delete(existingId);
        });
        verify(repository, times(1))
                .deleteById(existingId);
    }

    @Test
    public void deleteShouldThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingId);
        });
        verify(repository, times(1))
                .deleteById(nonExistingId);
    }

    @Test
    public void deleteShouldThrowsDatabaseExceptionWhenDependantId() {
        Assertions.assertThrows(DatabaseException.class, () -> {
            service.delete(dependentId);
        });
        verify(repository, times(1))
                .deleteById(dependentId);
    }
}
