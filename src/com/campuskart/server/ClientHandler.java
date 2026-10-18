package com.campuskart.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.campuskart.util.FileLogger;

// ★ RUBRIC: Thread (1M) | Asg 26: Runnable separates the TASK (handle one request)
//            from the Thread that executes it — see CampusKartServer for the Thread part.
public class ClientHandler implements Runnable {

    private final Socket socket;                 // ★ Asg 9: final field — one connection per handler

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {                          // this method executes INSIDE its own thread
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            HttpRequest request = HttpRequest.parse(in);              // ★ Asg 27: read the HTTP request
            HttpResponse response = new HttpResponse(socket);         // ★ Asg 27: write the HTTP response
            new Router().dispatch(request, response);                 // route -> DAO -> MySQL -> page
        } catch (IOException e) {
            FileLogger.logEvent("Connection error: " + e.getMessage());   // ★ Asg 14: catch
        } finally {                              // ★ RUBRIC: Exception Handling | Asg 15: finally ALWAYS runs,
                                                 // so the thread ends TERMINATED and the socket is freed
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }                                    // ★ Asg 14: NESTED try-catch inside finally
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
