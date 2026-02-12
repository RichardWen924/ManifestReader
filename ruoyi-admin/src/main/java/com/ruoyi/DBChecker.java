package com.ruoyi;

import java.sql.*;

public class DBChecker {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/ruoyi?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8";
        try (Connection conn = DriverManager.getConnection(url, "root", "123456")) {
            System.out.println("--- Table: sys_company_user ---");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt
                            .executeQuery("SELECT user_id, company_name, company_code FROM sys_company_user")) {
                while (rs.next()) {
                    System.out.printf("ID: %d, Name: %s, Code: %s%n",
                            rs.getLong("user_id"), rs.getString("company_name"), rs.getString("company_code"));
                }
            }

            System.out.println("\n--- Table: bill_of_lading_v5 ---");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT id, bl_no, create_by FROM bill_of_lading_v5")) {
                while (rs.next()) {
                    System.out.printf("ID: %d, BL_NO: %s, CreateBy: %s%n",
                            rs.getLong("id"), rs.getString("bl_no"), rs.getString("create_by"));
                }
            }

            System.out.println("\n--- Count by CreateBy ---");
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(
                            "SELECT create_by, COUNT(*) as count FROM bill_of_lading_v5 GROUP BY create_by")) {
                while (rs.next()) {
                    System.out.printf("CreateBy: %s, Count: %d%n", rs.getString("create_by"), rs.getInt("count"));
                }
            }
        }
    }
}
