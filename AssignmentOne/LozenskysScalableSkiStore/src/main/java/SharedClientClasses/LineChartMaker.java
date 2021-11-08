package SharedClientClasses;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;


public class LineChartMaker extends JFrame {

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

    public void fillDataset(HashMap<Integer,Integer> data, int minEntry, int maxEntry) {
        for (int i = minEntry; i <= maxEntry; i++) {
            if (data.containsKey(i)){
             this.categoryData.addValue(data.get(i),this.valueLabel,String.valueOf(i));
            }
        }
    }

    public void fillDataset(ArrayList<Integer> data) {
        for (int i = 0; i < data.size(); i++){
            if (data.get(i) == null) { continue; }
            this.categoryData.addValue(data.get(i),this.valueLabel,String.valueOf(i));
        }
    }

    public void makeChart(){;
        JFreeChart lineChart = ChartFactory.createLineChart(this.title, this.timeLabel, this.valueLabel,
            this.categoryData);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        setContentPane(chartPanel);
    }


}
