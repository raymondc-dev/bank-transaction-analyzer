import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * A command-line Java program that analyzes a CSV file of bank transactions.
 * 
 * The program:
 *  - Reads transaction data (date, description, amount) from a CSV file
 *  - Categorizes transactions based on keyword rules
 *  - Calculates total spending or income per category
 *  - Generates a category by month summary CSV report
 */
public class BankTransactionAnalyzer {

    /**
    Represents a single transaction record.
    */
    static class Transaction {
        LocalDate date;
        String description;
        double amount;
        String category = "Uncategorized";

        Transaction(LocalDate date, String description, double amount) {
            this.date = date;
            this.description = description;
            this.amount = amount;
        }
    }

    /**
    Keyword rules used to assign transaction categories.
    */
    static final LinkedHashMap<String, String> KEYWORD_RULES = new LinkedHashMap<>();
    static {
        KEYWORD_RULES.put("whole foods", "Groceries");
        KEYWORD_RULES.put("walmart", "Groceries");
        KEYWORD_RULES.put("costco", "Groceries");
        KEYWORD_RULES.put("uber", "Transport");
        KEYWORD_RULES.put("lyft", "Transport");
        KEYWORD_RULES.put("netflix", "Entertainment");
        KEYWORD_RULES.put("spotify", "Entertainment");
        KEYWORD_RULES.put("shell", "Gas");
        KEYWORD_RULES.put("exxon", "Gas");
        KEYWORD_RULES.put("amazon", "Shopping");
        KEYWORD_RULES.put("starbucks", "Dining");
        KEYWORD_RULES.put("rent", "Rent");
        KEYWORD_RULES.put("payroll", "Income");
    }

    /**
     * Main entry point for the Bank Transaction Analyzer.
     *
     * @param args expects one argument: the path to a CSV file
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -cp src BankTransactionAnalyzer <path-to-csv>");
            System.exit(1);
        }
        String csvPath = args[0];

        List<Transaction> txns = readCsv(csvPath);
        categorizeAll(txns);

        Map<String, Double> totals = totalsByCategory(txns);
        System.out.println("=== Spending / Income by Category ===");
        totals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("%-14s : %10.2f%n", e.getKey(), e.getValue()));

        writeCategoryMonthReport(txns, "category_month_report.csv");
        System.out.println("Wrote category-by-month report to category_month_report.csv");
    }

    /**
    Reads transactions from a CSV file.
    */
    static List<Transaction> readCsv(String path) {
        List<Transaction> list = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            String header = br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 3) continue;
                String dateStr = parts[0].trim();
                String desc = parts[1].trim();
                String amtStr = parts[2].trim();
                if (dateStr.isEmpty() || desc.isEmpty() || amtStr.isEmpty()) continue;

                LocalDate date = LocalDate.parse(dateStr, dtf);
                double amt = Double.parseDouble(amtStr);
                list.add(new Transaction(date, desc, amt));
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
        }
        return list;
    }

    /**
    Categorizes all transactions.
    */
    static void categorizeAll(List<Transaction> txns) {
        for (Transaction t : txns) {
            t.category = categorize(t.description);
        }
    }

    /**
    Determines category based on keyword rules.
    */
    static String categorize(String description) {
        String d = description.toLowerCase();
        for (Map.Entry<String, String> rule : KEYWORD_RULES.entrySet()) {
            if (d.contains(rule.getKey())) return rule.getValue();
        }
        return "Other";
    }

    /**
    Aggregates totals by category.
    */
    static Map<String, Double> totalsByCategory(List<Transaction> txns) {
        Map<String, Double> totals = new TreeMap<>();
        for (Transaction t : txns) {
            String cat = (t.category == null) ? "Other" : t.category;
            totals.put(cat, totals.getOrDefault(cat, 0.0) + t.amount);
        }
        return totals;
    }

    /**
    Writes a month-by-category CSV report.
    */
    static void writeCategoryMonthReport(List<Transaction> txns, String outPath) {
        Map<String, Double> sums = new TreeMap<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        for (Transaction t : txns) {
            String key = t.date.format(monthFmt) + "," + t.category;
            sums.put(key, sums.getOrDefault(key, 0.0) + t.amount);
        }
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Paths.get(outPath)))) {
            pw.println("month,category,amount");
            for (Map.Entry<String, Double> e : sums.entrySet()) {
                String[] parts = e.getKey().split(",", 2);
                pw.printf("%s,%s,%.2f%n", parts[0], parts[1], e.getValue());
            }
        } catch (IOException e) {
            System.err.println("Error writing report: " + e.getMessage());
        }
    }
}
