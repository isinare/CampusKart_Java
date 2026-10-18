package com.campuskart.server;

import java.util.Vector;

// ★ RUBRIC: Collections (1M) | Asg 5: VECTOR because it is synchronized —
//            every client thread shares this one session table.
//            (A per-request ArrayList would not be safe here; see the DAO layer for ArrayList.)
public class SessionStore {

    private static Vector<Session> sessions = new Vector<Session>();

    // login creates a session and returns its token (stored in the browser's "sid" cookie)
    public static String create(int userId, String role) {
        String token = "s" + System.currentTimeMillis() + (int) (Math.random() * 100000);
        sessions.add(new Session(token, userId, role));
        return token;
    }

    public static Session find(String token) {
        if (token == null) {
            return null;
        }
        for (int i = 0; i < sessions.size(); i++) {   // index loop — Vector's classic API style
            Session session = sessions.get(i);
            if (session.getToken().equals(token)) {
                return session;
            }
        }
        return null;
    }

    public static void remove(String token) {
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getToken().equals(token)) {
                sessions.remove(i);
                return;
            }
        }
    }

    // one logged-in user
    public static class Session {

        private final String token;              // ★ Asg 9: final fields — a session never changes
        private final int userId;
        private final String role;

        public Session(String token, int userId, String role) {
            this.token = token;
            this.userId = userId;
            this.role = role;
        }

        public String getToken() { return token; }
        public int getUserId() { return userId; }
        public String getRole() { return role; }
    }
}
