package SharedClientClasses;

import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.apache.commons.httpclient.HttpMethod;

public class RequestData {

    /*
    Linked list for quick merging of RequestData
     */

    private final RequestDatum root;
    private RequestDatum fullTail;
    private RequestDatum nullTail;
    private static final String delimiter = "|";
    private String alternateDelimiter = null;
    private int numDatapoints;
    private float avgLatency;
    private PriorityQueue<RequestDatum> percentileTracker;

    public RequestData() {
        this.root = new RequestDatum();
        this.nullTail = this.root;
        this.fullTail = null;
        this.numDatapoints = 0;
    }

    public synchronized RequestData merge(RequestData data) {
        if (!data.hasData()) {
            return this;
        }
        if (!this.hasData()) {
            return data;
        }
        this.fullTail.setNext(data.getRoot());
        this.fullTail = data.getFullTail();
        this.nullTail = data.getNullTail();
        this.numDatapoints += data.getNumDatapoints();

        return this;
    }

    private RequestDatum getNullTail() {
        return this.nullTail;
    }

    private RequestDatum getFullTail() {
        return this.fullTail;
    }

    private int getNumDatapoints(){ return this.numDatapoints; }

    public static RequestData merge(ArrayList<RequestData> dataList) {
        if ( dataList == null || dataList.size() == 0 ) {
            return new RequestData();
        }
        RequestData requestData = dataList.get(0);
        for (int i = 1; i < dataList.size(); i++) {
            requestData.merge(dataList.get(i));
        }
        return requestData;
    }

    private RequestDatum getRoot() {
        return this.root;
    }

    public String addRecord(HttpMethod postType, int responseCode, Instant startTime, Instant endTime){
        // todo implement start and end time validation and throw error
        LocalDateTime start = this.makeLocalDate(startTime);
        long latency = Duration.between(startTime,endTime).toMillis();
        this.numDatapoints += 1;
        return this.addRecord(postType.getName(),responseCode,start,(int)latency);

    }

    private LocalDateTime makeLocalDate(Instant i) {
        return LocalDateTime.ofInstant(i, ZoneId.systemDefault());
    }

    private String addRecord(String postType, int responseCode, LocalDateTime startTime, int latency){

        this.nullTail.recordRequest(postType, responseCode,startTime,latency);
        RequestDatum newTail = new RequestDatum();
        this.nullTail.setNext(newTail);
        this.fullTail = this.nullTail;
        this.nullTail = newTail;
        return this.fullTail.toString();
    }

    /**
     * Allows setting an alternate delimeter for return strings
     * @param newDelimiter
     */
    public void setDelimiter(String newDelimiter) {
        this.alternateDelimiter = newDelimiter;
    }

    public boolean hasData() {

        return this.root.dataEntered();
    }

    private String getDelimiter() {
        if (this.alternateDelimiter != null){
            return this.alternateDelimiter;
        } else {
            return delimiter;
        }
    }


    public String getMetrics() {
        long totalLatency = 0;
        long meanResponseTime;
        int medianResponseTime;
        float throughput;
        long latency99Percentile  =  Math.round(.99 * this.numDatapoints);
        int maxResponseTime = 0;
        RequestComparator comparator = new RequestComparator();
        this.percentileTracker = new PriorityQueue<>(comparator);

        // set the current node to root
        RequestDatum curNode = this.root;

        // Iterate through all nodes to sum the total latency and also find the target percentile
        while(curNode.dataEntered){
            int curLatency = curNode.getLatency();
            totalLatency += curLatency;
            if (this.percentileTracker.size() < latency99Percentile){
                this.percentileTracker.add(curNode);
                if (curLatency > maxResponseTime) {maxResponseTime = curLatency;}
            } else if
             (curLatency < this.percentileTracker.peek().getLatency()){
                percentileTracker.poll();
                percentileTracker.add(curNode);
                if (curLatency > maxResponseTime) {maxResponseTime = curLatency;}
            }
            curNode = curNode.next;
        }

        System.out.println("\ntotal requests " + this.numDatapoints + "\n");
        System.out.println("total latency " + totalLatency + "ms\n");
        System.out.println("time taken in ms " + (totalLatency / 256) + "\n");
        System.out.println("max response time " + maxResponseTime + " ms\n");
        System.out.println("mean latency " + (totalLatency / this.numDatapoints) + " ms\n");
        System.out.println("throughput requests/sec " + (this.numDatapoints / ((totalLatency / 1000) /256)));
        System.out.println("99 percentile in ms " + percentileTracker.poll().getLatency() + " ms\n");

        return"";

    }

    public float getAvgLatency() {
        return this.avgLatency;
    }



    private class RequestDatum{
        private LocalDateTime start;
        private String requestType;
        private int latency;
        private RequestDatum next;
        private boolean dataEntered;
        private int responseCode;

        public RequestDatum() {
            this.dataEntered = false;
            this.next = null;
            this.requestType = null;
        }

        public void recordRequest(String requestType, int responseCode, LocalDateTime startTime, int latency) {
            this.requestType = requestType;
            this.responseCode = responseCode;
            this.start = startTime;
            this.latency = latency;
            this.dataEntered = true;

        }


        public boolean dataEntered(){
            return this.dataEntered;
        }

        public LocalDateTime getStart() {
            return this.start;
        }

        public String getRequestType() {
            return this.requestType;
        }

        public void setRequestType(String requestType) {
            this.requestType = requestType;
        }

        public int getLatency() {
            return this.latency;
        }

        public void setLatency(int latency) {
            this.latency = latency;
        }

        public RequestDatum getNext() {
            return this.next;
        }

        public boolean hasNext() {
            return this.next != null;
        }

        public void setNext(RequestDatum next) {
            this.next = next;
        }

        public String getRecordHeader() {
            return new String("RequestType,ResponseCode,StartTime,Latency");
        }

        @Override
        public String toString() {
            String delim = getDelimiter();

            StringBuilder sb = new StringBuilder();

            sb.append(this.requestType);
            sb.append(delim);
            sb.append(this.responseCode);
            sb.append(delim);
            sb.append(this.start);
            sb.append(delim);
            sb.append(this.latency);
            return sb.toString();
        }

    }

    private static class RequestComparator implements Comparator<RequestDatum> {

        /**
         * Compares its two arguments for order.  Returns a negative integer, zero, or a positive integer as the first
         * argument is less than, equal to, or greater than the second.<p>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or
         * greater than the second.
         * @throws NullPointerException if an argument is null and this comparator does not permit null arguments
         * @throws ClassCastException   if the arguments' types prevent them from being compared by this comparator.
         */
        @Override
        public int compare(RequestDatum o1, RequestDatum o2) {
            return o2.getLatency() - o1.getLatency();
        }
    }

}
