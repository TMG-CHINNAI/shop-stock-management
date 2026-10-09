package com.shop.app;

import com.shop.model.Owner;
import com.shop.model.Staff;
import com.shop.model.User;
import com.shop.service.InventoryService;
import com.shop.util.FileManager;

import java.util.Scanner;

public class Main {

    private static final Scanner scanner =
        new Scanner(System.in);

    private static final InventoryService service =
        new InventoryService(
            new FileManager()
        );

    public static void main(String[] args) {

        while (true) {

            System.out.println(
                "\n=== SMALL SHOP STOCK MANAGEMENT ==="
            );

            System.out.println("1. Owner menu");
            System.out.println("2. Staff menu");
            System.out.println("0. Exit");

            int choice =
                inputInt("Choose: ");

            if (choice == 1) {

                showOwnerMenu();

            } else if (choice == 2) {

                showStaffMenu();

            } else if (choice == 0) {

                System.out.println(
                    "Goodbye!"
                );

                break;

            } else {

                System.out.println(
                    "Invalid choice."
                );
            }
        }
    }

    private static void showOwnerMenu() {

        User user = new Owner();

        user.showProfile();

        while (true) {

            System.out.println(
                "\n--- OWNER MENU ---"
            );

            System.out.println("1. Add product");
            System.out.println(
                "2. View products and stock"
            );
            System.out.println("3. Add stock");
            System.out.println("4. Remove stock");
            System.out.println(
                "5. View low stock and reports"
            );
            System.out.println("0. Back");

            int choice =
                inputInt("Choose: ");

            switch (choice) {

                case 1:
                    addProduct();
                    break;

                case 2:
                    service.showProducts();
                    break;

                case 3:
                    addStock();
                    break;

                case 4:
                    removeStock();
                    break;

                case 5:
                    showReports();
                    break;

                case 0:
                    return;

                default:
                    System.out.println(
                        "Invalid choice."
                    );
            }
        }
    }

    private static void showStaffMenu() {

        User user = new Staff();

        user.showProfile();

        while (true) {

            System.out.println(
                "\n--- STAFF MENU ---"
            );

            System.out.println(
                "1. View products and stock"
            );
            System.out.println("2. Add stock");
            System.out.println("3. Remove stock");
            System.out.println("0. Back");

            int choice =
                inputInt("Choose: ");

            switch (choice) {

                case 1:
                    service.showProducts();
                    break;

                case 2:
                    addStock();
                    break;

                case 3:
                    removeStock();
                    break;

                case 0:
                    return;

                default:
                    System.out.println(
                        "Invalid choice."
                    );
            }
        }
    }

    private static void addProduct() {

        System.out.println(
            "\n--- ADD PRODUCT ---"
        );

        String code =
            inputText("Code: ");

        String name =
            inputText("Name: ");

        int categoryChoice;

        while (true) {

            System.out.println("1. Stationery");
            System.out.println("2. Grocery");
            System.out.println("3. Household");
            System.out.println("4. Other");

            categoryChoice =
                inputInt("Category: ");

            if (categoryChoice >= 1
                    && categoryChoice <= 4) {

                break;
            }

            System.out.println(
                "Choose a category from 1 to 4."
            );
        }

        int openingStock =
            inputRange(
                "Opening stock (0-1000000): ",
                0,
                1000000
            );

        int lowStockLimit =
            inputRange(
                "Low-stock limit (0-1000000): ",
                0,
                1000000
            );

        boolean saved =
            service.addProduct(
                code,
                name,
                categoryChoice,
                openingStock,
                lowStockLimit
            );

        if (saved) {

            System.out.println(
                "Product saved successfully."
            );

        } else {

            System.out.println(
                "Could not save product."
                + " Check duplicate code or input."
            );
        }
    }

    private static void addStock() {

        int productId =
            inputInt("Product ID: ");

        int quantity =
            inputRange(
                "Quantity (1-1000000): ",
                1,
                1000000
            );

        if (service.addStock(
                productId,
                quantity)) {

            System.out.println(
                "Stock added. Current stock: "
                + service.getCurrentStock(productId)
            );

        } else {

            System.out.println(
                "Could not add stock."
                + " Check product ID and quantity."
            );
        }
    }

    private static void removeStock() {

        int productId =
            inputInt("Product ID: ");

        int quantity =
            inputRange(
                "Quantity (1-1000000): ",
                1,
                1000000
            );

        long currentStock =
            service.getCurrentStock(productId);

        if (currentStock < 0) {

            System.out.println(
                "Unknown product ID."
            );

            return;
        }

        if (quantity > currentStock) {

            System.out.println(
                "Not enough stock. Change refused."
            );

            return;
        }

        if (service.removeStock(
                productId,
                quantity)) {

            System.out.println(
                "Stock removed. Current stock: "
                + service.getCurrentStock(productId)
            );

        } else {

            System.out.println(
                "Could not save stock movement."
            );
        }
    }

    private static void showReports() {

        System.out.println(
            "\n--- LOW STOCK ---"
        );

        service.showLowStock();

        System.out.println(
            "\n--- REPORT ---"
        );

        service.showReport();

        System.out.print(
            "Save report to report.txt? (Y/N): "
        );

        String answer =
            scanner.nextLine().trim();

        if (answer.equalsIgnoreCase("Y")) {

            if (service.saveReport()) {

                System.out.println(
                    "Report saved successfully."
                );

            } else {

                System.out.println(
                    "Could not save report."
                );
            }
        }
    }

    private static int inputInt(
            String message) {

        while (true) {

            System.out.print(message);

            try {

                return Integer.parseInt(
                    scanner.nextLine().trim()
                );

            } catch (Exception e) {

                System.out.println(
                    "Please enter a valid number."
                );
            }
        }
    }

    private static int inputRange(
            String message,
            int minimum,
            int maximum) {

        while (true) {

            int number =
                inputInt(message);

            if (number >= minimum
                    && number <= maximum) {

                return number;
            }

            System.out.println(
                "Enter a value from "
                + minimum
                + " to "
                + maximum
                + "."
            );
        }
    }

    private static String inputText(
            String message) {

        while (true) {

            System.out.print(message);

            String value =
                scanner.nextLine().trim();

            if (!value.isBlank()
                    && !value.contains("|")
                    && !value.contains("\n")
                    && !value.contains("\r")) {

                return value;
            }

            System.out.println(
                "Value cannot be blank or contain |."
            );
        }
    }
}