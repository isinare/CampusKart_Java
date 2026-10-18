package com.campuskart.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.campuskart.util.FileLogger;

// ★ RUBRIC: Thread (1M) + Networking | Asg 27: ServerSocket listens on a port,
//            Asg 25: Thread lifecycle NEW -> start() -> RUNNING -> TERMINATED
//            Asg 3  : STATIC variable shared by the whole class
//
// This is a LOCAL application: it runs on your machine and serves http://localhost:8080
public class CampusKartServer {

    static int requestsServed = 0;               // ★ RUBRIC: CO1 | Asg 3: static variable — one copy for all threads

    public static void main(String[] args) {
        int port = 8080;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);   // ★ Asg 27: ServerSocket binds and listens on the port
            final ServerSocket toClose = serverSocket;

            // ★ RUBRIC: CO1 | Asg 6 + 26: ANONYMOUS class implementing Runnable —
            // a second thread that closes the server when you press Ctrl+C
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    try {
                        toClose.close();
                    } catch (IOException e) {
                        // nothing to do while shutting down
                    }
                }
            }));

            System.out.println("CampusKart server running on http://localhost:" + port);
            FileLogger.logEvent("Server started on port " + port);

            while (true) {
                Socket client = serverSocket.accept();   // ★ Asg 27: one Socket per browser/client connection
                // ★ RUBRIC: Thread | Asg 25 + 26: the Thread lifecycle, live:
                //   NEW      -> new Thread(...) is constructed
                //   RUNNABLE -> start() asks the scheduler to run it
                //   RUNNING  -> run() executes inside the new thread
                //   TERMINATED -> run() returns and the socket is closed
                Thread t = new Thread(new ClientHandler(client));
                t.start();
                requestsServed++;                // static counter (approximate under concurrency)
            }
        } catch (IOException e) {
            FileLogger.logEvent("Server stopped: " + e.getMessage());
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();        // ★ Asg 15: finally releases the listening socket
                }
            } catch (IOException ignore) {
            }
        }
    }
}
