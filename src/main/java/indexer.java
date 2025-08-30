import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.xcontent.XContentType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.*;


public class indexer {

    indexer(String line , int i) {
        // Initialize Elasticsearch client
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")))) {


            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(line); // Replace with your website URL
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(httpResponse.getEntity().getContent(), "UTF-8", "");

            // Extract content
            String title = document.select("#news > div.container.night_mode_news > div > div.col-md-22.col-sm-24.gutter_news > div.top_news_title > div.title > h1").text();
            int newsID = Integer.parseInt(document.getElementsByClass("news_id").text());
            String newsDate= document.getElementsByClass("fa_date").text();
            String brieftxt;
            try {
                brieftxt = document.select("#newsMainBody > p:nth-child(1)").text();
            } catch (Exception e) {
                brieftxt="";
            }
            String bodytxt= document.select("#newsMainBody > p").text();
            String tags = document.getElementsByClass("tag_items").text();
            String seviceID=document.select("#box-top-news > div.hidden-xs.col-md-12.col-sm-36.col-ms-14 > div > a:nth-child(1)").text();
            String catID=document.select("#box-top-news > div.hidden-xs.col-md-12.col-sm-36.col-ms-14 > div > a:nth-child(2)").text();
            String newsURL=line;

            FileWriter writer = new FileWriter("C:\\Users\\Test\\Documents\\mydocs\\computereng\\information retrieval\\proj1\\news\\newsindex.txt",true);

            writer.write(tags);
            writer.write("\n");
            writer.flush();


            Map<String,Object> parsiAnalyzer= new HashMap<>();
            parsiAnalyzer.put("analysis.analyzer.persian_analyzer.type", "custom");
            parsiAnalyzer.put("analysis.analyzer.persian_analyzer.tokenizer", "standard");



            Map<String,Object> settings= new HashMap<>();
            settings.put("number_of_shards",1);
            settings.put("number_of_replicas",1);
            settings.put("analysis",Map.of("analyzer",Map.of("persian_analyzer",parsiAnalyzer)));





            Map<String,Object> mappings = new HashMap<>();
            Map<String,Object> properties= new HashMap<>();
            properties.put("Title",Map.of("type","text","analyzer","persian_analyzer",
                    "similarity","BM25","similarity.bm25.k1",1.0));
            properties.put("ID",Map.of("type","integer"));
            properties.put("Date",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("Brief",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("Tags",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("Body",Map.of("type","text","analyzer","persian_analyzer",
                    "similarity","BM25","similarity.bm25.k1",1.0));
            properties.put("Service",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("Category",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("URL",Map.of("type","text"));

            mappings.put("properties",properties);
            CreateIndexRequest createIndexRequest=new CreateIndexRequest("news"+i);
            createIndexRequest.mapping(mappings);
            createIndexRequest.settings(settings);


            IndexRequest indexRequest=new IndexRequest("news"+i);
            Map<String,Object> jsonMap= new HashMap<>();
            jsonMap.put("Title", title);
            jsonMap.put("ID", newsID);
            jsonMap.put("Date", newsDate);
            jsonMap.put("Brief", brieftxt);
            jsonMap.put("Tags", tags);
            jsonMap.put("Body", bodytxt);
            jsonMap.put("Service", seviceID);
            jsonMap.put("Category", catID);
            jsonMap.put("URL", newsURL);

            indexRequest.source(jsonMap,XContentType.JSON);

            IndexResponse indexResponse=client.index(indexRequest,RequestOptions.DEFAULT);

            System.out.println(indexResponse.getIndex());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
