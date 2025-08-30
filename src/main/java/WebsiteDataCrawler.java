import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class WebsiteDataCrawler {

    WebsiteDataCrawler(String line , int i) {
        // Initialize Elasticsearch client
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")))) {


                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(line); // Replace with your website URL
                CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
                Document document = Jsoup.parse(httpResponse.getEntity().getContent(), "UTF-8", "");

                // Extract content
                String title = document.select("#news > div.container.night_mode_news > div > div.col-md-22.col-sm-24.gutter_news > div.top_news_title > div.title > h1").text();
                String newsID = document.getElementsByClass("news_id").text();
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

            CreateIndexRequest request=new CreateIndexRequest("news"+i);
            //IndexRequest request = new IndexRequest("news"+i);
/*
            CreateIndexRequest settings = request.settings(Settings.builder()
                    .put("analysis.analyzer.persian_analyzer",""));

            request.mapping( "{\n"+
                    " \"blog\": {\n"+
                    "  \"properties\": {\n"+
                    "   \"Title\": { \n"+
                    "  \"type\":     \"" +title+
                    "\", \n"+
                    "  \"analyzer\": \"parsi\"\n"+
                    "}\n"+
                    "}\n"+
                    "}\n"+
                    "}", XContentType.JSON);

 */
                Settings.Builder settingsBuilder =
                    Settings.builder()
                            .put("analysis.analyzer.persian_analyzer.type", "custom")
                            .put("analysis.analyzer.persian_analyzer.tokenizer", "standard")
                            .putList("analysis.analyzer.persian_analyzer.filter",
                                    "parsi_stem_filter" , "parsi_normalizer" , "parsi_stop_filter");
                request.settings(settingsBuilder);

            //CreateIndexResponse createIndexResponse=client.indices().create(request,RequestOptions.DEFAULT);
            // Index data into Elasticsearch

            Map<String,Object> mappings = new HashMap<>();
            Map<String,Object> properties= new HashMap<>();
            properties.put("Title",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("ID",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("Date",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("Brief",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("Tags",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("Body",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("Service",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("Category",Map.of("type","text","analyzer","persian_analyzer"));
            properties.put("URL",Map.of("type","text"));

            mappings.put("properties",properties);

            /*

            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            {
                builder.field("Title", title);
                builder.field("ID", newsID);
                builder.field("Date", newsDate);
                builder.field("Brief", brieftxt);
                builder.field("Tags", tags);
                builder.field("Body", bodytxt);
                builder.field("Service", seviceID);
                builder.field("Category", catID);
                builder.field("URL", newsURL);
            }
            builder.endObject();
            request.source(builder);
*/
                //IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            client.indices().create((CreateIndexRequest) Map.of("index","news"+i,
                    "settings",settingsBuilder,"mappings",mappings),
                    RequestOptions.DEFAULT);

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

            request.source((BytesReference) jsonMap,XContentType.JSON);

            /*
            if(indexResponse !=null && indexResponse.isAcknowledged()){
                GetIndexRequest getIndexRequest=new GetIndexRequest("news"+i);
                GetIndexResponse getIndexResponse=client.indices().get(getIndexRequest,RequestOptions.DEFAULT);
                String[] indices=getIndexResponse.getIndices();

                System.out.println(indices);
            }

             */

            GetIndexRequest getIndexRequest=new GetIndexRequest("news"+i);
            GetIndexResponse getIndexResponse=client.indices().get(getIndexRequest,RequestOptions.DEFAULT);
            String[] indices=getIndexResponse.getIndices();

            System.out.println(indices);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
