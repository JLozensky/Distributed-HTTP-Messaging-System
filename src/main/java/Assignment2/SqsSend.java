package Assignment2;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import software.amazon.awssdk.services.sqs.model.SqsException;

public class SqsSend {



    private static AmazonSQSClient getLocalStack() {
        AmazonSQSClientBuilder clientBuilder = AmazonSQSClientBuilder.standard();
        clientBuilder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8010",
            Regions.US_EAST_1.getName()));
        return (AmazonSQSClient) clientBuilder.build();
    }



    public static void main(String args[]) {
        AmazonSQSClient client = getLocalStack();
        try {
            ListQueuesRequest listQueuesRequest = new ListQueuesRequest();
            ListQueuesResult listQueuesResponse = client.listQueues(listQueuesRequest);

            for (String url : listQueuesResponse.getQueueUrls()) {
                System.out.println(url);
            }

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        System.exit(11);
    }

}
