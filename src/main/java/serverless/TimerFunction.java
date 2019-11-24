package serverless;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

//import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.google.gson.JsonArray;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.*;

import redis.clients.jedis.Jedis;
import srv.CosmosDBFactory;
import srv.RedisCache;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import serverless.HttpFunction;
//import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer Trigger.
 */
public class TimerFunction {
    private static final int CACHE_THRESHOLD = 20;
    public static int count = 0;

    @FunctionName("periodic-compute")
    public void cosmosFunction( @TimerTrigger(name = "keepAliveTrigger", schedule = "*/5 * * * * *") String timerInfo,
                                ExecutionContext context) {
        synchronized(HttpFunction.class) {
            HttpFunction.count++;
        }

        try (Jedis jedis = RedisCache.getCache().getJedisPool().getResource()) {
            jedis.set("serverlesstime", new SimpleDateFormat().format(new Date()));
            List<String> lst = jedis.lrange("MostLikedPosts", 0, CACHE_THRESHOLD);
            try {
                FeedOptions queryOptions = new FeedOptions();
                queryOptions.setEnableCrossPartitionQuery(true);
                queryOptions.setMaxDegreeOfParallelism(-1);
                String PostsCollection = CosmosDBFactory.getCollectionString("Posts");
                Iterator<FeedResponse<Document>> it = CosmosDBFactory.getDocumentClient()
                        .queryDocuments(PostsCollection, "SELECT * FROM Posts p ORDER BY p.numberLikes DESC OFFSET 0 LIMIT 20",
                                queryOptions)
                        .toBlocking()
                        .getIterator();

                while(it.hasNext()) {
                    for(Document d : it.next().getResults()) {
                        Long cnt = jedis.lpush("MostLikedPosts", d.toJson());
                        if (cnt > CACHE_THRESHOLD)
                            jedis.ltrim("MostLikedPosts", 0, cnt);
                    }
                }
                /*String result = "[";
                while( it.hasNext())
                    for( Document d : it.next().getResults()) {
                        if( result.length() > 1)
 {                            result += ",";
                        result += d.toJson();
                    }
                result += "]";*/

                //jedis.set("MostLikedPosts", result); // ==== Later on change to MostLikedPosts ====
            } catch (Exception e) {
                //jedis.lpush("MostLikedPosts", "vinhoVerde");
                e.printStackTrace();
            }
        }
    }
}
