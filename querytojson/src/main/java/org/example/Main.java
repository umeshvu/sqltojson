package org.example;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String sql = "INSERT INTO test (one, two, three, four, date_column) VALUES (1, 'text', NULL, 42, TO_DATE('03-SEP-24','DD-MON-RR'))";
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
                "INSERT INTO \\w+ \\(([^)]+)\\) VALUES \\((.+)\\)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            // Extract column names and values
            String[] columns = matcher.group(1).split(",");
            String valuesString = matcher.group(2);

            // Parse values carefully to account for functions with commas
            List<String> values = parseValues(valuesString);

            // Check if the number of columns matches the number of values
            if (columns.length != values.size()) {
                System.out.println("Mismatch between column and value count.");
                return null;
            }

            // Initialize JSON object to store column-value pairs
            JSONObject json = new JSONObject();

            // Populate JSON object with parsed column-value pairs
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i].trim();
                String value = values.get(i).trim();

                // Parse value to appropriate JSON type
                json.put(column, parseValue(value));
            }
            return json;
        }
        return null;
    }

    private static List<String> parseValues(String valuesString) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenthesesDepth = 0;

        for (int i = 0; i < valuesString.length(); i++) {
            char c = valuesString.charAt(i);

            if (c == ',' && parenthesesDepth == 0) {
                // Split values by commas not within parentheses
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                if (c == '(') parenthesesDepth++;
                else if (c == ')') parenthesesDepth--;
                current.append(c);
            }
        }

        // Add the last value
        if (current.length() > 0) {
            values.add(current.toString().trim());
        }

        return values;
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

        // Check for Oracle-style date functions like TO_DATE or TO_TIMESTAMP
        if (value.matches("(?i)TO_DATE\\s*\\(\\s*'[^']+'\\s*,\\s*'[^']+'\\s*\\)")
                || value.matches("(?i)TO_TIMESTAMP\\s*\\(\\s*'[^']+'\\s*,\\s*'[^']+'\\s*\\)")) {
            return value; // Keep as string, or convert to date if needed
        }

        // Remove surrounding single quotes for strings and handle escaped quotes
        if (value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1).replace("''", "'");
        }

        // For anything else, assume it's a string for general cases
        return value;
    }
}
