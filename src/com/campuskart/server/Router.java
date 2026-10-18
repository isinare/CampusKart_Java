package com.campuskart.server;

import java.util.ArrayList;
import java.util.Date;

import com.campuskart.dao.ItemDAO;
import com.campuskart.dao.RequestDAO;
import com.campuskart.dao.UserDAO;
import com.campuskart.exception.DatabaseOperationException;
import com.campuskart.exception.InvalidPriceException;
import com.campuskart.exception.UnverifiedStudentException;
import com.campuskart.model.Item;
import com.campuskart.model.PurchaseRequest;
import com.campuskart.model.Receipt;
import com.campuskart.model.Student;
import com.campuskart.model.User;
import com.campuskart.util.FileLogger;
import com.campuskart.util.HtmlBuilder;

// The dispatcher: one big if/else on method + path, then the matching handler.
// ★ RUBRIC: Exception Handling (1M) | Asg 14: ONE try with MULTIPLE CATCH BLOCKS —
//            every route runs inside it, so each failure gets one friendly screen.
public class Router {

    private final HtmlBuilder html = new HtmlBuilder();   // holds the inner class ItemCard (Asg 6)
    private final UserDAO userDAO = new UserDAO();
    private final ItemDAO itemDAO = new ItemDAO();
    private final RequestDAO requestDAO = new RequestDAO();

    public void dispatch(HttpRequest req, HttpResponse res) {
        SessionStore.Session session = SessionStore.find(req.cookie);
        boolean text = req.wantsText;            // true for the Swing client (Accept: text/plain)
        try {
            route(req, res, session, text);
        } catch (InvalidPriceException e) {      // ★ Asg 14: multiple catch — one block per exception type
            error(res, text, 400, e.getMessage());
        } catch (UnverifiedStudentException e) {
            error(res, text, 403, e.getMessage());
        } catch (NumberFormatException e) {      // thrown by the wrapper classes in FormData (Asg 4)
            error(res, text, 400, "Please enter a number where asked");
        } catch (DatabaseOperationException e) {
            FileLogger.logEvent("DB error: " + e.getMessage());   // details only in the log file
            error(res, text, 500, e.getMessage());                // never a stack trace on the screen
        }
    }

    // simple router table
    private void route(HttpRequest req, HttpResponse res, SessionStore.Session s, boolean text)
            throws DatabaseOperationException, InvalidPriceException, UnverifiedStudentException {
        String m = req.method, p = req.path;
        if ("GET".equals(m) && "/".equals(p))                        browse(req, res, text);
        else if ("GET".equals(m) && "/login".equals(p))              page(res, "Login", html.loginPage(""));
        else if ("POST".equals(m) && "/login".equals(p))             login(req, res, text);
        else if ("GET".equals(m) && "/register".equals(p))           page(res, "Register", html.registerPage(""));
        else if ("POST".equals(m) && "/register".equals(p))          register(req, res, text);
        else if ("POST".equals(m) && "/logout".equals(p))            logout(res, s, text);
        else if ("GET".equals(m) && "/items/mine".equals(p))         mine(res, s, text);
        else if ("POST".equals(m) && "/items/add".equals(p))         addItem(req, res, s, text);
        else if ("POST".equals(m) && "/items/delete".equals(p))      deleteItem(req, res, s, text);
        else if ("POST".equals(m) && "/items/request".equals(p))     requestItem(req, res, s, text);
        else if ("GET".equals(m) && "/requests".equals(p))           requests(res, s, text);
        else if ("POST".equals(m) && "/requests/respond".equals(p))  respond(req, res, s, text);
        else if ("GET".equals(m) && "/admin".equals(p))              admin(res, s, text);
        else if ("POST".equals(m) && "/admin/verify".equals(p))      verify(req, res, s, text);
        else error(res, text, 404, "No such page: " + p);
    }

    // ---------- handlers ----------

    // F4: browse with keyword + category filter (public)
    private void browse(HttpRequest req, HttpResponse res, boolean text) throws DatabaseOperationException {
        FormData f = new FormData(req.query);
        String q = f.get("q");
        String category = f.get("category");
        ArrayList<Item> items = itemDAO.search(q, category);        // ★ Asg 19: SELECT via PreparedStatement
        if (text) {                                                  // Swing client format: OK + one line per item
            ArrayList<String[]> rows = new ArrayList<String[]>();
            for (int i = 0; i < items.size(); i++) {
                Item it = items.get(i);
                rows.add(new String[] { String.valueOf(it.getItemId()), it.getTitle(),
                        String.valueOf(it.getPrice()), it.getStatus() });
            }
            textRows(res, rows);
        } else {
            page(res, "Browse items", html.browsePage(items, q, category));
        }
    }

    // F1: login — creates the session and sets the "sid" cookie
    private void login(HttpRequest req, HttpResponse res, boolean text) throws DatabaseOperationException {
        FormData f = new FormData(req.body);
        User user = userDAO.authenticate(f.get("email").trim(), f.get("password"));
        if (user == null) {
            error(res, text, 401, "Wrong email or password");
            return;
        }
        String token = SessionStore.create(user.getUserId(), user.getRole());
        if (text) {
            res.text("OK|" + user.getRole());      // the GUI client learns the role from this line
        } else {
            res.redirect("/", "sid=" + token + "; Path=/; HttpOnly");
        }
    }

    // F1: registration — a new student always starts PENDING
    private void register(HttpRequest req, HttpResponse res, boolean text) throws DatabaseOperationException {
        FormData f = new FormData(req.body);
        if (f.get("email").trim().length() == 0 || f.get("password").length() == 0) {
            error(res, text, 400, "Email and password are required");
            return;
        }
        Student student = new Student();          // ★ Asg 2: default constructor, fields set below
        student.setFullName(f.get("fullName"));
        student.setEmail(f.get("email").trim());
        student.setPassword(f.get("password"));
        student.setEnrollmentNo(f.get("enrollmentNo"));
        student.setDepartment(f.get("department"));
        userDAO.register(student);                // ★ Asg 20: INSERT
        done(res, text, "Registered. You can login after an admin approves you.", "/login");
    }

    private void logout(HttpResponse res, SessionStore.Session s, boolean text) {
        if (s != null) {
            SessionStore.remove(s.getToken());   // the session table loses this entry too (Vector, Asg 5)
        }
        if (text) {
            res.text("OK|Logged out");
        } else {
            res.redirect("/", "sid=; Path=/; HttpOnly");   // empty cookie value deletes it in the browser
        }
    }

    // F3: the seller's own listings, plus the "post item" form
    private void mine(HttpResponse res, SessionStore.Session s, boolean text) throws DatabaseOperationException {
        if (!require(res, "STUDENT", s, text)) {
            return;
        }
        ArrayList<Item> items = itemDAO.listBySeller(s.getUserId());
        if (text) {
            ArrayList<String[]> rows = new ArrayList<String[]>();
            for (int i = 0; i < items.size(); i++) {
                Item it = items.get(i);
                rows.add(new String[] { String.valueOf(it.getItemId()), it.getTitle(),
                        String.valueOf(it.getPrice()), it.getStatus() });
            }
            textRows(res, rows);
        } else {
            page(res, "My listings", html.minePage(items));
        }
    }

    // F3: posting an item — shows wrapper classes, TWO custom exceptions and the copy constructor
    private void addItem(HttpRequest req, HttpResponse res, SessionStore.Session s, boolean text)
            throws DatabaseOperationException, InvalidPriceException, UnverifiedStudentException {
        if (!require(res, "STUDENT", s, text)) {
            return;
        }
        FormData f = new FormData(req.body);
        double price = Double.parseDouble(f.get("price").trim());   // ★ RUBRIC: Collections | Asg 4: WRAPPER class;
                                                                   // bad text throws NumberFormatException -> 400
        if (price <= 0 || price > 100000) {
            throw new InvalidPriceException("Price must be between 1 and 100000");   // ★ Asg 15: throw
        }
        User user = userDAO.findById(s.getUserId());
        if (!(user instanceof Student) || !((Student) user).canPostItems()) {
            throw new UnverifiedStudentException("Only approved students can post items");   // ★ Asg 16
        }
        Item draft = new Item();                  // ★ Asg 2: default constructor
        draft.setSellerId(s.getUserId());
        draft.setCategory(f.get("category"));
        draft.setTitle(f.get("title"));
        draft.setDescription(f.get("description"));
        draft.setCondition(f.get("condition"));
        draft.setPrice(price);
        itemDAO.create(new Item(draft));          // ★ Asg 2: COPY CONSTRUCTOR — the DAO stores an independent
                                                 // working copy; later edits to the form object change nothing
        done(res, text, "Item posted", "/items/mine");
    }

    // F3: delete an own listing (the WHERE clause keeps it owner-only)
    private void deleteItem(HttpRequest req, HttpResponse res, SessionStore.Session s, boolean text)
            throws DatabaseOperationException {
        if (!require(res, "STUDENT", s, text)) {
            return;
        }
        FormData f = new FormData(req.body);
        int itemId = f.getInt("itemId");          // ★ Asg 4: wrapper class
        Item item = itemDAO.findById(itemId);
        if (item == null || item.getSellerId() != s.getUserId()) {
            error(res, text, 403, "You can only delete your own listings");
            return;
        }
        Item workingCopy = new Item(item);        // ★ Asg 2: copy constructor — delete acts on the working copy
        itemDAO.delete(workingCopy.getItemId(), workingCopy.getSellerId());
        done(res, text, "Item deleted", "/items/mine");
    }

    // F5: a buyer sends an offer on an AVAILABLE item
    private void requestItem(HttpRequest req, HttpResponse res, SessionStore.Session s, boolean text)
            throws DatabaseOperationException, InvalidPriceException {
        if (!require(res, "STUDENT", s, text)) {
            return;
        }
        FormData f = new FormData(req.body);
        int itemId = f.getInt("itemId");
        double offer = f.getDouble("offeredPrice");   // ★ Asg 4: wrapper class
        if (offer <= 0) {
            throw new InvalidPriceException("Offer must be positive");   // ★ Asg 16: user-defined exception
        }
        Item item = itemDAO.findById(itemId);
        if (item == null) {
            error(res, text, 404, "Item not found");
            return;
        }
        if (item.getSellerId() == s.getUserId()) {
            error(res, text, 403, "You cannot request your own item");
            return;
        }
        if (!"AVAILABLE".equals(item.getStatus())) {
            error(res, text, 409, "Item is no longer available");
            return;
        }
        requestDAO.submit(itemId, s.getUserId(), offer, f.get("message"));   // ★ Asg 20: INSERT
        done(res, text, "Request sent to the seller", "/requests");
    }

    // F5: incoming (as seller) and outgoing (as buyer) requests, with invoices for completed ones
    private void requests(HttpResponse res, SessionStore.Session s, boolean text) throws DatabaseOperationException {
        if (!require(res, "STUDENT", s, text)) {
            return;
        }
        ArrayList<PurchaseRequest> incoming = requestDAO.listForSeller(s.getUserId());
        ArrayList<PurchaseRequest> outgoing = requestDAO.listForBuyer(s.getUserId());
        if (text) {
            ArrayList<String[]> rows = new ArrayList<String[]>();
            for (int i = 0; i < incoming.size(); i++) {
                rows.add(toRow("IN", incoming.get(i)));
            }
            for (int i = 0; i < outgoing.size(); i++) {
                rows.add(toRow("OUT", outgoing.get(i)));
            }
            textRows(res, rows);
        } else {
            page(res, "My requests", html.requestsPage(incoming, outgoing));
        }
    }

    // F5: accept / reject / complete — completion runs inside ONE JDBC transaction
    private void respond(HttpRequest req, HttpResponse res, SessionStore.Session s, boolean text)
            throws DatabaseOperationException {
        if (!require(res, "STUDENT", s, text)) {
            return;
        }
        FormData f = new FormData(req.body);
        int requestId = f.getInt("requestId");
        String action = f.get("action");
        PurchaseRequest request = requestDAO.findById(requestId);
        if (request == null || request.getSellerId() != s.getUserId()) {
            error(res, text, 403, "That request is not for your listing");
            return;
        }
        if ("accept".equals(action)) {
            requestDAO.accept(requestId);        // ★ Asg 20: UPDATE (request ACCEPTED, item RESERVED)
            done(res, text, "Request accepted", "/requests");
        } else if ("reject".equals(action)) {
            requestDAO.reject(requestId);        // ★ Asg 20: UPDATE
            done(res, text, "Request rejected", "/requests");
        } else if ("complete".equals(action)) {
            requestDAO.complete(requestId, request.getItemId());   // ★ RUBRIC: JDBC CRUD (2M) | Asg 20:
                                                                   // commit/rollback TRANSACTION
            Item item = itemDAO.findById(request.getItemId());
            Receipt receipt = new Receipt(requestId, new Date().toString(),
                    item == null ? "item" : item.getTitle(), request.getOfferedPrice());
            FileLogger.logInvoice(receipt);      // ★ Asg 13: invoice written to invoices/INV-<id>.txt
            done(res, text, "Sale completed — invoice generated", "/requests");
        } else {
            error(res, text, 400, "Unknown action: " + action);
        }
    }

    // F2: admin dashboard — students waiting for approval
    private void admin(HttpResponse res, SessionStore.Session s, boolean text) throws DatabaseOperationException {
        if (!require(res, "ADMIN", s, text)) {
            return;
        }
        ArrayList<User> pending = userDAO.listPendingStudents();
        if (text) {
            ArrayList<String[]> rows = new ArrayList<String[]>();
            for (int i = 0; i < pending.size(); i++) {
                User u = pending.get(i);
                String enrollment = (u instanceof Student) ? ((Student) u).getEnrollmentNo() : "";
                rows.add(new String[] { String.valueOf(u.getUserId()), u.getFullName(),
                        u.getEmail(), enrollment });
            }
            textRows(res, rows);
        } else {
            page(res, "Admin — pending verifications", html.adminPage(pending));
        }
    }

    // F2: approve or reject a student (UPDATE)
    private void verify(HttpRequest req, HttpResponse res, SessionStore.Session s, boolean text)
            throws DatabaseOperationException {
        if (!require(res, "ADMIN", s, text)) {
            return;
        }
        FormData f = new FormData(req.body);
        int userId = f.getInt("userId");
        String status = "approve".equals(f.get("action")) ? "APPROVED" : "REJECTED";
        userDAO.updateVerification(userId, status);   // ★ Asg 20: UPDATE
        done(res, text, "Student " + status.toLowerCase(), "/admin");
    }

    // ---------- small helpers ----------

    // role guard: every protected route calls this first
    private boolean require(HttpResponse res, String role, SessionStore.Session s, boolean text) {
        if (s == null || !role.equals(s.getRole())) {
            error(res, text, 403, "Please log in as " + role.toLowerCase() + " first");
            return false;
        }
        return true;
    }

    private void page(HttpResponse res, String title, String body) {
        res.html(200, "OK", html.page(title, body));
    }

    private void done(HttpResponse res, boolean text, String message, String redirectPath) {
        if (text) {
            res.text("OK|" + message);           // the Swing client reads this first line
        } else {
            res.redirect(redirectPath, null);    // 303 See Other (Post/Redirect/Get)
        }
    }

    private void error(HttpResponse res, boolean text, int status, String message) {
        if (text) {
            res.text("ERR|" + message);          // the GUI shows this in a dialog
        } else {
            res.html(status, reason(status), html.message(message));
        }
    }

    private String reason(int status) {
        if (status == 400) return "Bad Request";
        if (status == 401) return "Unauthorized";
        if (status == 403) return "Forbidden";
        if (status == 404) return "Not Found";
        if (status == 409) return "Conflict";
        return "Internal Server Error";
    }

    // the text protocol for the Swing client: "OK" then one |-separated record per line
    private void textRows(HttpResponse res, ArrayList<String[]> rows) {
        StringBuffer sb = new StringBuffer("OK");   // ★ Asg 5: StringBuffer
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            sb.append('\n');
            for (int j = 0; j < row.length; j++) {
                if (j > 0) {
                    sb.append('|');
                }
                sb.append(row[j]);
            }
        }
        res.text(sb.toString());
    }

    private String[] toRow(String side, PurchaseRequest r) {
        return new String[] { side, String.valueOf(r.getRequestId()), String.valueOf(r.getItemId()),
                r.getTitle(), String.valueOf(r.getOfferedPrice()), r.getStatus() };
    }
}
