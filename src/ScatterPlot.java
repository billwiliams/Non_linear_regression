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
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.util.ShapeUtilities;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class ScatterPlot {
    //the dataset required to obtain x and y points on the chart
    static  XYSeriesCollection dataset;
    static String [] graphLabels;


    public static   void ScatterCreate (double[] time,double[] Bearing,double[] Bi,String[] graphtitles) {

        dataset = new XYSeriesCollection();

        XYSeries data1 = new XYSeries("Bm (Measured)");
        XYSeries data2 = new XYSeries("Bi(computed)");
        //obtain the data for the points
        for(int i=0;i<time.length;i++) {
            data1.add(time[i],Bearing[i]);
            data2.add(time[i],Bi[i]);
        }
        dataset.addSeries(data1);

        dataset.addSeries(data2);
        //Graph naming of axis title etc
        graphLabels=graphtitles;

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
                graphLabels[0],                  // chart title
                graphLabels[1],                      // x axis label
                graphLabels[2],                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        XYPlot xyPlot = (XYPlot) chart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);;


        renderer.setSeriesPaint(1, Color.blue);
        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);

        lineRenderer.setSeriesPaint(0, Color.green);
        xyPlot.setRenderer(0, lineRenderer);



        return chart;
    }

}
