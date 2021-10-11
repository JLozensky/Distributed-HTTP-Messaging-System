package Client2;

import java.util.ArrayList;
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

    public RequestData() {
        this.root = new RequestDatum();
        this.nullTail = this.root;
        this.fullTail = null;
    }

    public RequestData merge(RequestData data) {
        if (!data.hasData()) {
            return this;
        }
        if (!this.hasData()) {
            return data;
        }
        this.fullTail.setNext(data.getRoot());
        this.fullTail = data.getFullTail();
        this.nullTail = data.getNullTail();

        return this;
    }

    private RequestDatum getNullTail() {
        return this.nullTail;
    }

    private RequestDatum getFullTail() {
        return this.fullTail;
    }

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

    public void addRecord(HttpMethod postType, int responseCode, int startTime, int endTime){
        // todo implement start and end time validation and throw error

        this.nullTail.recordRequest(postType.toString(), responseCode,startTime,endTime);
        RequestDatum newTail = new RequestDatum();
        this.nullTail.setNext(newTail);
        this.fullTail = this.nullTail;
        this.nullTail = newTail;

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



    private class RequestDatum{
        private int start;
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

        public void recordRequest(String requestType, int responseCode, int startTime, int endTime) {
            this.requestType = requestType;
            this.responseCode = responseCode;
            this.start = startTime;
            this.latency = endTime - startTime;
            this.dataEntered = true;

        }


        public boolean dataEntered(){
            return dataEntered;
        }

        public int getStart() {
            return start;
        }

        public int getEndTime() {
            return this.start + this.latency;
        }

        public String getRequestType() {
            return requestType;
        }

        public void setRequestType(String requestType) {
            this.requestType = requestType;
        }

        public int getLatency() {
            return latency;
        }

        public void setLatency(int latency) {
            this.latency = latency;
        }

        public RequestDatum getNext() {
            return next;
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

}
