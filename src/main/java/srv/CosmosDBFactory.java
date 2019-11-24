package srv;

import java.util.Properties;
import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import utils.AzureProperties;

public class CosmosDBFactory
{
	private static String COSMOS_DB_ENDPOINT;
	private static String COSMOS_DB_MASTER_KEY;
	private static String COSMOS_DB_DATABASE;
	private static AsyncDocumentClient client;
	
	public static synchronized AsyncDocumentClient getDocumentClient( ) {
		if( client == null) {
			Properties props = AzureProperties.getProperties();
			if( props.containsKey(AzureProperties.COSMOSDB_DATABASE))
				COSMOS_DB_DATABASE = props.getProperty(AzureProperties.COSMOSDB_DATABASE);
			if( props.containsKey(AzureProperties.COSMOSDB_KEY))
				COSMOS_DB_MASTER_KEY = props.getProperty(AzureProperties.COSMOSDB_KEY);
			if( props.containsKey(AzureProperties.COSMOSDB_URL))
				COSMOS_DB_ENDPOINT = props.getProperty(AzureProperties.COSMOSDB_URL);

			ConnectionPolicy connectionPolicy = ConnectionPolicy.GetDefault();
//			connectionPolicy.setConnectionMode(ConnectionMode.Direct);
			client = new AsyncDocumentClient.Builder()
		         .withServiceEndpoint(COSMOS_DB_ENDPOINT)
		         .withMasterKeyOrResourceToken(COSMOS_DB_MASTER_KEY)
		         .withConnectionPolicy(connectionPolicy)
		         .withConsistencyLevel(ConsistencyLevel.Session)
		         .build();
		}
		return client;
	}
	
	/**
	 * Returns the string to access a CosmosDB collection names col
	 * @param col Name of collection
	 * @return
	 */
	public static String getCollectionString( String col) {
		return String.format("/dbs/%s/colls/%s", COSMOS_DB_DATABASE, col);		
	}
	

}
