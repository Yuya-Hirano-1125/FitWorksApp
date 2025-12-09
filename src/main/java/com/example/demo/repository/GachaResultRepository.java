package com.example.demo.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.demo.model.GachaResult;

@Repository
public class GachaResultRepository {

    private final String DB_URL = "jdbc:sqlite:fitworks.db";

    public GachaResultRepository() {
        createTableIfNotExists();
    }

    // ▼ テーブル自動生成
    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS gacha_result (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                item_name TEXT NOT NULL,
                rarity TEXT NOT NULL,
                created_at TEXT NOT NULL
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.execute();
            System.out.println("✔ gacha_result テーブル確認OK");

        } catch (SQLException e) {
            System.err.println("テーブル作成エラー:");
            e.printStackTrace();
        }
    }

    // ▼ 保存
    public void save(GachaResult result) {

        String sql = "INSERT INTO gacha_result (user_id, item_name, rarity, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, result.getUserId());
            pstmt.setString(2, result.getItemName());
            pstmt.setString(3, result.getRarity());
            pstmt.setString(4, result.getCreatedAt());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("保存エラー:");
            e.printStackTrace();
        }
    }

    // ▼ userId で検索
    public List<GachaResult> findByUserId(Long userId) {

        String sql = "SELECT * FROM gacha_result WHERE user_id = ? ORDER BY id DESC";
        List<GachaResult> list = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                GachaResult result = new GachaResult(
                    rs.getLong("user_id"),
                    rs.getString("item_name"),
                    rs.getString("rarity"),
                    rs.getString("created_at")
                );

                result.setId(rs.getLong("id"));
                list.add(result);
            }

        } catch (SQLException e) {
            System.err.println("検索エラー:");
            e.printStackTrace();
        }

        return list;
    }
}
