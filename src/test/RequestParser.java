package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {

    public static class RequestInfo {
        private String httpCommand;
        private String uri;
        private String[] uriParts;  // שינינו את השם של השדה מ-uriSegments ל-uriParts
        private Map<String, String> parameters;
        private byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriParts,
                           Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriParts = uriParts;
            this.parameters = parameters;
            this.content = content;
        }

        // Getters
        public String getHttpCommand() { return httpCommand; }
        public String getUri() { return uri; }
        public String[] getUriSegments() { return uriParts; }  // השם של המתודה נשאר getUriSegments
        public Map<String, String> getParameters() { return parameters; }
        public byte[] getContent() { return content; }
    }

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        //System.out.println("Starting to parse request...");

        // Read the first line (request line)
        String requestLine = reader.readLine();
        if (requestLine == null) {
           // System.out.println("Request line is null. Returning null.");
            return null;
        }
       // System.out.println("Request Line: " + requestLine);

        // Parse headers
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
             // System.out.println("Header: " + line);
        }
        //System.out.println("Finished reading headers.");

        // Read content
        StringBuilder contentBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
           // System.out.println("Content Line: " + line);
            contentBuilder.append(line).append("\n");
        }
        byte[] content = contentBuilder.toString().getBytes();
        //System.out.println("Finished reading content.");

        return new RequestInfo("GET", "/test", new String[] {}, new HashMap<>(), content);
    }
}