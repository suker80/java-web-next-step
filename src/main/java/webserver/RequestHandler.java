package webserver;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpHeader;
import util.HttpRequestUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static db.DataBase.*;
import static util.HttpRequestUtils.parseQueryString;
import static util.IOUtils.readData;

public class RequestHandler extends Thread {
    public static final String WEBAPP = "./webapp";
    public static final String BASE_URL = "http://localhost:8080/";
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = bufferedReader.readLine();
            if (line == null) {
                return;
            }

            String[] http = line.split(" ");
            String method = http[0];
            String url = http[1];
            String requestUrl = url;
            String[] s = url.split(" ");
            Map<String, String> queryString = new HashMap<>();
            int index = url.indexOf("?");
            if (index != -1) {
                requestUrl = url.substring(0, index);
                String param = url.substring(index + 1);
                queryString = parseQueryString(param);
            }
            HashMap<String, String> header;
            header = readHeader(bufferedReader, line);

            if (method.equals("GET")) {

                if (requestUrl.equals("/user/create")) {
                    User user = new User(
                            queryString.get("userId"),
                            queryString.get("password"),
                            queryString.get("name"),
                            queryString.get("email")

                    );
                    addUser(user);
                } else if (requestUrl.equals("/user/list")) {
                    Map<String, String> cookie = HttpRequestUtils.parseCookies(header.get("Cookie"));

                    if (Boolean.parseBoolean(cookie.get("logined"))) {

                        ArrayList<User> users = new ArrayList<>(findAll());
                        StringBuilder sb = new StringBuilder();
                        for (User user : users) {
                            sb.append(user.toString()).append('\n');
                        }
                        DataOutputStream dos = new DataOutputStream(out);
                        response200Header(dos, sb.length(),header);
                        responseBody(dos, String.valueOf(sb).getBytes(StandardCharsets.UTF_8));

                    } else {
                        response302Header(new DataOutputStream(out), "/index.html");
                    }


                } else {
                    DataOutputStream dos = new DataOutputStream(out);
                    byte[] body = Files.readAllBytes(new File(WEBAPP + requestUrl).toPath());
                    response200Header(dos, body.length, header);
                    responseBody(dos, body);
                }


            } else if (method.equals("POST")) {

                if (requestUrl.equals("/user/create")) {
                    String requestBody = readData(bufferedReader, Integer.parseInt(header.get(HttpHeader.CONTENT_LENGTH)));
                    queryString = parseQueryString(requestBody);
                    User user = new User(
                            queryString.get("userId"),
                            queryString.get("password"),
                            queryString.get("name"),
                            queryString.get("email")

                    );
                    addUser(user);
                    log.info(user.toString());

                    DataOutputStream dos = new DataOutputStream(out);
                    String redirectUrl = "http://localhost:8080/index.html";
                    response302Header(dos, redirectUrl);
                } else if (requestUrl.equals("/user/login")) {
                    String requestBody = readData(bufferedReader, Integer.parseInt(header.get(HttpHeader.CONTENT_LENGTH)));
                    queryString = parseQueryString(requestBody);
                    String userId = queryString.get("userId");
                    String password = queryString.get("password");
                    User findUser = findUserById(userId);
                    DataOutputStream dos = new DataOutputStream(out);
                    if (findUser.getPassword().equals(password)) {
                        responseLogin302SuccessHeader(dos);

                    } else {

                        dos.writeBytes("Set-Cookie: logined=false\n");
                        dos.writeBytes("access-control-expose-headers: Set-Cookie\r\n");

                        response302Header(dos, BASE_URL + "/login.html\r\n");
                    }
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseLogin302SuccessHeader(DataOutputStream dos) throws IOException {
        dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
        dos.writeBytes("Set-Cookie: logined=true \r\n");
        dos.writeBytes("Location: /index.html \r\n");
        dos.writeBytes("Accept: text/css, */*;q=0.1\r\n");

        dos.writeBytes("\r\n");


    }

    private HashMap<String, String> readHeader(BufferedReader bufferedReader, String line) throws IOException {
        HashMap<String, String> headerMap = new HashMap<>();
        while (!"".equals(line)) {
            line = bufferedReader.readLine();
            if (line.isEmpty()) {
                break;
            }
            String[] split = line.split(": ");
            headerMap.put(split[0], split[1]);


        }
        return headerMap;
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, HashMap<String, String> header) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            if (header.get("Accept").equals("text/html")) {
                dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            } else if (header.get("Accept").equals("text/css")) {
                dos.writeBytes("Content-type : text/css");
            }
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
//            dos.writeBytes("Accept: text/css, */*;q=0.1\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \n");
            dos.writeBytes("Accept: text/css, */*;q=0.1\r\n");
            dos.writeBytes("Location: " + redirectUrl + '\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
