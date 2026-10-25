package com.utilitywatch.controller;

import com.utilitywatch.EnvironmentalEvaluator.FootprintPoint;
import com.utilitywatch.EnvironmentalEvaluator.Grouping;
import com.utilitywatch.FootprintTrendFormatter;

import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import java.util.ArrayList;
import java.util.List;

// US10.3 - Line chart of the footprint per week or month, with one line per utility shown
// (energy or water alone, or both as two separate lines).
// Periods with readings are joined by a line (a genuine 0 kg is a point on the zero line).
// A period with no readings for a utility gets no point on that line, so the line breaks there.
// A period with no readings at all is also covered by a grey "No data" band, so it can't be mistaken for 0 kg.
/**
 * Controls the footprint trend chart.
 */
public class FootprintTrendChart extends LineChart<String, Number> {

    private final CategoryAxis xAxis;
    private final List<String> noDataCategories = new ArrayList<>();
    private final List<Rectangle> noDataBands = new ArrayList<>();
    private final List<Label> noDataLabels = new ArrayList<>();

    public FootprintTrendChart() {
        this(new CategoryAxis(), new NumberAxis());
    }

    private FootprintTrendChart(CategoryAxis xAxis, NumberAxis yAxis) {
        super(xAxis, yAxis);
        this.xAxis = xAxis;
        yAxis.setLabel("kg CO₂e");
        yAxis.setForceZeroInRange(true);
        xAxis.setAnimated(false);
        yAxis.setAnimated(false);
        setAnimated(false);
        setLegendVisible(false);
    }

    // One line to draw: its name (used in tooltips when there are several lines), colour (e.g. "#FFBA4A")
    // and footprint per period, covering the same periods as the chart.
    public record Line(String name, String colour, List<FootprintPoint> points) {
    }

    // Replaces what the chart shows. periods is the footprint for the whole selection: it sets the axis,
    // and its periods with no data get the "No data" band. lines are drawn on top, one line each.
    public void show(List<FootprintPoint> periods, Grouping grouping, List<Line> lines) {
        clearChart();

        List<String> labels = FootprintTrendFormatter.labels(periods, grouping);
        // set the categories first, including periods with no data, so every period has its place on the axis in order.
        // Change the axis's own list rather than calling setCategories(newList): with auto-ranging off, a new list
        // doesn't make the axis redraw its tick labels, so switching Weekly -> Monthly kept showing the week labels.
        xAxis.setAutoRanging(false);
        xAxis.getCategories().setAll(labels);

        boolean nameInTooltips = lines.size() > 1;
        for (Line line : lines) {
            // one series per run of periods with data, so the line stops at gaps
            for (List<Integer> run : FootprintTrendFormatter.runsWithData(line.points())) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                for (int i : run) {
                    series.getData().add(new XYChart.Data<>(labels.get(i), line.points().get(i).kg()));
                }
                getData().add(series);
                styleSeries(series, line, run, nameInTooltips);
            }
        }

        for (int i = 0; i < periods.size(); i++) {
            if (!periods.get(i).hasData()) {
                addNoDataBand(labels.get(i), FootprintTrendFormatter.describe(periods.get(i)));
            }
        }
        requestChartLayout();
    }

    public void clearChart() {
        getData().clear();
        getPlotChildren().removeAll(noDataBands);
        getPlotChildren().removeAll(noDataLabels);
        noDataBands.clear();
        noDataLabels.clear();
        noDataCategories.clear();
        xAxis.getCategories().clear();
    }

    // Series nodes only exist once the series is on the chart, so this runs after getData().add(...)
    private void styleSeries(XYChart.Series<String, Number> series, Line line, List<Integer> run, boolean nameInTooltips) {
        Node path = series.getNode();
        if (path != null) {
            path.setStyle("-fx-stroke: " + line.colour() + "; -fx-stroke-width: 2px;");
        }
        for (int j = 0; j < series.getData().size(); j++) {
            Node symbol = series.getData().get(j).getNode();
            if (symbol != null) {
                symbol.setStyle("-fx-background-color: " + line.colour() + ", white;");
                FootprintPoint point = line.points().get(run.get(j));
                String text = nameInTooltips
                        ? FootprintTrendFormatter.describe(line.name(), point)
                        : FootprintTrendFormatter.describe(point);
                Tooltip.install(symbol, new Tooltip(text));
            }
        }
    }

    private void addNoDataBand(String category, String description) {
        // styled here rather than in stylesheet.css: a light grey dashed band (a Rectangle is solid black by default)
        Rectangle band = new Rectangle();
        band.setFill(Color.rgb(120, 120, 120, 0.15));
        band.setStroke(Color.web("#9e9e9e"));
        band.getStrokeDashArray().setAll(4.0, 4.0);
        band.setStrokeType(StrokeType.INSIDE);
        Tooltip.install(band, new Tooltip(description));

        Label label = new Label("No data");
        label.setStyle("-fx-text-fill: #616161; -fx-font-size: 11px; -fx-font-style: italic;");
        label.setMouseTransparent(true);

        noDataCategories.add(category);
        noDataBands.add(band);
        noDataLabels.add(label);
        // behind the lines and points
        getPlotChildren().add(0, band);
        getPlotChildren().add(label);
    }

    // Places the bands each time the chart lays itself out (e.g. when the window is resized)
    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        double width = xAxis.getCategorySpacing();
        double height = getYAxis().getHeight();
        for (int i = 0; i < noDataCategories.size(); i++) {
            double centre = xAxis.getDisplayPosition(noDataCategories.get(i));
            Rectangle band = noDataBands.get(i);
            band.setX(centre - width / 2);
            band.setY(0);
            band.setWidth(width);
            band.setHeight(height);

            Label label = noDataLabels.get(i);
            label.autosize();
            // turn the text sideways when the band is too narrow for it (e.g. many weeks)
            label.setRotate(label.getWidth() + 4 > width ? -90 : 0);
            label.relocate(centre - label.getWidth() / 2, height / 2 - label.getHeight() / 2);
        }
    }
}
