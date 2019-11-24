package serverless;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;

import redis.clients.jedis.Jedis;
import srv.RedisCache;

import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 * These functions can be accessed at {Server_URL}/api/{route}
 */
public class HttpFunction {
    public static int count = 0;

    @FunctionName("http-test")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS, route = "serverless/test") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        StringBuffer result = new StringBuffer();
        result.append("Serverless stats: v. 0001 : \n");
        result.append("HTTP functions called ");
        synchronized(HttpFunction.class) {
            result.append(HttpFunction.count);
        }
        result.append(" times with the current container\n");
        result.append("CosmosDB functions called ");
        synchronized(CosmosDBFunction.class) {
            result.append(CosmosDBFunction.count);
        }
        result.append(" times with the current container\n");
        result.append("Timer functions called ");
        synchronized(TimerFunction.class) {
            result.append(TimerFunction.count);
        }
        result.append(" times with the current container\n");
        result.append("BlobStore functions called ");
        synchronized(BlobStoreFunction.class) {
            result.append(BlobStoreFunction.count);
        }
        result.append(" times with the current container\n");
        return request.createResponseBuilder(HttpStatus.OK).body(result.toString()).build();
    }


    @FunctionName("get-redis")
    public HttpResponseMessage getRedis(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS, route = "serverless/redis/{key}") HttpRequestMessage<Optional<String>> request,
            @BindingName("key") String key,
            final ExecutionContext context) {
        synchronized(HttpFunction.class) {
            HttpFunction.count++;
        }
        try (Jedis jedis = RedisCache.getCache().getJedisPool().getResource()) {
            String val = jedis.get(key);
            return request.createResponseBuilder(HttpStatus.OK).body( "GET key = " + key + "; val = " + val).build();
        }
    }

    @FunctionName("set-redis")
    public HttpResponseMessage setRedis(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS, route = "serverless/redis/{key}") HttpRequestMessage<Optional<String>> request,
            @BindingName("key") String key,
            final ExecutionContext context) {
        synchronized(HttpFunction.class) {
            HttpFunction.count++;
        }
        String val = request.getBody().orElse("");
        try (Jedis jedis = RedisCache.getCache().getJedisPool().getResource()) {
            jedis.set(key,val);
            return request.createResponseBuilder(HttpStatus.OK).body( "SET key = " + key + "; val = " + val).build();
        }
    }

    @FunctionName("echo")
    public HttpResponseMessage echoRedis(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS, route = "serverless/echo/{key}") HttpRequestMessage<Optional<String>> request,
            @BindingName("key") String key,
            final ExecutionContext context) {
        synchronized(HttpFunction.class) {
            HttpFunction.count++;
        }
        try (Jedis jedis = RedisCache.getCache().getJedisPool().getResource()) {
            return request.createResponseBuilder(HttpStatus.OK).body("get key = " + key).build();
        }
    }
}
