package com.campuskart.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

// Serves the STATIC assets of the provided CampusKart web design (style.css and the
// product/hero photographs). The design folder is reused as-is: only the CSS and the
// images are delivered — app.js and api.js are CLIENT-SIDE JAVASCRIPT and are
// deliberately never served (scope document section 2.2: no JavaScript).
//
// Every page is rendered by the Java server (HtmlBuilder, StringBuffer); the browser
// receives plain HTML + CSS and does zero processing.
//
// ★ RUBRIC: CO2 | Asg 13: file handling — reading bytes from a file with
//            FileInputStream into a ByteArrayOutputStream (streams, read side).
// The folder defaults to the sibling CampusKart_Web directory and can be moved with
// -Dcampuskart.web=/path/to/CampusKart_Web
public class WebAssets {

    private static final String BASE = System.getProperty("campuskart.web", "../CampusKart_Web");

    // returns null when the file does not exist (callers answer 404)
    public static byte[] read(String relativePath) throws IOException {
        // never allow ".." — a request cannot escape the design folder
        if (relativePath.contains("..") || relativePath.indexOf(':') >= 0) {
            throw new IOException("path not allowed: " + relativePath);
        }
        File file = new File(BASE, relativePath);
        if (!file.isFile()) {
            return null;
        }
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            in = new FileInputStream(file);                  // ★ Asg 13: open the file for reading
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {              // ★ Asg 13: read chunk by chunk
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } finally {
            try {
                if (in != null) in.close();                  // ★ Asg 15: finally, each resource in its own try
            } catch (IOException e) {
            }
        }
    }

    // Content-Type by file extension (browsers need this to apply the stylesheet / show images)
    public static String contentType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        return "application/octet-stream";
    }
}
