package SharedClientClasses;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import org.apache.commons.httpclient.HttpMethod;

public class RequestData {

    /*
    Linked list for quick merging of RequestData
     */

    private final RequestDatum root;
    private RequestDatum fullTail;
    private RequestDatum nullTail;
    private static final String delimiter = ",";
    private String alternateDelimiter = null;
    private int numDatapoints;
    private int numUnsuccessfulDatapoints;
    private float avgLatency;

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
        int latency = Math.toIntExact(endTime.toEpochMilli() - startTime.toEpochMilli());
        this.numDatapoints += 1;
        if (responseCode != 200 && responseCode != 201) {
            this.numUnsuccessfulDatapoints += 1;
        }
        return this.addRecord(postType.getName(),responseCode,startTime,latency);

    }

    private String addRecord(String postType, int responseCode, Instant startTime, int latency){

        this.nullTail.recordRequest(postType, responseCode,startTime,latency);
        RequestDatum newTail = new RequestDatum();
        this.nullTail.setNext(newTail);
        this.fullTail = this.nullTail;
        this.nullTail = newTail;
        return this.fullTail.toString();
    }

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

    public String toDelimitedString() {
        if (!this.hasData()){
            return null;
        }

        String delim = getDelimiter();
        if (this.alternateDelimiter != null){
            delim = this.alternateDelimiter;
        } else {
            delim = delimiter;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(root.getRecordHeader());
        RequestDatum curNode = root;
        while(curNode.dataEntered()) {
            sb.append(curNode);
            sb.append(delim);
            curNode = curNode.getNext();
        }
        // remove final comma
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

    public String getMetrics() {
        long totalLatency;
        int meanResponseTime;
        int medianResponseTime;
        float throughput;
        int latencyPercentile99;
        int maxResponseTime;


        return"";

    }

    public float getAvgLatency() {
        return this.avgLatency;
    }

    public void calculateMetrics() {
    }


    private class RequestDatum{
        private Instant start;
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

        public void recordRequest(String requestType, int responseCode, Instant startTime, int latency) {
            this.requestType = requestType;
            this.responseCode = responseCode;
            this.start = startTime;
            this.latency = latency;
            this.dataEntered = true;

        }


        public boolean dataEntered(){
            return this.dataEntered;
        }

        public Instant getStart() {
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
            return o1.getLatency() - o2.getLatency();
        }
    }

}
