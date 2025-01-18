package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {

    public static class RequestInfo {
        private String httpCommand;
        private String uri;
        private String[] uriParts;
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

        public String getHttpCommand() { return httpCommand; }
        public String getUri() { return uri; }
        public String[] getUriSegments() { return uriParts; }
        public Map<String, String> getParameters() { return parameters; }
        public byte[] getContent() { return content; }
    }

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        //System.out.println("[Parser] Starting to parse request...");

        // Read the first line (request line)
        String requestLine = reader.readLine();
        //System.out.println("[Parser] Request line: " + requestLine);

        if (requestLine == null) {
            //System.out.println("[Parser] Request line is null, returning null");
            return null;
        }

        // Parse request line
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            //System.out.println("[Parser] Invalid request line format");
            return null;
        }

        String httpCommand = requestParts[0];
        String fullUri = requestParts[1];
        //System.out.println("[Parser] HTTP Command: " + httpCommand);
        //System.out.println("[Parser] Full URI: " + fullUri);

        // Parse URI and parameters
        Map<String, String> parameters = new HashMap<>();
        String uri = fullUri;
        String[] uriParts;

        // Handle query parameters in URI
        int questionMarkIndex = fullUri.indexOf('?');
        if (questionMarkIndex != -1) {
            uri = fullUri.substring(0, questionMarkIndex);
            String queryString = fullUri.substring(questionMarkIndex + 1);
            //System.out.println("[Parser] Query string: " + queryString);

            String[] queryParts = queryString.split("&");
            for (String part : queryParts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    parameters.put(keyValue[0], keyValue[1]);
                    //System.out.println("[Parser] Added parameter: " + keyValue[0] + "=" + keyValue[1]);
                }
            }
        }

        // Parse URI segments
        String[] tempParts = uri.substring(1).split("/");
        uriParts = tempParts[0].isEmpty() ? new String[0] : tempParts;
        //System.out.println("[Parser] URI parts: " + String.join(", ", uriParts));

        //System.out.println("[Parser] Reading headers...");
        // Parse headers and look for Content-Length
        String line;
        int contentLength = -1;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            //System.out.println("[Parser] Header line: " + line);
            if (line.toLowerCase().startsWith("content-length:")) {
                try {
                    contentLength = Integer.parseInt(line.substring("content-length:".length()).trim());
                } catch (NumberFormatException e) {
                    //System.out.println("[Parser] Invalid Content-Length format");
                }
            }
        }

        // Read content if exists
        StringBuilder contentBuilder = new StringBuilder();
        // Parse content parameters
        // Parse content parameters if Content-Length > 0
        if (contentLength > 0) {
            //System.out.println("[Parser] Reading content with length: " + contentLength);
            char[] buffer = new char[contentLength];
            int totalRead = 0; // To track how much is read
            while (totalRead < contentLength) {
                int read = reader.read(buffer, totalRead, contentLength - totalRead);
                if (read == -1) break; // End of stream
                totalRead += read;
            }
            contentBuilder.append(buffer, 0, totalRead);
        }



        byte[] content = contentBuilder.toString().getBytes();
        //System.out.println("[Parser] Content read: " + contentBuilder.toString());

        //System.out.println("[Parser] Finished parsing request");
        return new RequestInfo(httpCommand, fullUri, uriParts, parameters, content);
    }
}