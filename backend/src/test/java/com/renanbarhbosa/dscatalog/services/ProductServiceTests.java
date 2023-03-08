package com.renanbarhbosa.dscatalog.services;

import com.renanbarhbosa.dscatalog.dto.ProductDTO;
import com.renanbarhbosa.dscatalog.entities.Product;
import com.renanbarhbosa.dscatalog.repositories.CategoryRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private PageImpl<Product> page;
    private Product product;
    private ProductDTO productDTO;


    @BeforeEach
    void setup() throws Exception {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;

        product = Factory.createProduct();

        productDTO = Factory.createProductDTO();

        page = new PageImpl<>(List.of(product));

        when(productRepository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

        when(productRepository.save(ArgumentMatchers.any())).thenReturn(product);

        when(productRepository.findById(existingId)).thenReturn(Optional.of(product));

        when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        when(productRepository.getReferenceById(existingId)).thenReturn(product);

        when(productRepository.getReferenceById(nonExistingId))
                .thenThrow(ResourceNotFoundException.class);

        doNothing().when(productRepository).deleteById(existingId);

        doThrow(EmptyResultDataAccessException.class)
                .when(productRepository).deleteById(nonExistingId);

        doThrow(DataIntegrityViolationException.class)
                .when(productRepository).deleteById(dependentId);
    }

    @Test
    public void findAllPagedShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductDTO> result = productService.findAllPaged(pageable);
        Assertions.assertNotNull(result);
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExists() {
        ProductDTO res = productService.findById(existingId);
        Assertions.assertNotNull(res);
        verify(productRepository, times(1)).findById(existingId);
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.findById(nonExistingId);
        });
        verify(productRepository, times(1))
                .findById(nonExistingId);
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists() {
        productDTO = Factory.createProductDTO();
        categoryRepository.getReferenceById(existingId);
        ProductDTO res = productService.update(existingId, productDTO);
        Assertions.assertNotNull(res);
        verify(productRepository, times(1)).getReferenceById(existingId);
        verify(categoryRepository, times(1)).getReferenceById(existingId);
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.update(nonExistingId, productDTO);
        });
        verify(productRepository, times(1)).getReferenceById(nonExistingId);
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> {
            productService.delete(existingId);
        });
        verify(productRepository, times(1))
                .deleteById(existingId);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productService.delete(nonExistingId);
        });
        verify(productRepository, times(1))
                .deleteById(nonExistingId);
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependantId() {
        Assertions.assertThrows(DatabaseException.class, () -> {
            productService.delete(dependentId);
        });
        verify(productRepository, times(1))
                .deleteById(dependentId);
    }
}
