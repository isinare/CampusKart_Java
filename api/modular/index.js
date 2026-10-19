const USE_MOCK = true;
const BASE_URL = "http://localhost:8080/campuskart";

const DB_KEY = "ck_db_v5";
const SESSION_KEY = "ck_session";
const SESSION_HOURS = 8;
const REQUEST_DAYS = 3;
const BUNDLE_DISCOUNT = 40;
const PASS_SECRET = 7919;

const pad = n => String(n).padStart(2, "0");
const fmtDate = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const fmtTime = d => `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
const now = () => { const d = new Date(); return fmtDate(d) + " " + fmtTime(d); };
const today = () => fmtDate(new Date());
const ago = n => { const d = new Date(Date.now() - n * 864e5); return fmtDate(d) + " " + fmtTime(d); };
const addDays = (s, n) => { const d = new Date(s.slice(0, 10) + "T00:00:00"); d.setDate(d.getDate() + n); return fmtDate(d); };
const dayDiff = (a, b) => Math.round((new Date(b.slice(0, 10) + "T00:00:00") - new Date(a.slice(0, 10) + "T00:00:00")) / 864e5);
const hoursBetween = (a, b) => (new Date(b.replace(" ", "T")) - new Date(a.replace(" ", "T"))) / 36e5;
const fail = msg => { throw new Error(msg); };
const pageOf = (list, page, size) => {
  const total = list.length, pages = Math.max(1, Math.ceil(total / size)), p = Math.min(Math.max(1, +page || 1), pages);
  return { items: list.slice((p - 1) * size, p * size), total, page: p, pages };
};

function seed() {
  const cats = [
    [1, "Textbooks", 1, 0], [2, "Notes", 1, 0], [3, "Previous Year Papers", 1, 0], [4, "Calculators", 1, 1],
    [5, "Hardware Components", 1, 1], [6, "Lab & Drawing", 1, 1], [7, "Outdoors", 0, 1], [8, "Cycles", 0, 1],
    [9, "Formal Wear", 0, 1], [10, "Hostel Furniture", 0, 0], [11, "Electricals", 0, 1]];
  const inst = [["SIT", "Symbiosis Institute of Technology"], ["SCIT", "Symbiosis Centre for Information Technology"],
    ["SICSR", "Symbiosis Institute of Computer Studies and Research"], ["SIBM", "Symbiosis Institute of Business Management"],
    ["SCMS", "Symbiosis Centre for Management Studies"], ["SIIB", "Symbiosis Institute of International Business"],
    ["SIMC", "Symbiosis Institute of Media and Communication"], ["SLS", "Symbiosis Law School"], ["SID", "Symbiosis Institute of Design"],
    ["SSLA", "Symbiosis School of Liberal Arts"], ["SSE", "Symbiosis School of Economics"], ["SIHS", "Symbiosis Institute of Health Sciences"],
    ["SSBS", "Symbiosis School of Biological Sciences"], ["SIG", "Symbiosis Institute of Geoinformatics"], ["SSBF", "Symbiosis School of Banking and Finance"]];
  const subj = [["Engineering Mathematics", "MA101"], ["Engineering Physics", "PH101"], ["Basic Electrical Engineering", "EE101"],
    ["Data Structures", "CS201"], ["Database Management Systems", "CS202"], ["Discrete Mathematics", "MA201"],
    ["Object Oriented Programming", "CS203"], ["Computer Organization", "CS204"], ["Digital Electronics", "EC201"],
    ["Network Theory", "EC202"], ["Operating Systems", "CS301"], ["Computer Networks", "CS302"],
    ["Engineering Mechanics", "ME101"], ["Engineering Drawing", "ME102"]];
  const db = {
    seq: { user: 6, item: 0, req: 0, rating: 0, report: 0, wish: 0, log: 0, bundle: 0, rental: 0, booking: 0, bill: 0, flag: 0, act: 0, note: 0 },
    settings: { farewell_banner: false },
    institute: inst.map((x, i) => ({ institute_id: i + 1, institute_code: x[0], institute_name: x[1] })),
    branch: [{ branch_id: 1, branch_name: "Computer Science and Engineering", branch_code: "CSE" }, { branch_id: 6, branch_name: "Artificial Intelligence and Machine Learning", branch_code: "AIML" }, { branch_id: 7, branch_name: "Civil Engineering", branch_code: "CIVIL" }, { branch_id: 3, branch_name: "Mechanical Engineering", branch_code: "MECH" }, { branch_id: 10, branch_name: "Robotics and Automation", branch_code: "RNA" }, { branch_id: 2, branch_name: "Electronics and Telecommunication", branch_code: "ENTC" }, { branch_id: 4, branch_name: "All branches", branch_code: "ALL" }],
    category: cats.map(c => ({ category_id: c[0], category_name: c[1], is_academic: !!c[2], is_rentable: !!c[3], parent_category_id: null })),
    subject: subj.map((s, i) => ({ subject_id: i + 1, subject_name: s[0], subject_code: s[1] })),
    branch_subject: [[4, 1, 1], [4, 2, 1], [1, 3, 1], [2, 3, 1], [3, 13, 1], [1, 4, 3], [1, 5, 3], [1, 6, 3], [1, 7, 3], [1, 8, 3],
      [1, 11, 5], [1, 12, 5], [2, 9, 3], [2, 10, 3], [2, 6, 3], [6, 2, 3], [6, 3, 3]].map(x => ({ branch_id: x[0], subject_id: x[1], semester: x[2] })),
    users: [], user_phone: [], student: [], admin: [{ user_id: 1, staff_code: "ADM01", admin_level: "SUPER" }],
    item: [], academic_item: [], general_item: [], item_image: [], item_status_log: [],
    wishlist: [], wishlist_match: [], buy_request: [], rating: [], report: [], bill: [],
    bundle: [], rental_item: [], rental_image: [], rental_booking: [], fraud_flag: [], admin_activity: [], notification: []
  };
  const user = (id, f, l, role, phones) => { db.users.push({ user_id: id, first_name: f, last_name: l, email: f.toLowerCase() + "@sitpune.edu.in", password: role === "ADMIN" ? "admin123" : "pass1234", role, is_active: true, registered_on: ago(60) }); phones.forEach(p => db.user_phone.push({ user_id: id, phone_no: p })); };
  user(1, "Admin", "Moderator", "ADMIN", ["9000000001"]);
  user(2, "Aarav", "Kulkarni", "STUDENT", ["9000000002"]);
  user(3, "Meera", "Joshi", "STUDENT", ["9000000003"]);
  user(4, "Kabir", "Shah", "STUDENT", ["9000000004"]);
  user(5, "Sana", "Pathan", "STUDENT", ["9000000005"]);
  user(6, "Rohit", "Deshmukh", "STUDENT", ["9000000006"]);
  const stu = (id, prn, gender, age, sem, year, br, status, method) => db.student.push({ user_id: id, college_id: prn, name_on_id: db.users[id - 1].first_name + " " + db.users[id - 1].last_name, gender, age, semester: sem, year, branch_id: br, institute_id: 1, verification_status: status, verification_method: method, id_card_path: "private/idcards/" + id + ".jpg", verified_on: status === "VERIFIED" ? ago(50) : null, verified_by: null, remarks: "" });
  stu(2, "22070122001", "Male", 21, 7, 4, 1, "VERIFIED", "ID_CARD_AUTO");
  stu(3, "24070122014", "Female", 19, 3, 2, 1, "VERIFIED", "ID_CARD_AUTO");
  stu(4, "23070123020", "Male", 20, 5, 3, 2, "VERIFIED", "ID_CARD_AUTO");
  stu(5, "25070122099", "Female", 18, 1, 1, 1, "PENDING", null);
  stu(6, "24070123045", "Male", 19, 4, 2, 2, "VERIFIED", "ADMIN");

  const log = (it, oldS, newS, by, remark, daysAgo) => db.item_status_log.push({ item_id: it, log_no: ++db.seq.log, old_status: oldS, new_status: newS, changed_at: ago(daysAgo), changed_by: by, remark });
  const addItem = (seller, cat, name, orig, sell, cond, loc, desc, listedAgo, ac, gn, status = "AVAILABLE") => {
    const id = ++db.seq.item, c = db.category.find(x => x.category_id === cat);
    db.item.push({ item_id: id, item_name: name, description: desc, original_price: orig, selling_price: sell, item_condition: cond, pickup_location: loc, status, item_kind: c.is_academic ? "ACADEMIC" : "GENERAL", listed_on: ago(listedAgo), seller_id: seller, category_id: cat, bundle_id: null, last_price_drop_on: null });
    if (ac) db.academic_item.push({ item_id: id, subject_id: ac[0], branch_id: ac[1], target_semester: ac[2], edition: ac[3] });
    if (gn) db.general_item.push({ item_id: id, brand: gn[0], age_in_months: gn[1] });
    for (let n = 1; n <= 3; n++) db.item_image.push({ item_id: id, image_no: n, image_path: `ph:${cat}:${n}:${id}`, is_primary: n === 1 });
    log(id, null, "AVAILABLE", seller, "Listed", listedAgo);
    return id;
  };
  addItem(2, 1, "DBMS Concepts (Silberschatz)", 650, 300, "GOOD", "Library", "Clean copy with light pencil highlighting in the first five chapters. All pages intact.", 20, [5, 1, 3, "6th edition"]);
  addItem(2, 1, "Data Structures Using C", 500, 200, "FAIR", "Canteen", "Cover is worn but every page is readable. Some notes written in the margins of the tree chapters.", 20, [4, 1, 3, "2nd edition"]);
  addItem(2, 2, "Discrete Mathematics Handwritten Notes", 150, 60, "GOOD", "Library", "Neat handwritten notes for the full semester, includes solved examples for every unit.", 15, [6, 1, 3, "2025 notes"]);
  addItem(2, 3, "DBMS Previous Year Papers 2019-2025", 100, 40, "GOOD", "Library", "Printed set of seven years of university papers with a few solved answers by the seller.", 15, [5, 1, 3, "2025 set"]);
  addItem(2, 4, "Casio fx-991EX Calculator", 1200, 700, "GOOD", "Hostel Gate", "Works perfectly, comes with the cover. Allowed in all internal and university exams.", 12, [1, 4, 1, "ClassWiz"]);
  addItem(4, 5, "Arduino Uno Starter Kit", 1800, 900, "GOOD", "Department Lobby", "Uno R3 board, breadboard, jumper wires, LEDs, resistors and a USB cable. Used for one semester.", 9, [9, 2, 3, "R3 kit"]);
  addItem(4, 5, "Digital Multimeter DT830", 700, 350, "GOOD", "Department Lobby", "Reads voltage, current and resistance. New probes, battery included. Tested before listing.", 9, [9, 2, 3, "DT830"]);
  addItem(4, 6, "Lab Coat (size M)", 400, 150, "GOOD", "Hostel Gate", "White cotton lab coat, washed and ironed. Fits chest size 38 to 40 inches.", 8, [2, 4, 1, "Size M"]);
  addItem(4, 6, "Mechanical Mini Drafter", 600, 250, "FAIR", "Library", "Smooth sliding arm and clear scale. Minor scratches on the board, still accurate for drawing sheets.", 5, [13, 3, 1, "Mini drafter"]);
  addItem(4, 8, "Hero Sprint Cycle", 6000, 2500, "FAIR", "Hostel Block B", "Two years old with new tyres and a working bell. Gears need light oil but ride well.", 14, null, ["Hero", 24]);
  addItem(2, 10, "Foldable Study Table", 1800, 900, "GOOD", "Hostel Block A", "Sturdy foldable table that fits a hostel room, supports a laptop and books easily.", 4, null, ["Nilkamal", 10]);
  addItem(4, 11, "4-Socket Extension Board", 500, 200, "GOOD", "Hostel Block A", "Anchor extension board with 2 metre cable and surge switch. No loose pins, tested for load.", 3, null, ["Anchor", 18]);
  addItem(2, 9, "Formal Blazer (navy, size 40)", 3500, 1200, "GOOD", "Hostel Gate", "Navy blazer worn for two placement interviews, dry cleaned and kept in a cover.", 2, null, ["Raymond", 14]);
  addItem(2, 1, "Engineering Mathematics I", 700, 280, "GOOD", "Library", "Standard first-year textbook with all chapters, solved examples marked in pencil only.", 18, [1, 4, 1, "9th edition"]);
  addItem(4, 1, "Operating System Concepts", 800, 350, "GOOD", "Library", "Dinosaur book in good shape, no torn pages. Useful for the third year operating systems course.", 7, [11, 1, 5, "10th edition"]);
  addItem(4, 1, "Java: The Complete Reference", 900, 400, "GOOD", "Library", "Covers core Java and OOP concepts with chapter-wise exercises. Spine is intact and pages are clean.", 6, [7, 1, 3, "12th edition"]);
  addItem(2, 1, "Engineering Physics Lab Manual", 200, 80, "GOOD", "Library", "Completed journal-style manual for first year physics practicals, all experiments written.", 25, [2, 4, 1, "2024 manual"], null, "SOLD");
  addItem(4, 4, "Texas Instruments TI-30XS", 1500, 600, "GOOD", "Hostel Gate", "Scientific calculator with a protective slide case, display is clear and keys respond well.", 22, [1, 4, 1, "TI-30XS"], null, "SOLD");

  db.bundle.push({ bundle_id: ++db.seq.bundle, bundle_name: "Semester 3 CSE Kit", discount_percent: BUNDLE_DISCOUNT, status: "AVAILABLE", created_on: ago(6), seller_id: 2 });
  [1, 3, 4].forEach(i => { db.item.find(x => x.item_id === i).bundle_id = 1; });

  const req = (o) => { const r = Object.assign({ request_id: ++db.seq.req, request_type: "BUY", message: "", offered_price: null, counter_price: null, counter_accepted: false, agreed_price: null, offered_item_id: null, item_id: null, bundle_id: null, status: "PENDING", decided_on: null, handover_location: null, handover_time: null, requested_on: ago(1), expires_on: addDays(today(), REQUEST_DAYS) }, o); db.buy_request.push(r); return r; };
  req({ buyer_id: 3, item_id: 6, message: "Is the kit complete?" });
  db.item.find(x => x.item_id === 6).status = "BUY_REQUESTED"; log(6, "AVAILABLE", "BUY_REQUESTED", 3, "Buy request received", 1);
  req({ buyer_id: 3, item_id: 11, request_type: "OFFER", offered_price: 700, message: "Can you do 700?" });
  db.item.find(x => x.item_id === 11).status = "BUY_REQUESTED"; log(11, "AVAILABLE", "BUY_REQUESTED", 3, "Buy request received", 1);
  req({ buyer_id: 3, item_id: 14, status: "ACCEPTED", agreed_price: 280, requested_on: ago(2), decided_on: ago(1.75), handover_location: "Library", handover_time: addDays(today(), 1) + " 11:00:00" });
  db.item.find(x => x.item_id === 14).status = "ACCEPTED"; log(14, "AVAILABLE", "BUY_REQUESTED", 3, "Buy request received", 2); log(14, "BUY_REQUESTED", "ACCEPTED", 2, "Request accepted", 1.75);
  const sold = (itemId, buyer, price, daysAgo, stars, review, replyHrs) => {
    const rq = req({ buyer_id: buyer, item_id: itemId, status: "ACCEPTED", agreed_price: price, requested_on: ago(daysAgo + 1), decided_on: ago(daysAgo + 1 - replyHrs / 24), handover_location: "Library", handover_time: ago(daysAgo) });
    log(itemId, "AVAILABLE", "BUY_REQUESTED", buyer, "Buy request received", daysAgo + 1); log(itemId, "BUY_REQUESTED", "ACCEPTED", null, "Request accepted", daysAgo + 0.5); log(itemId, "ACCEPTED", "SOLD", null, "Handover pass verified", daysAgo);
    db.bill.push({ bill_id: ++db.seq.bill, bill_no: "CK-2026-" + String(db.seq.bill).padStart(4, "0"), issued_on: ago(daysAgo), amount: price, deposit_amount: null, pdf_path: "bills/" + db.seq.bill + ".pdf", buyer_emailed_on: null, seller_emailed_on: null, request_id: rq.request_id, booking_id: null });
    db.rating.push({ rating_id: ++db.seq.rating, request_id: rq.request_id, stars, review, rated_on: ago(daysAgo - 0.2) });
  };
  sold(16, 3, 80, 20, 5, "Smooth handover, exactly as described.", 5);
  sold(17, 2, 600, 18, 4, "Good calculator, seller replied fast.", 9);

  const wish = (sid, subjectId, categoryId, kw, max) => db.wishlist.push({ wish_id: ++db.seq.wish, student_id: sid, subject_id: subjectId, category_id: categoryId, keyword: kw, max_price: max, created_on: today(), is_active: true });
  wish(3, 5, null, null, null);
  wish(3, null, 4, "calculator", 800);
  db.wishlist_match.push({ wish_id: 1, item_id: 1, matched_on: ago(1), is_seen: false }, { wish_id: 1, item_id: 4, matched_on: ago(1), is_seen: false }, { wish_id: 2, item_id: 5, matched_on: ago(1), is_seen: false });

  const rent = (owner, cat, name, cond, rate, dep, maxd, loc, desc) => {
    const id = ++db.seq.rental;
    db.rental_item.push({ rental_item_id: id, rental_name: name, description: desc, item_condition: cond, rate_per_day: rate, deposit_amount: dep, max_days: maxd, pickup_location: loc, status: "AVAILABLE", listed_on: ago(10), owner_id: owner, category_id: cat });
    for (let n = 1; n <= 3; n++) db.rental_image.push({ rental_item_id: id, image_no: n, image_path: `ph:${cat}:${n}:${id}`, is_primary: n === 1 });
    return id;
  };
  rent(2, 5, "Raspberry Pi 4 Kit", "GOOD", 40, 800, 7, "Department Lobby", "Pi 4 board with case, power supply, 32 GB card and HDMI cable. Ideal for a mini project demo.");
  rent(4, 4, "Scientific Calculator fx-82", "GOOD", 10, 300, 10, "Hostel Gate", "Reliable exam calculator, fresh batteries. Handy for the exam week when you do not want to buy one.");
  rent(2, 7, "Trekking Backpack 50L", "GOOD", 60, 500, 5, "Hostel Block B", "50 litre rucksack with rain cover and padded straps. Clean and ready for a weekend trek.");
  rent(4, 5, "Soldering Iron Kit", "FAIR", 20, 400, 5, "Department Lobby", "40 W iron with stand, solder wire and a sponge. Use in the lab or hostel for small circuit work.");
  const bk = (rid, renter, s, e, status, extra) => db.rental_booking.push(Object.assign({ booking_id: ++db.seq.booking, rental_item_id: rid, renter_id: renter, start_date: s, end_date: e, requested_on: ago(3), status, handed_over_on: null, returned_on: null, damage_note: null, late_notified: false }, extra || {}));
  bk(1, 3, addDays(today(), 3), addDays(today(), 5), "ACCEPTED");
  bk(4, 3, addDays(today(), -5), addDays(today(), -2), "HANDED_OVER", { handed_over_on: ago(5) });
  db.rental_item.find(x => x.rental_item_id === 4).status = "ON_RENT";
  bk(3, 4, addDays(today(), -12), addDays(today(), -9), "RETURNED", { handed_over_on: ago(12), returned_on: ago(9), damage_note: "Zip slightly stuck, otherwise fine." });
  db.bill.push({ bill_id: ++db.seq.bill, bill_no: "CK-2026-" + String(db.seq.bill).padStart(4, "0"), issued_on: ago(9), amount: 240, deposit_amount: 500, pdf_path: "bills/" + db.seq.bill + ".pdf", buyer_emailed_on: null, seller_emailed_on: null, request_id: null, booking_id: 3 });
  bk(2, 3, addDays(today(), 7), addDays(today(), 9), "PENDING");

  db.report.push({ report_id: ++db.seq.report, reporter_id: 3, item_id: 12, reason: "WRONG_INFO", details: "Cable looks shorter than the listing says.", reported_on: ago(1), status: "OPEN", resolved_by: null, resolved_on: null, action_taken: null });
  db.fraud_flag.push({ flag_id: ++db.seq.flag, student_id: 6, reason: "MANY_REJECTIONS", raised_on: ago(2), status: "OPEN", reviewed_by: null, reviewed_on: null, note: null });
  db.admin_activity.push({ admin_id: 1, log_no: ++db.seq.act, action: "VERIFY", target_type: "STUDENT", target_id: "6", performed_at: ago(30), remark: "Checked ID card by hand" });
  const note = (uid, type, text, link, daysAgo) => db.notification.push({ notification_id: ++db.seq.note, user_id: uid, type, text, link, created_on: ago(daysAgo) });
  note(3, "ACCEPTED", "Aarav accepted your request for Engineering Mathematics I. Meet at Library.", "#/requests", 1);
  note(3, "MATCH", "New match for your wish: Casio fx-991EX Calculator.", "#/item/5", 1);
  note(3, "RENTAL_LATE", "Soldering Iron Kit was due back two days ago.", "#/my-rentals", 0.5);
  return db;
}

function load() { try { const d = JSON.parse(localStorage.getItem(DB_KEY)); if (d && d.seq) return d; } catch (e) { } const d = seed(); save(d); return d; }
function save(d) { try { localStorage.setItem(DB_KEY, JSON.stringify(d)); } catch (e) { } }
function resetMock() { try { localStorage.removeItem(DB_KEY); localStorage.removeItem(SESSION_KEY); } catch (e) { } }
function session() {
  try {
    const s = JSON.parse(localStorage.getItem(SESSION_KEY));
    if (!s) return null;
    if (s.expires && Date.now() > s.expires) { localStorage.removeItem(SESSION_KEY); localStorage.setItem("ck_expired", "1"); return null; }
    return s;
  } catch (e) { return null; }
}

const PENDING_LIKE = ["PENDING", "COUNTERED"];
const allBranchId = db => db.branch.find(b => b.branch_code === "ALL").branch_id;
const userName = (db, id) => { const u = db.users.find(x => x.user_id === id); return u ? u.first_name + " " + u.last_name : "-"; };
const studentOf = (db, id) => db.student.find(s => s.user_id === id);
const itemOf = (db, id) => db.item.find(i => i.item_id === +id);
const bundleOf = (db, id) => db.bundle.find(b => b.bundle_id === +id);
const rentalOf = (db, id) => db.rental_item.find(r => r.rental_item_id === +id);
const catOf = (db, id) => db.category.find(c => c.category_id === id);
const imagesOf = (db, id) => db.item_image.filter(i => i.item_id === id).sort((a, b) => a.image_no - b.image_no);
const moneyRound = n => Math.max(5, Math.round(n / 5) * 5);

function notify(db, uid, type, text, link) { db.notification.push({ notification_id: ++db.seq.note, user_id: uid, type, text, link: link || "#/", created_on: now() }); }
function logStatus(db, item, newStatus, by, remark) {
  db.item_status_log.push({ item_id: item.item_id, log_no: ++db.seq.log, old_status: item.status, new_status: newStatus, changed_at: now(), changed_by: by, remark: remark || "" });
  item.status = newStatus;
}
function logAdmin(db, adminId, action, targetType, targetId, remark) {
  db.admin_activity.push({ admin_id: adminId, log_no: ++db.seq.act, action, target_type: targetType, target_id: String(targetId), performed_at: now(), remark: remark || "" });
}
function passFor(r) {
  let h = r.request_id * 31 + PASS_SECRET;
  for (const ch of String(r.handover_time || "")) h = (h * 131 + ch.charCodeAt(0)) % 1000003;
  return String(1000 + (h % 9000));
}
function fairPrice(p) {
  const f = { NEW: 0.85, GOOD: 0.65, FAIR: 0.45, POOR: 0.3 }[p.item_condition] || 0.5;
  const op = +p.original_price || 0;
  const s = op * f * (1 - Math.min(+p.age_in_months || 0, 48) * 0.004);
  const suggested = Math.min(op, moneyRound(s)), low = Math.min(suggested, moneyRound(s * 0.85)), high = Math.min(op, moneyRound(s * 1.15));
  return op > 0 ? { low, suggested, high } : { low: 0, suggested: 0, high: 0 };
}
const targetOf = (db, r) => r.bundle_id ? { kind: "BUNDLE", id: r.bundle_id, name: bundleOf(db, r.bundle_id).bundle_name } : { kind: "ITEM", id: r.item_id, name: itemOf(db, r.item_id).item_name };
function bundleItems(db, id) { return db.item.filter(i => i.bundle_id === id); }
function bundlePrices(db, b) {
  const total = bundleItems(db, b.bundle_id).reduce((a, i) => a + i.selling_price, 0);
  return { total_price: total, bundle_price: Math.round(total * (1 - b.discount_percent / 100)) };
}
function releaseTarget(db, r, remark, by) {
  if (r.item_id) {
    const it = itemOf(db, r.item_id);
    if (it.status === "BUY_REQUESTED" && !db.buy_request.some(x => x.item_id === it.item_id && PENDING_LIKE.includes(x.status))) logStatus(db, it, "AVAILABLE", by || null, remark);
  } else {
    const b = bundleOf(db, r.bundle_id);
    if (b.status === "REQUESTED" && !db.buy_request.some(x => x.bundle_id === b.bundle_id && [...PENDING_LIKE, "ACCEPTED"].includes(x.status))) b.status = "AVAILABLE";
  }
}
function sweep(db) {
  const t = today(); let dirty = false;
  db.buy_request.forEach(r => {
    if (PENDING_LIKE.includes(r.status) && r.expires_on < t) {
      r.status = "EXPIRED"; dirty = true;
      releaseTarget(db, r, "Request expired");
      notify(db, r.buyer_id, "EXPIRED", "Your request for " + targetOf(db, r).name + " expired.", "#/requests");
    }
  });
  db.rental_booking.forEach(b => {
    if (b.status === "HANDED_OVER" && b.end_date < t && !b.late_notified) {
      b.late_notified = true; dirty = true;
      const ri = rentalOf(db, b.rental_item_id);
      notify(db, b.renter_id, "RENTAL_LATE", ri.rental_name + " was due back on " + b.end_date + ". Please return it.", "#/my-rentals");
      notify(db, ri.owner_id, "RENTAL_LATE", userName(db, b.renter_id) + " has not returned " + ri.rental_name + " yet.", "#/my-rentals");
    }
  });
  if (dirty) save(db);
}
function needUser() { const s = session(); if (!s) fail("Your session expired. Please log in again."); return s; }
function needStudent(db, verified = true) {
  const s = needUser(), st = studentOf(db, s.user_id);
  if (!st) fail("Students only.");
  if (verified && st.verification_status !== "VERIFIED") fail("Only verified students can do this. Your ID card must be verified first.");
  return { s, st };
}
function needAdmin() { const s = needUser(); if (s.role !== "ADMIN") fail("Admins only."); return s; }

function subjectIdsFor(db, st) {
  const all = allBranchId(db);
  return db.branch_subject.filter(x => (x.branch_id === st.branch_id || x.branch_id === all) && x.semester === st.semester).map(x => x.subject_id);
}
function wishMatches(db, item, w) {
  const ac = db.academic_item.find(a => a.item_id === item.item_id);
  if (!w.is_active || w.student_id === item.seller_id) return false;
  if (w.subject_id && !(ac && ac.subject_id === w.subject_id)) return false;
  if (w.category_id && item.category_id !== w.category_id) return false;
  if (w.keyword && !(item.item_name + " " + item.description).toLowerCase().includes(w.keyword.toLowerCase())) return false;
  if (w.max_price && item.selling_price > w.max_price) return false;
  return true;
}
function relevance(db, item, st) {
  const ac = db.academic_item.find(a => a.item_id === item.item_id);
  if (!st || !ac) return null;
  const why = []; let score = 0;
  if (ac.branch_id === st.branch_id || ac.branch_id === allBranchId(db)) { score += 30; why.push("Your branch"); }
  if (ac.target_semester === st.semester) { score += 30; why.push("Your semester"); }
  if (ac.subject_id && subjectIdsFor(db, st).includes(ac.subject_id)) { score += 25; why.push("Subject you study"); }
  if (db.wishlist.some(w => w.student_id === st.user_id && wishMatches(db, item, w))) { score += 15; why.push("On your wishlist"); }
  const label = score >= 80 ? "Highly relevant" : score >= 60 ? "Relevant" : score >= 40 ? "Possibly relevant" : "Low";
  return { score, label, why };
}
function sellerStats(db, id) {
  const st = studentOf(db, id);
  const deals = db.buy_request.filter(r => r.status === "ACCEPTED" && (r.item_id ? (itemOf(db, r.item_id).seller_id === id && itemOf(db, r.item_id).status === "SOLD") : (bundleOf(db, r.bundle_id).seller_id === id && bundleOf(db, r.bundle_id).status === "SOLD")));
  const rs = db.rating.filter(x => deals.some(d => d.request_id === x.request_id));
  const decided = db.buy_request.filter(r => r.decided_on && (r.item_id ? itemOf(db, r.item_id).seller_id === id : bundleOf(db, r.bundle_id).seller_id === id));
  const reply = decided.length ? decided.reduce((a, r) => a + hoursBetween(r.requested_on, r.decided_on), 0) / decided.length : null;
  return { user_id: id, name: userName(db, id), semester: st && st.semester, branch: st && db.branch.find(b => b.branch_id === st.branch_id).branch_code,
    institute: st && db.institute.find(i => i.institute_id === st.institute_id).institute_code, verification_status: st && st.verification_status,
    avg_rating: rs.length ? +(rs.reduce((a, b) => a + b.stars, 0) / rs.length).toFixed(1) : null, rating_count: rs.length, sales_count: deals.length,
    reply_hours: reply == null ? null : +reply.toFixed(1), member_since: db.users.find(u => u.user_id === id).registered_on.slice(0, 10), _ratings: rs, _deals: deals };
}
function trustCard(db, id) { const s = sellerStats(db, id); delete s._ratings; delete s._deals; return s; }
function decorate(db, i, st) {
  const ac = db.academic_item.find(a => a.item_id === i.item_id), gn = db.general_item.find(a => a.item_id === i.item_id);
  const imgs = imagesOf(db, i.item_id), cover = (imgs.find(x => x.is_primary) || imgs[0] || {}).image_path;
  const age = gn ? gn.age_in_months : 0, fp = fairPrice({ original_price: i.original_price, item_condition: i.item_condition, age_in_months: age });
  const b = i.bundle_id ? bundleOf(db, i.bundle_id) : null;
  return Object.assign({}, i, ac || {}, gn || {}, {
    images: imgs.map(x => x.image_path), cover, image_count: imgs.length,
    discount_percent: +(((i.original_price - i.selling_price) / i.original_price) * 100).toFixed(0),
    category_name: catOf(db, i.category_id).category_name,
    subject_name: ac && ac.subject_id ? db.subject.find(s => s.subject_id === ac.subject_id).subject_name : null,
    branch_code: ac ? db.branch.find(x => x.branch_id === ac.branch_id).branch_code : null,
    seller: trustCard(db, i.seller_id), relevance: relevance(db, i, st), fair: fp,
    bundle: b && ["AVAILABLE", "REQUESTED"].includes(b.status) ? { bundle_id: b.bundle_id, bundle_name: b.bundle_name } : null
  });
}
function checkFraud(db, sid) {
  const open = (reason) => db.fraud_flag.some(f => f.student_id === sid && f.reason === reason && f.status === "OPEN");
  const raise = (reason) => { if (!open(reason)) { db.fraud_flag.push({ flag_id: ++db.seq.flag, student_id: sid, reason, raised_on: now(), status: "OPEN", reviewed_by: null, reviewed_on: null, note: null }); } };
  const mine = db.item.filter(i => i.seller_id === sid).map(i => i.item_id);
  if (db.report.filter(r => r.status === "OPEN" && mine.includes(r.item_id)).length >= 3) raise("REPEATED_REPORTS");
  const cut = ago(30);
  if (db.buy_request.filter(r => r.buyer_id === sid && r.status === "REJECTED" && r.requested_on >= cut).length >= 5) raise("MANY_REJECTIONS");
  if (db.item.some(i => i.seller_id === sid && i.status === "AVAILABLE" && i.selling_price > fairPrice({ original_price: i.original_price, item_condition: i.item_condition, age_in_months: (db.general_item.find(g => g.item_id === i.item_id) || {}).age_in_months || 0 }).high)) raise("PRICE_ABOVE_GUIDE");
}
function addWishMatches(db, item) {
  db.wishlist.forEach(w => {
    if (wishMatches(db, item, w) && !db.wishlist_match.some(m => m.wish_id === w.wish_id && m.item_id === item.item_id)) {
      db.wishlist_match.push({ wish_id: w.wish_id, item_id: item.item_id, matched_on: now(), is_seen: false });
      notify(db, w.student_id, "MATCH", "New match for your wish: " + item.item_name + ".", "#/item/" + item.item_id);
    }
  });
}
function validatePhotos(photos, label) {
  if (!Array.isArray(photos) || photos.length < 3 || photos.length > 5) fail(label + " needs 3 to 5 clear photos.");
}
function validateListingCore(p) {
  if (!p.item_name || !p.item_name.trim()) fail("Item name is required.");
  if (!p.description || p.description.trim().length < 30) fail("Description must be at least 30 characters.");
  if (p.item_name.trim().length > 60) fail("Item name can be at most 60 characters.");
  if (p.description.trim().length > 200) fail("Description can be at most 200 characters.");
  if (!p.pickup_location) fail("Choose a pickup place.");
  if (!["NEW", "GOOD", "FAIR", "POOR"].includes(p.item_condition)) fail("Choose the condition.");
}

const LOCATIONS = ["Library", "Hostel Gate", "Canteen", "Main Gate", "Hostel Block A", "Hostel Block B", "Department Lobby"];
const EMAIL_OK = e => /\.(edu\.in|ac\.in)$/i.test(e);
const sessionFor = (db, u) => {
  const st = studentOf(db, u.user_id);
  return { user_id: u.user_id, name: u.first_name + " " + u.last_name, role: u.role, verification_status: st ? st.verification_status : null, remarks: st ? st.remarks : "", expires: Date.now() + SESSION_HOURS * 36e5 };
};
const toDT = v => { if (!v) return null; const s = String(v).replace("T", " "); return s.length === 16 ? s + ":00" : s; };

const H = {
  meta() { const d = load(); return { branches: d.branch, categories: d.category, subjects: d.subject, institutes: d.institute, locations: LOCATIONS, settings: d.settings }; },
  settings() { return load().settings; },

  login({ email, password }) {
    const d = load(); sweep(d);
    const u = d.users.find(x => x.email.toLowerCase() === String(email).toLowerCase() && x.password === password);
    if (!u) fail("Wrong email or password.");
    if (!u.is_active) fail("Your account is blocked. Please contact the admin.");
    const s = sessionFor(d, u); localStorage.setItem(SESSION_KEY, JSON.stringify(s)); localStorage.removeItem("ck_expired"); return s;
  },
  register(p) {
    const d = load();
    const full = String(p.full_name || "").trim().replace(/\s+/g, " ");
    if (full.split(" ").length < 2) fail("Enter your full name exactly as on your ID card.");
    if (!EMAIL_OK(p.email || "")) fail("Use your college email ending in .edu.in or .ac.in");
    if (d.users.some(u => u.email.toLowerCase() === p.email.toLowerCase())) fail("This email is already registered.");
    if (d.student.some(s => s.college_id === p.college_id)) fail("This college ID / PRN is already registered.");
    if (!/^\d{10}$/.test(p.phone_no || "")) fail("Phone number must be 10 digits.");
    if ((p.password || "").length < 8) fail("Password must be at least 8 characters.");
    const age = +p.age; if (!(age >= 15 && age <= 60)) fail("Age must be between 15 and 60.");
    if (!["Female", "Male", "Other"].includes(p.gender)) fail("Select your gender.");
    const year = +p.year, sem = +p.semester;
    if (!(year >= 1 && year <= 5)) fail("Year must be 1 to 5.");
    if (![2 * year - 1, 2 * year].includes(sem)) fail("Semester " + sem + " does not belong to year " + year + ".");
    if (!p.institute_id || !p.branch_id) fail("Select your college and branch.");
    if (!p.id_card) fail("Upload a photo of your ID card.");
    const norm = s => String(s || "").toUpperCase().replace(/[^A-Z0-9 ]+/g, " ").replace(/\s+/g, " ").trim();
    const cardText = norm(p.card_text), cardCompact = cardText.replace(/ /g, ""), nameN = norm(full), words = nameN.split(" ");
    const problems = [], checked = [];
    if (cardText.length < 12) problems.push("The card text could not be read clearly");
    else {
      const padded = " " + cardText + " ";
      if (padded.includes(" " + nameN + " ")) checked.push("name (word for word)");
      else if (words.every(w => padded.includes(" " + w + " "))) problems.push("The name order differs from the card");
      else problems.push("The name does not match the card");
      if (cardCompact.includes(norm(p.college_id).replace(/ /g, ""))) checked.push("college ID number"); else problems.push("The college ID number was not found on the card");
    }
    const auto = problems.length === 0;
    const id = ++d.seq.user, parts = full.split(" ");
    d.users.push({ user_id: id, first_name: parts[0], last_name: parts.slice(1).join(" "), email: p.email, password: p.password, role: "STUDENT", is_active: true, registered_on: now() });
    d.user_phone.push({ user_id: id, phone_no: p.phone_no });
    d.student.push({ user_id: id, college_id: p.college_id, name_on_id: full, gender: p.gender, age, semester: sem, year, branch_id: +p.branch_id, institute_id: +p.institute_id,
      verification_status: auto ? "VERIFIED" : "PENDING", verification_method: auto ? "ID_CARD_AUTO" : null, id_card_path: "private/idcards/" + id + ".jpg", verified_on: auto ? now() : null, verified_by: null, remarks: auto ? "" : "Flagged by the automatic card check: " + problems.join("; ") + "." });
    notify(d, id, "VERIFICATION", auto ? "Your ID card matched. You are verified and can start trading." : "Your ID card check was flagged. An admin will review it.", "#/");
    save(d);
    return auto ? { verified: true, checked, message: "Your ID card matched." } : { verified: false, flagged: true, problems, checked, message: "Your details did not fully match the ID card, so your account was flagged for a manual check by an admin." };
  },
  logout() { localStorage.removeItem(SESSION_KEY); return { ok: true }; },
  profile() {
    const d = load(), s = needUser(), u = d.users.find(x => x.user_id === s.user_id), st = studentOf(d, s.user_id);
    return { user_id: u.user_id, first_name: u.first_name, last_name: u.last_name, email: u.email, role: u.role, phones: d.user_phone.filter(p => p.user_id === u.user_id).map(p => p.phone_no),
      student: st ? Object.assign({}, st, { branch_code: d.branch.find(b => b.branch_id === st.branch_id).branch_code, institute_code: d.institute.find(i => i.institute_id === st.institute_id).institute_code }) : null };
  },
  updateProfile({ first_name, last_name, semester }) {
    const d = load(), { s, st } = needStudent(d, false), u = d.users.find(x => x.user_id === s.user_id);
    if (!first_name || !last_name) fail("Name cannot be empty.");
    const sem = +semester; if (!(sem >= 1 && sem <= 8)) fail("Semester must be 1 to 8.");
    u.first_name = first_name.trim(); u.last_name = last_name.trim(); st.semester = sem; st.year = Math.ceil(sem / 2);
    const cur = JSON.parse(localStorage.getItem(SESSION_KEY)); cur.name = u.first_name + " " + u.last_name; localStorage.setItem(SESSION_KEY, JSON.stringify(cur));
    save(d); return { ok: true };
  },
  addPhone({ phone_no }) {
    const d = load(), s = needUser();
    if (!/^\d{10}$/.test(phone_no || "")) fail("Phone number must be 10 digits.");
    if (d.user_phone.some(p => p.user_id === s.user_id && p.phone_no === phone_no)) fail("You already added this number.");
    d.user_phone.push({ user_id: s.user_id, phone_no }); save(d); return { ok: true };
  },
  removePhone({ phone_no }) {
    const d = load(), s = needUser(), mine = d.user_phone.filter(p => p.user_id === s.user_id);
    if (mine.length <= 1) fail("At least one phone number must stay.");
    d.user_phone = d.user_phone.filter(p => !(p.user_id === s.user_id && p.phone_no === phone_no)); save(d); return { ok: true };
  },
  changePassword({ old_password, new_password }) {
    const d = load(), s = needUser(), u = d.users.find(x => x.user_id === s.user_id);
    if (u.password !== old_password) fail("Current password is wrong.");
    if ((new_password || "").length < 8) fail("New password must be at least 8 characters.");
    u.password = new_password; save(d); return { ok: true };
  },

  home() {
    const d = load(); sweep(d); const s = session(), st = s && studentOf(d, s.user_id);
    const live = d.item.filter(i => ["AVAILABLE", "BUY_REQUESTED"].includes(i.status)).sort((a, b) => b.listed_on.localeCompare(a.listed_on));
    const out = { just_listed: live.slice(0, 6).map(i => decorate(d, i, st)), stats: { items: live.length, bundles: d.bundle.filter(b => b.status === "AVAILABLE").length, rentals: d.rental_item.filter(r => r.status === "AVAILABLE").length } };
    if (st) {
      const kit = H.semesterKit();
      out.kit = { covered: kit.covered, total: kit.total };
      const mine = d.wishlist.filter(w => w.student_id === st.user_id && w.is_active);
      out.wishes = { count: mine.length, new_matches: d.wishlist_match.filter(m => !m.is_seen && mine.some(w => w.wish_id === m.wish_id)).length };
      out.picked = live.filter(i => i.seller_id !== st.user_id).map(i => decorate(d, i, st)).filter(i => i.relevance && i.relevance.score >= 40).sort((a, b) => b.relevance.score - a.relevance.score).slice(0, 8);
    }
    return out;
  },

  listItems(f) {
    const d = load(); sweep(d); const s = session(), st = s && studentOf(d, s.user_id);
    let r = d.item.filter(i => ["AVAILABLE", "BUY_REQUESTED"].includes(i.status)).map(i => decorate(d, i, st));
    if (f.q) r = r.filter(i => (i.item_name + " " + i.description + " " + (i.subject_name || "") + " " + (i.brand || "")).toLowerCase().includes(f.q.toLowerCase()));
    if (f.kind) r = r.filter(i => i.item_kind === f.kind);
    if (f.category_id) r = r.filter(i => i.category_id === +f.category_id);
    if (f.branch_id) r = r.filter(i => i.item_kind === "ACADEMIC" && (i.branch_id === +f.branch_id || i.branch_id === allBranchId(d)));
    if (f.semester) r = r.filter(i => i.item_kind === "ACADEMIC" && i.target_semester === +f.semester);
    if (f.condition) r = r.filter(i => i.item_condition === f.condition);
    if (f.subject_id) r = r.filter(i => i.subject_id === +f.subject_id);
    if (f.min_price) r = r.filter(i => i.selling_price >= +f.min_price);
    if (f.max_price) r = r.filter(i => i.selling_price <= +f.max_price);
    if (f.pickup) r = r.filter(i => i.pickup_location === f.pickup);
    const byNew = (a, b) => b.listed_on.localeCompare(a.listed_on) || b.item_id - a.item_id;
    if (f.sort === "price_asc") r.sort((a, b) => a.selling_price - b.selling_price);
    else if (f.sort === "price_desc") r.sort((a, b) => b.selling_price - a.selling_price);
    else if (f.sort === "new" || !st) r.sort(byNew);
    else r.sort((a, b) => ((b.relevance ? b.relevance.score : -1) - (a.relevance ? a.relevance.score : -1)) || byNew(a, b));
    return pageOf(r, f.page, 12);
  },
  getItem({ item_id }) {
    const d = load(); sweep(d); const s = session(), st = s && studentOf(d, s.user_id), it = itemOf(d, item_id);
    if (!it) fail("Item not found.");
    if (st) { d.wishlist_match.forEach(m => { if (m.item_id === it.item_id && d.wishlist.some(w => w.wish_id === m.wish_id && w.student_id === st.user_id)) m.is_seen = true; }); save(d); }
    const mine = s ? d.buy_request.filter(r => r.item_id === it.item_id && r.buyer_id === s.user_id).slice(-1)[0] || null : null;
    return { item: decorate(d, it, st), log: d.item_status_log.filter(l => l.item_id === it.item_id), my_request: mine && Object.assign({}, mine, { pass: mine.status === "ACCEPTED" && it.status === "ACCEPTED" ? passFor(mine) : null }),
      swap_choices: s ? d.item.filter(i => i.seller_id === s.user_id && i.status === "AVAILABLE" && !i.bundle_id && i.item_id !== it.item_id).map(i => ({ item_id: i.item_id, item_name: i.item_name, selling_price: i.selling_price })) : [] };
  },
  fairPrice(p) { return fairPrice(p); },
  createItem(p) {
    const d = load(), { s } = needStudent(d), cat = catOf(d, +p.category_id);
    if (!cat) fail("Choose a category.");
    validateListingCore(p); validatePhotos(p.photos, "A listing");
    const op = +p.original_price, sp = +p.selling_price;
    if (!(op > 0 && sp > 0 && sp <= op)) fail("Your price must be above 0 and not above the original price.");
    if (cat.is_academic) {
      if (!p.subject_id || !p.branch_id) fail("Choose the subject and branch.");
      if (!(+p.target_semester >= 1 && +p.target_semester <= 8)) fail("Target semester must be 1 to 8.");
      if (!p.edition || !p.edition.trim()) fail("Edition or model is required.");
    } else {
      if (!p.brand || !p.brand.trim()) fail("Brand is required (write No brand if none).");
      if (p.age_in_months === "" || p.age_in_months == null || +p.age_in_months < 0) fail("Age in months is required.");
    }
    const id = ++d.seq.item;
    const it = { item_id: id, item_name: p.item_name.trim(), description: p.description.trim(), original_price: op, selling_price: sp, item_condition: p.item_condition, pickup_location: p.pickup_location, status: "AVAILABLE", item_kind: cat.is_academic ? "ACADEMIC" : "GENERAL", listed_on: now(), seller_id: s.user_id, category_id: cat.category_id, bundle_id: null, last_price_drop_on: null };
    d.item.push(it);
    if (cat.is_academic) d.academic_item.push({ item_id: id, subject_id: +p.subject_id, branch_id: +p.branch_id, target_semester: +p.target_semester, edition: p.edition.trim() });
    else d.general_item.push({ item_id: id, brand: p.brand.trim(), age_in_months: +p.age_in_months });
    p.photos.forEach((ph, n) => d.item_image.push({ item_id: id, image_no: n + 1, image_path: ph.data, is_primary: n === (+p.cover || 0) }));
    d.item_status_log.push({ item_id: id, log_no: ++d.seq.log, old_status: null, new_status: "AVAILABLE", changed_at: now(), changed_by: s.user_id, remark: "Listed" });
    addWishMatches(d, it); checkFraud(d, s.user_id); save(d); return { item_id: id };
  },
  updateItem(p) {
    const d = load(), { s } = needStudent(d), it = itemOf(d, p.item_id);
    if (!it || it.seller_id !== s.user_id) fail("Not your item.");
    if (it.status !== "AVAILABLE") fail("You can edit an item only while it is available.");
    validateListingCore(p);
    const sp = +p.selling_price; if (!(sp > 0 && sp <= it.original_price)) fail("Your price must be above 0 and not above the original price.");
    it.item_name = p.item_name.trim(); it.description = p.description.trim(); it.selling_price = sp; it.item_condition = p.item_condition; it.pickup_location = p.pickup_location;
    checkFraud(d, s.user_id); save(d); return { ok: true };
  },
  withdrawItem({ item_id }) {
    const d = load(), { s } = needStudent(d), it = itemOf(d, item_id);
    if (!it || it.seller_id !== s.user_id) fail("Not your item.");
    if (it.bundle_id && bundleOf(d, it.bundle_id).status !== "REMOVED" && bundleOf(d, it.bundle_id).status !== "SOLD") fail("This item is in a bundle. Withdraw the bundle first.");
    if (!["AVAILABLE", "BUY_REQUESTED"].includes(it.status)) fail("This item can no longer be withdrawn.");
    d.buy_request.filter(r => r.item_id === it.item_id && PENDING_LIKE.includes(r.status)).forEach(r => { r.status = "REJECTED"; r.decided_on = now(); notify(d, r.buyer_id, "REJECTED", it.item_name + " was withdrawn by the seller.", "#/requests"); });
    logStatus(d, it, "REMOVED", s.user_id, "Withdrawn by seller"); save(d); return { ok: true };
  },
  myItems() {
    const d = load(); sweep(d); const { s } = needStudent(d, false), st = studentOf(d, s.user_id);
    const items = d.item.filter(i => i.seller_id === s.user_id).sort((a, b) => b.item_id - a.item_id).map(i => Object.assign(decorate(d, i, st), { waiting: d.buy_request.filter(r => r.item_id === i.item_id && PENDING_LIKE.includes(r.status)).length }));
    const stats = sellerStats(d, s.user_id);
    const earned = stats._deals.reduce((a, r) => a + (r.agreed_price || 0), 0);
    return { items, total_earned: earned, deals: stats.sales_count };
  },
  sellerCard({ user_id }) {
    const d = load(), st = sellerStats(d, +user_id), me = session();
    const reviews = st._ratings.map(r => { const rq = d.buy_request.find(x => x.request_id === r.request_id); return { stars: r.stars, review: r.review, rated_on: r.rated_on, buyer_name: userName(d, rq.buyer_id), item_name: targetOf(d, rq).name }; }).sort((a, b) => b.rated_on.localeCompare(a.rated_on));
    const stv = me && studentOf(d, me.user_id);
    const items = d.item.filter(i => i.seller_id === +user_id && i.status === "AVAILABLE").map(i => decorate(d, i, stv));
    delete st._ratings; delete st._deals; return Object.assign(st, { reviews, items });
  },

  sendRequest(p) {
    const d = load(); sweep(d); const { s } = needStudent(d), type = p.request_type || "BUY";
    if ((p.message || "").length > 100) fail("Message can be at most 100 characters.");
    const base = { request_id: ++d.seq.req, buyer_id: s.user_id, request_type: type, message: (p.message || "").trim(), offered_price: null, counter_price: null, counter_accepted: false, agreed_price: null, offered_item_id: null, item_id: null, bundle_id: null, status: "PENDING", decided_on: null, handover_location: null, handover_time: null, requested_on: now(), expires_on: addDays(today(), REQUEST_DAYS) };
    if (p.bundle_id) {
      const b = bundleOf(d, p.bundle_id);
      if (!b || !["AVAILABLE", "REQUESTED"].includes(b.status)) fail("This bundle is no longer available.");
      if (b.seller_id === s.user_id) fail("You cannot request your own bundle.");
      if (type !== "BUY") fail("Bundles can only be requested at the bundle price.");
      if (d.buy_request.some(r => r.bundle_id === b.bundle_id && r.buyer_id === s.user_id && PENDING_LIKE.includes(r.status))) fail("You already have a waiting request for this bundle.");
      base.bundle_id = b.bundle_id; b.status = "REQUESTED"; d.buy_request.push(base);
      notify(d, b.seller_id, "REQUEST", userName(d, s.user_id) + " requested your bundle " + b.bundle_name + ".", "#/requests");
    } else {
      const it = itemOf(d, p.item_id);
      if (!it) fail("Item not found.");
      if (it.seller_id === s.user_id) fail("You cannot request your own item.");
      if (!["AVAILABLE", "BUY_REQUESTED"].includes(it.status)) fail("This item is no longer available.");
      if (it.bundle_id && ["AVAILABLE", "REQUESTED"].includes(bundleOf(d, it.bundle_id).status)) fail("This item is part of a bundle. Request the bundle instead.");
      if (d.buy_request.some(r => r.item_id === it.item_id && r.buyer_id === s.user_id && PENDING_LIKE.includes(r.status))) fail("You already have a waiting request for this item.");
      if (type === "OFFER") {
        const o = +p.offered_price; if (!(o > 0 && o <= it.selling_price)) fail("Your offer must be above 0 and not above " + it.selling_price + ".");
        base.offered_price = o;
      } else if (type === "SWAP") {
        const mine = itemOf(d, p.offered_item_id);
        if (!mine || mine.seller_id !== s.user_id || mine.status !== "AVAILABLE" || mine.item_id === it.item_id || mine.bundle_id) fail("Pick one of your own available items to swap.");
        base.offered_item_id = mine.item_id;
      } else if (type !== "BUY") fail("Unknown request type.");
      base.item_id = it.item_id; d.buy_request.push(base);
      if (it.status === "AVAILABLE") logStatus(d, it, "BUY_REQUESTED", s.user_id, type + " request received");
      notify(d, it.seller_id, "REQUEST", userName(d, s.user_id) + " sent a " + type.toLowerCase() + " request for " + it.item_name + ".", "#/requests");
    }
    save(d); return { request_id: base.request_id };
  },
  myRequests() {
    const d = load(); sweep(d); const { s } = needStudent(d, false);
    return d.buy_request.filter(r => r.buyer_id === s.user_id).sort((a, b) => b.request_id - a.request_id).map(r => {
      const t = targetOf(d, r), it = r.item_id ? itemOf(d, r.item_id) : null, b = r.bundle_id ? bundleOf(d, r.bundle_id) : null;
      const sold = it ? it.status === "SOLD" : b.status === "SOLD", sellerId = it ? it.seller_id : b.seller_id;
      const price = it ? it.selling_price : bundlePrices(d, b).bundle_price;
      const bill = d.bill.find(x => x.request_id === r.request_id);
      return Object.assign({}, r, { target: Object.assign(t, { price, cover: it ? (imagesOf(d, it.item_id)[0] || {}).image_path : null }), seller: { user_id: sellerId, name: userName(d, sellerId) },
        offered_item_name: r.offered_item_id ? itemOf(d, r.offered_item_id).item_name : null,
        pass: r.status === "ACCEPTED" && !sold ? passFor(r) : null, sold, bill_id: bill ? bill.bill_id : null, rated: d.rating.some(x => x.request_id === r.request_id) });
    });
  },
  incomingRequests() {
    const d = load(); sweep(d); const { s } = needStudent(d, false);
    const mineItems = d.item.filter(i => i.seller_id === s.user_id).map(i => i.item_id), mineBundles = d.bundle.filter(b => b.seller_id === s.user_id).map(b => b.bundle_id);
    return d.buy_request.filter(r => (r.item_id && mineItems.includes(r.item_id)) || (r.bundle_id && mineBundles.includes(r.bundle_id))).sort((a, b) => b.request_id - a.request_id).map(r => {
      const t = targetOf(d, r), it = r.item_id ? itemOf(d, r.item_id) : null, b = r.bundle_id ? bundleOf(d, r.bundle_id) : null, st = studentOf(d, r.buyer_id);
      const price = it ? it.selling_price : bundlePrices(d, b).bundle_price, sold = it ? it.status === "SOLD" : b.status === "SOLD";
      const off = r.offered_item_id ? itemOf(d, r.offered_item_id) : null;
      return Object.assign({}, r, { target: Object.assign(t, { price }), buyer_name: userName(d, r.buyer_id), buyer_semester: st.semester, buyer_id: r.buyer_id, sold,
        offered_item: off ? { item_id: off.item_id, item_name: off.item_name, selling_price: off.selling_price, status: off.status } : null,
        bill_id: (d.bill.find(x => x.request_id === r.request_id) || {}).bill_id || null });
    });
  },
  decideRequest({ request_id, decision, handover_location, handover_time, counter_price }) {
    const d = load(), { s } = needStudent(d), r = d.buy_request.find(x => x.request_id === +request_id);
    if (!r) fail("Request not found.");
    const it = r.item_id ? itemOf(d, r.item_id) : null, b = r.bundle_id ? bundleOf(d, r.bundle_id) : null, sellerId = it ? it.seller_id : b.seller_id;
    if (sellerId !== s.user_id) fail("Not your listing.");
    if (r.status !== "PENDING") fail("This request is already answered or waiting for the buyer.");
    const name = targetOf(d, r).name;
    if (decision === "ACCEPT") {
      const when = toDT(handover_time);
      if (!handover_location || !when) fail("Choose a campus place and a time.");
      if (new Date(when.replace(" ", "T")) < new Date()) fail("Handover time must be in the future.");
      if (r.request_type === "SWAP" && itemOf(d, r.offered_item_id).status !== "AVAILABLE") fail("The item offered in the swap is no longer available.");
      r.agreed_price = r.request_type === "SWAP" ? 0 : r.request_type === "OFFER" ? (r.counter_accepted ? r.counter_price : r.offered_price) : (it ? it.selling_price : bundlePrices(d, b).bundle_price);
      r.status = "ACCEPTED"; r.decided_on = now(); r.handover_location = handover_location; r.handover_time = when;
      d.buy_request.filter(x => x.request_id !== r.request_id && PENDING_LIKE.includes(x.status) && x.item_id === r.item_id && x.bundle_id === r.bundle_id).forEach(x => { x.status = "REJECTED"; x.decided_on = now(); notify(d, x.buyer_id, "REJECTED", name + " was taken by another buyer.", "#/requests"); });
      if (it) logStatus(d, it, "ACCEPTED", s.user_id, "Request " + r.request_id + " accepted");
      notify(d, r.buyer_id, "ACCEPTED", userName(d, s.user_id) + " accepted your request for " + name + ". Meet at " + handover_location + ".", "#/requests");
    } else if (decision === "REJECT") {
      r.status = "REJECTED"; r.decided_on = now(); releaseTarget(d, r, "All requests rejected", s.user_id);
      notify(d, r.buyer_id, "REJECTED", "Your request for " + name + " was rejected.", "#/requests"); checkFraud(d, r.buyer_id);
    } else if (decision === "COUNTER") {
      if (r.request_type !== "OFFER") fail("Only offers can be countered.");
      if (r.counter_price != null) fail("You can counter only once.");
      const c = +counter_price; if (!(c > r.offered_price && c <= it.selling_price)) fail("Counter price must be above the offer and not above " + it.selling_price + ".");
      r.counter_price = c; r.status = "COUNTERED"; r.decided_on = now();
      notify(d, r.buyer_id, "COUNTER", userName(d, s.user_id) + " countered your offer for " + name + " with " + c + ".", "#/requests");
    } else fail("Unknown decision.");
    save(d); return { ok: true };
  },
  respondCounter({ request_id, accept }) {
    const d = load(), { s } = needStudent(d, false), r = d.buy_request.find(x => x.request_id === +request_id);
    if (!r || r.buyer_id !== s.user_id || r.status !== "COUNTERED") fail("Nothing to answer.");
    const sellerId = r.item_id ? itemOf(d, r.item_id).seller_id : bundleOf(d, r.bundle_id).seller_id;
    if (accept) { r.status = "PENDING"; r.counter_accepted = true; notify(d, sellerId, "COUNTER", userName(d, s.user_id) + " accepted your counter price. Choose a handover place and time.", "#/requests"); }
    else { r.status = "CANCELLED"; releaseTarget(d, r, "Counter declined", s.user_id); notify(d, sellerId, "REJECTED", userName(d, s.user_id) + " declined your counter price.", "#/requests"); }
    save(d); return { ok: true };
  },
  cancelRequest({ request_id }) {
    const d = load(), { s } = needStudent(d, false), r = d.buy_request.find(x => x.request_id === +request_id);
    if (!r || r.buyer_id !== s.user_id || !PENDING_LIKE.includes(r.status)) fail("This request cannot be cancelled.");
    r.status = "CANCELLED"; releaseTarget(d, r, "Request cancelled", s.user_id); save(d); return { ok: true };
  },
  markSold({ request_id, pass }) {
    const d = load(), { s } = needStudent(d), r = d.buy_request.find(x => x.request_id === +request_id);
    if (!r || r.status !== "ACCEPTED") fail("This request is not ready for handover.");
    const it = r.item_id ? itemOf(d, r.item_id) : null, b = r.bundle_id ? bundleOf(d, r.bundle_id) : null;
    if ((it ? it.seller_id : b.seller_id) !== s.user_id) fail("Not your listing.");
    if ((it ? it.status : b.status) === "SOLD") fail("Already sold.");
    if (String(pass).trim() !== passFor(r)) fail("Wrong handover pass. Ask the buyer to show the 4-digit code in their Requests page.");
    if (it) {
      logStatus(d, it, "SOLD", s.user_id, "Handover pass verified");
      if (r.offered_item_id) { const sw = itemOf(d, r.offered_item_id); if (sw.status === "AVAILABLE") logStatus(d, sw, "SOLD", s.user_id, "Swapped for " + it.item_name); }
    } else { b.status = "SOLD"; bundleItems(d, b.bundle_id).forEach(i => logStatus(d, i, "SOLD", s.user_id, "Sold in bundle " + b.bundle_name)); }
    const bill = { bill_id: ++d.seq.bill, bill_no: "CK-2026-" + String(d.seq.bill).padStart(4, "0"), issued_on: now(), amount: r.agreed_price, deposit_amount: null, pdf_path: "bills/" + d.seq.bill + ".pdf", buyer_emailed_on: null, seller_emailed_on: null, request_id: r.request_id, booking_id: null };
    d.bill.push(bill);
    notify(d, r.buyer_id, "BILL", "Bill " + bill.bill_no + " for " + targetOf(d, r).name + " is ready.", "#/bill/" + bill.bill_id);
    notify(d, s.user_id, "BILL", "Bill " + bill.bill_no + " for " + targetOf(d, r).name + " is ready.", "#/bill/" + bill.bill_id);
    save(d); return { bill_id: bill.bill_id };
  },
  rate({ request_id, stars, review }) {
    const d = load(), { s } = needStudent(d), r = d.buy_request.find(x => x.request_id === +request_id);
    if (!r || r.buyer_id !== s.user_id) fail("Only the buyer can rate.");
    const sold = r.item_id ? itemOf(d, r.item_id).status === "SOLD" : bundleOf(d, r.bundle_id).status === "SOLD";
    if (r.status !== "ACCEPTED" || !sold) fail("You can rate the seller after the sale is complete.");
    if (d.rating.some(x => x.request_id === r.request_id)) fail("You already rated this deal.");
    if (!(+stars >= 1 && +stars <= 5)) fail("Stars must be 1 to 5.");
    d.rating.push({ rating_id: ++d.seq.rating, request_id: r.request_id, stars: +stars, review: (review || "").slice(0, 255), rated_on: now() }); save(d); return { ok: true };
  },
  bill({ bill_id }) {
    const d = load(), s = needUser(), bl = d.bill.find(x => x.bill_id === +bill_id);
    if (!bl) fail("Bill not found.");
    let out;
    if (bl.request_id) {
      const r = d.buy_request.find(x => x.request_id === bl.request_id), t = targetOf(d, r), it = r.item_id ? itemOf(d, r.item_id) : null, b = r.bundle_id ? bundleOf(d, r.bundle_id) : null;
      const sellerId = it ? it.seller_id : b.seller_id;
      if (![r.buyer_id, sellerId].includes(s.user_id) && s.role !== "ADMIN") fail("This bill is not yours.");
      out = { kind: "SALE", buyer: userName(d, r.buyer_id), seller: userName(d, sellerId), buyer_email: d.users.find(u => u.user_id === r.buyer_id).email, seller_email: d.users.find(u => u.user_id === sellerId).email,
        lines: b ? bundleItems(d, b.bundle_id).map(i => ({ name: i.item_name, price: i.selling_price })) : [{ name: it.item_name, price: it.selling_price }], title: t.name,
        place: r.handover_location, date: r.handover_time, request_type: r.request_type, swapped_for: r.offered_item_id ? itemOf(d, r.offered_item_id).item_name : null, discount_percent: b ? b.discount_percent : null };
    } else {
      const bk = d.rental_booking.find(x => x.booking_id === bl.booking_id), ri = rentalOf(d, bk.rental_item_id);
      if (![bk.renter_id, ri.owner_id].includes(s.user_id) && s.role !== "ADMIN") fail("This bill is not yours.");
      const days = dayDiff(bk.start_date, bk.end_date) + 1;
      out = { kind: "RENTAL", buyer: userName(d, bk.renter_id), seller: userName(d, ri.owner_id), buyer_email: d.users.find(u => u.user_id === bk.renter_id).email, seller_email: d.users.find(u => u.user_id === ri.owner_id).email,
        lines: [{ name: ri.rental_name + " (" + days + " day" + (days > 1 ? "s" : "") + " at " + ri.rate_per_day + " per day)", price: days * ri.rate_per_day }], title: ri.rental_name, place: ri.pickup_location, date: bk.start_date + " to " + bk.end_date, damage_note: bk.damage_note };
    }
    return Object.assign({}, bl, out);
  }
};

function removeBundle(d, b, by, remark) {
  b.status = "REMOVED";
  d.item.forEach(i => { if (i.bundle_id === b.bundle_id) i.bundle_id = null; });
  d.buy_request.filter(r => r.bundle_id === b.bundle_id && PENDING_LIKE.includes(r.status)).forEach(r => { r.status = "REJECTED"; r.decided_on = now(); notify(d, r.buyer_id, "REJECTED", b.bundle_name + " is no longer available.", "#/requests"); });
}
function bookingView(d, b) {
  const ri = rentalOf(d, b.rental_item_id), days = dayDiff(b.start_date, b.end_date) + 1, bill = d.bill.find(x => x.booking_id === b.booking_id);
  return Object.assign({}, b, { rental_name: ri.rental_name, rental_item_id: ri.rental_item_id, cover: (d.rental_image.find(x => x.rental_item_id === ri.rental_item_id) || {}).image_path, owner_id: ri.owner_id, owner_name: userName(d, ri.owner_id), renter_name: userName(d, b.renter_id),
    rental_days: days, total_rent: days * ri.rate_per_day, deposit_amount: ri.deposit_amount, rate_per_day: ri.rate_per_day, pickup_location: ri.pickup_location, is_late: b.status === "HANDED_OVER" && b.end_date < today(), bill_id: bill ? bill.bill_id : null });
}
function rentalView(d, ri) {
  const imgs = d.rental_image.filter(x => x.rental_item_id === ri.rental_item_id).sort((a, b) => a.image_no - b.image_no);
  return Object.assign({}, ri, { images: imgs.map(x => x.image_path), cover: (imgs.find(x => x.is_primary) || imgs[0] || {}).image_path, category_name: catOf(d, ri.category_id).category_name, owner: trustCard(d, ri.owner_id),
    booked: d.rental_booking.filter(b => b.rental_item_id === ri.rental_item_id && ["ACCEPTED", "HANDED_OVER"].includes(b.status)).map(b => ({ start_date: b.start_date, end_date: b.end_date })) });
}
const overlaps = (d, rid, s, e, skip) => d.rental_booking.some(b => b.rental_item_id === rid && b.booking_id !== skip && ["ACCEPTED", "HANDED_OVER"].includes(b.status) && !(e < b.start_date || s > b.end_date));
function bundleView(d, b, st) {
  const items = bundleItems(d, b.bundle_id).map(i => decorate(d, i, st));
  return Object.assign({}, b, bundlePrices(d, b), { items, seller: trustCard(d, b.seller_id), saving: bundlePrices(d, b).total_price - bundlePrices(d, b).bundle_price, cover: items[0] && items[0].cover });
}

Object.assign(H, {
  wishlist() {
    const d = load(), { s, st } = needStudent(d, false);
    return d.wishlist.filter(w => w.student_id === s.user_id && w.is_active).sort((a, b) => b.wish_id - a.wish_id).map(w => Object.assign({}, w,
      { category_name: w.category_id ? catOf(d, w.category_id).category_name : null, subject_name: w.subject_id ? d.subject.find(x => x.subject_id === w.subject_id).subject_name : null,
        matches: d.wishlist_match.filter(m => m.wish_id === w.wish_id && ["AVAILABLE", "BUY_REQUESTED"].includes(itemOf(d, m.item_id).status)).map(m => Object.assign(decorate(d, itemOf(d, m.item_id), st), { is_new: !m.is_seen })) }));
  },
  addWish(p) {
    const d = load(), { s } = needStudent(d, false);
    if (!(p.keyword || "").trim()) fail("Keyword is required.");
    if (!(+p.max_price > 0)) fail("Maximum price is required.");
    if (p.keyword.trim().length > 40) fail("Keyword can be at most 40 characters.");
    d.wishlist.push({ wish_id: ++d.seq.wish, student_id: s.user_id, subject_id: p.subject_id ? +p.subject_id : null, category_id: p.category_id ? +p.category_id : null, keyword: (p.keyword || "").trim() || null, max_price: p.max_price ? +p.max_price : null, created_on: today(), is_active: true });
    const w = d.wishlist[d.wishlist.length - 1];
    d.item.filter(i => i.status === "AVAILABLE").forEach(i => { if (wishMatches(d, i, w) && !d.wishlist_match.some(m => m.wish_id === w.wish_id && m.item_id === i.item_id)) d.wishlist_match.push({ wish_id: w.wish_id, item_id: i.item_id, matched_on: now(), is_seen: false }); });
    save(d); return { ok: true };
  },
  removeWish({ wish_id }) { const d = load(), { s } = needStudent(d, false); const w = d.wishlist.find(x => x.wish_id === +wish_id && x.student_id === s.user_id); if (w) w.is_active = false; save(d); return { ok: true }; },

  semesterKit() {
    const d = load(), { s, st } = needStudent(d, false), ids = subjectIdsFor(d, st), all = allBranchId(d);
    const liveBundles = d.bundle.filter(b => ["AVAILABLE", "REQUESTED"].includes(b.status) && b.seller_id !== s.user_id);
    const bundled = new Set(liveBundles.flatMap(b => bundleItems(d, b.bundle_id).map(i => (d.academic_item.find(a => a.item_id === i.item_id) || {}).subject_id)));
    const subjects = ids.map(id => {
      const sub = d.subject.find(x => x.subject_id === id);
      const items = d.item.filter(i => i.status === "AVAILABLE" && i.seller_id !== s.user_id && !(i.bundle_id && bundleOf(d, i.bundle_id).status !== "REMOVED") && d.academic_item.some(a => a.item_id === i.item_id && a.subject_id === id)).sort((a, b) => a.selling_price - b.selling_price).map(i => decorate(d, i, st));
      return { subject_id: id, subject_name: sub.subject_name, subject_code: sub.subject_code, items, via_bundle: bundled.has(id), covered: items.length > 0 || bundled.has(id), cheapest: items[0] ? items[0].selling_price : null };
    });
    const bundles = d.bundle.filter(b => ["AVAILABLE", "REQUESTED"].includes(b.status) && b.seller_id !== s.user_id && bundleItems(d, b.bundle_id).some(i => d.academic_item.some(a => a.item_id === i.item_id && ids.includes(a.subject_id)))).map(b => bundleView(d, b, st));
    return { semester: st.semester, branch: d.branch.find(b => b.branch_id === st.branch_id).branch_code, subjects, covered: subjects.filter(x => x.covered).length, total: subjects.length, cheapest_total: subjects.reduce((a, x) => a + (x.cheapest || 0), 0), bundles, all_branch_id: all };
  },
  clearoutPreview() {
    const d = load(), { s } = needStudent(d, false), t = today();
    return d.item.filter(i => i.seller_id === s.user_id && i.status === "AVAILABLE").map(i => {
      const floor = Math.ceil(i.original_price * 0.4), next = Math.max(floor, Math.round(i.selling_price * 0.9));
      let reason = "";
      if (dayDiff(i.listed_on, t) < 7) reason = "Listed less than 7 days ago";
      else if (i.last_price_drop_on && dayDiff(i.last_price_drop_on, t) < 7) reason = "Price already lowered this week";
      else if (next >= i.selling_price) reason = "Already at the 40% floor";
      return { item_id: i.item_id, item_name: i.item_name, original_price: i.original_price, current_price: i.selling_price, new_price: next, floor_price: floor, eligible: !reason, reason, days_listed: dayDiff(i.listed_on, t) };
    });
  },
  clearoutApply({ item_ids }) {
    const d = load(), { s } = needStudent(d), pv = H.clearoutPreview();
    let n = 0;
    pv.filter(x => x.eligible && (item_ids || []).map(Number).includes(x.item_id)).forEach(x => { const it = itemOf(d, x.item_id); it.selling_price = x.new_price; it.last_price_drop_on = today(); n++; });
    if (!n) fail("Nothing to apply.");
    save(d); return { updated: n };
  },
  report({ item_id, reason, details }) {
    const d = load(), { s } = needStudent(d, false), it = itemOf(d, item_id);
    if (!it) fail("Item not found.");
    if (it.seller_id === s.user_id) fail("You cannot report your own item.");
    if (d.report.some(r => r.reporter_id === s.user_id && r.item_id === it.item_id)) fail("You already reported this item.");
    if (!["FAKE", "INAPPROPRIATE", "WRONG_INFO", "SPAM", "OTHER"].includes(reason)) fail("Pick a reason.");
    d.report.push({ report_id: ++d.seq.report, reporter_id: s.user_id, item_id: it.item_id, reason, details: (details || "").slice(0, 255), reported_on: now(), status: "OPEN", resolved_by: null, resolved_on: null, action_taken: null });
    checkFraud(d, it.seller_id); save(d); return { ok: true };
  },
  notifications() {
    const d = load(); sweep(d); const s = needUser(), cut = ago(14);
    return d.notification.filter(n => n.user_id === s.user_id && n.created_on >= cut).sort((a, b) => b.created_on.localeCompare(a.created_on) || b.notification_id - a.notification_id);
  },

  listBundles(f) {
    const d = load(); sweep(d); const s = session(), st = s && studentOf(d, s.user_id);
    let r = d.bundle.filter(b => ["AVAILABLE", "REQUESTED"].includes(b.status)).map(b => bundleView(d, b, st));
    if (f.q) r = r.filter(b => (b.bundle_name + " " + b.items.map(i => i.item_name).join(" ")).toLowerCase().includes(f.q.toLowerCase()));
    r.sort((a, b) => b.bundle_id - a.bundle_id);
    return pageOf(r, f.page, 12);
  },
  getBundle({ bundle_id }) {
    const d = load(); sweep(d); const s = session(), st = s && studentOf(d, s.user_id), b = bundleOf(d, bundle_id);
    if (!b) fail("Bundle not found.");
    const mine = s ? d.buy_request.filter(r => r.bundle_id === b.bundle_id && r.buyer_id === s.user_id).slice(-1)[0] || null : null;
    return { bundle: bundleView(d, b, st), my_request: mine && Object.assign({}, mine, { pass: mine.status === "ACCEPTED" && b.status !== "SOLD" ? passFor(mine) : null }) };
  },
  createBundle({ bundle_name, item_ids }) {
    const d = load(), { s } = needStudent(d), ids = (item_ids || []).map(Number);
    if (!(bundle_name || "").trim()) fail("Give the bundle a name.");
    if (ids.length < 2) fail("A bundle needs at least 2 items.");
    const items = ids.map(id => itemOf(d, id));
    if (items.some(i => !i || i.seller_id !== s.user_id || i.status !== "AVAILABLE" || i.bundle_id)) fail("Bundles can only hold your own available items that are not already in a bundle.");
    const id = ++d.seq.bundle;
    d.bundle.push({ bundle_id: id, bundle_name: bundle_name.trim(), discount_percent: BUNDLE_DISCOUNT, status: "AVAILABLE", created_on: now(), seller_id: s.user_id });
    items.forEach(i => { i.bundle_id = id; }); save(d); return { bundle_id: id };
  },
  myBundles() {
    const d = load(); sweep(d); const { s, st } = needStudent(d, false);
    return d.bundle.filter(b => b.seller_id === s.user_id).sort((a, b) => b.bundle_id - a.bundle_id).map(b => bundleView(d, b, st));
  },
  withdrawBundle({ bundle_id }) {
    const d = load(), { s } = needStudent(d), b = bundleOf(d, bundle_id);
    if (!b || b.seller_id !== s.user_id) fail("Not your bundle.");
    if (b.status !== "AVAILABLE" && b.status !== "REQUESTED") fail("This bundle can no longer be withdrawn.");
    if (d.buy_request.some(r => r.bundle_id === b.bundle_id && r.status === "ACCEPTED")) fail("A buyer is already coming for this bundle.");
    removeBundle(d, b, s.user_id, "Withdrawn"); save(d); return { ok: true };
  },

  listRentals(f) {
    const d = load(); sweep(d);
    let r = d.rental_item.filter(x => x.status !== "REMOVED").map(x => rentalView(d, x));
    if (f.q) r = r.filter(x => (x.rental_name + " " + x.description).toLowerCase().includes(f.q.toLowerCase()));
    if (f.category_id) r = r.filter(x => x.category_id === +f.category_id);
    if (f.max_rate) r = r.filter(x => x.rate_per_day <= +f.max_rate);
    if (f.sort === "price_asc") r.sort((a, b) => a.rate_per_day - b.rate_per_day); else r.sort((a, b) => b.rental_item_id - a.rental_item_id);
    return pageOf(r, f.page, 12);
  },
  getRental({ rental_item_id }) {
    const d = load(); sweep(d); const s = session(), ri = rentalOf(d, rental_item_id);
    if (!ri) fail("Rental item not found.");
    const mine = s ? d.rental_booking.filter(b => b.rental_item_id === ri.rental_item_id && b.renter_id === s.user_id).map(b => bookingView(d, b)) : [];
    return { rental: rentalView(d, ri), my_bookings: mine };
  },
  createRental(p) {
    const d = load(), { s } = needStudent(d), cat = catOf(d, +p.category_id);
    if (!cat || !cat.is_rentable) fail("Choose a rentable category.");
    if (!(p.rental_name || "").trim()) fail("Name is required.");
    if ((p.description || "").trim().length < 30) fail("Description must be at least 30 characters.");
    if (p.rental_name.trim().length > 60) fail("Name can be at most 60 characters.");
    if (p.description.trim().length > 200) fail("Description can be at most 200 characters.");
    if (!["NEW", "GOOD", "FAIR", "POOR"].includes(p.item_condition)) fail("Choose the condition.");
    if (!p.pickup_location) fail("Choose a pickup place.");
    if (!(+p.rate_per_day > 0)) fail("Rate per day must be above 0.");
    if (!(+p.deposit_amount >= 0) || p.deposit_amount === "") fail("Enter the refundable deposit (0 if none).");
    if (!(+p.max_days >= 1 && +p.max_days <= 30)) fail("Maximum days must be 1 to 30.");
    validatePhotos(p.photos, "A rental");
    const id = ++d.seq.rental;
    d.rental_item.push({ rental_item_id: id, rental_name: p.rental_name.trim(), description: p.description.trim(), item_condition: p.item_condition, rate_per_day: +p.rate_per_day, deposit_amount: +p.deposit_amount, max_days: +p.max_days, pickup_location: p.pickup_location, status: "AVAILABLE", listed_on: now(), owner_id: s.user_id, category_id: cat.category_id });
    p.photos.forEach((ph, n) => d.rental_image.push({ rental_item_id: id, image_no: n + 1, image_path: ph.data, is_primary: n === (+p.cover || 0) }));
    save(d); return { rental_item_id: id };
  },
  withdrawRental({ rental_item_id }) {
    const d = load(), { s } = needStudent(d), ri = rentalOf(d, rental_item_id);
    if (!ri || ri.owner_id !== s.user_id) fail("Not your rental item.");
    if (d.rental_booking.some(b => b.rental_item_id === ri.rental_item_id && ["ACCEPTED", "HANDED_OVER"].includes(b.status))) fail("This item has an accepted or running booking.");
    d.rental_booking.filter(b => b.rental_item_id === ri.rental_item_id && b.status === "PENDING").forEach(b => { b.status = "REJECTED"; notify(d, b.renter_id, "REJECTED", ri.rental_name + " is no longer available to rent.", "#/my-rentals"); });
    ri.status = "REMOVED"; save(d); return { ok: true };
  },
  requestRental({ rental_item_id, start_date, end_date }) {
    const d = load(), { s } = needStudent(d), ri = rentalOf(d, rental_item_id);
    if (!ri || ri.status === "REMOVED") fail("Rental item not found.");
    if (ri.owner_id === s.user_id) fail("You cannot rent your own item.");
    if (!start_date || !end_date) fail("Pick the start and end dates.");
    if (start_date < today()) fail("Start date cannot be in the past.");
    if (end_date < start_date) fail("End date must be on or after the start date.");
    const days = dayDiff(start_date, end_date) + 1;
    if (days > ri.max_days) fail("This item can be rented for at most " + ri.max_days + " days.");
    if (overlaps(d, ri.rental_item_id, start_date, end_date)) fail("Those dates clash with another booking.");
    if (d.rental_booking.some(b => b.rental_item_id === ri.rental_item_id && b.renter_id === s.user_id && b.status === "PENDING")) fail("You already have a waiting booking for this item.");
    const id = ++d.seq.booking;
    d.rental_booking.push({ booking_id: id, rental_item_id: ri.rental_item_id, renter_id: s.user_id, start_date, end_date, requested_on: now(), status: "PENDING", handed_over_on: null, returned_on: null, damage_note: null, late_notified: false });
    notify(d, ri.owner_id, "RENTAL", userName(d, s.user_id) + " wants to rent " + ri.rental_name + " from " + start_date + " to " + end_date + ".", "#/my-rentals");
    save(d); return { booking_id: id, rental_days: days, total_rent: days * ri.rate_per_day, deposit_amount: ri.deposit_amount };
  },
  myRentals() {
    const d = load(); sweep(d); const { s } = needStudent(d, false);
    const mine = d.rental_item.filter(r => r.owner_id === s.user_id && r.status !== "REMOVED").map(r => rentalView(d, r));
    return { as_renter: d.rental_booking.filter(b => b.renter_id === s.user_id).sort((a, b) => b.booking_id - a.booking_id).map(b => bookingView(d, b)),
      as_owner: d.rental_booking.filter(b => rentalOf(d, b.rental_item_id).owner_id === s.user_id).sort((a, b) => b.booking_id - a.booking_id).map(b => bookingView(d, b)), my_rentals: mine };
  },
  decideRental({ booking_id, decision }) {
    const d = load(), { s } = needStudent(d), b = d.rental_booking.find(x => x.booking_id === +booking_id), ri = b && rentalOf(d, b.rental_item_id);
    if (!b || ri.owner_id !== s.user_id) fail("Not your rental item.");
    if (b.status !== "PENDING") fail("Already answered.");
    if (decision === "ACCEPT") {
      if (overlaps(d, ri.rental_item_id, b.start_date, b.end_date, b.booking_id)) fail("These dates now clash with an accepted booking.");
      b.status = "ACCEPTED"; notify(d, b.renter_id, "RENTAL", userName(d, s.user_id) + " accepted your booking for " + ri.rental_name + ".", "#/my-rentals");
    } else { b.status = "REJECTED"; notify(d, b.renter_id, "REJECTED", "Your booking for " + ri.rental_name + " was rejected.", "#/my-rentals"); }
    save(d); return { ok: true };
  },
  cancelBooking({ booking_id }) {
    const d = load(), { s } = needStudent(d, false), b = d.rental_booking.find(x => x.booking_id === +booking_id);
    if (!b || b.renter_id !== s.user_id || !["PENDING", "ACCEPTED"].includes(b.status)) fail("This booking cannot be cancelled.");
    b.status = "CANCELLED"; save(d); return { ok: true };
  },
  handoverRental({ booking_id }) {
    const d = load(), { s } = needStudent(d), b = d.rental_booking.find(x => x.booking_id === +booking_id), ri = b && rentalOf(d, b.rental_item_id);
    if (!b || ri.owner_id !== s.user_id) fail("Not your rental item.");
    if (b.status !== "ACCEPTED") fail("Accept the booking first.");
    b.status = "HANDED_OVER"; b.handed_over_on = now(); ri.status = "ON_RENT";
    notify(d, b.renter_id, "RENTAL", ri.rental_name + " was handed over. Return it by " + b.end_date + ".", "#/my-rentals"); save(d); return { ok: true };
  },
  returnRental({ booking_id, damage_note }) {
    const d = load(), { s } = needStudent(d), b = d.rental_booking.find(x => x.booking_id === +booking_id), ri = b && rentalOf(d, b.rental_item_id);
    if (!b || ri.owner_id !== s.user_id) fail("Not your rental item.");
    if (b.status !== "HANDED_OVER") fail("Mark it handed over first.");
    if ((damage_note || "").length > 150) fail("Damage note can be at most 150 characters.");
    b.status = "RETURNED"; b.returned_on = now(); b.damage_note = (damage_note || "").trim() || null; ri.status = "AVAILABLE";
    const days = dayDiff(b.start_date, b.end_date) + 1;
    const bill = { bill_id: ++d.seq.bill, bill_no: "CK-2026-" + String(d.seq.bill).padStart(4, "0"), issued_on: now(), amount: days * ri.rate_per_day, deposit_amount: ri.deposit_amount, pdf_path: "bills/" + d.seq.bill + ".pdf", buyer_emailed_on: null, seller_emailed_on: null, request_id: null, booking_id: b.booking_id };
    d.bill.push(bill);
    [b.renter_id, ri.owner_id].forEach(u => notify(d, u, "BILL", "Rental bill " + bill.bill_no + " for " + ri.rental_name + " is ready.", "#/bill/" + bill.bill_id));
    save(d); return { bill_id: bill.bill_id };
  },

  adminOverview() {
    needAdmin(); const d = load();
    const live = d.item.filter(i => ["AVAILABLE", "BUY_REQUESTED", "ACCEPTED"].includes(i.status));
    const saleBills = d.bill.filter(b => b.request_id);
    const cats = d.category.map(c => ({ name: c.category_name, count: d.item.filter(i => i.category_id === c.category_id && i.status !== "REMOVED").length })).filter(x => x.count);
    const statuses = ["AVAILABLE", "BUY_REQUESTED", "ACCEPTED", "SOLD", "REMOVED"].map(s => ({ name: s, count: d.item.filter(i => i.status === s).length }));
    return { students: d.student.length, items_on_sale: live.length, items_sold: d.item.filter(i => i.status === "SOLD").length, value_sold: saleBills.reduce((a, b) => a + b.amount, 0),
      open_reports: d.report.filter(r => r.status === "OPEN").length, pending_verifications: d.student.filter(s => s.verification_status === "PENDING").length, open_flags: d.fraud_flag.filter(f => f.status === "OPEN").length, by_category: cats, by_status: statuses };
  },
  adminVerifications() {
    needAdmin(); const d = load();
    const row = st => { const u = d.users.find(x => x.user_id === st.user_id); return Object.assign({}, st, { name: u.first_name + " " + u.last_name, email: u.email, branch_code: d.branch.find(b => b.branch_id === st.branch_id).branch_code, institute_code: d.institute.find(i => i.institute_id === st.institute_id).institute_code }); };
    return { pending: d.student.filter(s => s.verification_status === "PENDING").map(row), verified: d.student.filter(s => s.verification_status === "VERIFIED").map(row), rejected: d.student.filter(s => s.verification_status === "REJECTED").map(row) };
  },
  verifyStudent({ user_id, status, remarks }) {
    const a = needAdmin(), d = load(), st = studentOf(d, +user_id);
    if (!st) fail("Student not found.");
    if (!["VERIFIED", "REJECTED"].includes(status)) fail("Unknown status.");
    if (status === "REJECTED" && !(remarks || "").trim()) fail("Add a remark so the student knows why.");
    const was = st.verification_status;
    st.verification_status = status; st.verification_method = "ADMIN"; st.verified_by = a.user_id; st.verified_on = now(); st.remarks = (remarks || "").trim();
    logAdmin(d, a.user_id, status === "VERIFIED" ? "VERIFY" : "REVOKE", "STUDENT", st.user_id, st.remarks || (was === "VERIFIED" ? "Revoked" : "Approved"));
    notify(d, st.user_id, "VERIFICATION", status === "VERIFIED" ? "An admin verified your ID card. You can start trading." : "Your verification was not accepted: " + st.remarks, "#/");
    save(d); return { ok: true };
  },
  adminReports() {
    needAdmin(); const d = load();
    return d.report.slice().sort((a, b) => b.report_id - a.report_id).map(r => { const it = itemOf(d, r.item_id); return Object.assign({}, r, { item_name: it.item_name, item_status: it.status, seller_name: userName(d, it.seller_id), reporter_name: userName(d, r.reporter_id) }); });
  },
  resolveReport({ report_id, status, action_taken, remove_item }) {
    const a = needAdmin(), d = load(), r = d.report.find(x => x.report_id === +report_id);
    if (!r || r.status !== "OPEN") fail("Report is not open.");
    if (!["RESOLVED", "DISMISSED"].includes(status)) fail("Unknown status.");
    if (!(action_taken || "").trim()) fail("Write the action taken.");
    r.status = status; r.resolved_by = a.user_id; r.resolved_on = now(); r.action_taken = action_taken.trim();
    logAdmin(d, a.user_id, "RESOLVE_REPORT", "REPORT", r.report_id, r.action_taken);
    if (remove_item) {
      const it = itemOf(d, r.item_id);
      if (it.bundle_id) { const b = bundleOf(d, it.bundle_id); if (b.status === "AVAILABLE" || b.status === "REQUESTED") removeBundle(d, b, a.user_id, "Item removed"); }
      if (!["REMOVED", "SOLD"].includes(it.status)) { logStatus(d, it, "REMOVED", a.user_id, "Removed by admin"); logAdmin(d, a.user_id, "REMOVE_ITEM", "ITEM", it.item_id, r.action_taken); notify(d, it.seller_id, "REMOVED", it.item_name + " was removed by an admin.", "#/my-listings"); }
    }
    save(d); return { ok: true };
  },
  adminUsers({ q }) {
    needAdmin(); const d = load();
    return d.users.filter(u => u.role === "STUDENT" && (!q || (u.first_name + " " + u.last_name + " " + u.email).toLowerCase().includes(q.toLowerCase()))).map(u => { const st = studentOf(d, u.user_id); return { user_id: u.user_id, name: u.first_name + " " + u.last_name, email: u.email, is_active: u.is_active, verification_status: st.verification_status, college_id: st.college_id, semester: st.semester }; });
  },
  setActive({ user_id, is_active }) {
    const a = needAdmin(), d = load(), u = d.users.find(x => x.user_id === +user_id);
    if (!u || u.role === "ADMIN") fail("User not found.");
    u.is_active = !!+is_active; logAdmin(d, a.user_id, u.is_active ? "UNBLOCK" : "BLOCK", "STUDENT", u.user_id, ""); save(d); return { ok: true };
  },
  setSettings({ farewell_banner }) { const a = needAdmin(), d = load(); d.settings.farewell_banner = !!farewell_banner; save(d); return d.settings; },
  fraudFlags() {
    needAdmin(); const d = load();
    return d.fraud_flag.slice().sort((a, b) => b.flag_id - a.flag_id).map(f => { const st = studentOf(d, f.student_id); return Object.assign({}, f, { student_name: userName(d, f.student_id), is_active: d.users.find(u => u.user_id === f.student_id).is_active, semester: st.semester }); });
  },
  reviewFlag({ flag_id, action, note }) {
    const a = needAdmin(), d = load(), f = d.fraud_flag.find(x => x.flag_id === +flag_id);
    if (!f || f.status !== "OPEN") fail("Flag is not open.");
    f.status = action === "BLOCK" ? "BLOCKED" : "CLEARED"; f.reviewed_by = a.user_id; f.reviewed_on = now(); f.note = (note || "").trim() || null;
    if (action === "BLOCK") { d.users.find(u => u.user_id === f.student_id).is_active = false; logAdmin(d, a.user_id, "BLOCK", "STUDENT", f.student_id, "Fraud flag " + f.flag_id); }
    logAdmin(d, a.user_id, "REVIEW_FLAG", "FRAUD_FLAG", f.flag_id, f.note || f.status);
    save(d); return { ok: true };
  },
  activityLog() {
    needAdmin(); const d = load();
    return d.admin_activity.slice().sort((a, b) => b.performed_at.localeCompare(a.performed_at) || b.log_no - a.log_no).map(x => Object.assign({}, x, { admin_name: userName(d, x.admin_id) }));
  },
  adminOversight() {
    needAdmin(); const d = load();
    return { rentals: d.rental_item.filter(r => r.status !== "REMOVED").map(r => Object.assign(rentalView(d, r), { owner_name: userName(d, r.owner_id), damage_notes: d.rental_booking.filter(b => b.rental_item_id === r.rental_item_id && b.damage_note).map(b => ({ booking_id: b.booking_id, renter_name: userName(d, b.renter_id), note: b.damage_note, returned_on: b.returned_on })) })),
      bundles: d.bundle.filter(b => ["AVAILABLE", "REQUESTED"].includes(b.status)).map(b => Object.assign(bundleView(d, b, null), { seller_name: userName(d, b.seller_id) })) };
  },
  adminRemoveRental({ rental_item_id }) {
    const a = needAdmin(), d = load(), ri = rentalOf(d, rental_item_id);
    if (!ri) fail("Not found.");
    ri.status = "REMOVED"; d.rental_booking.filter(b => b.rental_item_id === ri.rental_item_id && b.status === "PENDING").forEach(b => { b.status = "REJECTED"; });
    logAdmin(d, a.user_id, "REMOVE_ITEM", "RENTAL_ITEM", ri.rental_item_id, "Removed by admin"); notify(d, ri.owner_id, "REMOVED", ri.rental_name + " was removed by an admin.", "#/my-rentals"); save(d); return { ok: true };
  },
  adminRemoveBundle({ bundle_id }) {
    const a = needAdmin(), d = load(), b = bundleOf(d, bundle_id);
    if (!b) fail("Not found.");
    removeBundle(d, b, a.user_id, "Removed by admin"); logAdmin(d, a.user_id, "REMOVE_ITEM", "BUNDLE", b.bundle_id, "Removed by admin"); notify(d, b.seller_id, "REMOVED", b.bundle_name + " was removed by an admin.", "#/my-bundles"); save(d); return { ok: true };
  }
});

function fileToDataURL(file, max = 900, quality = 0.72) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file), img = new Image();
    img.onload = () => {
      const k = Math.min(1, max / Math.max(img.width, img.height)), c = document.createElement("canvas");
      c.width = Math.round(img.width * k); c.height = Math.round(img.height * k);
      c.getContext("2d").drawImage(img, 0, 0, c.width, c.height);
      URL.revokeObjectURL(url); resolve(c.toDataURL("image/jpeg", quality));
    };
    img.onerror = () => { URL.revokeObjectURL(url); reject(new Error("Could not read " + file.name)); };
    img.src = url;
  });
}

async function api(name, payload = {}) {
  if (USE_MOCK) {
    if (!H[name]) throw new Error("Unknown call: " + name);
    const p = Object.assign({}, payload);
    if (p.photos) p.photos = await Promise.all(p.photos.map(async f => ({ name: f.name, data: await fileToDataURL(f) })));
    if (p.id_card) p.id_card = p.id_card.name || "id-card";
    return H[name](p);
  }
  const hasFiles = payload.photos || payload.id_card;
  let opts = { method: "POST", credentials: "include" };
  if (hasFiles) {
    const fd = new FormData();
    Object.entries(payload).forEach(([k, v]) => { if (k === "photos") v.forEach(f => fd.append("photos", f)); else if (v instanceof File) fd.append(k, v); else if (v != null) fd.append(k, v); });
    opts.body = fd;
  } else { opts.headers = { "Content-Type": "application/json" }; opts.body = JSON.stringify(payload); }
  const res = await fetch(BASE_URL + "/api/" + name, opts);
  let data = null; try { data = await res.json(); } catch (e) { }
  if (res.status === 401) { localStorage.removeItem(SESSION_KEY); localStorage.setItem("ck_expired", "1"); throw new Error("Your session expired. Please log in again."); }
  if (!res.ok) throw new Error((data && data.error) || "Server error " + res.status);
  if (name === "login") localStorage.setItem(SESSION_KEY, JSON.stringify(Object.assign({ expires: Date.now() + SESSION_HOURS * 36e5 }, data)));
  if (name === "logout") localStorage.removeItem(SESSION_KEY);
  return data;
}
