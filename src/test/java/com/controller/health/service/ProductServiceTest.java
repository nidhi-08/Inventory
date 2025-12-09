package com.controller.health.service;

import com.controller.health.dto.ProductRequest;
import com.controller.health.dto.ProductResponse;
import com.controller.health.entity.Product;
import com.controller.health.exceptions.ProductNotFoundException;
import com.controller.health.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ------------------------------------------------------
    // TEST 1: create()
    // ------------------------------------------------------
    @Test
    void testCreateProduct() {
        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setCategory("Electronics");
        request.setPrice(BigDecimal.valueOf(90000));

        Product saved = new Product();
        saved.setId(1L);
        saved.setName("Laptop");
        saved.setCategory("Electronics");
        saved.setPrice(BigDecimal.valueOf(90000));

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.create(request);

        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getName());
        assertEquals("Electronics", response.getCategory());
    }

    // ------------------------------------------------------
    // TEST 2: getById()
    // ------------------------------------------------------
    @Test
    void testGetByIdSuccess() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Phone");
        product.setCategory("Electronics");
        product.setPrice(BigDecimal.valueOf(30000));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getById(1L);

        assertEquals("Phone", response.getName());
        assertEquals("Electronics", response.getCategory());
    }

    @Test
    void testGetByIdNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getById(99L));
    }

    // ------------------------------------------------------
    // TEST 3: update()
    // ------------------------------------------------------
    @Test
    void testUpdateProduct() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setName("Old");
        existing.setCategory("Electronics");
        existing.setPrice(BigDecimal.valueOf(50000));

        ProductRequest request = new ProductRequest();
        request.setName("Updated");
        request.setCategory("Electronics");
        request.setPrice(BigDecimal.valueOf(55000));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);

        ProductResponse response = productService.update(1L, request);

        assertEquals("Updated", response.getName());
        assertEquals(BigDecimal.valueOf(55000), response.getPrice());
    }

    @Test
    void testUpdateNotFound() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        ProductRequest req = new ProductRequest();
        req.setName("X");

        assertThrows(ProductNotFoundException.class, () -> productService.update(5L, req));
    }

    // ------------------------------------------------------
    // TEST 4: delete()
    // ------------------------------------------------------
    @Test
    void testDeleteSuccess() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteNotFound() {
        when(productRepository.existsById(1L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> productService.delete(1L));
    }

    // ------------------------------------------------------
    // TEST 5: search()
    // ------------------------------------------------------
    @Test
    void testSearchWithCategory() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price"));

        Product p = new Product();
        p.setId(1L);
        p.setName("Apple");
        p.setCategory("fruit");
        p.setPrice(BigDecimal.valueOf(120.0));

        when(productRepository.findByCategory(eq("fruit"), eq(pageable)))
                .thenReturn(List.of(p));

        List<ProductResponse> res = productService.search("fruit", 0, 10, "price");

        assertEquals(1, res.size());
        assertEquals("Apple", res.get(0).getName());
    }

    @Test
    void testSearchWithoutCategory() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price"));

        Product p = new Product();
        p.setId(1L);
        p.setName("Soap");
        p.setCategory("cosmetic");
        p.setPrice(BigDecimal.valueOf(50.0));

        Page<Product> page = new PageImpl<>(List.of(p), pageable, 1);

        when(productRepository.findAll(eq(pageable)))
                .thenReturn(page);

        List<ProductResponse> res = productService.search(null, 0, 10, "price");

        assertEquals(1, res.size());
        assertEquals("Soap", res.get(0).getName());
    }
}
