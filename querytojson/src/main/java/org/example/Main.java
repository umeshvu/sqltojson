package org.example;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        String sql = "INSERT INTO test (one, two, three, four, five) VALUES (1, 'text', NULL, 42, 'extra')";
        JSONObject json = convertSqlToJson(sql);
        if (json != null) {
            System.out.println(json.toString(4)); // pretty-print with indentation
        } else {
            System.out.println("Invalid SQL query or mismatch between columns and values.");
        }
    }

    public static JSONObject convertSqlToJson(String sql) {
        // Regular expression pattern to capture column names and values from an INSERT statement
        Pattern pattern = Pattern.compile(
                "INSERT INTO \\w+ \\(([^)]+)\\) VALUES \\(([^)]+)\\)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            // Extract column names and values as comma-separated strings
            String[] columns = matcher.group(1).split(",");
            String[] values = matcher.group(2).split(",");

            // Trim whitespace for columns and values
            for (int i = 0; i < columns.length; i++) {
                columns[i] = columns[i].trim();
            }
            for (int i = 0; i < values.length; i++) {
                values[i] = values[i].trim();
            }

            // Check if the number of columns matches the number of values
            if (columns.length != values.length) {
                System.out.println("Mismatch between column and value count.");
                return null;
            }

            // Initialize JSON object to store column-value pairs
            JSONObject json = new JSONObject();

            // Populate JSON object with parsed column-value pairs
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                String value = values[i];

                // Parse value to appropriate JSON type
                json.put(column, parseValue(value));
            }
            return json;
        }
        return null;
    }

    private static Object parseValue(String value) {
        // Check for NULL values
        if (value.equalsIgnoreCase("NULL")) {
            return JSONObject.NULL;
        }

        // Check for numeric values (integer or floating-point)
        if (value.matches("-?\\d+(\\.\\d+)?")) {
            return Double.parseDouble(value);
        }

        // Remove surrounding single quotes for strings and handle escaped quotes
        if (value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1).replace("''", "'");
        }

        // For anything else, assume it's a string for general cases
        return value;
    }
}