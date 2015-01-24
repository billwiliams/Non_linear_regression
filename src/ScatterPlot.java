/**
 * Created by Bill on 1/24/2015.
 * Aids in the drawing of the scatter plot
 */
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;


import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.util.ShapeUtilities;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class ScatterPlot {
    //the dataset required to obtain x and y points on the chart
    static  XYSeriesCollection dataset;


    public static   void ScatterCreate (double[] Xo,double[] Yo) {

        dataset = new XYSeriesCollection();
        XYSeries data = new XYSeries("coordinates");
        //obtain the data for the points
        for(int i=0;i<Xo.length;i++) {
        data.add(Xo[i],Yo[i]);
        }
        dataset.addSeries(data);
        showGraph();
    }
    //A method to show the graph using jfreechart
    private static  void showGraph() {
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(700, 700));
        final ApplicationFrame frame = new ApplicationFrame("Scatter Plot");
        frame.setContentPane(chartPanel);

        frame.pack();
        frame.setVisible(true);
    }
    //Chart particulars
    private static JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart chart = ChartFactory.createScatterPlot(
                "Scatter plot ( Ownship Course)",                  // chart title
                "ownship course Xo",                      // x axis label
                "ownship course Yo",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        XYPlot xyPlot = (XYPlot) chart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesShape(0, new Ellipse2D.Double(-1, -1, 4, 4));
        renderer.setSeriesPaint(0, Color.blue);

        return chart;
    }

}
