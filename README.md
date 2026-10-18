# CampusKart — Java mini project (local build)

A campus marketplace built **only from the lab manual**: a raw-socket HTTP server,
a MySQL database reached through pure JDBC, and a Swing desktop client.
No frameworks, no build tool, no JavaScript — and **no cloud deployment**: everything
runs on your own machine (`localhost`).

**Features kept deliberately minimal** (rubric-minimum set):

| Feature | Where |
| --- | --- |
| Register + login (session cookie) | `Router`, `UserDAO`, `SessionStore` |
| Browse with keyword + category search | `ItemDAO.search`, `HtmlBuilder.browsePage` |
| Post / delete listings (approved students only) | `Router.addItem`, `ItemDAO` |
| Purchase requests: accept / reject / **complete (JDBC transaction + invoice file)** | `RequestDAO.complete`, `FileLogger` |
| Admin dashboard: approve / reject students | `Router.admin`, `UserDAO.updateVerification` |

---

## Requirements

- **JDK 17 or newer** — everything is compiled and run from the command line, no IDE
  (that *is* Assignment 1).
- **MySQL 8.0.16 or newer** (older versions ignore `CHECK` constraints).
- **mysql-connector-j-8.x.x.jar** — the only external dependency, and it exists because
  the manual (Asg 19) requires a JDBC driver. Download it from the MySQL website into a
  `lib/` folder next to `src/`.

---

## Build and run (all steps are CLI)

Run every command from inside the `CampusKart_Java/` folder.

### 1. Create the database and seed data

```bash
mysql -u root -p < campuskart.sql
```

This creates the `campuskart` database, the `campus` / `campus_pw` user, three tables
and the seed rows used below. (If your MySQL root needs no password: `mysql -u root < campuskart.sql`.)

### 2. Put the JDBC driver in lib/

```bash
mkdir lib
# copy the downloaded jar into lib/, e.g. lib/mysql-connector-j-8.4.0.jar
```

### 3. Compile — Assignment 1 (CLI, no IDE)

```bash
javac -cp "lib/*" -d out $(find src -name "*.java")
```

Windows (PowerShell / cmd):

```bat
javac -cp "lib\*" -d out src\com\campuskart\model\*.java src\com\campuskart\exception\*.java src\com\campuskart\dao\*.java src\com\campuskart\server\*.java src\com\campuskart\util\*.java src\com\campuskart\client\*.java
```

### 4. Run the server

```bash
java -cp "out:lib/*" com.campuskart.server.CampusKartServer
```

(Windows: use `;` instead of `:` in the classpath.)

It prints:

```
CampusKart server running on http://localhost:8080
```

Open **http://localhost:8080** in a browser — the full site works there (HTML, forms,
cookies). The `logs/` and `invoices/` folders are created automatically, so start the
server from inside `CampusKart_Java/`.

### 5. Run the Swing client (separate terminal)

```bash
java -cp out com.campuskart.client.ClientApp localhost 8080
```

The client needs only a JDK with Swing: no MySQL driver, no database credentials.
It reaches the database through the server over a `Socket`.

---

## Test accounts (from the seed script)

| Role | Email | Password | Use it to see |
| --- | --- | --- | --- |
| Admin | `admin@sitpune.edu` | `admin123` | Admin tab → approve Kabir (PENDING) |
| Student | `meera@sitpune.edu` | `pass1234` | My listings, accept/complete requests, invoice file |
| Student | `aarav@sitpune.edu` | `pass1234` | Send a request, browse as a second user |
| Student | `kabir@sitpune.edu` | `pass1234` | Login blocked from posting until the admin approves him |
| Student | `sana@sitpune.edu` | `pass1234` | REJECTED account (also cannot post) |

### Quick demo script (5 minutes)

1. Browse → search `java` → only AVAILABLE items match.
2. Login as **aarav** → Send request on *Core Java Volume 1* with offer `400`.
3. Login as **meera** → Requests → **Accept**, then **Complete**.
   Three SQL statements ran as one transaction (request COMPLETED, item SOLD,
   every other request auto-rejected) and `invoices/INV-1.txt` was written.
   Refresh the page: the invoice is read back from the file and shown.
4. Login as **admin** → Admin → **Approve** Kabir; Kabir can now post items.
5. Log in as **kabir** *before* step 4 to see `UnverifiedStudentException` ("Only
   approved students can post items").
6. Post an item with price `abc` or `-5` → `InvalidPriceException` (400).
7. Open the site in two browser tabs at once → two threads serve them together.

---

## Lab manual → code map

Every tagged line can be found by searching the source for `★`. Each tag names the
assignment, so the code doubles as a viva answer sheet.

| Asg | Manual concept | File(s) |
| --- | --- | --- |
| 1 | CLI compile/run, no IDE | this README, steps 3–5 |
| 2 | Default / parameterized / **copy** constructors | `model/Item.java`, `model/User.java`, `model/Student.java` |
| 3 | Method overloading, **static** members | `model/Receipt.java` (3 × `calculateTotal`), `CampusKartServer.requestsServed` |
| 4 | Wrapper classes | `FormData.getInt/getDouble`, `Router.addItem`, `ClientApp.main` |
| 5 | ArrayList, Vector, **StringBuffer** | `FormData` + `SessionStore` (Vector), all DAO results (ArrayList), every page/response (StringBuffer) |
| 6 | Inner + anonymous classes | `HtmlBuilder.ItemCard` (inner), shutdown hook + GUI listeners (anonymous) |
| 7 | Inheritance + interfaces | `User → Student/Admin`, `Billable` |
| 8 | `super` keyword | `Student`/`Admin` constructors, `InvalidPriceException` |
| 9 | `final` variables/methods | `userId`, `itemId`, `requestId`, session token, `Receipt` fields |
| 10 | Abstract class + method | `abstract class User`, `getDashboardPath()` |
| 11 | Interface implementation | `Receipt implements Billable` |
| 12 | User-defined packages | all six `com.campuskart.*` packages |
| 13 | File handling (write **and** read) | `util/FileLogger.java` — invoices + server log |
| 14 | try/catch, **nested**, **multiple** catch | `Router.dispatch`, every DAO `finally`, `RequestDAO.complete` |
| 15 | `finally`, `throw`, `throws` | every DAO method, `ClientHandler.run`, `Router.addItem` |
| 16 | User-defined exceptions | `exception/` package (3 exceptions) |
| 17 | Swing components | `client/ClientApp.java` (JFrame, JPanel, JTextField, JComboBox, JTable, JOptionPane) |
| 18 | Button click handling | every `addActionListener` in `ClientApp` |
| 19 | JDBC SELECT | `ItemDAO.search`, `UserDAO.authenticate` |
| 20 | INSERT / UPDATE / DELETE + **transaction** | `ItemDAO.create/delete`, `UserDAO.updateVerification`, `RequestDAO.complete` (commit/rollback) |
| 21 | DriverManager, Connection | `dao/DBConnection.java` |
| 22 | PreparedStatement, ResultSet | every DAO query — parameterized only, never concatenated |
| 23 | ResultSet navigation | `next()` loops in every DAO read method |
| 24 | GUI + JDBC combined | Swing client → `ServerLink` → server → DAO → MySQL, shown in a JTable |
| 25 | Thread lifecycle | `CampusKartServer`: NEW → start() → RUNNING → TERMINATED |
| 26 | Runnable | `ClientHandler implements Runnable` |
| 27 | Socket / ServerSocket | server side: `CampusKartServer`/`HttpResponse`; client side: `client/ServerLink.java` |

**Out of scope on purpose:** Applets (Asg 17's second half, obsolete), RMI (28),
JSP/Servlets (29–30), Spring, Maven/Gradle, JavaScript, lambdas/streams,
`HashMap`, `ExecutorService`, `synchronized`, try-with-resources — none of these
are in the manual.

---

## Known limitations (deliberate, mention them yourself before the examiner does)

- Passwords are stored in plain text (hashing via `MessageDigest` is not in the manual).
- HTTP only — no HTTPS, no CSRF token, no rate limiting.
- Thread-per-connection with no cap, sessions kept in memory (`Vector`), lost on restart.
- `logs/` and `invoices/` are relative to the folder the server was started from.
- The Swing client performs its network call on the Swing event thread, so the window
  pauses briefly — Swing's threading helpers are not in the manual.
