import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;

import java.io.IOException;

public class ElasticSearch {

    ElasticSearch(){


    }

    public SearchResponse Search(String fieldName, String query) {

        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")))) {


            SearchRequest searchRequest = new SearchRequest("*");

            searchRequest.source().query(QueryBuilders.matchQuery(fieldName, query));


            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            return searchResponse;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
