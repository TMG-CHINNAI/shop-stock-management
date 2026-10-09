package com.shop.model;

public class Product {

    private int id;
    private int openingStock;
    private int lowStockLimit;

    private String code;
    private String name;
    private String category;

    public Product(int id, String code, String name,
                   String category, int openingStock,
                   int lowStockLimit) {

        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
        this.openingStock = openingStock;
        this.lowStockLimit = lowStockLimit;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getOpeningStock() {
        return openingStock;
    }

    public int getLowStockLimit() {
        return lowStockLimit;
    }

    public void showDetails(long currentStock) {

        System.out.println(
            "ID: " + id +
            " | Code: " + code +
            " | Name: " + name +
            " | Category: " + category +
            " | Stock: " + currentStock +
            " | Low limit: " + lowStockLimit
        );
    }
}