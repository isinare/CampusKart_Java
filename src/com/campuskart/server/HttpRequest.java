package com.campuskart.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;

// ★ RUBRIC: Networking | Asg 27: an HTTP/1.1 request parsed by hand from a Socket's reader —
//            no framework, no library beyond java.io and java.net
public class HttpRequest {

    public String method = "GET";
    public String path = "/";
    public String query = "";
    public String body = "";
    public String cookie = "";
    public boolean wantsText = false;            // true when the Swing client sends "Accept: text/plain"

    // Asg 27 (server side): read the request line, the headers, then exactly Content-Length body characters
    public static HttpRequest parse(BufferedReader in) throws IOException {
        HttpRequest request = new HttpRequest();

        String line = in.readLine();             // request line: "GET /path?q=x HTTP/1.1"
        if (line == null) {
            return request;
        }
        String[] parts = line.split(" ");
        if (parts.length >= 2) {
            request.method = parts[0];
            int questionMark = parts[1].indexOf('?');
            if (questionMark >= 0) {
                request.path = parts[1].substring(0, questionMark);
                request.query = parts[1].substring(questionMark + 1);
            } else {
                request.path = parts[1];
            }
        }

        int contentLength = 0;
        while ((line = in.readLine()) != null && line.length() > 0) {   // headers end at the blank line
            if (line.startsWith("Cookie:")) {
                int sid = line.indexOf("sid=");
                if (sid >= 0) {
                    request.cookie = line.substring(sid + 4).split(";")[0].trim();
                }
            } else if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.substring(15).trim());
            } else if (line.startsWith("Accept:") && line.indexOf("text/plain") >= 0) {
                request.wantsText = true;        // the Swing client gets text instead of HTML
            }
        }

        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            int got = 0;
            while (got < contentLength) {        // read exactly Content-Length characters
                int n = in.read(buffer, got, contentLength - got);
                if (n < 0) {
                    break;
                }
                got += n;
            }
            request.body = new String(buffer, 0, got);
        }
        return request;
    }
}
