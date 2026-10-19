# CampusKart Architecture (Java Perspective)

## 1) High-level Overview
CampusKart is a minimal campus marketplace built strictly to the lab manual. It follows a client–server split:
- Raw-socket HTTP server written in Java (no frameworks, no servlets, no Spring, no Maven/Gradle). One `ClientHandler` per connection (thread-per-connection). 
- Pure JDBC (MySQL Connector/J) for persistence; SQL is in DAO classes with `PreparedStatement` only.
- Two front-ends: (a) browser (plain HTML + CSS served by the Java server; **no client-side JavaScript is served by the Java paths**; the Java-rendered pages are zero-JS), (b) Swing desktop client that talks to the same HTTP server using the text protocol.
- Everything runs locally on `localhost` (no cloud/EC2).

## 2) Package layout
All under `com.campuskart.*`:

| Package | Responsibility | Key files |
|---|---|---|
| `model` | Domain entities (POJOs). Constructors, fields, accessors. | `User.java` (abstract), `Student.java`, `Admin.java`, `Item.java` (copy constructor), `PurchaseRequest.java`, `Receipt.java` (implements `Billable`), `SessionStore.Session` (inner) |
| `exception` | Three user-defined exceptions (lab scope). | `InvalidPriceException.java`, `UnverifiedStudentException.java`, `DatabaseOperationException.java` |
| `dao` | JDBC data access. `DriverManager`, `Connection`, `PreparedStatement`, `ResultSet`. One transaction in `RequestDAO.complete()`. | `DBConnection.java`, `UserDAO.java`, `ItemDAO.java`, `RequestDAO.java` |
| `server` | Raw HTTP + routing + sessions. | `CampusKartServer.java`, `ClientHandler.java`, `HttpRequest.java`, `HttpResponse.java`, `Router.java`, `FormData.java`, `SessionStore.java` |
| `util` | HTML rendering, logging, static assets. | `HtmlBuilder.java` (inner class `ItemCard`), `FileLogger.java` (read/write invoices + log), `WebAssets.java` (serves CSS/images from `../CampusKart_Web/`) |
| `client` | Swing GUI (JFrame/JTable etc.), talks to server over Socket. | `ClientApp.java`, `ServerLink.java` |

## 3) Startup & threading (Asg 25–27)
- `CampusKartServer.main()` creates a `ServerSocket` on port `8080`, loops `accept()`; for each connection spawns `new Thread(new ClientHandler(socket)).start()` (Runnable, Asg 26). Tracks `requestsServed` (static, Asg 3).
- `ClientHandler.run()` reads the request line/headers/body, builds `HttpRequest`, creates `HttpResponse`, delegates to `Router.dispatch(req,res)`, then closes the socket.
- `Runtime.getRuntime().addShutdownHook(new Thread(...) )` in `CampusKartServer` logs shutdown (anonymous class, Asg 6).

## 4) HTTP, request parsing, response
- `HttpRequest`: splits method/path/protocol, parses headers (maps), reads `Content-Length` body, detects `Accept: text/plain` → `wantsText = true` (Swing client uses this).
- `FormData`: parses `application/x-www-form-urlencoded` (body/query) into key→value; `getInt()`, `getDouble()` use wrapper classes (Asg 4); throws `NumberFormatException` on bad numeric input.
- `HttpResponse`: `html(status, reason, content)`, `text(content)`, `redirect(path, cookie)`, `bytes(status, reason, contentType, byte[])` (binary static assets). Uses `StringBuffer` for headers/body where applicable (Asg 5).
- `Router`: one big if/else over method+path (table-driven in structure). All handlers inside `dispatch()` `try { route(...) } catch(...)` with **multiple catch blocks** (Asg 14): `InvalidPriceException`, `UnverifiedStudentException`, `NumberFormatException`, `DatabaseOperationException`. `DatabaseOperationException` → logged via `FileLogger`, user gets friendly message (no stack trace). Errors render either `text("ERR|...")` (Swing) or HTML via `HtmlBuilder.message(...)` (browser).

## 5) Sessions (Vector, synchronized collection choice)
- `SessionStore` uses a `static Vector<Session>` (Asg 5: Vector is synchronized — intentional for shared mutable session map across threads).
- `Session` has `final` token/userId/role (Asg 9). `create(userId,role)` generates a token; `find(token)` scans by index; `remove(token)` removes by index.
- Browser: `login` creates session and sets cookie `sid=<token>; Path=/; HttpOnly`. `logout` clears cookie. Swing client receives role in `text("OK|<role>")` after login (no cookie needed for its own protocol).
- Role guard `require(res, role, s, text)`: anonymous → redirect to `/login?msg=Please log in as <role> first` (browser); logged-in wrong role → 403 HTML with message; text mode returns `ERR|...`.

## 6) Model & OOP
- `abstract class User` (Asg 10) with `getDashboardPath()` abstract; `final` `userId` (Asg 9).
- `Student extends User` (Asg 7), uses `super(...)` in constructor (Asg 8); `canPostItems()` checks `verificationStatus == "APPROVED"`.
- `Admin extends User` (Asg 7), uses `super` (Asg 8).
- `Item`: default constructor + **copy constructor** `Item(Item other)` (Asg 2). DAO stores a working copy (defensive copy). 
- `Receipt implements Billable` (Asg 11). `Billable` defines billing contract (Asg 7).
- `PurchaseRequest`: has `final int requestId` (Asg 9); constructor overloading style present (Asg 3 not directly here, but Receipt has 3 overloaded `calculateTotal` forms).
- Inner class `HtmlBuilder.ItemCard` (Asg 6). Anonymous classes: shutdown hook in `CampusKartServer`, Swing `ActionListener`s in `ClientApp` (Asg 6, 18).

## 7) DAO & JDBC (Asg 19–23)
- `DBConnection.getConnection()` returns `java.sql.Connection` via `DriverManager.getConnection("jdbc:mysql://localhost:3306/campuskart", "campus", "campus_pw")`, loads `com.mysql.cj.jdbc.Driver` (Asg 21).
- All queries use `PreparedStatement` with parameter placeholders (no string concatenation) (Asg 22). 
- `ResultSet` iterated with `while (rs.next())` (Asg 23).
- `UserDAO.authenticate`, `listPendingStudents`, etc use SELECT (Asg 19). `register`, `create`, `updateVerification`, `delete`, `submit`, `accept`, `reject` use INSERT/UPDATE/DELETE (Asg 20).
- **Transaction (double-weight rubric item):** `RequestDAO.complete(int requestId, int itemId)` does `conn.setAutoCommit(false);` then three statements (mark request COMPLETED, mark item SOLD, reject all other PENDING requests for that item) in one transaction; `commit()` on success, `rollback()` in `catch` and rethrows `DatabaseOperationException` (Asg 14/15, 20). `finally` closes resources (each close in its own try, Asg 15).
- Resources closed in `finally` blocks (nested/structured as taught) (Asg 14/15).

## 8) File handling (Asg 13)
- `FileLogger.logInvoice(Receipt)` creates `invoices/` if absent, writes `INV-<id>.txt` via `FileWriter` (write). 
- `FileLogger.readInvoice(int requestId)` reads `invoices/INV-<id>.txt` via `FileReader` + `BufferedReader` (read). The browser requests page reads back invoice text for completed incoming requests and renders it (the demo expects this).
- `WebAssets.read(relativePath)` reads bytes from `CampusKart_Web/` (default `../CampusKart_Web`, overridable `-Dcampuskart.web=...`) with `FileInputStream` → `ByteArrayOutputStream` and a `..` path-traversal guard (read). Used for `style.css` and images.
- `Router.staticFile()` and new `staticFrontend/staticUi/staticWeb/staticApi()` read from local subdirs (`frontend/`, `ui/`, `web/`, `api/`) with same guard (binary bytes served via `HttpResponse.bytes()`).

## 9) HTML rendering (server-side, zero JS by design)
- `HtmlBuilder` assembles all browser pages with `StringBuffer` (Asg 5). 
- Shared shell: `page(title, active, role, notice, body)` → DOCTYPE, Inter font, `<link rel="stylesheet" href="/style.css">`, topbar, optional `.strip info`, main wrap, footer. 
- Topbar: role-aware — anonymous → `Login` + `Sign Up`; `STUDENT` → `Browse/My listings/Requests` + `Student account` + `Logout`; `ADMIN` → `Browse/Admin` + `Admin account` + `Logout`. 
- Browse (`browsePage(items, keyword, category, role, viewerId)`): hero (`.herofull`), category tiles, product grid. `ItemCard.render()` + `cardAction()`:
  - not `AVAILABLE` → status note
  - student owns it → "this is your listing" link to `/items/mine`
  - admin viewing available → no action
  - anonymous → "Login as a student..." 
  - student viewing someone else's available → offer form POST to `/items/request`
- Pages: `loginPage(error)`, `registerPage(error)` (.auth2 split), `minePage(items)`, `requestsPage(incoming,outgoing,invoices)` (reads invoice texts), `adminPage(pending)`, `message(text,role)`.
- All user-supplied values escaped (`escape()`) with `& < > " '` (Asg 2/4 style checks noted).
- Flash messages travel via `?msg=` (PRG) and render in `.strip info`.

## 10) Routes (Java server)
`Router.route()` matches method+path:

| Method | Path | Handler | Notes |
|---|---|---|---|
| GET | `/` | `browse` | public; role shapes UI |
| GET | `/login` | render login | redirects to `/` if already logged in; shows `msg` if present |
| POST | `/login` | `login` | sets `sid` cookie on success, redirects to `/?msg=Welcome...`; bad creds re-render form |
| GET | `/register` | render register | redirects if logged in; `.edu` enforced on submit |
| POST | `/register` | `register` | creates `Student` (PENDING), flashes message, redirects to `/login`; `.edu` check returns form error |
| POST | `/logout` | `logout` | removes session, clears cookie, redirects to `/?msg=You have been logged out.` |
| GET | `/items/mine` | `mine` (requires STUDENT) | shows seller items + post form |
| POST | `/items` or `/items/add` | `addItem` (STUDENT) | price validated (1–100000) → `InvalidPriceException`; unverified → `UnverifiedStudentException`; stores copy via `new Item(draft)` |
| POST | `/items/delete` | `deleteItem` (STUDENT) | owner-only; uses working copy |
| POST | `/items/request` | `requestItem` (STUDENT) | cannot request own item; only `AVAILABLE` |
| GET | `/requests` | `requests` (STUDENT) | incoming/outgoing; reads invoice texts for COMPLETED incoming via `FileLogger.readInvoice` |
| POST | `/requests/accept\|reject\|complete` or `/requests/respond` | `respond` (STUDENT, owner) | `complete` runs `requestDAO.complete()` (transaction) then logs invoice file |
| GET | `/admin` | `admin` (ADMIN) | pending students |
| POST | `/admin/approve\|reject` or `/admin/verify` | `verify` (ADMIN) | UPDATE verification status |
| GET | `/style.css` | static CSS | from `WebAssets` |
| GET | `/images/*` | static images | from `WebAssets` |
| GET | `/frontend/*`, `/ui/*`, `/web/*`, `/api/*` | local copies | served from disk (copies of provided web files + JS API) |

Browser paths remain HTML+CSS only (no JS endpoints). The JS copies under `/frontend/*`, `/ui/*`, `/web/*`, `/api/*` are reference/frontend copies for inspection/data verification (kept maximally modularized: `api/modular/api-core.js`, `api/modular/index.js`, `api/modular/api-modules.js`, `api/modules/`, `frontend/js/`, etc.).

## 11) Text protocol (Swing client)
- Client sends `GET/POST <path> HTTP/1.0` with header `Accept: text/plain`. Body for POST is `application/x-www-form-urlencoded`.
- Server returns lines: first line `OK|...` on success, or `ERR|<message>` on error. Browse/requests/mine/admin return `OK` followed by `|`-separated records (one per line) for the JTable. `login` returns `OK|<role>`.
- `ServerLink` builds the raw HTTP, reads the socket, returns the full text to `ClientApp`. `ClientApp` shows tables/dialogs (JTable, JOptionPane) and performs network calls on the Swing event thread (noted as deliberate per manual scope).

## 12) Validation & rules
- Prices: `1 <= price <= 100000` (browser/route) → `InvalidPriceException` (400).
- Posting items: only `Student` with `canPostItems()` true (`APPROVED`) → `UnverifiedStudentException` (403/handled).
- Cannot request own item; cannot request if not `AVAILABLE`.
- Registration: `.edu` email required (browser POST returns form error; text mode returns `ERR|Only emails ending with .edu are allowed`).
- Accept/reject/complete only by the seller of that request.

## 13) Database schema (trimmed for rubric minimum)
Tables: `users` (role, email, password, status), `items` (seller_id, category VARCHAR, title, description, condition, price, status), `purchase_requests` (item_id, buyer_id, seller_id, offered_price, status). `category` is a plain `VARCHAR` column on `items` (no separate `categories` table). Seed includes admin `admin@sitpune.edu` / `admin123` and students with `.sitpune.edu` emails, plus sample items/one PENDING request.

## 14) Static assets & copies
- Design CSS/images served from `../CampusKart_Web/` (default). 
- Local copies for inspection: `frontend/`, `ui/`, `web/` (each has `index.html`, `style.css`, `js/api.js`, `js/app.js`, `images/`) and `api/` + `api/modular/` (reference JS API, maximally modularized: `api-core.js`, `index.js`, `api-modules.js`, `modules/` directory, `README.md`). 
- `Router` serves all these under `/frontend/*`, `/ui/*`, `/web/*`, `/api/*`. These JS copies are **not** used by the Java-rendered browser pages (Java pages stay zero-JS) — they are optional for frontend data verification/reference only.

## 15) Build/run (exact)
From `CampusKart_Java/`:
```bash
mysql -u root -p < campuskart.sql
javac -cp "lib/*" -d out $(find src -name "*.java")
java -cp "out:lib/*" com.campuskart.server.CampusKartServer
java -cp out com.campuskart.client.ClientApp localhost 8080
```
Web UI at `http://localhost:8080/`. Swing client in separate terminal. Logs/invoices created relative to working dir.

## 16) Mapping to lab manual (highlights)
Asg 1 (CLI), 2 (constructors/copy), 3 (static), 4 (wrappers), 5 (ArrayList/Vector/StringBuffer), 6 (inner+anonymous), 7 (inheritance/interface), 8 (super), 9 (final), 10 (abstract), 11 (implements), 12 (packages), 13 (file read/write + static assets), 14 (multiple catch), 15 (finally/throw/throws), 16 (user-defined exceptions), 17–18 (Swing), 19 (SELECT), 20 (CRUD+transaction), 21 (DriverManager/Connection), 22 (PreparedStatement), 23 (ResultSet), 24 (GUI+JDBC), 25 (thread lifecycle), 26 (Runnable), 27 (Socket/ServerSocket).
