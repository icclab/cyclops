package ch.icclab.cyclops.rule.versioning;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.kohsuke.github.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

public class GitConnector {
    //List<GHEvent> events = Arrays.asList(GHEvent.PUSH);
    private GitHub gitHub;


    public GitConnector(String repo, String webHook) {
        try {
            gitHub = GitHub.connect();
            GHRepository repository = gitHub.getRepository(repo);

            List<GHEvent> events = Arrays.asList(GHEvent.PUSH);


            System.out.println(repository.getName());

            startServer();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Git connection is unable");
        }
    }
    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RequestHandler() );
        server.setExecutor(null);
        server.start();
    }
    class RequestHandler implements HttpHandler {

        public void handle(HttpExchange httpExchange) throws IOException {
            InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();

            String payload = URLDecoder.decode(query, "utf-8").replaceFirst("payload=", "");

            Gson gson = new Gson();
            JsonObject pushEvent = gson.fromJson(payload, JsonObject.class);

            System.out.println("Payload is: " + payload);

            if (httpExchange.getRequestHeaders().containsValue("push")) {
                System.out.println("Git pull performs..");
            }

            httpExchange.sendResponseHeaders(200, 0);
            OutputStream os = httpExchange.getResponseBody();
            os.close();
        }
    }
}
