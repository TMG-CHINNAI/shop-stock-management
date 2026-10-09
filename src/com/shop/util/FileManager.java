package com.shop.util;

import com.shop.model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FileManager {

    private final Path dataDirectory =
        Paths.get("data");

    private final Path productsFile =
        dataDirectory.resolve("products.txt");

    private final Path movementsFile =
        dataDirectory.resolve("movements.txt");

    private final Path reportFile =
        dataDirectory.resolve("report.txt");

    public FileManager() {

        try {

            Files.createDirectories(dataDirectory);

        } catch (IOException e) {

            throw new RuntimeException(
                "Cannot create data folder: "
                + e.getMessage()
            );
        }
    }

    public List<Product> loadProducts() {

        List<Product> products = new ArrayList<>();

        if (!Files.exists(productsFile)) {
            return products;
        }

        Set<Integer> ids = new HashSet<>();
        Set<String> codes = new HashSet<>();

        readLines(
            productsFile,
            (line, lineNumber) -> {

                String[] fields =
                    line.split("\\|", -1);

                if (fields.length != 6) {

                    throw invalid(
                        lineNumber,
                        "products.txt",
                        "Expected 6 fields"
                    );
                }

                int id = parseInteger(
                    fields[0],
                    lineNumber,
                    "products.txt",
                    "id"
                );

                int openingStock = parseInteger(
                    fields[4],
                    lineNumber,
                    "products.txt",
                    "opening stock"
                );

                int lowStockLimit = parseInteger(
                    fields[5],
                    lineNumber,
                    "products.txt",
                    "low-stock limit"
                );

                String code =
                    fields[1]
                        .trim()
                        .toUpperCase(Locale.ROOT);

                String name =
                    fields[2].trim();

                String category =
                    fields[3].trim();

                if (id <= 0
                        || openingStock < 0
                        || lowStockLimit < 0
                        || openingStock > 1000000
                        || lowStockLimit > 1000000
                        || code.isBlank()
                        || name.isBlank()
                        || category.isBlank()
                        || !isValidCategory(category)
                        || !ids.add(id)
                        || !codes.add(code)) {

                    throw invalid(
                        lineNumber,
                        "products.txt",
                        "Invalid or duplicate product record"
                    );
                }

                products.add(
                    new Product(
                        id,
                        code,
                        name,
                        category,
                        openingStock,
                        lowStockLimit
                    )
                );
            }
        );

        return products;
    }

    public List<StockMovement> loadMovements(
            List<Product> products) {

        List<StockMovement> movements =
            new ArrayList<>();

        if (!Files.exists(movementsFile)) {
            return movements;
        }

        Set<Integer> ids =
            new HashSet<>();

        Map<Integer, Long> balances =
            new HashMap<>();

        for (Product product : products) {

            balances.put(
                product.getId(),
                (long) product.getOpeningStock()
            );
        }

        readLines(
            movementsFile,
            (line, lineNumber) -> {

                String[] fields =
                    line.split("\\|", -1);

                if (fields.length != 4) {

                    throw invalid(
                        lineNumber,
                        "movements.txt",
                        "Expected 4 fields"
                    );
                }

                int id = parseInteger(
                    fields[0],
                    lineNumber,
                    "movements.txt",
                    "id"
                );

                int productId = parseInteger(
                    fields[1],
                    lineNumber,
                    "movements.txt",
                    "product ID"
                );

                int quantity = parseInteger(
                    fields[3],
                    lineNumber,
                    "movements.txt",
                    "quantity"
                );

                MovementType type;

                try {

                    type =
                        MovementType.valueOf(
                            fields[2]
                                .trim()
                                .toUpperCase(Locale.ROOT)
                        );

                } catch (Exception e) {

                    throw invalid(
                        lineNumber,
                        "movements.txt",
                        "Type must be IN or OUT"
                    );
                }

                if (id <= 0
                        || quantity < 1
                        || quantity > 1000000
                        || !ids.add(id)
                        || !balances.containsKey(productId)) {

                    throw invalid(
                        lineNumber,
                        "movements.txt",
                        "Invalid movement or unknown product"
                    );
                }

                long current =
                    balances.get(productId);

                long next;

                if (type == MovementType.IN) {

                    next = current + quantity;

                } else {

                    next = current - quantity;
                }

                if (next < 0) {

                    throw invalid(
                        lineNumber,
                        "movements.txt",
                        "Movement makes stock negative"
                    );
                }

                balances.put(productId, next);

                movements.add(
                    new StockMovement(
                        id,
                        productId,
                        type,
                        quantity
                    )
                );
            }
        );

        return movements;
    }

    public void saveProducts(
            List<Product> products) {

        Path temporaryFile =
            dataDirectory.resolve("products.tmp");

        try {

            try (BufferedWriter writer =
                    Files.newBufferedWriter(
                        temporaryFile,
                        StandardCharsets.UTF_8)) {

                for (Product product : products) {

                    writer.write(
                        product.getId()
                        + "|" + clean(product.getCode())
                        + "|" + clean(product.getName())
                        + "|" + clean(product.getCategory())
                        + "|" + product.getOpeningStock()
                        + "|" + product.getLowStockLimit()
                    );

                    writer.newLine();
                }
            }

            Files.move(
                temporaryFile,
                productsFile,
                StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            deleteTemporaryFile(temporaryFile);

            throw new RuntimeException(
                "Could not save products.txt: "
                + e.getMessage()
            );
        }
    }

    public void saveMovements(
            List<StockMovement> movements) {

        Path temporaryFile =
            dataDirectory.resolve("movements.tmp");

        try {

            try (BufferedWriter writer =
                    Files.newBufferedWriter(
                        temporaryFile,
                        StandardCharsets.UTF_8)) {

                for (StockMovement movement : movements) {

                    writer.write(
                        movement.getId()
                        + "|" + movement.getProductId()
                        + "|" + movement.getType()
                        + "|" + movement.getQuantity()
                    );

                    writer.newLine();
                }
            }

            Files.move(
                temporaryFile,
                movementsFile,
                StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            deleteTemporaryFile(temporaryFile);

            throw new RuntimeException(
                "Could not save movements.txt: "
                + e.getMessage()
            );
        }
    }

    public void saveReport(String text) {

        Path temporaryFile =
            dataDirectory.resolve("report.tmp");

        try {

            Files.writeString(
                temporaryFile,
                text,
                StandardCharsets.UTF_8
            );

            Files.move(
                temporaryFile,
                reportFile,
                StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            deleteTemporaryFile(temporaryFile);

            throw new RuntimeException(
                "Could not save report.txt: "
                + e.getMessage()
            );
        }
    }

    private interface LineReader {

        void accept(
            String line,
            int lineNumber
        );
    }

    private void readLines(
            Path path,
            LineReader reader) {

        try (
            BufferedReader bufferedReader =
                Files.newBufferedReader(
                    path,
                    StandardCharsets.UTF_8
                )
        ) {

            String line;
            int lineNumber = 0;

            while (
                (line = bufferedReader.readLine())
                    != null
            ) {

                lineNumber++;

                reader.accept(
                    line,
                    lineNumber
                );
            }

        } catch (IOException e) {

            throw new RuntimeException(
                "File error: "
                + path.getFileName()
                + " - "
                + e.getMessage()
            );
        }
    }

    private int parseInteger(
            String value,
            int lineNumber,
            String fileName,
            String fieldName) {

        try {

            return Integer.parseInt(
                value.trim()
            );

        } catch (Exception e) {

            throw invalid(
                lineNumber,
                fileName,
                "Invalid " + fieldName
            );
        }
    }

    private RuntimeException invalid(
            int lineNumber,
            String fileName,
            String message) {

        return new RuntimeException(
            fileName
            + " line "
            + lineNumber
            + ": "
            + message
        );
    }

    private boolean isValidCategory(
            String category) {

        return Arrays.asList(
            "Stationery",
            "Grocery",
            "Household",
            "Other"
        ).contains(category);
    }

    private String clean(String value) {

        return value
            .replace("|", " ")
            .replace("\n", " ")
            .replace("\r", " ")
            .trim();
    }

    private void deleteTemporaryFile(
            Path path) {

        try {

            Files.deleteIfExists(path);

        } catch (IOException ignored) {
        }
    }
}