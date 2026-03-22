package com.ashik.SpringEcom.service;

import com.ashik.SpringEcom.model.Product;
import com.ashik.SpringEcom.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public Product getProductById(int id) {
        return productRepo.findById(id).orElse(null);
    }

    public Product addProduct(Product product, MultipartFile image) throws IOException {
        product.setImageName(image.getOriginalFilename());
        product.setImageType(image.getContentType());
        product.setImageData(image.getBytes());
        return productRepo.save(product);
    }

    public Product updateProduct(Integer id, Product product, MultipartFile image) throws IOException {

        // ✅ Step 1: Fetch existing product
        Product existing = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // ✅ Step 2: Update fields
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setBrand(product.getBrand());
        existing.setPrice(product.getPrice());
        existing.setCategory(product.getCategory());
        existing.setReleaseDate(product.getReleaseDate());
        existing.setProductAvailable(product.isProductAvailable());
        existing.setStockQuantity(product.getStockQuantity());

        // ✅ Step 3: Update image only if provided
        if (image != null && !image.isEmpty()) {
            existing.setImageName(image.getOriginalFilename());
            existing.setImageType(image.getContentType());
            existing.setImageData(image.getBytes());
        }

        // ✅ Step 4: Save EXISTING entity
        return productRepo.save(existing);
    }

    public void deleteProduct(Integer id) {

        Product existing = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepo.delete(existing);
    }
}
