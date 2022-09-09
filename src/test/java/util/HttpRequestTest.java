package util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpRequestTest {

    private final String testDirectory = "src/test/resources/";

    @Test
    void request_GET() throws Exception {
        InputStream inputStream = Files.newInputStream(new File(testDirectory + "Http_Get.txt").toPath());

        HttpRequest request = new HttpRequest(inputStream);
        assertEquals("GET",request.getMethod());
        assertEquals("/user/create",request.getPath());
        assertEquals("keep-alive",request.getHeader("Connection"));
        assertEquals("javajigi",request.getParameter("userId"));


    }

    @Test
    void request_POST() throws IOException {
        InputStream inputStream = Files.newInputStream(new File(testDirectory + "Http_Post.txt").toPath());

        HttpRequest request = new HttpRequest(inputStream);
        assertEquals("POST",request.getMethod());
        assertEquals("/user/create",request.getPath());
        assertEquals("keep-alive",request.getHeader("Connection"));
        assertEquals("javajigi",request.getParameter("userId"));
    }
}
