package com.utilitywatch.controller;

import com.utilitywatch.EnvironmentalEvaluator.FootprintPoint;
import com.utilitywatch.EnvironmentalEvaluator.Grouping;
import com.utilitywatch.FootprintTrendFormatter;

import javafx.collections.FXCollections;
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

// US10.3 - Line chart of the footprint per week or month.
// Periods with readings are joined by a line (a genuine 0 kg is a point on the zero line).
// Periods with no readings get no point, break the line, and are covered by a grey "No data" band,
// so they can't be mistaken for a 0 kg footprint.
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

    // Replaces what the chart shows. colour is the line colour for the selected utility, e.g. "#FFBA4A".
    public void show(List<FootprintPoint> points, Grouping grouping, String colour) {
        clearChart();

        List<String> labels = FootprintTrendFormatter.labels(points, grouping);
        // set the categories first, including periods with no data, so every period has its place on the axis in order
        xAxis.setAutoRanging(false);
        xAxis.setCategories(FXCollections.observableArrayList(labels));

        // one series per run of periods with data, so the line stops at gaps
        for (List<Integer> run : FootprintTrendFormatter.runsWithData(points)) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (int i : run) {
                series.getData().add(new XYChart.Data<>(labels.get(i), points.get(i).kg()));
            }
            getData().add(series);
            styleSeries(series, colour, run, points);
        }

        for (int i = 0; i < points.size(); i++) {
            if (!points.get(i).hasData()) {
                addNoDataBand(labels.get(i), FootprintTrendFormatter.describe(points.get(i)));
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
        xAxis.setCategories(FXCollections.observableArrayList());
    }

    // Series nodes only exist once the series is on the chart, so this runs after getData().add(...)
    private void styleSeries(XYChart.Series<String, Number> series, String colour, List<Integer> run,
                             List<FootprintPoint> points) {
        Node line = series.getNode();
        if (line != null) {
            line.setStyle("-fx-stroke: " + colour + "; -fx-stroke-width: 2px;");
        }
        for (int j = 0; j < series.getData().size(); j++) {
            Node symbol = series.getData().get(j).getNode();
            if (symbol != null) {
                symbol.setStyle("-fx-background-color: " + colour + ", white;");
                Tooltip.install(symbol, new Tooltip(FootprintTrendFormatter.describe(points.get(run.get(j)))));
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
