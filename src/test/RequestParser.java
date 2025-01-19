package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

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
        // Read and parse request line
        String requestLine = reader.readLine();
        System.out.println("Request line: " + requestLine);

        if (requestLine == null) {
            return null;
        }

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            return null;
        }

        String httpCommand = requestParts[0];
        String fullUri = requestParts[1];

        // Parse URI parameters
        Map<String, String> parameters = new HashMap<>();
        String uri = fullUri;
        String[] uriParts;

        int questionMarkIndex = fullUri.indexOf('?');
        if (questionMarkIndex != -1) {
            uri = fullUri.substring(0, questionMarkIndex);
            String queryString = fullUri.substring(questionMarkIndex + 1);
            String[] queryParts = queryString.split("&");
            for (String part : queryParts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    parameters.put(keyValue[0], keyValue[1]);
                }
            }
        }

        // Parse URI segments
        String[] tempParts = uri.substring(1).split("/");
        uriParts = tempParts[0].isEmpty() ? new String[0] : tempParts;

        // Read headers
        String line;
        int contentLength = 0;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            System.out.println("Header line: " + line);
            if (line.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.substring("content-length:".length()).trim());
                System.out.println("Content length found: " + contentLength);
            }
        }

        // Read content if exists
        if (contentLength > 0) {
            // Read parameter line
            String paramLine = reader.readLine();
            System.out.println("Parameter line: " + paramLine);
            if (paramLine != null && paramLine.contains("=")) {
                String[] paramParts = paramLine.split("=");
                if (paramParts.length == 2) {
                    parameters.put(paramParts[0], paramParts[1]);
                }
            }

            // Skip empty line
            reader.readLine();

            // Read actual content
            String contentLine = reader.readLine();
            System.out.println("Content line: " + contentLine);

            if (contentLine != null) {
                // Make sure to include the newline at the end
                String finalContent = contentLine + "\n";
                byte[] contentBytes = finalContent.getBytes();
                System.out.println("Final content (as string): '" + finalContent + "'");
                return new RequestInfo(httpCommand, fullUri, uriParts, parameters, contentBytes);
            }
        }

        // If no content, return with empty content
        return new RequestInfo(httpCommand, fullUri, uriParts, parameters, new byte[0]);
    }
}