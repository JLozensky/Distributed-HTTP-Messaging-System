package SharedUtilities;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DatabaseInteractor {

    protected static final DynamoDbClient dynamoClient = DynamoDbClient.builder()
        .region(Region.US_EAST_1)
        .build();

    protected static final DynamoDbEnhancedClient dynamoEnhancedClient = DynamoDbEnhancedClient.builder()
        .dynamoDbClient(dynamoClient)
        .build();

}
