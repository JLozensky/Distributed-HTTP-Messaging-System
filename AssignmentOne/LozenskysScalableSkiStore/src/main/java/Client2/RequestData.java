package Client2;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.apache.commons.httpclient.HttpMethod;
import SharedClientClasses.*;

/**
 * Linked list data structure to hold, merge, and evaluate latency data from server requests. Rather than make a
 * threadsafe collection each thread can have its own linked list and merge once all threads are done running. Adding
 * elements to the list is O(n) where n is number of datapoints merging is O(N) where N is the number of lists, in
 * relation to data points it is O(1)
 */
public class RequestData {


    private static final String delimiter = "|";
    private String alternateDelimiter = null;
    private final RequestComparator comparator = new RequestComparator();

    // Nodes to track the list's structure the root is the head of the list, the full tail will contain datapoints
    // while the nullTail will be a container for the next datapoint submitted
    private final RequestDatum root;
    private RequestDatum fullTail;
    private RequestDatum nullTail;

    // Metrics variables
    private RequestDatum earliestNode;
    private RequestDatum latestNode;
    private int numDatapoints;
    private long meanResponseTime;
    private int medianResponseTime;
    private int maxResponseTime;
    private int p99ResponseTime;
    private int numLists;
    private long totalTime;


    /**
     * Constructor initializes required parameters
     */
    public RequestData() {
        this.root = new RequestDatum();
        this.nullTail = this.root;
        this.fullTail = null;
        this.numDatapoints = 0;
        this.earliestNode = this.root;
        this.numLists = 1;
        this.maxResponseTime = 0;
    }


    /**
     * Merge function that combines two lists and relevant metrics fields
     * @param data the list to be merged to the current instance
     * @return The head of this list unless this list is empty, then return the head of data
     */
    public synchronized RequestData merge(RequestData data) {
        // if there are no records in the parameter data, return the head of this list and visa-versa
        if (!data.hasData()) {
            return this;
        }
        if (!this.hasData()) {
            return data;
        }

        // sum the count of datapoints from the two lists
        this.numLists += data.numLists;

        // if the start time of the head of the list to be merged is earlier than the current start time, set the
        // earliest node variable accordingly, same for the latestNode variable
        if (data.getRoot().getStart().isBefore(this.earliestNode.getStart())){
            this.earliestNode = data.getRoot();
        }
        if (data.getFullTail().getStart().isAfter(this.getFullTail().getStart())) {
            this.latestNode = data.getFullTail();
        }

        // Set the tail of this list to the head of the list to be merged
        this.fullTail.setNext(data.getRoot());
        this.fullTail = data.getFullTail();
        this.nullTail = data.getNullTail();
        this.numDatapoints += data.getNumDatapoints();

        return this;
    }

    /**
     * getter for the empty node at the end of the list
     * @return the nullTail node
     */
    private RequestDatum getNullTail() {
        return this.nullTail;
    }

    /**
     * getter for the full node second from the end of the list, points at nullTail
     * @return the fullTail node
     */
    private RequestDatum getFullTail() {
        return this.fullTail;
    }

    /**
     * getter
     * @return
     */
    private int getNumDatapoints(){ return this.numDatapoints; }

    private RequestDatum getRoot() {
        return this.root;
    }

    /**
     * Public facing method to gather the data from an http request and transform it to
     * @param requestType the type of http request made usually GET or POST
     * @param responseCode The status indication code returned by the server such as 200, 201, 404, etc...
     * @param startTime the time the request was sent to the server
     * @param endTime the time the response was received from the server
     * @return a delimited String containing the data RequestType, ResponseCode, StartTime, and Latency
     */
    public String addRecord(HttpMethod requestType, int responseCode, Instant startTime, Instant endTime){
        // Transform the start time to LocalDate object
        LocalDateTime start = this.makeLocalDate(startTime);

        // Calculate latency i.e. the delta between the time the request was sent and the response arrived
        long latency = Duration.between(startTime,endTime).toMillis();

        // increment the datapoints counter so the list knows how many datapoints it contains
        this.numDatapoints += 1;

        // call the private overloaded version of this method to add the new data to the list
        return this.addRecord(requestType.getName(),responseCode,start,(int)latency);
    }

    /**
     * Private method overloaded from the public one above to fill in the nullTail with the new data, move it to the
     * fullTail slot and make a new nullTail node
     * @param requestType the type of http request made usually GET or POST
     * @param responseCode The status indication code returned by the server such as 200, 201, 404, etc...
     * @param startTime the time the request was sent to the server
     * @param latency the time the response was received from the server
     * @return a delimited String containing the data RequestType, ResponseCode, StartTime, and Latency
     */
    private String addRecord(String requestType, int responseCode, LocalDateTime startTime, int latency){

        // Insert the data into the nullTail object
        this.nullTail.recordRequest(requestType, responseCode,startTime,latency);

        // Make a new nullTail
        RequestDatum newTail = new RequestDatum();

        // point the old nullTail to the new one
        this.nullTail.setNext(newTail);

        // set the old nullTail which now has data to the fullTail position in the list and the newTail to the nullTail
        this.fullTail = this.nullTail;
        this.nullTail = newTail;

        // return a delimited string of the data record
        return this.fullTail.toString();
    }

    /**
     * helper method to build a LocalDateTime object from an Instant object
     * @param i the Instant time object to be converted
     * @return the new LocalDateTime object
     */
    private LocalDateTime makeLocalDate(Instant i) {
        return LocalDateTime.ofInstant(i, ZoneId.systemDefault());
    }

    /**
     * Allows setting an alternate delimiter for formatted data strings
     * @param newDelimiter the new delimiter character(s)
     */
    public void setDelimiter(String newDelimiter) {
        this.alternateDelimiter = newDelimiter;
    }

    /**
     * Informs the caller whether there are any non-null datapoints in this list
     * @return true if the list contains data, else false
     */
    public boolean hasData() {

        return this.root.dataEntered();
    }

    /**
     * Gets the current delimiter, if none is set returns the default
     * @return a String to be used as a delimiter between data entries
     */
    private String getDelimiter() {
        if (this.alternateDelimiter != null){
            return this.alternateDelimiter;
        } else {
            return delimiter;
        }
    }

    /**
     * Calculates and plots all metrics for the datapoints contained within this list
     * @return the total wall time taken from the start of the earliest sent request to the start of the latest sent
     */
    public long getMetrics() {
        // if no latest node has been specified set it to the final node in the list that has data since it means no
        // merge occurred which effectively makes this list sorted by time
        if (this.latestNode == null) { this.latestNode = this.getFullTail(); }

        // set the total wall time taken from the start of the earliest sent request to the start of the latest sent
        this.totalTime = Duration.between(this.earliestNode.getStart(),this.latestNode.getStart()).toMillis();

        // find the index of the median if the list were sorted (if not merged the list is already sorted)
        long latencyMedianPercentileIndex = Math.round(.5 * this.numDatapoints);

        // find the difference between the indices of the median datapoint and the 99th percentile datapoint
        long numDataTo99Percentile  =  Math.round(.99 * this.numDatapoints) - latencyMedianPercentileIndex;

        // create a PriorityQueue for heap implementation with a comparator that sorts
        PriorityQueue<RequestDatum> percentileTracker = new PriorityQueue<>(this.comparator);

        // Call helper to create the list of lists that will hold latencies by the second in which the request was sent
        ArrayList<ArrayList<Integer>> latencyPlotData = this.createLatencyLists();

        // Call helper to process the latencies of all datapoints in the list, setting instance metrics where
        // applicable and filling in the latencyPlotData array lists
        this.processLatency(percentileTracker, latencyMedianPercentileIndex, latencyPlotData);

        // Call helper to find the mean latency for each second in the range of start times for the datapoints
        ArrayList<Integer> avgLatencyPlotData = this.calculateAverages(latencyPlotData);

        // Call helper to calculate the median and 99 percentile latency values
        this.calculatePercentileValues(percentileTracker,numDataTo99Percentile);

        // Call helper to print the metrics to the console
        this.printMetrics();

        // Call helper to draw the graph of mean latency by time in seconds, blocks until the graph window is closed
        this.drawMeanLatencyLineChart(avgLatencyPlotData);

        // Return the total wall time taken for use in comparing to other datasets
        return this.totalTime;

    }

    /**
     * Prints the ClientTwo assignment metrics to the console
     */
    private void printMetrics() {
        // Calculates the number of threads submitted in the args from the number of threads actually run, since
        // numThreads + 1/4 numThreads + 1/4 numThreads = 3/2 total threads each with their own list, we multiply by
        // the reciprocal of 3/2 aka 2/3 to get the original numThreads
        int numThreads = this.numLists * 2 / 3;

        // Print statements for all ClientTwo metrics
        System.out.println("\n\n" + numThreads + "-thread test results: \n");
        System.out.println("Total time (ms): " + this.totalTime + "\n");
        System.out.println("mean response time (ms): " + this.meanResponseTime + "\n");
        System.out.println("median response time (ms): " + this.medianResponseTime + "\n");

        // if the total wall time in seconds is less than 5 report in ms otherwise seconds
        if (this.totalTime/1000 < 5){
            System.out.println("throughput requests/ms " + (this.numDatapoints / (this.totalTime)) + "\n");
        } else {
            System.out.println("throughput requests/sec " + (this.numDatapoints / (this.totalTime / 1000)) + "\n");
        }
        System.out.println("99 percentile in ms " + this.p99ResponseTime + " ms\n");
        System.out.println("max response time " + maxResponseTime + " ms\n");
    }

    /**
     * draws a line chart showing the average latency per second across the range of all datapoint start times
     * @param avgLatencyPlotData An array where each index is a second in time since the earliest data record start
     *                           time and each value is the average latency for all requests started within that second
     */
    private void drawMeanLatencyLineChart(ArrayList<Integer> avgLatencyPlotData){
        // Create a new line chart maker with title and axis labels
        LineChartMaker lcm = new LineChartMaker("Mean Latency over time", "Seconds", "Average Latency");

        // add the datapoints to the chart
        lcm.fillDataset(avgLatencyPlotData);

        // invoke the drawing thread and block until completion
        try {
            SwingUtilities.invokeAndWait(()-> {

                // apply the supplied data to the chart
                lcm.makeChart();

                // set the size and position on the screen for the window in which the chart will be displayed
                lcm.setSize(800, 400);
                lcm.setLocationRelativeTo(null);

                // set the chart to be removed on closing the display window
                lcm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                // show the chart
                lcm.setVisible(true);
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }

        // while the chart is visible i.e. the window hasn't been closed, block the thread to keep the chart
        // displayed until the user closes it
        while (lcm.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * initializes the nested arrays for sorting latencies by the second in which the corresponding request was sent
      */
    private ArrayList<ArrayList<Integer>> createLatencyLists() {

        // initialize the outer array representing seconds in time
        ArrayList<ArrayList<Integer>> latencyPlotData = new ArrayList<>();

        // get the total number of seconds by taking totalTime in ms dividing by 1000 and rounding up by adding one
        long numSecondsWallTime = (this.totalTime/1000) +1;

        // for each second initialize a new list to hold latencies
        for (int i = 0; i < numSecondsWallTime; i++) {
            latencyPlotData.add(new ArrayList<>());
        }

        // return the top ArrayList
        return latencyPlotData;
    }

    /**
     * Flattens a 2d list of lists into a single layer list of avg values
     * @param latencyPlotData Nested array of all latencies among this RequestData instances datapoints
     * @return the flattened list of mean latencies
     */
    private ArrayList<Integer> calculateAverages(ArrayList<ArrayList<Integer>> latencyPlotData) {

        // initialize the flattened ArrayList
        ArrayList<Integer> avgLatencyPlotData = new ArrayList<>();

        // for all seconds in the time range
        for (int i = 0; i < latencyPlotData.size(); i++) {

            // if there are no latencies for requests in a given second, set that index to null to maintain ordering
            if (latencyPlotData.get(i).isEmpty()) {
                avgLatencyPlotData.add(null);
            } else {
                // sum all latencies in the i-th second and average
                Integer totalLatency = latencyPlotData.get(i).stream().mapToInt(Integer::intValue).sum();
                avgLatencyPlotData.add(totalLatency / latencyPlotData.get(i).size());
            }
        }
        return avgLatencyPlotData;
    }

    /**
     * Iterate through all datapoints and record latency metric measurements
     * @param percentileTracker min heap to track percentile values
     * @param latencyMedianPercentileIndex the index of the median in a time-sorted list
     * @param latencyPlotData nested list to bucketize latencies by the second the respective request was sent in
     */
    private void processLatency(
        PriorityQueue<RequestDatum> percentileTracker, long latencyMedianPercentileIndex,
        ArrayList<ArrayList<Integer>> latencyPlotData) {

        // init variable for total latency
        long totalLatency = 0;

        // bring the reference to the earliest request's start time into a local variable
        LocalDateTime startTime = this.earliestNode.getStart();

        // set the current node to root
        RequestDatum curNode = this.root;

        // Iterate through all nodes to sum the total latency and add datapoints to the percentile heap and
        // bucketized timescale Array
        while(curNode.dataEntered()){

            // get the current node's latency value
            int curLatency = curNode.getLatency();

            // add cur latency to the total latency measure
            totalLatency += curLatency;

            // get the number of seconds from the start of the first request to the start of the current request and
            // bucket the current latency value
            int timeSinceStart = (int) Duration.between(startTime, curNode.getStart()).toSeconds();
            latencyPlotData.get(timeSinceStart).add(curLatency);

            // until the heap's size reaches the median datapoint's index (50% of all datapoints), add the current
            // node to the heap and check for the max response time
            if (percentileTracker.size() < latencyMedianPercentileIndex){
                percentileTracker.add(curNode);
                if (curLatency > this.maxResponseTime) {this.maxResponseTime = curLatency;}

            // otherwise, if the current latency is greater than the minimum latency in the heap, remove the lowest
                // latency from the heap and add the larger value. Check for max latency
            } else if (curLatency > percentileTracker.peek().getLatency()) {
                percentileTracker.poll();
                percentileTracker.add(curNode);
                if (curLatency > maxResponseTime) {maxResponseTime = curLatency;}
            }
            // switch to the next node
            curNode = curNode.next;
        }
        // calculate average response time per request
        this.meanResponseTime = totalLatency/ this.numDatapoints;
    }

    /**
     * Iterate through the min heap containing the top half of latency values to set relevant percentile measures
     * @param percentileTracker min heap of RequestDatum objects
     * @param numDataTo99Percentile the number of dataPoints between the median latency and the target percentile
     */
    private void calculatePercentileValues(PriorityQueue<RequestDatum> percentileTracker, long numDataTo99Percentile) {

        // set the median response time
        this.medianResponseTime = percentileTracker.poll().getLatency();

        // iterate to and then set the target percentile latency value
        for (int i = 0; i <= (numDataTo99Percentile); i++){
            if (i == numDataTo99Percentile) {
                this.p99ResponseTime = percentileTracker.poll().getLatency();
                break;
            } else {
                percentileTracker.poll();
            }
        }
    }

    /**
     * getter for meanResponseTime
     * @return mean latency across all datapoints
     */
    public float getMeanResponseTime() {
        return this.meanResponseTime;
    }

    /**
     * Private class representing a single datapoint acting as nodes for the overall linkedlist structure
     */
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

        public int getLatency() {
            return this.latency;
        }

        public void setNext(RequestDatum next) {
            this.next = next;
        }

        /**
         * Causes the toString method to return the record in a delineated format
         * @return the formatted String representation of this instance's http request record
         */
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

    /**
     * comparator to sort nodes based on length of latency
     */
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
