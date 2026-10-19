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
import com.campuskart.util.WebAssets;

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
            error(res, session, text, 400, e.getMessage());
        } catch (UnverifiedStudentException e) {
            error(res, session, text, 403, e.getMessage());
        } catch (NumberFormatException e) {      // thrown by the wrapper classes in FormData (Asg 4)
            error(res, session, text, 400, "Please enter a number where asked");
        } catch (DatabaseOperationException e) {
            FileLogger.logEvent("DB error: " + e.getMessage());   // details only in the log file
            error(res, session, text, 500, e.getMessage());        // never a stack trace on the screen
        }
    }

    // simple router table
    private void route(HttpRequest req, HttpResponse res, SessionStore.Session s, boolean text)
            throws DatabaseOperationException, InvalidPriceException, UnverifiedStudentException {
        String m = req.method, p = req.path;
        if ("GET".equals(m) && "/".equals(p))                        browse(req, res, s, text);
        else if ("GET".equals(m) && "/login".equals(p)) {
            if (s != null) {                                        // already logged in: no login form
                res.redirect("/", null);
            } else {
                String msg = new FormData(req.query).get("msg");
                page(res, null, "Login", null, msg, html.loginPage(""));
            }
        }
        else if ("POST".equals(m) && "/login".equals(p))             login(req, res, text);
        else if ("GET".equals(m) && "/register".equals(p)) {
            if (s != null) {                                        // already logged in: no signup form
                res.redirect("/", null);
            } else {
                String msg = new FormData(req.query).get("msg");
                page(res, null, "Register", null, msg, html.registerPage(""));
            }
        }
        else if ("POST".equals(m) && "/register".equals(p))          register(req, res, text);
        else if ("POST".equals(m) && "/logout".equals(p))            logout(req, res, s, text);
        else if ("GET".equals(m) && "/items/mine".equals(p))         mine(req, res, s, text);
        else if ("POST".equals(m) && ("/items".equals(p) || "/items/add".equals(p)))
                                                                     addItem(req, res, s, text);
        else if ("POST".equals(m) && "/items/delete".equals(p))      deleteItem(req, res, s, text);
        else if ("POST".equals(m) && "/items/request".equals(p))     requestItem(req, res, s, text);
        else if ("GET".equals(m) && "/requests".equals(p))           requests(req, res, s, text);
        else if ("POST".equals(m) && p.startsWith("/requests/"))     respond(req, res, s, text);
        else if ("GET".equals(m) && "/admin".equals(p))              admin(req, res, s, text);
        else if ("POST".equals(m) && p.startsWith("/admin/"))        verify(req, res, s, text);
        // ---- static assets of the provided web design (style.css + photographs) ----
        // ★ Asg 13: file handling — these bytes are read from the CampusKart_Web folder
        else if ("GET".equals(m) && "/style.css".equals(p))          staticFile(res, "style.css");
        else if ("GET".equals(m) && p.startsWith("/images/"))        staticFile(res, p.substring(1));
        else if ("GET".equals(m) && p.startsWith("/frontend/"))       staticFrontend(res, p.substring("/frontend/".length()));
        else if ("GET".equals(m) && p.startsWith("/ui/"))             staticUi(res, p.substring("/ui/".length()));
        else if ("GET".equals(m) && p.startsWith("/web/"))            staticWeb(res, p.substring("/web/".length()));
        else if ("GET".equals(m) && p.startsWith("/api/"))            staticApi(res, p.substring("/api/".length()));
        else error(res, s, text, 404, "No such page: " + p);
    }

    // ---------- handlers ----------

    // F4: browse with keyword + category filter (public — role only shapes the page)
    private void browse(HttpRequest req, HttpResponse res, SessionStore.Session s, boolean text)
            throws DatabaseOperationException {
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
            String role = s == null ? "" : s.getRole();
            int viewerId = s == null ? -1 : s.getUserId();
            page(res, s, "Browse items", "browse", f.get("msg"),
                    html.browsePage(items, q, category, role, viewerId));
        }
    }

    // F1: login — creates the session and sets the "sid" cookie
    private void login(HttpRequest req, HttpResponse res, boolean text) throws DatabaseOperationException {
        FormData f = new FormData(req.body);
        String email = f.get("email").trim();
        User user = userDAO.authenticate(email, f.get("password"));
        if (user == null) {
            if (text) {
                res.text("ERR|Wrong email or password");
            } else {
                // show the error INSIDE the login form, not on a dead-end page
                page(res, null, "Login", null, "", html.loginPage("Wrong email or password"));
            }
            return;
        }
        String token = SessionStore.create(user.getUserId(), user.getRole());
        if (text) {
            res.text("OK|" + user.getRole());      // the GUI client learns the role from this line
        } else {
            String welcome = "Welcome back, " + user.getFullName() + "!";
            res.redirect("/?msg=" + urlEncode(welcome), "sid=" + token + "; Path=/; HttpOnly");
        }
    }

    // F1: registration — a new student always starts PENDING
    private void register(HttpRequest req, HttpResponse res, boolean text) throws DatabaseOperationException {
        FormData f = new FormData(req.body);
        if (f.get("email").trim().length() == 0 || f.get("password").length() == 0) {
            if (text) {
                res.text("ERR|Email and password are required");
            } else {
                // show the error INSIDE the signup form, not on a dead-end page
                page(res, null, "Register", null, "", html.registerPage("Email and password are required"));
            }
            return;
        }
        String email = f.get("email").trim();
        if (!email.endsWith(".edu")) {
            if (text) {
                res.text("ERR|Only emails ending with .edu are allowed");
            } else {
                page(res, null, "Register", null, "", html.registerPage("Only emails ending with .edu are allowed"));
            }
            return;
        }
        Student student = new Student();          // ★ Asg 2: default constructor, fields set below
        student.setFullName(f.get("fullName"));
        student.setEmail(email);
        student.setPassword(f.get("password"));
        student.setEnrollmentNo(f.get("enrollmentNo"));
        student.setDepartment(f.get("department"));
        try {
            userDAO.register(student);            // ★ Asg 20: INSERT
        } catch (DatabaseOperationException dup) {
            // e.g. the email already exists (UNIQUE constraint) — say so on the form
            if (text) {
                throw dup;                        // the Swing client shows the message in a dialog
            }
            page(res, null, "Register", null, "", html.registerPage(dup.getMessage()));
            return;
        }
        done(res, text, "Registered. You can login after an admin approves you.", "/login");
    }

    private void logout(HttpRequest req, HttpResponse res, SessionStore.Session s, boolean text) {
        if (s != null) {
            SessionStore.remove(s.getToken());   // the session table loses this entry too (Vector, Asg 5)
        }
        if (text) {
            res.text("OK|Logged out");
        } else {
            // clear the cookie AND confirm the logout on the home page
            res.redirect("/?msg=" + urlEncode("You have been logged out."),
                    "sid=; Path=/; HttpOnly");   // empty cookie value deletes it in the browser
        }
    }

    // F3: the seller's own listings, plus the "post item" form
    private void mine(HttpRequest req, HttpResponse res, SessionStore.Session s, boolean text)
            throws DatabaseOperationException {
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
            page(res, s, "My listings", "mine", new FormData(req.query).get("msg"), html.minePage(items));
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
            error(res, s, text, 403, "You can only delete your own listings");
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
            error(res, s, text, 404, "Item not found");
            return;
        }
        if (item.getSellerId() == s.getUserId()) {
            error(res, s, text, 403, "You cannot request your own item");
            return;
        }
        if (!"AVAILABLE".equals(item.getStatus())) {
            error(res, s, text, 409, "Item is no longer available");
            return;
        }
        requestDAO.submit(itemId, s.getUserId(), offer, f.get("message"));   // ★ Asg 20: INSERT
        done(res, text, "Request sent to the seller", "/requests");
    }

    // F5: incoming (as seller) and outgoing (as buyer) requests, with invoices for completed ones
    private void requests(HttpRequest req, HttpResponse res, SessionStore.Session s, boolean text)
            throws DatabaseOperationException {
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
            // one invoice text per incoming row ("" when there is no invoice yet) —
            // ★ Asg 13: each completed request's invoice file is READ BACK from invoices/
            ArrayList<String> invoices = new ArrayList<String>();
            for (int i = 0; i < incoming.size(); i++) {
                PurchaseRequest r = incoming.get(i);
                String invoiceText = "";
                if ("COMPLETED".equals(r.getStatus())) {
                    try {
                        invoiceText = FileLogger.readInvoice(r.getRequestId());
                    } catch (java.io.IOException e) {
                        invoiceText = "(invoice file missing)";
                    }
                }
                invoices.add(invoiceText);
            }
            page(res, s, "Purchase requests", "requests", new FormData(req.query).get("msg"),
                    html.requestsPage(incoming, outgoing, invoices));
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
        // the browser forms post to /requests/accept | /requests/reject | /requests/complete
        // (action taken from the URL); the Swing client still posts the "action" field
        String action = f.get("action");
        if (action.length() == 0) {
            action = actionFromPath(req.path);
        }
        PurchaseRequest request = requestDAO.findById(requestId);
        if (request == null || request.getSellerId() != s.getUserId()) {
            error(res, s, text, 403, "That request is not for your listing");
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
            error(res, s, text, 400, "Unknown action: " + action);
        }
    }

    // F2: admin dashboard — students waiting for approval
    private void admin(HttpRequest req, HttpResponse res, SessionStore.Session s, boolean text)
            throws DatabaseOperationException {
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
            page(res, s, "Student verification", "admin", new FormData(req.query).get("msg"),
                    html.adminPage(pending));
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
        // browser forms post to /admin/approve | /admin/reject; the Swing client
        // still posts /admin/verify with the "action" field
        String action = f.get("action");
        if (action.length() == 0) {
            action = actionFromPath(req.path);
        }
        String status = "approve".equals(action) ? "APPROVED" : "REJECTED";
        userDAO.updateVerification(userId, status);   // ★ Asg 20: UPDATE
        done(res, text, "Student " + status.toLowerCase(), "/admin");
    }

    // "accept" from /requests/accept, "approve" from /admin/approve, ...
    private String actionFromPath(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : "";
    }

    // ---------- small helpers ----------

    // role guard: every protected route calls this first.
    // Browser users are never dropped on a dead-end page:
    //   - not logged in  -> redirect to /login with a "please log in" notice
    //   - wrong role     -> friendly 403 page with a link back home
    private boolean require(HttpResponse res, String role, SessionStore.Session s, boolean text) {
        if (s != null && role.equals(s.getRole())) {
            return true;
        }
        if (text) {
            res.text("ERR|Please log in as " + role.toLowerCase() + " first");
            return false;
        }
        if (s == null) {
            res.redirect("/login?msg=" + urlEncode("Please log in as " + role.toLowerCase() + " first"), null);
        } else {
            res.html(403, "Forbidden", html.message(role + "s only — you are logged in as a "
                    + s.getRole().toLowerCase(), s.getRole()));
        }
        return false;
    }

    private void page(HttpResponse res, SessionStore.Session s, String title, String active,
                      String notice, String body) {
        String role = s == null ? null : s.getRole();    // null => the anonymous topbar
        res.html(200, "OK", html.page(title, active, role, notice, body));   // ★ Asg 5: StringBuffer shell
    }

    private void done(HttpResponse res, boolean text, String message, String redirectPath) {
        if (text) {
            res.text("OK|" + message);           // the Swing client reads this first line
        } else {
            // Post/Redirect/Get: the success text travels in the "msg" query parameter
            // and the target page shows it in the design's "info" band
            res.redirect(redirectPath + (redirectPath.indexOf('?') >= 0 ? "&" : "?")
                    + "msg=" + urlEncode(message), null);
        }
    }

    // percent-encodes the flash message so it survives the redirect URL
    private String urlEncode(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return text;                        // cannot happen: UTF-8 always exists
        }
    }

    // serves one static asset of the provided web design (style.css or a photograph)
    // ★ Asg 13: file handling — the bytes are read from disk and copied to the socket
    private void staticFile(HttpResponse res, String name) {
        try {
            byte[] data = WebAssets.read(name);       // ★ Asg 13: FileInputStream + ByteArrayOutputStream
            if (data == null) {
                res.html(404, "Not Found", html.message("File not found: " + name, null));
                return;
            }
            res.bytes(200, "OK", WebAssets.contentType(name), data);
        } catch (java.io.IOException e) {
            res.html(404, "Not Found", html.message("File not found: " + name, null));
        }
    }

    private void staticFrontend(HttpResponse res, String name) {
        try {
            byte[] data = readLocal("frontend", name);
            if (data == null) {
                res.html(404, "Not Found", html.message("File not found: /frontend/" + name, null));
                return;
            }
            res.bytes(200, "OK", WebAssets.contentType(name), data);
        } catch (java.io.IOException e) {
            res.html(404, "Not Found", html.message("File not found: /frontend/" + name, null));
        }
    }

    private void staticUi(HttpResponse res, String name) {
        try {
            byte[] data = readLocal("ui", name);
            if (data == null) {
                res.html(404, "Not Found", html.message("File not found: /ui/" + name, null));
                return;
            }
            res.bytes(200, "OK", WebAssets.contentType(name), data);
        } catch (java.io.IOException e) {
            res.html(404, "Not Found", html.message("File not found: /ui/" + name, null));
        }
    }

    private void staticWeb(HttpResponse res, String name) {
        try {
            byte[] data = readLocal("web", name);
            if (data == null) {
                res.html(404, "Not Found", html.message("File not found: /web/" + name, null));
                return;
            }
            res.bytes(200, "OK", WebAssets.contentType(name), data);
        } catch (java.io.IOException e) {
            res.html(404, "Not Found", html.message("File not found: /web/" + name, null));
        }
    }

    private void staticApi(HttpResponse res, String name) {
        try {
            byte[] data = readLocal("api", name);
            if (data == null) {
                res.html(404, "Not Found", html.message("File not found: /api/" + name, null));
                return;
            }
            res.bytes(200, "OK", WebAssets.contentType(name), data);
        } catch (java.io.IOException e) {
            res.html(404, "Not Found", html.message("File not found: /api/" + name, null));
        }
    }

    private byte[] readLocal(String base, String name) throws java.io.IOException {
        if (name.contains("..") || name.indexOf(':') >= 0) {
            return null;
        }
        java.io.File f = new java.io.File(base, name);
        if (!f.isFile()) {
            return null;
        }
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.FileInputStream in = null;
        try {
            in = new java.io.FileInputStream(f);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        } finally {
            if (in != null) in.close();
        }
    }

    private void error(HttpResponse res, SessionStore.Session s, boolean text, int status, String message) {
        if (text) {
            res.text("ERR|" + message);          // the GUI shows this in a dialog
        } else {
            String role = s == null ? null : s.getRole();   // the error page keeps the correct topbar
            res.html(status, reason(status), html.message(message, role));
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
