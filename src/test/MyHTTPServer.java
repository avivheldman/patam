package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

public class MyHTTPServer extends Thread implements HTTPServer {
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = true;

    private final Map<String, Servlet> getServlets = new HashMap<>();
    private final Map<String, Servlet> postServlets = new HashMap<>();
    private final Map<String, Servlet> deleteServlets = new HashMap<>();

    public MyHTTPServer(int port) {
        this.port = port;
      //System.out.println("[Server] Created server on port " + port);
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            //System.out.println("[Server] Started listening on port " + port);

            while (running) {
                try {
                    serverSocket.setSoTimeout(1000); // Check periodically if running is false
                    //System.out.println("[Server] Waiting for client connection...");
                    Socket clientSocket = serverSocket.accept();
                    //System.out.println("[Server] Client connected from: " + clientSocket.getInetAddress());
                    handleClient(clientSocket);
                } catch (SocketTimeoutException e) {
                    // Continue waiting for connections
                }
            }
        } catch (IOException e) {
            if (running) { // Print errors only if the server wasn't intentionally stopped
                //System.out.println("[Server] Error in main server loop: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            //System.out.println("[Server] Exiting main server loop");
        }
    }


    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream()
        ) {
          //System.out.println("[Server] Starting to handle client request");

            RequestParser.RequestInfo ri = RequestParser.parseRequest(reader);
            if (ri != null) {
              //System.out.println("[Server] Parsed request: " + ri.getHttpCommand() + " " + ri.getUri());
                Servlet servlet = findServlet(ri.getHttpCommand(), ri.getUri());
                if (servlet != null) {
                  //System.out.println("[Server] Found matching servlet, handling request...");
                    servlet.handle(ri, out);
                  //System.out.println("[Server] Servlet finished processing request");
                } else {
                  //System.out.println("[Server] No matching servlet found. Sending 404.");
                    String response = "HTTP/1.1 404 Not Found\r\n\r\n";
                    out.write(response.getBytes());
                }
                out.flush();
            } else {
              //System.out.println("[Server] Failed to parse request");
            }
        } catch (IOException e) {
          //System.out.println("[Server] Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
              //System.out.println("[Server] Closed client connection");
            } catch (IOException e) {
              //System.out.println("[Server] Error closing client socket: " + e.getMessage());
            }
        }
    }

    private Servlet findServlet(String httpCommand, String uri) {
      //System.out.println("[Server] Finding servlet for " + httpCommand + " " + uri);

        // Remove query parameters for matching
        int questionMark = uri.indexOf('?');
        String matchUri = questionMark != -1 ? uri.substring(0, questionMark) : uri;
      //System.out.println("[Server] Matching against URI: " + matchUri);

        Map<String, Servlet> servletMap;
        switch (httpCommand.toUpperCase()) {
            case "GET":
                servletMap = getServlets;
                break;
            case "POST":
                servletMap = postServlets;
                break;
            case "DELETE":
                servletMap = deleteServlets;
                break;
            default:
              //System.out.println("[Server] Unsupported HTTP command: " + httpCommand);
                return null;
        }

        String matchingUri = "";
        Servlet matchingServlet = null;

        for (String registeredUri : servletMap.keySet()) {
          //System.out.println("[Server] Checking against registered URI: " + registeredUri);
            if (matchUri.startsWith(registeredUri) && registeredUri.length() > matchingUri.length()) {
                matchingUri = registeredUri;
                matchingServlet = servletMap.get(registeredUri);
              //System.out.println("[Server] Found matching servlet for URI: " + registeredUri);
            }
        }

        return matchingServlet;
    }

    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
      //System.out.println("[Server] Adding servlet for " + httpCommand + " " + uri);
        switch (httpCommand.toUpperCase()) {
            case "GET":
                getServlets.put(uri, s);
                break;
            case "POST":
                postServlets.put(uri, s);
                break;
            case "DELETE":
                deleteServlets.put(uri, s);
                break;
        }
    }

    @Override
    public void removeServlet(String httpCommand, String uri) {
      //System.out.println("[Server] Removing servlet for " + httpCommand + " " + uri);
        switch (httpCommand.toUpperCase()) {
            case "GET":
                getServlets.remove(uri);
                break;
            case "POST":
                postServlets.remove(uri);
                break;
            case "DELETE":
                deleteServlets.remove(uri);
                break;
        }
    }

    @Override
    public void start() {
      //System.out.println("[Server] Starting server...");
        super.start();
    }

    @Override
    public void close() {
      //System.out.println("[Server] Shutting down server...");
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
              //System.out.println("[Server] Server socket closed");
            }

            for (Servlet s : getServlets.values()) s.close();
            for (Servlet s : postServlets.values()) s.close();
            for (Servlet s : deleteServlets.values()) s.close();
          //System.out.println("[Server] All servlets closed");

        } catch (IOException e) {
          //System.out.println("[Server] Error while closing server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}