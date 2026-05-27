package com.igirepay.tools;

import com.igirepay.lab3.service.TransactionService;

public class ExportRunner {
    public static void main(String[] args) {
        TransactionService ts = new TransactionService();
        ts.exportToCSV(4, "transactions_account_4.csv");
        System.out.println("ExportRunner finished.");
    }
}
