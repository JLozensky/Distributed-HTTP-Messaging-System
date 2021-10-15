package SharedClientClasses;

import java.util.ArrayList;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.category.DefaultCategoryDataset;


public class LineChartMaker extends ApplicationFrame {

    private DefaultCategoryDataset categoryData;
    private String timeLabel;
    private String title;
    private String valueLabel;


    // I used this tutorial to set up the chart
    // https://www.tutorialspoint.com/jfreechart/jfreechart_line_chart.htm
    public LineChartMaker(String title, String timeLabel, String valueLabel){
        super(title);
        this.title = title;
        this.timeLabel = timeLabel;
        this.valueLabel = valueLabel;
        this.categoryData = new DefaultCategoryDataset();
    }

    private void fillDataset(ArrayList<Integer> data) {
        for (int i = 0; i < data.size(); i++){
            this.categoryData.addValue((Number) data.get(i),this.valueLabel,i);
        }
    }

    public void makeChart(ArrayList<Integer> data){
        this.fillDataset(data);
        JFreeChart chart = ChartFactory.createLineChart(this.title, this.timeLabel, this.valueLabel,
            this.categoryData, PlotOrientation.VERTICAL,true,true,false);

        ChartPanel chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);
    }

    public void addDatapoint(long value, String rowKey, String colKey) {
        this.categoryData.addValue(value, rowKey,colKey);

    }

}
