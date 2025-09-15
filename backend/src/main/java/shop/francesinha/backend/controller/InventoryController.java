package shop.francesinha.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shop.francesinha.backend.model.Inventory;
import shop.francesinha.backend.model.Product;
import shop.francesinha.backend.repo.InventoryRepository;
import shop.francesinha.backend.repo.ProductRepository;

import java.util.List;

@RequestMapping("/inventory")
@RestController
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping
    public List<Inventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    @PostMapping
    public Inventory saveInventory(@RequestBody Inventory inventory) {
        if (inventoryRepository.findById(inventory.getId()).isPresent()) {
            throw new RuntimeException("Inventory with ID " + inventory.getId() + " already exists.");
        }
        return inventoryRepository.save(inventory);
    }
}
