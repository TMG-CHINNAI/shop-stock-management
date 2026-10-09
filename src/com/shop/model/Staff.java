package com.shop.model;

public class Staff extends User {

    public Staff() {
        super(1, "Staff");
    }

    @Override
    public void showProfile() {
        System.out.println("Logged in as Staff (ID: " + getId() + ")");
    }
}

	