import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class BankTransactionAnalyzerTest {

    @Test
    void categorize_basicAndCaseInsensitive() {
        assertEquals("Groceries", BankTransactionAnalyzer.categorize("Whole Foods Market"));
        assertEquals("Entertainment", BankTransactionAnalyzer.categorize("Netflix.com"));
        assertEquals("Other", BankTransactionAnalyzer.categorize("Unknown Merchant"));
    }

    @Test
    void totalsByCategory_sumsCorrectly() {
        var t1 = new BankTransactionAnalyzer.Transaction(LocalDate.now(), "Whole Foods", -50);
        t1.category = "Groceries";
        var t2 = new BankTransactionAnalyzer.Transaction(LocalDate.now(), "Uber Trip", -20);
        t2.category = "Transport";
        var totals = BankTransactionAnalyzer.totalsByCategory(List.of(t1, t2));
        assertEquals(-50.0, totals.get("Groceries"));
        assertEquals(-20.0, totals.get("Transport"));
    }

    @Test
    void readCsv_parsesRowsAndSkipsHeader() throws IOException {
        String csv = "date,description,amount\n2025-08-01,Whole Foods Market,-54.23\n2025-08-02,Uber Trip,-12.50\n";
        Path p = Files.createTempFile("txns",".csv");
        Files.writeString(p, csv);
        var txns = BankTransactionAnalyzer.readCsv(p.toString());
        assertEquals(2, txns.size());
        assertEquals("Whole Foods Market", txns.get(0).description);
        assertEquals(-12.50, txns.get(1).amount);
    }

    @Test
    void writeCategoryMonthReport_writesAggregatedCsv(@TempDir Path tmp) throws IOException {
        var t1 = new BankTransactionAnalyzer.Transaction(LocalDate.of(2025,8,1),"Whole Foods",-50); t1.category="Groceries";
        var t2 = new BankTransactionAnalyzer.Transaction(LocalDate.of(2025,8,2),"Uber",-20); t2.category="Transport";
        var t3 = new BankTransactionAnalyzer.Transaction(LocalDate.of(2025,8,15),"Whole Foods",-10); t3.category="Groceries";

        Path out = tmp.resolve("report.csv");
        BankTransactionAnalyzer.writeCategoryMonthReport(List.of(t1,t2,t3), out.toString());
        String report = Files.readString(out);
        assertTrue(report.startsWith("month,category,amount"));
        assertTrue(report.contains("2025-08,Groceries,-60.00"));
        assertTrue(report.contains("2025-08,Transport,-20.00"));
    }
}
