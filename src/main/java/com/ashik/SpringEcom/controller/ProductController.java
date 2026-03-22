package com.ashik.SpringEcom.controller;

import com.ashik.SpringEcom.model.ApiResponse;
import com.ashik.SpringEcom.model.Product;
import com.ashik.SpringEcom.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173"})
public class ProductController {

    @Autowired
    private ProductService productService;

//    @GetMapping("/products")
//    public ResponseEntity<List<Product>> getProducts() {
//        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.ACCEPTED);
//    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        List<Product> products = productService.getAllProducts();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Products fetched successfully", products)
        );
    }

    //    @GetMapping("/product/{id}")
//    public ResponseEntity<Product> getProductById(@PathVariable int id) {
//        Product product = productService.getProductById(id);
//        if (product != null) {
//            return new ResponseEntity<>(product, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
    @GetMapping("/product/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable int id) {

        Product product = productService.getProductById(id);

        if (product != null) {
            return new ResponseEntity<>(
                    new ApiResponse<>(true, "Product fetched successfully", product),
                    HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Product not found"),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @GetMapping("/product/{productId}/image")
    public ResponseEntity<byte[]> getImageByProductId(@PathVariable int productId) {
        Product product = productService.getProductById(productId);
        if (product != null) {
            return new ResponseEntity<>(product.getImageData(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/product")
    public ResponseEntity<?> addProduct(
            @RequestPart("product") String productJson,
            @RequestPart("image") MultipartFile image) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Product product = mapper.readValue(productJson, Product.class);

            Product savedProduct = productService.addProduct(product, image);
            return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Integer id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            Product product = mapper.readValue(productJson, Product.class);

            Product updatedProduct = productService.updateProduct(id, product, image);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Product updated successfully", updatedProduct)
            );

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Invalid product JSON"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage()));
        }
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Integer id) {

        try {
            productService.deleteProduct(id);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Product deleted successfully")
            );

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage()));
        }
    }


}
