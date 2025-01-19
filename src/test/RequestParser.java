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
        String requestLine = reader.readLine();

        if (requestLine == null) {
            return null;
        }

        // Parse request line
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            return null;
        }

        String httpCommand = requestParts[0];
        String fullUri = requestParts[1];

        // Parse URI and parameters
        Map<String, String> parameters = new HashMap<>();
        String uri = fullUri;
        String[] uriParts;

        // Handle query parameters in URI
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

        // Parse headers and look for Content-Length
        String line;
        int contentLength = -1;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.substring("content-length:".length()).trim());
            }
        }

        // Read and parse content parameters
        StringBuilder contentBuilder = new StringBuilder();
        if (contentLength > 0) {
            // Read the first line which contains parameters
            String paramLine = reader.readLine();
            if (paramLine != null && paramLine.contains("=")) {
                String[] paramParts = paramLine.split("=");
                if (paramParts.length == 2) {
                    parameters.put(paramParts[0], paramParts[1]);
                }
            }

            // Skip empty line
            reader.readLine();

            // Read actual content
            String contentLine;
            while ((contentLine = reader.readLine()) != null) {
                contentBuilder.append(contentLine).append("\n");
            }
        }

        return new RequestInfo(httpCommand, fullUri, uriParts, parameters, contentBuilder.toString().getBytes());
    }
}