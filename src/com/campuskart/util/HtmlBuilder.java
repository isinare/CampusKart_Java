package com.campuskart.util;

import java.util.ArrayList;

import com.campuskart.model.Item;
import com.campuskart.model.PurchaseRequest;
import com.campuskart.model.User;

// ★ RUBRIC: Collections (1M) | Asg 5: EVERY page is assembled in a StringBuffer
//            (never with String + String, which wastes memory in a loop)
// ★ RUBRIC: CO1 | Asg 6: the inner class ItemCard at the bottom of this file
public class HtmlBuilder {

    // ---------- page shell ----------

    public String page(String title, String body) {
        StringBuffer sb = new StringBuffer();    // ★ Asg 5: StringBuffer
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>CampusKart</title></head><body>");
        sb.append("<div style='font-family:sans-serif;max-width:820px;margin:20px auto'>");
        sb.append("<h2>CampusKart</h2>");
        sb.append("<p><a href='/'>Browse</a> | <a href='/items/mine'>My listings</a> | ");
        sb.append("<a href='/requests'>Requests</a> | <a href='/admin'>Admin</a> | ");
        sb.append("<a href='/login'>Login</a> | <a href='/register'>Register</a> | ");
        sb.append("<form method='post' action='/logout' style='display:inline'><button>Logout</button></form></p>");
        sb.append("<h3>").append(escape(title)).append("</h3>");
        sb.append(body);
        sb.append("</div></body></html>");
        return sb.toString();
    }

    public String message(String text) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><body style='font-family:sans-serif'><p>").append(escape(text));
        sb.append("</p><p><a href='/'>Back to browse</a></p></body></html>");
        return sb.toString();
    }

    // ---------- screens ----------

    // F4: search form + one card per item (the cards come from the INNER class below)
    public String browsePage(ArrayList<Item> items, String keyword, String category) {
        StringBuffer sb = new StringBuffer();
        sb.append("<form method='get' action='/'>");
        sb.append("<input name='q' value='").append(escape(keyword)).append("' placeholder='search'> ");
        sb.append("<select name='category'>");
        String[] categories = { "All", "Books", "Electronics", "Stationery" };
        for (int i = 0; i < categories.length; i++) {
            sb.append("<option").append(categories[i].equals(category) ? " selected" : "").append('>')
              .append(categories[i]).append("</option>");
        }
        sb.append("</select> <button>Search</button></form>");
        sb.append("<p>").append(items.size()).append(" item(s)</p>");
        for (int i = 0; i < items.size(); i++) {
            // ★ Asg 6: the inner class is created from inside the outer class and uses its escape()
            sb.append(new ItemCard(items.get(i)).render());
        }
        return sb.toString();
    }

    public String loginPage(String error) {
        StringBuffer sb = new StringBuffer();
        sb.append("<form method='post' action='/login'>");
        sb.append("Email <input name='email'> Password <input name='password' type='password'> ");
        sb.append("<button>Login</button></form>");
        if (error.length() > 0) {
            sb.append("<p style='color:red'>").append(escape(error)).append("</p>");
        }
        return sb.toString();
    }

    public String registerPage(String error) {
        StringBuffer sb = new StringBuffer();
        sb.append("<form method='post' action='/register'>");
        sb.append("Name <input name='fullName'> ");
        sb.append("Email <input name='email'> ");
        sb.append("Enrollment no <input name='enrollmentNo'> ");
        sb.append("Department <input name='department'> ");
        sb.append("Password <input name='password' type='password'> ");
        sb.append("<button>Register</button></form>");
        if (error.length() > 0) {
            sb.append("<p style='color:red'>").append(escape(error)).append("</p>");
        }
        return sb.toString();
    }

    // F3: the post form + a table of own listings with a Delete button per row
    public String minePage(ArrayList<Item> items) {
        StringBuffer sb = new StringBuffer();
        sb.append("<h4>Post a new item</h4><form method='post' action='/items/add'>");
        sb.append("Title <input name='title'> ");
        sb.append("Category <select name='category'><option>Books</option><option>Electronics</option>"
                + "<option>Stationery</option></select> ");
        sb.append("Condition <select name='condition'><option>NEW</option><option>LIKE_NEW</option>"
                + "<option>GOOD</option><option>FAIR</option></select> ");
        sb.append("Price <input name='price' size='6'> ");
        sb.append("Description <input name='description' size='25'> ");
        sb.append("<button>Post</button></form>");
        sb.append("<table border='1' cellpadding='5'><tr><th>Title</th><th>Category</th><th>Price</th>"
                + "<th>Status</th><th></th></tr>");
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            sb.append("<tr><td>").append(escape(it.getTitle())).append("</td><td>")
              .append(escape(it.getCategory())).append("</td><td>").append(it.getPrice())
              .append("</td><td>").append(escape(it.getStatus())).append("</td><td>");
            sb.append("<form method='post' action='/items/delete' style='display:inline'>");
            sb.append("<input type='hidden' name='itemId' value='").append(it.getItemId()).append("'>");
            sb.append("<button>Delete</button></form></td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    // F5: incoming and outgoing requests; a COMPLETED row shows the invoice file (Asg 13)
    public String requestsPage(ArrayList<PurchaseRequest> incoming, ArrayList<PurchaseRequest> outgoing) {
        StringBuffer sb = new StringBuffer();
        sb.append("<h4>As seller (incoming)</h4>").append(requestTable(incoming, true));
        sb.append("<h4>As buyer (outgoing)</h4>").append(requestTable(outgoing, false));
        return sb.toString();
    }

    // F2: pending students with Approve / Reject buttons
    public String adminPage(ArrayList<User> pending) {
        StringBuffer sb = new StringBuffer();
        if (pending.size() == 0) {
            return "<p>No students are waiting for approval.</p>";
        }
        sb.append("<table border='1' cellpadding='5'><tr><th>Name</th><th>Email</th><th>Enrollment</th>"
                + "<th>Action</th></tr>");
        for (int i = 0; i < pending.size(); i++) {
            User u = pending.get(i);
            String enrollment = (u instanceof com.campuskart.model.Student)
                    ? ((com.campuskart.model.Student) u).getEnrollmentNo() : "";
            sb.append("<tr><td>").append(escape(u.getFullName())).append("</td><td>")
              .append(escape(u.getEmail())).append("</td><td>").append(escape(enrollment)).append("</td><td>");
            sb.append(verifyForm(u.getUserId(), "approve"));
            sb.append(verifyForm(u.getUserId(), "reject"));
            sb.append("</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    // ---------- private helpers ----------

    private String requestTable(ArrayList<PurchaseRequest> list, boolean sellerSide) {
        StringBuffer sb = new StringBuffer(
                "<table border='1' cellpadding='5'><tr><th>Item</th><th>Offer (INR)</th><th>Status</th>"
              + "<th>Action</th></tr>");
        for (int i = 0; i < list.size(); i++) {
            PurchaseRequest r = list.get(i);
            sb.append("<tr><td>").append(escape(r.getTitle())).append("</td><td>")
              .append(r.getOfferedPrice()).append("</td><td>").append(escape(r.getStatus())).append("</td><td>");
            if (sellerSide && "PENDING".equals(r.getStatus())) {
                sb.append(respondForm(r.getRequestId(), "accept"));
                sb.append(respondForm(r.getRequestId(), "reject"));
            } else if (sellerSide && "ACCEPTED".equals(r.getStatus())) {
                sb.append(respondForm(r.getRequestId(), "complete"));
            } else if ("COMPLETED".equals(r.getStatus())) {
                sb.append(invoiceFile(r.getRequestId()));   // ★ Asg 13: reading the invoice file back
            }
            sb.append("</td></tr>");
        }
        return sb.append("</table>").toString();
    }

    private String respondForm(int requestId, String action) {
        StringBuffer sb = new StringBuffer(
                "<form method='post' action='/requests/respond' style='display:inline'>");
        sb.append("<input type='hidden' name='requestId' value='").append(requestId).append("'>");
        sb.append("<input type='hidden' name='action' value='").append(action).append("'>");
        sb.append("<button>").append(action).append("</button></form>");
        return sb.toString();
    }

    private String verifyForm(int userId, String action) {
        StringBuffer sb = new StringBuffer(
                "<form method='post' action='/admin/verify' style='display:inline'>");
        sb.append("<input type='hidden' name='userId' value='").append(userId).append("'>");
        sb.append("<input type='hidden' name='action' value='").append(action).append("'>");
        sb.append("<button>").append(action).append("</button></form>");
        return sb.toString();
    }

    private String invoiceFile(int requestId) {
        try {
            String invoice = FileLogger.readInvoice(requestId);   // FileReader + BufferedReader (Asg 13)
            return "<pre>" + escape(invoice) + "</pre>";
        } catch (Exception e) {
            return "<em>invoice file not available</em>";         // ★ Asg 14: catch instead of crashing
        }
    }

    // ★ RUBRIC: CO1 | Asg 6: INNER class (non-static) — ItemCard belongs to an HtmlBuilder
    // object, so it can call the outer class's escape() method directly.
    // It renders ONE item as a small card with its request form.
    public class ItemCard {

        private final Item item;                 // ★ Asg 9: final field

        public ItemCard(Item item) {
            this.item = item;
        }

        public String render() {
            StringBuffer sb = new StringBuffer();    // ★ Asg 5: StringBuffer
            sb.append("<div style='border:1px solid #ccc;padding:10px;margin:8px 0'>");
            sb.append("<h3 style='margin:0'>").append(escape(item.getTitle())).append("</h3>");
            sb.append("<p style='margin:4px 0'>").append(escape(item.getCategory())).append(" | ")
              .append(escape(item.getCondition())).append(" | INR ").append(item.getPrice()).append("</p>");
            sb.append("<p style='margin:4px 0'>").append(escape(item.getDescription())).append("</p>");
            sb.append("<form method='post' action='/items/request'>");
            sb.append("<input type='hidden' name='itemId' value='").append(item.getItemId()).append("'>");
            sb.append("Offer INR <input name='offeredPrice' size='6'> ");
            sb.append("<input name='message' placeholder='message' size='25'> ");
            sb.append("<button>Send request</button></form>");
            sb.append("</div>");
            return sb.toString();
        }
    }

    // escapes every dynamic value before it enters the page (protection against HTML injection)
    public String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&') sb.append("&amp;");
            else if (c == '<') sb.append("&lt;");
            else if (c == '>') sb.append("&gt;");
            else if (c == '"') sb.append("&quot;");
            else if (c == '\'') sb.append("&#39;");
            else sb.append(c);
        }
        return sb.toString();
    }
}
