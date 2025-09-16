package shop.francesinha.products.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shop.francesinha.products.model.Inventory;
import shop.francesinha.products.repo.InventoryRepository;

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
