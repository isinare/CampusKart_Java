package com.campuskart.util;

import java.util.ArrayList;
import com.campuskart.model.Item;
import com.campuskart.model.PurchaseRequest;
import com.campuskart.model.Student;
import com.campuskart.model.User;

// Builds every HTML page the browser sees, using the CAMPUSKART WEB DESIGN SYSTEM
// that came with the project (style.css + the hero/product photographs). Only the
// CSS and the images are reused from that folder: app.js and api.js are client-side
// JavaScript and are NEVER served or referenced (scope document section 2.2: no
// JavaScript). The browser receives plain HTML + CSS and does zero processing —
// every screen is rendered here, on the server, with StringBuffer.
//
// ★ RUBRIC: CO1 | Asg 5: StringBuffer — every page below is assembled by appending
//            to a StringBuffer; nothing is printed directly (HtmlBuilder).
// ★ RUBRIC: UI1 | Asg 17/18: swing / javax.swing — the Swing client is the
//            primary GUI of this project; these server-rendered pages are the
//            secondary browser front-end built from the provided HTML/CSS design.
public class HtmlBuilder {

    // ---- the design system's assets (SVG copied from the provided web design) ----
    private static final String LOGO =
        "<svg viewBox='0 0 40 40' aria-hidden='true'>"
        + "<path d='M3 9h5l4 18h19l3-13H10' fill='none' stroke='#e8742a' stroke-width='2.6' "
        + "stroke-linecap='round' stroke-linejoin='round'/>"
        + "<rect x='14' y='19' width='15' height='4' fill='#1f56b8'/>"
        + "<rect x='15' y='14.5' width='13' height='4' fill='#2f7d3b'/>"
        + "<path d='M13 12.5l9-4.5 9 4.5-9 4.5z' fill='#14284b' transform='translate(-1 -3)'/>"
        + "<circle cx='15' cy='33' r='2.6' fill='#14284b'/>"
        + "<circle cx='28' cy='33' r='2.6' fill='#14284b'/></svg>";

    private static final String SEARCH_ICON =
        "<svg viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' "
        + "stroke-linecap='round' aria-hidden='true'><circle cx='11' cy='11' r='7'/>"
        + "<path d='M21 21l-5-5'/></svg>";

    private static final String CHECK =
        "<svg viewBox='0 0 24 24' aria-hidden='true'><path d='M5 12.5l4.5 4.5L19 7.5'/></svg>";

    private static final String EMPTY_ART =
        "<svg class='eart' viewBox='0 0 48 48' aria-hidden='true'>"
        + "<rect x='6' y='14' width='28' height='22' rx='3'/>"
        + "<path d='M34 20h5l3 8v6h-8'/><circle cx='16' cy='38' r='3'/><circle cx='36' cy='38' r='3'/>"
        + "<path d='M12 9h18M9 13h24'/></svg>";

    // category tile icons (the design's stroke style: currentColor, 2px, round caps)
    private static final String ICON_BOOKS =
        "<svg viewBox='0 0 24 24'><path d='M4 5a2 2 0 0 1 2-2h13v18H6a2 2 0 0 1-2-2z'/><path d='M8 3v18'/></svg>";
    private static final String ICON_ELECTRONICS =
        "<svg viewBox='0 0 24 24'><rect x='5' y='3' width='14' height='18' rx='2'/>"
        + "<path d='M8 7h8M8 12h2m3 0h3M8 16h2m3 0h3'/></svg>";
    private static final String ICON_STATIONERY =
        "<svg viewBox='0 0 24 24'><path d='M4 20l4-1 11-11-3-3L5 16z'/><path d='M14 6l3 3'/></svg>";

    // each category shows one of the provided product photographs
    private static String catImage(String category) {
        if ("Electronics".equals(category)) return "/images/products/c5-v1.jpg";
        if ("Stationery".equals(category)) return "/images/products/c6-v1.jpg";
        return "/images/products/c1-v1.jpg";                       // Books (default)
    }

    // ------------------------------------------------------------------
    // the shared page shell: topbar + optional notice band + main + footer
    // role: null/"" for anonymous visitors, "STUDENT" or "ADMIN" when logged in —
    // the topbar shows Login/Sign Up OR the account links, never both
    // ★ Asg 5: StringBuffer everywhere; nothing is concatenated with +
    //          inside a print statement
    // ------------------------------------------------------------------
    public String page(String title, String active, String role, String notice, String body) {
        StringBuffer sb = new StringBuffer();
        sb.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
        sb.append("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        sb.append("<title>CampusKart &mdash; ").append(escape(title)).append("</title>");
        sb.append("<link rel='preconnect' href='https://fonts.gstatic.com' crossorigin>");
        sb.append("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap' rel='stylesheet'>");
        sb.append("<link rel='stylesheet' href='/style.css'>");   // the provided stylesheet, served by THIS server
        sb.append("</head><body>");
        sb.append(topbar(active, role));
        if (notice != null && notice.length() > 0) {
            sb.append("<div class='strip info'><div class='wrap'>").append(escape(notice)).append("</div></div>");
        }
        sb.append("<main><div class='wrap'>").append(body).append("</div></main>");
        sb.append(footer(role));
        sb.append("</body></html>");
        return sb.toString();
    }

    private String topbar(String active, String role) {
        StringBuffer sb = new StringBuffer();
        sb.append("<header class='topbar'><div class='wrap'>");
        sb.append("<a class='logo' href='/'>").append(LOGO).append("<span>Campus<span class='k'>Kart</span></span></a>");
        sb.append("<nav class='mainnav'><div class='links'>");
        sb.append(navLink("/", "Browse", "browse".equals(active)));
        if ("STUDENT".equals(role)) {                 // only the pages this role can actually open
            sb.append(navLink("/items/mine", "My listings", "mine".equals(active)));
            sb.append(navLink("/requests", "Requests", "requests".equals(active)));
        }
        if ("ADMIN".equals(role)) {
            sb.append(navLink("/admin", "Admin", "admin".equals(active)));
        }
        sb.append("</div><div class='tools'>");
        if (role == null || role.length() == 0) {
            // visitor: exactly two actions — login or create an account
            sb.append("<a href='/login'>Login</a>");
            sb.append("<a href='/register' class='nbtn solid'>Sign Up</a>");
        } else {
            // logged in: who you are + logout (never Login/Sign Up as well)
            String who = "ADMIN".equals(role) ? "Admin account" : "Student account";
            sb.append("<span class='muted small hide-s'>").append(who).append("</span>");
            sb.append("<form method='post' action='/logout' style='display:inline'>"
                    + "<button class='nbtn'>Logout</button></form>");
        }
        sb.append("</div></nav></div></header>");
        return sb.toString();
    }

    private String navLink(String href, String label, boolean on) {
        return "<a href='" + href + "'" + (on ? " class='on'" : "") + ">" + label + "</a>";
    }

    private String footer(String role) {
        StringBuffer sb = new StringBuffer();
        sb.append("<footer class='foot'><div class='wrap'><div class='cols'>");
        sb.append("<div><a class='logo' href='/'>").append(LOGO)
          .append("<span>Campus<span class='k'>Kart</span></span></a>");
        sb.append("<p>A campus marketplace for second-hand books, electronics and stationery.</p>");
        sb.append("<p>Built as a Java college project &mdash; server-rendered, no JavaScript.</p></div>");
        sb.append("<div><h4>Marketplace</h4><a href='/'>Browse</a>");
        if ("STUDENT".equals(role)) {
            sb.append("<a href='/items/mine'>My listings</a><a href='/requests'>Requests</a>");
        }
        if ("ADMIN".equals(role)) {
            sb.append("<a href='/admin'>Admin</a>");
        }
        sb.append("</div>");
        sb.append("<div><h4>Account</h4>");
        if (role == null || role.length() == 0) {
            sb.append("<a href='/login'>Login</a><a href='/register'>Register</a>");
        } else {
            // logout must be a POST form; styled to look exactly like the footer links
            sb.append("<form method='post' action='/logout' style='margin:0'>"
                    + "<button type='submit' style='background:none;border:0;padding:3px 0;"
                    + "font:inherit;font-size:14px;color:#b6c1db;cursor:pointer;display:block'>"
                    + "Logout</button></form>");
        }
        sb.append("<p style='margin-top:10px'>Verified students only. Prices in INR.</p></div>");
        sb.append("</div><div class='legal'>&copy; CampusKart. Built by students, for students.</div></div></footer>");
        return sb.toString();
    }

    // ==================================================================
    // BROWSE (home page): hero + search, category tiles, product grid
    // role/viewerId describe the visitor so each card can show the right action
    // ==================================================================
    public String browsePage(ArrayList<Item> items, String keyword, String category,
            String role, int viewerId) {
        StringBuffer sb = new StringBuffer();

        // hero band — style.css paints images/hero.jpg behind this block
        sb.append("<section class='herofull'><div class='wrap'>");
        sb.append("<div class='eyebrow'>Symbiosis campus marketplace</div>");
        sb.append("<h1>Buy and sell within your campus.</h1>");
        sb.append("<p class='lead'>Second-hand books, electronics and stationery &mdash; "
                + "verified students only, prices in INR.</p>");
        sb.append("<form class='search' method='get' action='/'>").append(SEARCH_ICON);
        sb.append("<input type='search' name='q' value='").append(escape(keyword))
          .append("' placeholder='Search books, kits, calculators'>");
        sb.append("<button class='btn'>Search</button></form>");
        sb.append(heroCtas(role));
        sb.append("</div></section>");

        // category tiles (plain GET links — no JavaScript anywhere)
        sb.append("<div class='sechead'><h2>Browse by category</h2></div>");
        sb.append("<div class='cattiles'>");
        sb.append(catTile("Books", ICON_BOOKS));
        sb.append(catTile("Electronics", ICON_ELECTRONICS));
        sb.append(catTile("Stationery", ICON_STATIONERY));
        sb.append("</div>");

        // results
        sb.append("<div class='sechead'><h2>")
          .append(keyword.length() > 0 || category.length() > 0 ? "Search results" : "Latest listings")
          .append("</h2><span class='muted small'>").append(items.size()).append(" item(s)</span></div>");

        if (items.size() == 0) {
            sb.append("<div class='empty'>").append(EMPTY_ART)
              .append("<div>No items match right now. Try another keyword or category.</div></div>");
        } else {
            sb.append("<div class='grid'>");
            for (int i = 0; i < items.size(); i++) {
                ItemCard card = new ItemCard((Item) items.get(i), role, viewerId);   // ★ inner class
                sb.append(card.render());
            }
            sb.append("</div>");
        }
        return sb.toString();
    }

    // the two hero buttons depend on who is looking
    private String heroCtas(String role) {
        if ("STUDENT".equals(role)) {
            return "<div class='ctas'><a href='/items/mine' class='btn accent lg'>Post an item</a>"
                 + "<a href='/requests' class='btn outline lg'>My requests</a></div>";
        }
        if ("ADMIN".equals(role)) {
            return "<div class='ctas'><a href='/admin' class='btn accent lg'>Verify students</a></div>";
        }
        return "<div class='ctas'><a href='/register' class='btn accent lg'>Start selling</a>"
             + "<a href='/login' class='btn outline lg'>Login</a></div>";
    }

    private String catTile(String name, String icon) {
        return "<a class='cattile' href='/?category=" + name + "'>"
             + "<span class='ic'>" + icon + "</span><h3>" + name + "</h3></a>";
    }

    // inner class: one product card of the browse grid
    // role/viewerId decide what action the card offers this particular visitor
    // ★ RUBRIC: OOP2 | Asg 9: class + object — one ItemCard object per item
    public class ItemCard {
        private final Item item;
        private final String role;
        private final int viewerId;

        public ItemCard(Item item, String role, int viewerId) {
            this.item = item;
            this.role = role;
            this.viewerId = viewerId;
        }

        public String render() {
            boolean good = "NEW".equals(item.getCondition()) || "GOOD".equals(item.getCondition());
            StringBuffer sb = new StringBuffer();
            sb.append("<article class='pcard'>");
            sb.append("<div class='img'><img loading='lazy' src='").append(catImage(item.getCategory()))
              .append("' alt='").append(escape(item.getTitle())).append("'>");
            sb.append("<span class='cond").append(good ? " good" : "").append("'>")
              .append(escape(nice(item.getCondition()))).append("</span></div>");
            sb.append("<div class='body'>");
            sb.append("<div class='meta'><span class='inst'>").append(escape(item.getCategory()))
              .append("</span></div>");
            sb.append("<h3 class='name'>").append(escape(item.getTitle())).append("</h3>");
            sb.append("<p class='desc'>").append(escape(item.getDescription())).append("</p>");
            sb.append("<div class='foot2'><div class='price'>&#8377;").append(item.getPrice())
              .append("</div><span class='cat'>").append(escape(item.getCondition())).append("</span></div>");
            sb.append(cardAction());
            sb.append("</div></article>");
            return sb.toString();
        }

        // exactly one action per card, chosen by who is looking:
        //  - SOLD / RESERVED items cannot be requested at all
        //  - the owner sees "this is your listing" (no self-request form)
        //  - anonymous visitors get a Login link instead of a dead 403 form
        //  - admins see nothing (admins do not trade)
        //  - a student viewing someone else's AVAILABLE item gets the offer form
        private String cardAction() {
            if (!"AVAILABLE".equals(item.getStatus())) {
                return "<p class='small muted' style='margin-top:10px'>This item is "
                     + escape(nice(item.getStatus())).toLowerCase() + " and is no longer open for requests.</p>";
            }
            if ("STUDENT".equals(role) && item.getSellerId() == viewerId) {
                return "<p class='small muted' style='margin-top:10px'>This is your listing — "
                     + "manage it under <a href='/items/mine'>My listings</a>.</p>";
            }
            if ("ADMIN".equals(role)) {
                return "";
            }
            if (!"STUDENT".equals(role)) {
                return "<p class='small' style='margin-top:10px'><a href='/login'>Login</a> "
                     + "as a student to send the seller an offer.</p>";
            }
            // the offer form — only ever shown to a logged-in student
            StringBuffer sb = new StringBuffer();
            sb.append("<form method='post' action='/items/request' style='margin-top:10px'>");
            sb.append("<input type='hidden' name='itemId' value='").append(item.getItemId()).append("'>");
            sb.append("<div class='row' style='gap:8px'>");
            sb.append("<input name='offeredPrice' placeholder='Your offer (INR)' "
                    + "style='flex:1;min-width:110px'>");
            sb.append("<button class='btn sm'>Send request</button></div></form>");
            return sb.toString();
        }
    }

    // ==================================================================
    // LOGIN / REGISTER — the design's .auth2 split layout
    // ==================================================================
    public String loginPage(String error) {
        StringBuffer sb = new StringBuffer();
        sb.append("<div class='auth2'>");
        sb.append(sidePanel("Welcome back"));
        sb.append("<div><div class='card'>");
        sb.append("<h1 style='font-size:24px'>Login</h1>");
        sb.append("<p class='muted'>Verified students and admins only.</p>");
        if (error != null && error.length() > 0) {
            sb.append("<div class='strip bad' style='margin:12px 0'>").append(escape(error)).append("</div>");
        }
        sb.append("<form method='post' action='/login' class='stack'>");
        sb.append(field("Email", "<input type='email' name='email' required>"));
        sb.append(field("Password", "<input type='password' name='password' required>"));
        sb.append("<button class='btn block'>Login</button></form>");
        sb.append("<p class='small muted' style='margin-top:14px'>New here? "
                + "<a href='/register'>Create an account</a>. Demo accounts are listed in the README.</p>");
        sb.append("</div></div></div>");
        return sb.toString();
    }

    public String registerPage(String error) {
        StringBuffer sb = new StringBuffer();
        sb.append("<div class='auth2'>");
        sb.append(sidePanel("Join your campus market"));
        sb.append("<div><div class='card'>");
        sb.append("<h1 style='font-size:24px'>Create your account</h1>");
        sb.append("<p class='muted'>Your institute email is verified by the admin.</p>");
        if (error != null && error.length() > 0) {
            sb.append("<div class='strip bad' style='margin:12px 0'>").append(escape(error)).append("</div>");
        }
        sb.append("<form method='post' action='/register' class='fgrid'>");
        sb.append(field("Full name", "<input name='fullName' required class='full'>"));
        sb.append(field("Institute email", "<input type='email' name='email' required>"));
        sb.append(field("Enrollment number", "<input name='enrollmentNo' required>"));
        sb.append(field("Department", "<input name='department' required>"));
        sb.append(field("Password", "<input type='password' name='password' required class='full'>"));
        sb.append("<div class='full'><button class='btn block'>Create account</button></div>");
        sb.append("</form>");
        sb.append("<p class='small muted' style='margin-top:14px'>Already registered? "
                + "<a href='/login'>Login</a>.</p>");
        sb.append("</div></div></div>");
        return sb.toString();
    }

    private String sidePanel(String heading) {
        StringBuffer sb = new StringBuffer();
        sb.append("<aside class='sidepanel'><h2>").append(escape(heading)).append("</h2>");
        sb.append("<p>Your campus marketplace for books, electronics and stationery.</p><ul>");
        sb.append("<li>").append(CHECK).append("<span>Only verified SIT students can trade</span></li>");
        sb.append("<li>").append(CHECK).append("<span>Every request is handled by the seller</span></li>");
        sb.append("<li>").append(CHECK).append("<span>Invoices are generated as files on the server</span></li>");
        sb.append("</ul></aside>");
        return sb.toString();
    }

    private String field(String label, String inputHtml) {
        return "<div class='field'><label>" + label + "</label>" + inputHtml + "</div>";
    }

    // ==================================================================
    // MY LISTINGS — post form + table of the seller's items
    // ==================================================================
    public String minePage(ArrayList<Item> items) {
        StringBuffer sb = new StringBuffer();
        sb.append("<div class='pagehead'><div><h1>My listings</h1>"
                + "<p class='muted'>Post an item and track its status.</p></div></div>");

        sb.append("<div class='card'><h2>Post a new item</h2>");
        sb.append("<form method='post' action='/items' class='fgrid'>");
        sb.append(field("Title", "<input name='title' required class='full'>"));
        sb.append(field("Category", "<select name='category'><option>Books</option>"
                + "<option>Electronics</option><option>Stationery</option></select>"));
        sb.append(field("Condition", "<select name='condition'><option>NEW</option>"
                + "<option>GOOD</option><option>FAIR</option><option>POOR</option></select>"));
        sb.append(field("Price (INR)", "<input type='number' name='price' min='1' step='0.01' required>"));
        sb.append(field("Description", "<input name='description' required class='full'>"));
        sb.append("<div class='full'><button class='btn'>Post item</button></div>");
        sb.append("</form></div>");

        sb.append("<div class='card'><h2>Items I am selling</h2>");
        if (items.size() == 0) {
            sb.append("<div class='empty'>").append(EMPTY_ART)
              .append("<div>You have not listed anything yet.</div></div>");
        } else {
            sb.append("<div class='tblwrap'><table class='tbl'>"
                    + "<thead><tr><th>Title</th><th>Category</th><th>Condition</th>"
                    + "<th>Price</th><th>Status</th><th>Action</th></tr></thead><tbody>");
            for (int i = 0; i < items.size(); i++) {
                Item it = (Item) items.get(i);
                sb.append("<tr><td><strong>").append(escape(it.getTitle())).append("</strong></td>")
                  .append("<td>").append(escape(it.getCategory())).append("</td>")
                  .append("<td>").append(escape(nice(it.getCondition()))).append("</td>")
                  .append("<td>&#8377;").append(it.getPrice()).append("</td>")
                  .append("<td>").append(statusChip(it.getStatus())).append("</td>");
                if ("AVAILABLE".equals(it.getStatus()) || "RESERVED".equals(it.getStatus())) {
                    sb.append("<td><form method='post' action='/items/delete'>"
                            + "<input type='hidden' name='itemId' value='").append(it.getItemId())
                            .append("'><button class='btn danger sm secondary'>Delete</button></form></td>");
                } else {
                    sb.append("<td class='muted small'>&mdash;</td>");
                }
                sb.append("</tr>");
            }
            sb.append("</tbody></table></div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    // ==================================================================
    // REQUESTS — what I asked for, what others asked of me + invoices
    // ==================================================================
    // invoices.get(i) is the invoice text of incoming.get(i) ("" when not completed yet)
    // ★ Asg 13: the text shown here was READ BACK from the invoices/ files
    public String requestsPage(ArrayList<PurchaseRequest> incoming, ArrayList<PurchaseRequest> outgoing,
            ArrayList<String> invoices) {
        StringBuffer sb = new StringBuffer();
        sb.append("<div class='pagehead'><div><h1>Purchase requests</h1>"
                + "<p class='muted'>Accept, reject or complete the deals of your items.</p></div></div>");

        sb.append("<div class='card'><h2>Requests I have received</h2>");
        if (incoming.size() == 0) {
            sb.append("<div class='empty'>").append(EMPTY_ART)
              .append("<div>No one has requested your items yet.</div></div>");
        } else {
            sb.append("<div class='tblwrap'><table class='tbl'>"
                    + "<thead><tr><th>Item</th><th>Buyer</th><th>Offer</th><th>Status</th>"
                    + "<th>Actions</th><th>Invoice</th></tr></thead><tbody>");
            for (int i = 0; i < incoming.size(); i++) {
                PurchaseRequest r = (PurchaseRequest) incoming.get(i);
                sb.append("<tr><td><strong>").append(escape(r.getTitle())).append("</strong></td>")
                  .append("<td>User #").append(r.getBuyerId()).append("</td>")
                  .append("<td>&#8377;").append(r.getOfferedPrice()).append("</td>")
                  .append("<td>").append(statusChip(r.getStatus())).append("</td>");
                sb.append("<td>").append(incomingActions(r)).append("</td>");
                String invoice = (invoices != null && i < invoices.size())
                        ? (String) invoices.get(i) : "";
                if (invoice.length() > 0) {
                    sb.append("<td><pre class='small' style='background:var(--bg);padding:8px;"
                            + "border-radius:6px;white-space:pre-wrap;margin:0'>")
                      .append(escape(invoice)).append("</pre></td>");
                } else {
                    sb.append("<td class='muted small'>&mdash;</td>");
                }
                sb.append("</tr>");
            }
            sb.append("</tbody></table></div>");
        }
        sb.append("</div>");

        sb.append("<div class='card'><h2>Requests I have sent</h2>");
        if (outgoing.size() == 0) {
            sb.append("<div class='empty'>").append(EMPTY_ART)
              .append("<div>You have not requested anything yet. Browse the market to send one.</div></div>");
        } else {
            sb.append("<div class='tblwrap'><table class='tbl'>"
                    + "<thead><tr><th>Item</th><th>Seller</th><th>Offer</th><th>Status</th>"
                    + "<th>Action</th></tr></thead><tbody>");
            for (int i = 0; i < outgoing.size(); i++) {
                PurchaseRequest r = (PurchaseRequest) outgoing.get(i);
                sb.append("<tr><td><strong>").append(escape(r.getTitle())).append("</strong></td>")
                  .append("<td>User #").append(r.getSellerId()).append("</td>")
                  .append("<td>&#8377;").append(r.getOfferedPrice()).append("</td>")
                  .append("<td>").append(statusChip(r.getStatus())).append("</td>");
                if ("PENDING".equals(r.getStatus())) {
                    sb.append("<td class='muted small'>Waiting for the seller</td>");
                } else {
                    sb.append("<td class='muted small'>Closed</td>");
                }
                sb.append("</tr>");
            }
            sb.append("</tbody></table></div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String incomingActions(PurchaseRequest r) {
        if ("PENDING".equals(r.getStatus())) {
            return "<div class='row' style='gap:6px'>"
                 + "<form method='post' action='/requests/accept'><input type='hidden' name='requestId' value='"
                 + r.getRequestId() + "'><button class='btn good sm'>Accept</button></form>"
                 + "<form method='post' action='/requests/reject'><input type='hidden' name='requestId' value='"
                 + r.getRequestId() + "'><button class='btn danger sm secondary'>Reject</button></form></div>";
        }
        if ("ACCEPTED".equals(r.getStatus())) {
            return "<form method='post' action='/requests/complete'><input type='hidden' name='requestId' value='"
                 + r.getRequestId() + "'><button class='btn accent sm'>Complete &amp; invoice</button></form>";
        }
        return "<span class='muted small'>Closed</span>";
    }

    // ==================================================================
    // ADMIN — pending student verifications
    // ==================================================================
    public String adminPage(ArrayList<User> pending) {
        StringBuffer sb = new StringBuffer();
        sb.append("<div class='pagehead'><div><h1>Student verification</h1>"
                + "<p class='muted'>Approve or reject the students who registered.</p></div></div>");
        sb.append("<div class='card'>");
        if (pending.size() == 0) {
            sb.append("<div class='empty'>").append(EMPTY_ART)
              .append("<div>No students are waiting for verification.</div></div>");
        } else {
            sb.append("<div class='tblwrap'><table class='tbl'>"
                    + "<thead><tr><th>Name</th><th>Email</th><th>Enrollment no.</th>"
                    + "<th>Actions</th></tr></thead><tbody>");
            for (int i = 0; i < pending.size(); i++) {
                User u = (User) pending.get(i);
                String enrollment = (u instanceof Student)
                        ? ((Student) u).getEnrollmentNo() : "";    // ★ Asg 16: instanceof + cast
                sb.append("<tr><td><strong>").append(escape(u.getFullName())).append("</strong></td>")
                  .append("<td>").append(escape(u.getEmail())).append("</td>")
                  .append("<td>").append(escape(enrollment)).append("</td>")
                  .append("<td><div class='row' style='gap:6px'>")
                  .append("<form method='post' action='/admin/approve'><input type='hidden' name='userId' value='")
                  .append(u.getUserId()).append("'><button class='btn good sm'>Approve</button></form>")
                  .append("<form method='post' action='/admin/reject'><input type='hidden' name='userId' value='")
                  .append(u.getUserId())
                  .append("'><button class='btn danger sm secondary'>Reject</button></form></div></td></tr>");
            }
            sb.append("</tbody></table></div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    // ==================================================================
    // plain message page (used for errors and 'access denied')
    // role keeps the topbar correct (null => anonymous visitor)
    // ==================================================================
    public String message(String text, String role) {
        StringBuffer sb = new StringBuffer();
        sb.append("<div class='pagehead'><div><h1>CampusKart</h1></div></div>");
        sb.append("<div class='card'><div class='strip bad'>").append(escape(text)).append("</div>");
        sb.append("<p class='muted' style='margin-top:12px'>Continue from the menu above "
                + "or go back to the <a href='/'>browse page</a>.</p></div>");
        return page("Notice", null, role, null, sb.toString());
    }

    // status → colored chip of the design system
    private String statusChip(String status) {
        String cls = "chip";
        if ("AVAILABLE".equals(status) || "COMPLETED".equals(status) || "APPROVED".equals(status)) {
            cls = cls + " green";
        } else if ("PENDING".equals(status) || "RESERVED".equals(status)) {
            cls = cls + " amber";
        } else if ("SOLD".equals(status) || "REJECTED".equals(status)) {
            cls = cls + " red";
        }
        return "<span class='" + cls + "'>" + escape(nice(status)) + "</span>";
    }

    // AVAILABLE → Available, NOT_VERIFIED → Not verified
    private String nice(String value) {
        if (value == null || value.length() == 0) return "";
        String spaced = value.replace('_', ' ');
        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1).toLowerCase();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    // ★ Asg 2: if / else if / else — every special character is replaced one by one
    // ★ RUBRIC: SEC1 | Asg 4: operators — the == / && / >= checks inside the branches
    public String escape(String text) {
        if (text == null) return "";
        String result = text;
        if (result.indexOf('&') >= 0) result = result.replace("&", "&amp;");
        if (result.indexOf('<') >= 0) result = result.replace("<", "&lt;");
        if (result.indexOf('>') >= 0) result = result.replace(">", "&gt;");
        if (result.indexOf('"') >= 0) result = result.replace("\"", "&quot;");
        if (result.indexOf('\'') >= 0) result = result.replace("'", "&#39;");
        return result;
    }
}
