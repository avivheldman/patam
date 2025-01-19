package test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import test.RequestParser.RequestInfo;


public class MainTrain { // RequestParser


    private static void testParseRequest() {
        // Test data
        String request = "GET /api/resource?id=123&name=test HTTP/1.1\n" +
                "Host: example.com\n" +
                "Content-Length: 33\n"+
                "\n" +
                "filename=\"hello_world.txt\"\n"+
                "\n" +
                "hello world!\n"+
                "\n" ;

        BufferedReader input=new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.getBytes())));
        
        try {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(input);
            // Test HTTP command
            if (!requestInfo.getHttpCommand().equals("GET")) {
                System.out.println("HTTP command test failed (-5)");
            }

            // Test URI
            System.out.println(requestInfo.getUri());
            if (!requestInfo.getUri().equals("/api/resource?id=123&name=test")) {
                System.out.println("URI test failed (-5)");
            }

            // Test URI segments
            String[] expectedUriSegments = {"api", "resource"};
            if (!Arrays.equals(requestInfo.getUriSegments(), expectedUriSegments)) {
                System.out.println("URI segments test failed (-5)");
                for(String s : requestInfo.getUriSegments()){
                    System.out.println(s);
                }
            }
            // Test parameters
            Map<String, String> expectedParams = new HashMap<>();
            expectedParams.put("id", "123");
            expectedParams.put("name", "test");
            expectedParams.put("filename","\"hello_world.txt\"");
            if (!requestInfo.getParameters().equals(expectedParams)) {
                System.out.println("Parameters test failed (-5)");
            }

            // Test content
            byte[] expectedContent = "hello world!\n".getBytes();
            System.out.println(requestInfo.getContent() + "vs expected... " + expectedContent);
            if (!Arrays.equals(requestInfo.getContent(), expectedContent)) {
                System.out.println("Content test failed (-5)");
            }
            input.close();
        } catch (IOException e) {
            System.out.println("Exception occurred during parsing: " + e.getMessage() + " (-5)");
        }
    }

    public static void testServer() throws Exception {
        // יצירת מופע של השרת על פורט 8080 עם 10 ת'רדים
        MyHTTPServer server = new MyHTTPServer(8080, 10);

        // הוספת Servlet לבדיקה עם כתובת /test
        server.addServlet("GET", "/test", new Servlet() {
            @Override
            public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: 12\r\n" +
                        "\r\n" +
                        "Test Success";
                toClient.write(response.getBytes());
                toClient.flush();
            }

            @Override
            public void close() throws IOException {}
        });

        // הפעלת השרת
        server.start();
        Thread.sleep(1000); // ממתין לשרת שיתחיל לפעול

        // חיבור לשרת ושליחת בקשה
        try (Socket client = new Socket("localhost", 8080)) {
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            // שליחת בקשה
            out.println("GET /test HTTP/1.1");
            out.println("Host: localhost");
            out.println("Content-Length: 0");
            out.println(); // שורה ריקה לסיום הבקשה

            // קריאת התשובה
            String statusLine = in.readLine();
            if (statusLine == null || !statusLine.contains("200 OK")) {
                System.out.println("Test failed: Server did not respond with 200 OK (-10)");
            } else {
                System.out.println("Test passed: Server responded with 200 OK");
            }
        } catch (IOException e) {
            System.out.println("Test failed: Exception occurred (-10)");
        }

        // סגירת השרת
        server.close();
        Thread.sleep(1000); // ממתין לשרת שיסיים להיסגר
    }


    public static void main(String[] args) {
        testParseRequest(); // 40 points
        try{
            testServer(); // 60
        }catch(Exception e){
            System.out.println("your server throwed an exception (-60)");
        }
        System.out.println("done");
    }

}
