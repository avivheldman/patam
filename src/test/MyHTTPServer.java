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
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (running) {
                try {
                    serverSocket.setSoTimeout(1000);
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                } catch (SocketTimeoutException e) {
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream()
        ) {
            System.out.println("Client connected");

            RequestParser.RequestInfo ri = RequestParser.parseRequest(reader);
            if (ri != null) {
                Servlet servlet = findServlet(ri.getHttpCommand(), ri.getUri());
                if (servlet != null) {
                    System.out.println("Servlet found, handling request...");
                    servlet.handle(ri, out);
                    System.out.println("Response sent to client");
                } else {
                    System.out.println("No matching servlet found. Sending 404.");
                    String response = "HTTP/1.1 404 Not Found\r\n\r\n";
                    out.write(response.getBytes());
                }
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Servlet findServlet(String httpCommand, String uri) {
        // Remove query parameters for matching
        int questionMark = uri.indexOf('?');
        String matchUri = questionMark != -1 ? uri.substring(0, questionMark) : uri;

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
                return null;
        }

        String matchingUri = "";
        Servlet matchingServlet = null;

        for (String registeredUri : servletMap.keySet()) {
            if (matchUri.startsWith(registeredUri) && registeredUri.length() > matchingUri.length()) {
                matchingUri = registeredUri;
                matchingServlet = servletMap.get(registeredUri);
            }
        }

        return matchingServlet;
    }

    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
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
        super.start();
    }

    @Override
    public void close() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            for (Servlet s : getServlets.values()) s.close();
            for (Servlet s : postServlets.values()) s.close();
            for (Servlet s : deleteServlets.values()) s.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}