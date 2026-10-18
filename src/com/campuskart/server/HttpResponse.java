package com.campuskart.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import com.campuskart.util.FileLogger;

// ★ RUBRIC: Networking | Asg 27: the reply is written to the client Socket byte by byte,
//            assembled with StringBuffer (Asg 5) exactly like a real HTTP/1.1 response
public class HttpResponse {

    private final Socket socket;

    public HttpResponse(Socket socket) {
        this.socket = socket;
    }

    public void html(int status, String reason, String body) {
        send(status, reason, "text/html; charset=UTF-8", body, null);
    }

    public void text(String body) {              // the Swing client reads this plain-text format
        send(200, "OK", "text/plain; charset=UTF-8", body, null);
    }

    // Post/Redirect/Get: browsers follow the 303 and never resubmit the form
    public void redirect(String location, String setCookie) {
        StringBuffer head = new StringBuffer();
        head.append("HTTP/1.1 303 See Other\r\n");
        head.append("Location: ").append(location).append("\r\n");
        head.append("Content-Length: 0\r\n");
        if (setCookie != null) {
            head.append("Set-Cookie: ").append(setCookie).append("\r\n");
        }
        head.append("Connection: close\r\n\r\n");
        write(head.toString());
    }

    private void send(int status, String reason, String contentType, String body, String setCookie) {
        StringBuffer sb = new StringBuffer();    // ★ Asg 5: the whole response is assembled in memory
        byte[] bytes;
        try {
            bytes = body.getBytes("UTF-8");      // Content-Length must be BYTE length, not character count
        } catch (Exception e) {
            bytes = body.getBytes();
        }
        sb.append("HTTP/1.1 ").append(status).append(' ').append(reason).append("\r\n");
        sb.append("Content-Type: ").append(contentType).append("\r\n");
        sb.append("Content-Length: ").append(bytes.length).append("\r\n");
        if (setCookie != null) {
            sb.append("Set-Cookie: ").append(setCookie).append("\r\n");
        }
        sb.append("Connection: close\r\n\r\n");  // blank line separates headers from the body
        sb.append(body);
        write(sb.toString());
    }

    private void write(String response) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write(response.getBytes("UTF-8"));
            out.flush();
        } catch (IOException e) {
            FileLogger.logEvent("Could not write the response: " + e.getMessage());
        }
    }
}
