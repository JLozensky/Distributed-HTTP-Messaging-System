package A1.SharedClientClasses;

import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;


public class BarChartMaker extends JFrame {

    private DefaultCategoryDataset categoryData;
    private String catagoryLabel;
    private String title;
    private String valueLabel;


    // I used this tutorial to set up the chart
    // https://www.javatpoint.com/jfreechart-bar-chart
    public BarChartMaker(String title, String catagoryLabel, String valueLabel){
        super(title);
        this.title = title;
        this.catagoryLabel = catagoryLabel;
        this.valueLabel = valueLabel;
        this.categoryData = new DefaultCategoryDataset();
    }

    public void makeChart(){
        JFreeChart chart = ChartFactory.createBarChart(this.title, this.catagoryLabel, this.valueLabel,
            this.categoryData, PlotOrientation.VERTICAL,true,true,false);

        ChartPanel chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);
    }

    public void addDatapoint(long value, String rowKey, String colKey) {
        this.categoryData.addValue(value, rowKey,colKey);

    }

}
