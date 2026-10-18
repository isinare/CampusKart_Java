package com.campuskart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.campuskart.exception.DatabaseOperationException;
import com.campuskart.model.PurchaseRequest;

// ★ RUBRIC: JDBC CRUD (2M) — purchase requests (feature F5)
//   Asg 20: INSERT ... and the commit/rollback TRANSACTION when a sale is completed
public class RequestDAO {

    // CREATE — a buyer sends an offer (Asg 20: INSERT)
    public void submit(int itemId, int buyerId, double offeredPrice, String message)
            throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                    "INSERT INTO purchase_requests (item_id, buyer_id, offered_price, message, status) "
                  + "VALUES (?, ?, ?, ?, 'PENDING')");
            ps.setInt(1, itemId);
            ps.setInt(2, buyerId);
            ps.setDouble(3, offeredPrice);
            ps.setString(4, message);
            ps.executeUpdate();                  // ★ Asg 20: INSERT — the UNIQUE(item_id, buyer_id)
                                                // constraint stops a second request from the same buyer
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not send the request (one request per item, please): "
                    + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { }    // ★ Asg 14: nested try-catch
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    // READ — requests on MY items (I am the seller)
    public ArrayList<PurchaseRequest> listForSeller(int sellerId) throws DatabaseOperationException {
        return list("i.seller_id", sellerId);    // ★ Asg 3: same private method, reused by both listings
    }

    // READ — requests I sent (I am the buyer)
    public ArrayList<PurchaseRequest> listForBuyer(int buyerId) throws DatabaseOperationException {
        return list("r.buyer_id", buyerId);
    }

    // READ — one request by id (checked for ownership before responding)
    public PurchaseRequest findById(int requestId) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(select() + " WHERE r.request_id = ?");
            ps.setInt(1, requestId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRequest(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load the request: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { }
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    // UPDATE — seller accepts a request, the item becomes RESERVED (Asg 20)
    public void accept(int requestId) throws DatabaseOperationException {
        updateStatus(requestId, "ACCEPTED", "PENDING");
    }

    // UPDATE — seller rejects a request (Asg 20)
    public void reject(int requestId) throws DatabaseOperationException {
        updateStatus(requestId, "REJECTED", "PENDING");
    }

    // ★ RUBRIC: JDBC CRUD (2M) — TRANSACTION | Asg 20: setAutoCommit(false) + commit + rollback
    // Completing a sale is THREE changes that must succeed or fail together:
    //   1) this request becomes COMPLETED,  2) the item becomes SOLD,
    //   3) every other PENDING request on the item is auto-rejected.
    public void complete(int requestId, int itemId) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement requestPs = null;
        PreparedStatement itemPs = null;
        PreparedStatement othersPs = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);           // ★ Asg 20: the transaction starts here (no more auto-commit)

            requestPs = conn.prepareStatement(
                    "UPDATE purchase_requests SET status = 'COMPLETED', completed_at = NOW() "
                  + "WHERE request_id = ? AND status = 'ACCEPTED'");
            requestPs.setInt(1, requestId);
            int updated = requestPs.executeUpdate();

            itemPs = conn.prepareStatement("UPDATE items SET status = 'SOLD' WHERE item_id = ?");
            itemPs.setInt(1, itemId);
            itemPs.executeUpdate();

            othersPs = conn.prepareStatement(
                    "UPDATE purchase_requests SET status = 'REJECTED' WHERE item_id = ? AND status = 'PENDING'");
            othersPs.setInt(1, itemId);
            othersPs.executeUpdate();

            if (updated == 0) {
                throw new SQLException("only an ACCEPTED request can be completed");
            }
            conn.commit();                       // ★ Asg 20: all three changes are saved as one unit
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();             // ★ Asg 20: nothing is saved if any step failed
                }
            } catch (SQLException ignore) {
            }                                    // ★ Asg 14: NESTED try-catch inside a catch block
            throw new DatabaseOperationException("Could not complete the sale: " + e.getMessage(), e);
        } finally {                              // ★ Asg 15: resources closed deterministically
            try { if (othersPs != null) othersPs.close(); } catch (SQLException e) { }   // ★ Asg 14: nested
            try { if (itemPs != null) itemPs.close(); } catch (SQLException e) { }
            try { if (requestPs != null) requestPs.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    // ---- private helpers ----

    private ArrayList<PurchaseRequest> list(String column, int userId) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<PurchaseRequest> list = new ArrayList<PurchaseRequest>();   // ★ Asg 5: ArrayList
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(select() + " WHERE " + column + " = ? ORDER BY r.request_id DESC");
            ps.setInt(1, userId);
            rs = ps.executeQuery();              // ★ Asg 19: SELECT
            while (rs.next()) {                  // ★ Asg 23: cursor navigation
                list.add(mapRequest(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load requests: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { }
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
        return list;
    }

    private void updateStatus(int requestId, String newStatus, String requiredStatus)
            throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                    "UPDATE purchase_requests SET status = ? WHERE request_id = ? AND status = ?");
            ps.setString(1, newStatus);
            ps.setInt(2, requestId);
            ps.setString(3, requiredStatus);
            ps.executeUpdate();                  // ★ Asg 20: UPDATE — the status check keeps transitions legal
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not update the request: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    private String select() {
        // the item title is joined in so the requests page needs no second query
        return "SELECT r.request_id, r.item_id, r.buyer_id, i.seller_id, i.title, r.offered_price, r.status "
             + "FROM purchase_requests r JOIN items i ON r.item_id = i.item_id";
    }

    private PurchaseRequest mapRequest(ResultSet rs) throws SQLException {
        return new PurchaseRequest(rs.getInt("request_id"), rs.getInt("item_id"), rs.getInt("buyer_id"),
                rs.getInt("seller_id"), rs.getString("title"), rs.getDouble("offered_price"),
                rs.getString("status"));
    }
}
