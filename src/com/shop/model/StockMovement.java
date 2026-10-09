package com.shop.model;

public class StockMovement {

    private int id;
    private int productId;
    private int quantity;

    private MovementType type;

    public StockMovement(int id, int productId,
                         MovementType type, int quantity) {

        this.id = id;
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public int getProductId() {
        return productId;
    }

    public MovementType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }
}
