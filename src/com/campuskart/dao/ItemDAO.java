package com.campuskart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.campuskart.exception.DatabaseOperationException;
import com.campuskart.model.Item;

// ★ RUBRIC: JDBC CRUD (2M) — CREATE, READ and DELETE for listings (feature F3 / F4)
public class ItemDAO {

    // CREATE — a student posts a listing (Asg 20: INSERT)
    public void create(Item item) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                    "INSERT INTO items (seller_id, category, title, description, item_condition, price, status) "
                  + "VALUES (?, ?, ?, ?, ?, ?, 'AVAILABLE')");
            ps.setInt(1, item.getSellerId());
            ps.setString(2, item.getCategory());
            ps.setString(3, item.getTitle());
            ps.setString(4, item.getDescription());
            ps.setString(5, item.getCondition());
            ps.setDouble(6, item.getPrice());
            ps.executeUpdate();                  // ★ Asg 20: INSERT
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not post the item: " + e.getMessage(), e);
        } finally {                              // ★ Asg 15: finally
            try { if (ps != null) ps.close(); } catch (SQLException e) { }    // ★ Asg 14: nested try-catch
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    // READ — browse with keyword + category filter (Asg 19: SELECT)
    public ArrayList<Item> search(String keyword, String category) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        // ★ Asg 5: StringBuffer grows the SQL as filters are added — values are still bound, never pasted in
        StringBuffer sql = new StringBuffer(
                "SELECT item_id, seller_id, category, title, description, item_condition, price, status "
              + "FROM items WHERE status = 'AVAILABLE'");
        ArrayList<Object> params = new ArrayList<Object>();   // ★ Asg 5: parallel list of bound values
        if (keyword != null && keyword.trim().length() > 0) {
            sql.append(" AND (title LIKE ? OR description LIKE ?)");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }
        if (category != null && category.length() > 0 && !"All".equals(category)) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        ArrayList<Item> list = new ArrayList<Item>();         // ★ Asg 5: ArrayList of results
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));           // ★ Asg 22: every value is a bound parameter
            }
            rs = ps.executeQuery();                           // ★ Asg 19: SELECT
            while (rs.next()) {                               // ★ Asg 23: ResultSet navigation
                list.add(mapItem(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load items: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { }
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
        return list;
    }

    // READ — the seller's own listings
    public ArrayList<Item> listBySeller(int sellerId) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<Item> list = new ArrayList<Item>();         // ★ Asg 5: ArrayList
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                    "SELECT item_id, seller_id, category, title, description, item_condition, price, status "
                  + "FROM items WHERE seller_id = ? ORDER BY item_id DESC");
            ps.setInt(1, sellerId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapItem(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load your listings: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { }
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
        return list;
    }

    // READ — one item by id (used for ownership and availability checks)
    public Item findById(int itemId) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                    "SELECT item_id, seller_id, category, title, description, item_condition, price, status "
                  + "FROM items WHERE item_id = ?");
            ps.setInt(1, itemId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapItem(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load the item: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { }
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    // DELETE — seller removes an own listing (Asg 20: DELETE)
    public void delete(int itemId, int sellerId) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("DELETE FROM items WHERE item_id = ? AND seller_id = ?");
            ps.setInt(1, itemId);
            ps.setInt(2, sellerId);
            ps.executeUpdate();                  // ★ Asg 20: DELETE — the WHERE keeps it owner-only;
                                                // an item with request history hits the foreign key
                                                // and the SQLException becomes a friendly message
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Could not delete (an item with request history cannot be removed): " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    // UPDATE — item status changes: AVAILABLE -> RESERVED -> SOLD
    public void updateStatus(int itemId, String status) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE items SET status = ? WHERE item_id = ?");
            ps.setString(1, status);
            ps.setInt(2, itemId);
            ps.executeUpdate();                  // ★ Asg 20: UPDATE
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not update the item status: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    private Item mapItem(ResultSet rs) throws SQLException {
        return new Item(rs.getInt("item_id"), rs.getInt("seller_id"), rs.getString("category"),
                rs.getString("title"), rs.getString("description"), rs.getString("item_condition"),
                rs.getDouble("price"), rs.getString("status"));
    }
}
