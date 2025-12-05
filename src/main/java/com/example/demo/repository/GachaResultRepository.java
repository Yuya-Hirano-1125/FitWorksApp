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

    public void save(GachaResult result) {

        String sql = "INSERT INTO gacha_result (user_id, item_name, rarity, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, result.getUserId());
            pstmt.setString(2, result.getItemName());
            pstmt.setString(3, result.getRarity());
            pstmt.setString(4, result.getCreatedAt());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<GachaResult> findByUserId(int userId) {

        String sql = "SELECT * FROM gacha_result WHERE user_id = ? ORDER BY id DESC";
        List<GachaResult> list = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                GachaResult result = new GachaResult(
                    rs.getInt("user_id"),
                    rs.getString("item_name"),
                    rs.getString("rarity"),
                    rs.getString("created_at")
                );
                result.setId(rs.getInt("id"));

                list.add(result);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
