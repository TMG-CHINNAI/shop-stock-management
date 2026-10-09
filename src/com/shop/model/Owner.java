package com.shop.model;

public class Owner extends User {
	

	    public Owner() {
	        super(0, "Owner");
	    }

	    @Override
	    public void showProfile() {
	        System.out.println("Logged in as Owner (ID: " + getId() + ")");
	    }
	}