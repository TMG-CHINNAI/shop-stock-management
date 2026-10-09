package com.shop.service;

import com.shop.model.*;
import com.shop.util.FileManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class InventoryService {

    private final ArrayList<Product> products;
    private final ArrayList<StockMovement> movements;
    private final HashSet<String> codes;

    private final FileManager fileManager;

    private int nextProductId = 1;
    private int nextMovementId = 1;

    public InventoryService(FileManager fileManager) {

        this.fileManager = fileManager;

        products = new ArrayList<>(fileManager.loadProducts());

        movements = new ArrayList<>(
            fileManager.loadMovements(products)
        );

        codes = new HashSet<>();

        for (Product p : products) {
            codes.add(p.getCode());
        }

        for (Product p : products) {
            if (p.getId() >= nextProductId) {
                nextProductId = p.getId() + 1;
            }
        }

        for (StockMovement m : movements) {
            if (m.getId() >= nextMovementId) {
                nextMovementId = m.getId() + 1;
            }
        }
    }

    public String[] getCategories() {

        return new String[] {
            "Stationery",
            "Grocery",
            "Household",
            "Other"
        };
    }

    public boolean addProduct(
            String code,
            String name,
            int categoryChoice,
            int openingStock,
            int lowStockLimit) {

        code = code.trim().toUpperCase(Locale.ROOT);
        name = name.trim();

        String[] categories = getCategories();

        if (code.isBlank()
                || name.isBlank()
                || code.contains("|")
                || name.contains("|")
                || categoryChoice < 1
                || categoryChoice > 4
                || openingStock < 0
                || openingStock > 1000000
                || lowStockLimit < 0
                || lowStockLimit > 1000000
                || codes.contains(code)) {

            return false;
        }

        Product product = new Product(
            nextProductId,
            code,
            name,
            categories[categoryChoice - 1],
            openingStock,
            lowStockLimit
        );

        products.add(product);
        codes.add(code);

        try {

            fileManager.saveProducts(products);

            nextProductId++;

            return true;

        } catch (RuntimeException e) {

            products.remove(product);
            codes.remove(code);

            return false;
        }
    }

    public List<Product> getProducts() {

        return Collections.unmodifiableList(products);
    }

    public long getCurrentStock(int productId) {

        Product product = findProduct(productId);

        if (product == null) {
            return -1;
        }

        long balance = product.getOpeningStock();

        for (StockMovement movement : movements) {

            if (movement.getProductId() == productId) {

                if (movement.getType() == MovementType.IN) {
                    balance += movement.getQuantity();
                } else {
                    balance -= movement.getQuantity();
                }
            }
        }

        return balance;
    }

    public boolean addStock(int productId, int quantity) {

        return recordMovement(
            productId,
            MovementType.IN,
            quantity
        );
    }

    public boolean removeStock(int productId, int quantity) {

        if (quantity < 1
                || quantity > 1000000
                || getCurrentStock(productId) < quantity) {

            return false;
        }

        return recordMovement(
            productId,
            MovementType.OUT,
            quantity
        );
    }

    private boolean recordMovement(
            int productId,
            MovementType type,
            int quantity) {

        if (findProduct(productId) == null
                || quantity < 1
                || quantity > 1000000) {

            return false;
        }

        StockMovement movement = new StockMovement(
            nextMovementId,
            productId,
            type,
            quantity
        );

        movements.add(movement);

        try {

            fileManager.saveMovements(movements);

            nextMovementId++;

            return true;

        } catch (RuntimeException e) {

            movements.remove(movement);

            return false;
        }
    }

    public void showProducts() {

        if (products.isEmpty()) {

            System.out.println("No records found.");

            return;
        }

        for (Product product : products) {

            product.showDetails(
                getCurrentStock(product.getId())
            );
        }
    }

    public void showLowStock() {

        boolean found = false;

        for (Product product : products) {

            long stock = getCurrentStock(product.getId());

            if (stock <= product.getLowStockLimit()) {

                System.out.println(
                    "ID: " + product.getId()
                    + " | " + product.getName()
                    + " | Current: " + stock
                    + " | Limit: " + product.getLowStockLimit()
                );

                found = true;
            }
        }

        if (!found) {
            System.out.println("No low-stock products.");
        }
    }

    public void showReport() {

        String report = createReport();

        System.out.print(report);
    }

    public boolean saveReport() {

        try {

            fileManager.saveReport(createReport());

            return true;

        } catch (RuntimeException e) {

            return false;
        }
    }

    private String createReport() {

        long totalStock = 0;
        int lowStockProducts = 0;

        for (Product product : products) {

            long stock =
                getCurrentStock(product.getId());

            totalStock += stock;

            if (stock <= product.getLowStockLimit()) {
                lowStockProducts++;
            }
        }

        return
            "Total products: " + products.size() + "\n"
            + "Total stock units: " + totalStock + "\n"
            + "Low-stock products: " + lowStockProducts + "\n"
            + "Total recorded movements: " + movements.size() + "\n";
    }

    private Product findProduct(int id) {

        for (Product product : products) {

            if (product.getId() == id) {
                return product;
            }
        }

        return null;
    }
}

