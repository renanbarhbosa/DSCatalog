package com.renanbarhbosa.dscatalog.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renanbarhbosa.dscatalog.dto.ProductDTO;
import com.renanbarhbosa.dscatalog.services.ProductService;
import com.renanbarhbosa.dscatalog.services.exceptions.DatabaseException;
import com.renanbarhbosa.dscatalog.services.exceptions.ResourceNotFoundException;
import factory.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProductService service;

    @Autowired
    private ObjectMapper objectMapper;

    private Long existingId;
    private Long nonExistingId;
    private Long dependantId;
    private ProductDTO productDTO;
    private PageImpl<ProductDTO> page;

    @BeforeEach
    void setUp() throws Exception {

        existingId = 1L;

        nonExistingId = 2L;

        dependantId = 3L;

        productDTO = Factory.createProductDTO();

        page = new PageImpl<>(List.of(productDTO));

        when(service.findAllPaged(any())).thenReturn(page);

        when(service.findById(existingId)).thenReturn(productDTO);

        when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

        when(service.update(eq(existingId), any())).thenReturn(productDTO);

        when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);

        when(service.insert(any())).thenReturn(productDTO);

        doNothing().when(service).delete(existingId);

        doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);

        doThrow(DatabaseException.class).when(service).delete(dependantId);
    }

    @Test
    public void findAllShouldReturnPage() throws Exception {
        ResultActions res = mockMvc.perform(get("/products")
                .accept(MediaType.APPLICATION_JSON));
        res.andExpect(status().isOk());
    }

    @Test
    public void findByIdShouldReturnProductWhenIdExists() throws Exception {
        ResultActions res = mockMvc.perform(get("/products/{id}", existingId)
                .accept(MediaType.APPLICATION_JSON));
        res.andExpect(jsonPath("$.id").exists());
        res.andExpect(jsonPath("$.name").exists());
        res.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        ResultActions res = mockMvc.perform(get("/products/{id}", nonExistingId)
                .accept(MediaType.APPLICATION_JSON));
        res.andExpect(status().isNotFound());
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(productDTO);
        ResultActions res = mockMvc.perform(put("/products/{id}", existingId)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        res.andExpect(status().isOk());
        res.andExpect(jsonPath("$.id").exists());
        res.andExpect(jsonPath("$.name").exists());
        res.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void insertShouldReturnCreated201AsSoAProductDTO() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(productDTO);
        ResultActions res = mockMvc.perform(post("/products")
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        res.andExpect(status().isCreated());
        res.andExpect(jsonPath("$.id").exists());
        res.andExpect(jsonPath("$.name").exists());
        res.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(productDTO);
        ResultActions res = mockMvc.perform(put("/products/{id}", nonExistingId)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        res.andExpect(status().isNotFound());
    }

    @Test
    public void deleteShouldReturnNoContent204WhenIdExists() throws Exception {
        ResultActions res = mockMvc.perform(delete("/products/{id}", existingId))
                .andExpect(status().is(204));
    }

    @Test
    public void deleteShouldReturnNotFound404WhenIdDoesNotExist() throws Exception{
        ResultActions res = mockMvc.perform(delete("/products{id}", nonExistingId))
                .andExpect(status().isNotFound());
    }
}
