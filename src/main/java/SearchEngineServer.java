import com.google.gson.Gson;
import com.sun.net.httpserver.*;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public class SearchEngineServer {
    public static void main(String[] args)throws IOException {
        int port=9000;
        HttpServer server =HttpServer.create(new InetSocketAddress(port),0);
        server.createContext("/search",new SearchHandler());
        server.setExecutor(null);
        server.start();
    }
    static class SearchHandler implements HttpHandler{
        public void handle(HttpExchange exchange)throws IOException{
            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                String requestURL = exchange.getRequestURI().toString();

                String field = getReqs(requestURL)[0];
                String query = URLDecoder.decode(getReqs(requestURL)[1], StandardCharsets.UTF_8);

                ElasticSearch elasticSearch=new ElasticSearch();
                SearchResponse searchResponse=elasticSearch.Search(field,query);
                StringBuilder response= new StringBuilder();

                for (SearchHit hit:searchResponse.getHits().getHits()) {
                    response.append(hit.getSourceAsMap().get("Title"));
                    response.append("\n");
                    response.append(hit.getSourceAsMap().get("URL"));
                    response.append("\n");
                    response.append(hit.getSourceAsMap().get("Brief"));
                    response.append("\n");
                    response.append("\n");
                    response.append("\n");
                }
                Gson gson=new Gson();
                AtomicReference<String> jsonResponse= new AtomicReference<>(gson.toJson(response));
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin","*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods","GET,POST,PUT.DELETE,OPTIONS");
                exchange.getResponseHeaders().set("Content-Type","application/json");
                exchange.sendResponseHeaders(200, jsonResponse.get().getBytes().length);

                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.get().getBytes());
                System.out.println(jsonResponse.get());
                FileWriter writer = new FileWriter("C:\\Users\\Test\\Documents\\mydocs\\computereng\\information retrieval\\proj1\\news\\response.txt",true);

                writer.write(jsonResponse.get());
                writer.write("\n");
                writer.flush();

                os.close();
            }
        }
    }
    public static String[] getReqs(String requestURL){
        String [] reqs= new String[2];
        reqs[1]=requestURL.split(":")[0].substring(14);
        reqs[0]= requestURL.split(":")[1];
        return reqs;
    }
}
