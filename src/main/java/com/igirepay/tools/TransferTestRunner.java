package com.igirepay.tools;

import com.igirepay.lab2.db.DBConnection;
import com.igirepay.lab3.service.AccountService;
import com.igirepay.lab3.service.TransactionService;
import com.igirepay.lab1.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TransferTestRunner {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM accounts ORDER BY id LIMIT 2");
            ResultSet rs = ps.executeQuery();
            int[] ids = new int[2];
            int i = 0;
            while (rs.next() && i < 2) {
                ids[i++] = rs.getInt("id");
            }

            if (i < 2) {
                System.out.println("Need at least 2 accounts to run transfer test. Found: " + i);
                return;
            }

            int fromId = ids[0];
            int toId = ids[1];
            double amount = 1.0;
            if (args.length > 0) {
                try { amount = Double.parseDouble(args[0]); } catch (Exception ignored) {}
            }

            AccountService accountService = new AccountService();
            TransactionService txService = new TransactionService();

            Account aFrom = accountService.getAccountById(fromId);
            Account aTo = accountService.getAccountById(toId);
            System.out.println("Before transfer: fromId=" + fromId + " bal=" + (aFrom==null?"?":aFrom.getBalance()) +
                    " toId=" + toId + " bal=" + (aTo==null?"?":aTo.getBalance()));

            String ref = txService.generateReferenceId();
            boolean ok = txService.processTransfer(fromId, toId, amount, ref);
            System.out.println("Transfer result: " + ok + " ref=" + ref);

            aFrom = accountService.getAccountById(fromId);
            aTo = accountService.getAccountById(toId);
            System.out.println("After transfer: fromId=" + fromId + " bal=" + (aFrom==null?"?":aFrom.getBalance()) +
                    " toId=" + toId + " bal=" + (aTo==null?"?":aTo.getBalance()));

        } catch (Exception e) {
            System.out.println("Transfer test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
