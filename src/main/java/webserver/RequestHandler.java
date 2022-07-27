package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpHeader;
import util.HttpRequestUtils;
import util.IOUtils;

import static db.DataBase.addUser;
import static util.HttpRequestUtils.parseQueryString;
import static util.IOUtils.readData;

public class RequestHandler extends Thread {
    public static final String WEBAPP = "./webapp";
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            String line = bufferedReader.readLine();
            if (line == null) {
                return;
            }

            String[] http = line.split(" ");
            String method = http[0];
            String url = http[1];
            String protocol = http[2];
            String requestUrl = url;
            Map<String, String> queryString;
            queryString = new HashMap<>();
            int index = url.indexOf("?");
            if (index != -1) {
                requestUrl = url.substring(0, index);
                String param = url.substring(index + 1);
                queryString = parseQueryString(param);

            }
            HashMap<String, String> readHeader = readHeader(bufferedReader, line);

            if (method.equals("GET")) {

                if (requestUrl.equals("/user/create")) {
                    User user = new User(
                            queryString.get("userId"),
                            queryString.get("password"),
                            queryString.get("name"),
                            queryString.get("email")

                    );
                    addUser(user);
                    log.info(user.toString());
                }
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File(WEBAPP + requestUrl).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
            } else if (method.equals("POST")) {

                if (requestUrl.equals("/user/create")) {
                    String requestBody = readData(bufferedReader, Integer.parseInt(readHeader.get(HttpHeader.CONTENT_LENGTH)));
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
                }
            }


//            HashMap<String, String> readHeader = readHeader(bufferedReader, line);
//
//            DataOutputStream dos = new DataOutputStream(out);
//            byte[] body = Files.readAllBytes(new File(WEBAPP + url).toPath());
//            response200Header(dos, body.length);
//            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos,String redirectUrl) {
        try{
            dos.writeBytes("HTTP/1.1 302 Found \n");
            dos.writeBytes("Location: "+redirectUrl+'\n');
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
