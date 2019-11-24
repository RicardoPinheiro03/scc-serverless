package serverless;

import com.microsoft.azure.functions.annotation.*;

import redis.clients.jedis.Jedis;
import srv.RedisCache;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer Trigger.
 */
public class CosmosDBFunction {
    static int count = 0;

    @FunctionName("cosmosDBtest")
    public void cosmosDbProcessor(
            @CosmosDBTrigger(name = "users",
                    databaseName = "scccosmos41631",
                    collectionName = "Users",
                    createLeaseCollectionIfNotExists = true,
                    connectionStringSetting = "AzureCosmosDBConnection") String[] users,
            final ExecutionContext context ) {
        try (Jedis jedis = RedisCache.getCache().getJedisPool().getResource()) {
            jedis.set("serverless::cosmos::users", new Gson().toJson(users));
        }
    }

}
