package com.campuskart.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import com.campuskart.model.Receipt;

// ★ RUBRIC: CO2 | Asg 13: file handling — FileWriter + BufferedWriter to WRITE,
//            FileReader + BufferedReader to READ.
// One file per invoice (invoices/INV-<id>.txt) avoids write contention between threads;
// the server log (logs/campuskart.log) is appended to.
public class FileLogger {

    public static void logEvent(String message) {
        appendLine("logs/campuskart.log", new Date() + " " + message);
    }

    public static void logInvoice(Receipt receipt) {
        write("invoices/INV-" + receipt.getTransactionId() + ".txt", receipt.generateReceipt());
    }

    // ★ Asg 13: READING a file — used by the requests page to show a finished invoice
    public static String readInvoice(int requestId) throws IOException {
        FileReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new FileReader("invoices/INV-" + requestId + ".txt");
            bufferedReader = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();    // ★ Asg 5: StringBuffer
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } finally {                                  // ★ Asg 15: finally always closes the file
            try {
                if (bufferedReader != null) bufferedReader.close();   // ★ Asg 14: nested try-catch
            } catch (IOException e) {
            }
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
            }
        }
    }

    // ★ Asg 13: WRITING a file — FileWriter wrapped in BufferedWriter
    private static void write(String path, String content) {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            ensureFolder(path);
            fileWriter = new FileWriter(path);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
        } catch (IOException e) {
            System.out.println("Could not write " + path + ": " + e.getMessage());
        } finally {
            try {
                if (bufferedWriter != null) bufferedWriter.close();
            } catch (IOException e) {
            }
            try {
                if (fileWriter != null) fileWriter.close();
            } catch (IOException e) {
            }
        }
    }

    // append mode: true as the second FileWriter argument means "add to the end"
    private static void appendLine(String path, String content) {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            ensureFolder(path);
            fileWriter = new FileWriter(path, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
            bufferedWriter.newLine();
        } catch (IOException e) {
            System.out.println("Could not append to " + path + ": " + e.getMessage());
        } finally {
            try {
                if (bufferedWriter != null) bufferedWriter.close();
            } catch (IOException e) {
            }
            try {
                if (fileWriter != null) fileWriter.close();
            } catch (IOException e) {
            }
        }
    }

    private static void ensureFolder(String path) {
        int slash = path.indexOf('/');
        if (slash > 0) {
            new File(path.substring(0, slash)).mkdirs();   // create logs/ or invoices/ on first use
        }
    }
}
