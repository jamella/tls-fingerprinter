package de.rub.nds.ssl.analyzer.vnl.gui;

import com.google.common.collect.Multiset;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.FingerprintStatistics;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.SignatureDifference;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.EmptyBlock;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.DataUtilities;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import javax.annotation.Nonnull;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import static de.rub.nds.ssl.analyzer.vnl.fingerprint.FingerprintStatistics.ReportType.*;

/**
 * @author jBiegert azrdev@qrdn.de
 */
public class StatisticsModel implements Observer {
    private final FingerprintStatistics statistics;

    // dataset instances
    private final DefaultCategoryDataset reportsDataset = new DefaultCategoryDataset();
    private final XYSeries previousCountSeries =
            new XYSeries("Previous Fingerprints", true, false);
    private final ValueMarker previousCountAverageMarker = new AverageMarker(0);
    private final XYSeries diffSizeSeries =
            new XYSeries("Previous Fingerprints", true, false);
    private final ValueMarker diffSizeAverageMarker = new AverageMarker(0);
    private DefaultCategoryDataset signsCountDataset = new DefaultCategoryDataset();

    // TotalTitles
    private final TotalTitle reportsTotal = new TotalTitle();
    private final TotalTitle changedTotal = new TotalTitle();
    private final TotalTitle previousCountTotal = new TotalTitle();
    private final TotalTitle signCountTotal = new TotalTitle();

    public StatisticsModel(FingerprintStatistics statistics) {
        this.statistics = Objects.requireNonNull(statistics);
        statistics.addObserver(this);

        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());

        reportsDataset.addValue(null, "Count", "New");
        reportsDataset.addValue(null, "Count", "Update");

        update(null, "Change");
    }

    public JFreeChart getReportsChart() {
        final JFreeChart chart = ChartFactory.createBarChart(
                null, "Reports", "Count", reportsDataset,
                PlotOrientation.VERTICAL, false, true, false);
        chart.addSubtitle(assembleGraphTotalTitle(
                "All Fingerprint Reports", reportsTotal));
        chart.getCategoryPlot().setRenderer(new SimpleBarChartRenderer(
                Arrays.asList(newColor, updateColor, guessColor, changeColor)));
        chart.getCategoryPlot().getRenderer().setBaseItemLabelFont(FONT);
        return chart;
    }

    public JFreeChart getPreviousCountChart() {
        final JFreeChart chart = ChartFactory.createXYBarChart(
                null, "# Previous fingerprints", false, "Changed Report Count",
                new XYBarDataset(new XYSeriesCollection(previousCountSeries), 1),
                PlotOrientation.VERTICAL, false, true, false);
        chart.addSubtitle(assembleGraphTotalTitle(
                        "Previous fingerprints per change report",
                        changedTotal));
        XYPlot plot = chart.getXYPlot();
        plot.addDomainMarker(previousCountAverageMarker);
        plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new PercentageXYLabelGenerator());
        renderer.setBasePositiveItemLabelPosition(innerItemLabel);
        renderer.setBasePaint(changeColor); // this is probably overwritten by seriesPaint
        renderer.setSeriesPaint(0, changeColor);
        renderer.setBaseItemLabelFont(FONT);
        return chart;
    }

    public JFreeChart getChangedSignsCountChart() {
        final JFreeChart chart = ChartFactory.createXYBarChart(
                null, "# of signs in diff", false,
                "Count of previous fingerprints",
                new XYBarDataset(new XYSeriesCollection(diffSizeSeries), 1),
                PlotOrientation.VERTICAL, false, true, false);
        chart.addSubtitle(assembleGraphTotalTitle(
                "Diff sizes", previousCountTotal));
        final XYPlot plot = chart.getXYPlot();
        plot.addDomainMarker(diffSizeAverageMarker);
        plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new PercentageXYLabelGenerator());
        renderer.setBasePositiveItemLabelPosition(innerItemLabelUpright);
        renderer.setBasePaint(changeColor); // this is probably overwritten by seriesPaint
        renderer.setSeriesPaint(0, changeColor);
        renderer.setBaseItemLabelFont(FONT);
        return chart;
    }

    public JFreeChart getSignsCountChart() {
        final JFreeChart chart = ChartFactory.createBarChart(
                null, null, "Count", signsCountDataset,
                PlotOrientation.HORIZONTAL, false, true, false);
        chart.addSubtitle(assembleGraphTotalTitle(
                "Signs in all changed reports", signCountTotal));
        final BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setBaseItemLabelGenerator(new PercentageBarLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBasePositiveItemLabelPosition(innerItemLabel);
        renderer.setPositiveItemLabelPositionFallback(outerItemLabel);
        renderer.setBasePaint(changeColor);
        renderer.setSeriesPaint(0, changeColor);
        renderer.setBaseItemLabelFont(FONT);
        return chart;
    }

    // Observer implementation

    @Override
    public void update(Observable observable, Object o) {
        reportsTotal.setTotal(statistics.getReportCount());
        reportsDataset.setValue(statistics.getReportCount(New), "Count", "New");
        reportsDataset.setValue(statistics.getReportCount(Update), "Count", "Update");
        reportsDataset.setValue(statistics.getReportCount(Generated), "Count", "Guess");
        reportsDataset.setValue(statistics.getReportCount(Change), "Count", "Changed");

        if(! Objects.equals(o, "Change"))
            return;

        changedTotal.setTotal(statistics.getDiffsToPreviousDistribution().size());
        for (final Multiset.Entry<Number> entry :
                statistics.getDiffsToPreviousDistribution().entrySet()) {
            previousCountSeries.addOrUpdate(entry.getElement(), entry.getCount());
        }
        previousCountAverageMarker.setValue(statistics.getDiffsToPreviousAverage().doubleValue());

        previousCountTotal.setTotal(statistics.getDiffSizeDistribution().size());
        for (final Multiset.Entry<Number> entry :
                statistics.getDiffSizeDistribution().entrySet()) {
            diffSizeSeries.addOrUpdate(entry.getElement(), entry.getCount());
        }
        diffSizeAverageMarker.setValue(statistics.getChangedSignsAverage().doubleValue());

        signCountTotal.setTotal(statistics.getChangedSignsCount());
        // clear the dataset beforehand, there is no other way to do sorting by value
        signsCountDataset.clear();
        for (final Multiset.Entry<SignatureDifference.SignIdentifier> entry :
                statistics.getMostCommonChangedSigns().entrySet()) {
            signsCountDataset.setValue(entry.getCount(), "Count", entry.getElement().toString());
        }
    }

    // Chart helper(s)
    private static final Color newColor = new Color(201, 212, 0); // tu5b
    private static final Color updateColor = new Color(0, 131, 204); //tu2b
    private static final Color guessColor = new Color(253, 202, 0); // tu6b
    private static final Color changeColor = new Color(230, 0, 26); // tu9b

    private static final Font FONT = new Font("SansSerif", Font.BOLD, 14);

    private static final ItemLabelPosition innerItemLabel =
            new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER);
    private static final ItemLabelPosition innerItemLabelUpright =
            new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER,
                    TextAnchor.CENTER, Math.toRadians(-90));
    private static final ItemLabelPosition outerItemLabel =
            new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER);

    /**
     * A {@link BarRenderer} for bar charts with only one series (i.e., row in the
     * table). Sets different colors for each category (i.e. column).
     */
    private static class SimpleBarChartRenderer extends BarRenderer {
        private final List<Color> colors;

        public SimpleBarChartRenderer(final List<Color> colors) {
            this.colors = colors;

            setBaseItemLabelGenerator(new PercentageBarLabelGenerator());
            setBaseItemLabelsVisible(true);
            setBasePositiveItemLabelPosition(innerItemLabel);
            setPositiveItemLabelPositionFallback(outerItemLabel);
        }

        @Override
        public Paint getItemPaint(int row, int column) {
            return colors.get( column % colors.size() );
        }
    }

    /**
     * A {@link ValueMarker} for displaying the average
     */
    private static class AverageMarker extends ValueMarker {
        final DecimalFormat df = new DecimalFormat("Average: #");
        public AverageMarker(double value) {
            super(value);
            setLabelAnchor(RectangleAnchor.TOP_RIGHT);
            setLabelTextAnchor(TextAnchor.TOP_LEFT);
            df.setMaximumFractionDigits(2);
        }

        @Override
        public String getLabel() {
            return df.format(getValue());
        }
    }

    /**
     * Generator for Item Labels of a bar graph, which include the value and a
     * percentage relative to the series total.
     */
    private static class PercentageBarLabelGenerator
            extends StandardCategoryItemLabelGenerator {
        private static final NumberFormat numberFormat = NumberFormat.getInstance();
        private static final NumberFormat percentFormat = NumberFormat.getPercentInstance();

        @Override
        public String generateLabel(CategoryDataset dataset, int row, int column) {
            final double base = DataUtilities.calculateRowTotal(dataset, row);
            final Number value = dataset.getValue(row, column);
            if(value != null) {
                return numberFormat.format(value) + " (" +
                        percentFormat.format(value.doubleValue() / base) + ")";
            }
            return "-";
        }
    }

    /**
     * Generator for Item Labels of a XY graph, which include the y value and a
     * percentage relative to the series y total.
     */
    private static class PercentageXYLabelGenerator
            extends StandardXYItemLabelGenerator {
        private static final NumberFormat yFormat = NumberFormat.getInstance();
        private static final NumberFormat percentFormat = NumberFormat.getPercentInstance();

        @Override
        public String generateLabel(XYDataset dataset, int series, int item) {
            final double base = calculateSeriesYTotal(dataset, series);
            final Number value = dataset.getY(series, item);
            if(value != null) {
                return yFormat.format(value) + " (" +
                        percentFormat.format(value.doubleValue() / base) + ")";
            }
            return "-";
        }
    }

    /** @return The sum of all Y values in the series of the dataset */
    private static double calculateSeriesYTotal(XYDataset dataset, int series) {
        final int count = dataset.getItemCount(series);
        double sum = 0;
        for(int i = 0; i < count; ++i) {
            final Number y = dataset.getY(series, i);
            if(y != null)
                sum += y.doubleValue();
        }
        return sum;
    }

    /**
     * @return A {@link Title} to use as Char subtitle, including the titleText and a
     * {@link TotalTitle }
     * @see JFreeChart#addSubtitle(Title)
     */
    private static Title assembleGraphTotalTitle(final String titleText,
                                                 final TotalTitle total) {
        final TextTitle title = new TextTitle(titleText, JFreeChart.DEFAULT_TITLE_FONT);
        final BlockContainer container = new BlockContainer();
        container.add(title, RectangleEdge.LEFT);
        container.add(new EmptyBlock(2000, 0));
        container.add(total, RectangleEdge.RIGHT);
        final CompositeTitle composite = new CompositeTitle(container);
        composite.setPosition(RectangleEdge.TOP);
        return composite;
    }

    /** A {@link Title} displaying a "Total" number */
    private static class TotalTitle extends TextTitle {
        private NumberFormat numberFormat = NumberFormat.getInstance();

        public void setTotal(@Nonnull Number total) {
            setText("Total: " + numberFormat.format(total));
        }
    }
}
