package util;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class HttpResponseTest {
    public static final String WEBAPP = "./webapp/";

    private final String testDirectory = "./src/test/resources/";

    @Test
    public void responseForward() {
        HttpResponse response = new HttpResponse(createOutPutStream("Http_Forward.txt"));
        response.forward(WEBAPP + "/index.html");
    }

    @Test
    public void responseRedirect() {
        // Http_redirect.txt 결과는 응답 header에
        // Location 정보가 /index.html로 있어야 한다.
        HttpResponse response = new HttpResponse(createOutPutStream("Http_Redirect.txt"));
        response.sendRedirect("./index.html");
    }

    @Test
    public void responseCookies() {
        HttpResponse response = new HttpResponse(createOutPutStream("Http_Cookie.txt"));
        response.sendRedirect("./index.html");

    }

    private OutputStream createOutPutStream(String filename) {
        try {
            return new FileOutputStream(testDirectory + filename);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}