package com.campuskart.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.ArrayList;

// ★ RUBRIC: GUI (1M) | Asg 27 (client side): a raw Socket writes an HTTP/1.1 request by hand
//            and reads the reply — the GUI's only way to reach the server and the database.
// The client needs nothing but a JDK: no database driver, no credentials, no model classes.
public class ServerLink {

    private final String host;                  // ★ Asg 9: final fields
    private final int port;
    private String cookie = "";                 // the "sid" session cookie returned by login

    public ServerLink(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ArrayList<String[]> get(String path) throws IOException {
        return send("GET", path, "");
    }

    public ArrayList<String[]> post(String path, String form) throws IOException {
        return send("POST", path, form);
    }

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");   // spaces and special characters are safe in a form
        } catch (Exception e) {
            return value;
        }
    }

    private ArrayList<String[]> send(String method, String path, String body) throws IOException {
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        ArrayList<String[]> rows = new ArrayList<String[]>();   // ★ Asg 5: ArrayList of reply lines
        try {
            socket = new Socket(host, port);           // ★ Asg 27: one Socket per request
            out = new PrintWriter(socket.getOutputStream());
            StringBuffer request = new StringBuffer(); // ★ Asg 5: StringBuffer assembles the request
            request.append(method).append(' ').append(path).append(" HTTP/1.1\r\n");
            request.append("Host: ").append(host).append("\r\n");
            request.append("Accept: text/plain\r\n");   // the server answers in its text format, not HTML
            request.append("Cookie: ").append(cookie).append("\r\n");
            request.append("Content-Type: application/x-www-form-urlencoded\r\n");
            request.append("Content-Length: ").append(body.length()).append("\r\n");
            request.append("Connection: close\r\n\r\n");
            request.append(body);
            out.print(request);
            out.flush();

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = in.readLine()) != null && line.length() > 0) {   // skip the response headers
                if (line.startsWith("Set-Cookie:")) {
                    cookie = line.substring(11).trim();  // keep only "sid=..." (drop ; Path=/; HttpOnly)
                    int semicolon = cookie.indexOf(';');
                    if (semicolon >= 0) {
                        cookie = cookie.substring(0, semicolon);
                    }
                }
            }
            // body: first line is "OK" or "ERR|message", every later line is one | separated record
            while ((line = in.readLine()) != null) {
                rows.add(line.split("\\|"));
            }
        } finally {                                      // ★ RUBRIC: Exception Handling | Asg 15: finally
            try {
                if (in != null) in.close();              // ★ Asg 14: nested try-catch inside finally
            } catch (IOException ignore) {
            }
            if (out != null) out.close();
            try {
                if (socket != null) socket.close();
            } catch (IOException ignore) {
            }
        }
        return rows;
    }
}
