package com.atividade.produtos.service;

import com.atividade.produtos.dto.ProductRequest;
import com.atividade.produtos.dto.ProductResponse;
import com.atividade.produtos.exception.ProductNotFoundException;
import com.atividade.produtos.model.Product;
import com.atividade.produtos.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProductResponse create(ProductRequest request, String user) {
        Product product = new Product();
        copyFields(request, product);
        product.setCreatedBy(user);
        return ProductResponse.from(repository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(String category) {
        List<Product> products;
        if (category == null || category.isBlank()) {
            products = repository.findByActiveTrueOrderByNameAsc();
        } else {
            products = repository.findByCategoryIgnoreCaseAndActiveTrueOrderByNameAsc(category.trim());
        }
        return products.stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(String id) {
        return ProductResponse.from(getProductOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> top5ByPrice() {
        return repository.findTop5ByActiveTrueOrderByPriceDesc().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse update(String id, ProductRequest request) {
        Product product = getProductOrThrow(id);
        copyFields(request, product);
        return ProductResponse.from(repository.saveAndFlush(product));
    }

    @Transactional
    public void inactivate(String id) {
        Product product = getProductOrThrow(id);
        product.setActive(false);
    }

    private Product getProductOrThrow(String id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private void copyFields(ProductRequest request, Product product) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
    }
}