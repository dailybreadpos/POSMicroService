package com.coder.pema.posmicroservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for the Inventory microservice.
 * The 'name' attribute must match the 'spring.application.name' of the
 * Inventory service
 * as registered in Eureka (which is "INVENTORY" based on your Eureka console).
 * The 'path' attribute specifies the base path for the Inventory's REST API.
 */
@FeignClient(name = "INVENTORY", path = "/api/inventory") // Use the exact service name "INVENTORY"
public interface InventoryClient {

    /**
     * Defines the API call to get a single product from the Inventory service.
     * This method corresponds to GET /api/inventory/{id} in the Inventory service.
     *
     * We need a simple DTO here to match the response from the Inventory service.
     * This DTO must have fields that match the JSON structure returned by the
     * Inventory's GET /api/inventory/{id} endpoint (e.g., id, name, description,
     * image, price).
     */
    @GetMapping("/{id}")
    InventoryProductResponse getProductDetails(@PathVariable("id") Long id);

    // Inner class to represent the response structure from Inventory's product
    // endpoint
    // This should mirror your Inventory's ProductDTO or Inventory entity's fields
    // that are exposed
    class InventoryProductResponse {
        public Long id;
        public String name;
        public String description;
        public String image;
        public double price;
        public int stock; // Include if available and relevant, otherwise remove
        public String category; // Include if available and relevant, otherwise remove

        // Getters and setters (or use Lombok @Data)
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }
}
