import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws IOException {
        String dir = "C:\\Users\\Test\\Documents\\mydocs\\computereng\\information retrieval\\proj1\\news\\newsurl2.txt";

        BufferedReader br = new BufferedReader(new FileReader(dir));
        // Fetch data from the website
        String l;
        int i=1;
        while((l = br.readLine()) != null) {
            try{
                String line= URLDecoder.decode(l, StandardCharsets.UTF_8);
                indexer ix = new indexer(line,i);
            }catch (IllegalArgumentException e){

            }

            i++;

        }

    }
}

