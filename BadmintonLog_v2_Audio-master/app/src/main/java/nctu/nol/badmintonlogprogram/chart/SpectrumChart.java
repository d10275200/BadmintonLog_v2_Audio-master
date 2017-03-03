package nctu.nol.badmintonlogprogram.chart;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.CombinedXYChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.ScatterChart;
import org.achartengine.chart.XYChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import nctu.nol.bt.devices.SoundWaveHandler;

/**
 * Created by Smile on 2016/7/22.
 */
public class SpectrumChart {
    private final static String TAG = SpectrumChart.class.getSimpleName();

    private Context context;

    // Dataset
    private List<double[]> list_X_dataset = new ArrayList<double[]>(); // 點的x坐標, 每個element代表一種資料
    private List<double[]> list_Y_dataset = new ArrayList<double[]>(); // 點的y坐標
    private List<Integer> list_color = new ArrayList<Integer>(); // 不同資料集的代表顏色

    // Chart
    private RelativeLayout layout;
    private XYMultipleSeriesDataset XYDataset = new XYMultipleSeriesDataset();
    private GraphicalView chart;
    private final static double Y_Min = 0, Y_Max = 60;

    public SpectrumChart(Context c, RelativeLayout l){
        this.context = c;
        this.layout = l;
    }

    public void AddChartDataset(double[] time, double[] val, int color){
        list_X_dataset.add(time);
        list_Y_dataset.add(val);
        list_color.add(color);
    }

    public void ClearAllDataset(){
        list_X_dataset.clear();
        list_Y_dataset.clear();
        list_color.clear();
        XYDataset.clear();
    }

    public void MakeChart(){
        buildDataset(list_X_dataset, list_Y_dataset); // 儲存座標值

        // Line Render
        //PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND }; // 折線點的形狀
        XYMultipleSeriesRenderer renderer = buildRenderer(list_color, true);

        // Chart Render
        setChartSettings(renderer, "Frequency (Hz)", "", 0, SoundWaveHandler.SAMPLE_RATE/3, Y_Min, Y_Max);// 定義折線圖


        CombinedXYChart.XYCombinedChartDef[] types = new CombinedXYChart.XYCombinedChartDef[list_X_dataset.size()];
        for(int i = 0; i < list_X_dataset.size(); i++){
            if(i == 1)
                types[i] = new CombinedXYChart.XYCombinedChartDef(ScatterChart.TYPE, i);
            else
                types[i] = new CombinedXYChart.XYCombinedChartDef(LineChart.TYPE, i);
        }
        chart = ChartFactory.getCombinedXYChartView(context, XYDataset, renderer, types);

        layout.removeAllViews();
        layout.addView(chart);
    }

    // 設定圖表樣式渲染
    private void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle,
                                  String yTitle, double xMin, double xMax, double yMin, double yMax) {
        //renderer.setChartTitle(title); // 折線圖名稱
        //renderer.setChartTitleTextSize(24); // 折線圖名稱字形大小

        renderer.setXTitle(xTitle); // X軸名稱
        renderer.setXAxisMin(xMin); // X軸顯示最小值
        renderer.setXAxisMax(xMax); // X軸顯示最大值
        renderer.setXLabelsColor(Color.BLACK); // X軸線顏色

        renderer.setYTitle(yTitle); // Y軸名稱
        renderer.setYAxisMin(yMin); // Y軸顯示最小值
        renderer.setYAxisMax(yMax); // Y軸顯示最大值
        renderer.setYLabelsColor(0, Color.BLACK); // Y軸線顏色
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setYLabelsVerticalPadding(-15.0f);

        renderer.setAxisTitleTextSize(42);
        renderer.setAxesColor(Color.BLACK); // 設定坐標軸顏色
        renderer.setLabelsTextSize(42);
        renderer.setLabelsColor(Color.BLACK); // 設定標籤顏色

        renderer.setMarginsColor(Color.WHITE); // 設定背景顏色
        renderer.setApplyBackgroundColor(true);
        renderer.setBackgroundColor(Color.WHITE);
        renderer.setShowGrid(true); // 設定格線
        renderer.setGridColor(Color.LTGRAY);
        renderer.setShowLegend(false);
        renderer.setMargins(new int[]{20, 80, 30, 80});

        renderer.setZoomEnabled(true, false);
        renderer.setPanEnabled(true, false);
        //renderer.setClickEnabled(true);

        //限制Scrolling的範圍
        double[] PanLimits={xMin,xMax,yMin,yMax}; // [panMinimumX, panMaximumX, panMinimumY, panMaximumY]
        renderer.setPanLimits(PanLimits);
        renderer.setZoomLimits(PanLimits);
    }

    // 定義折線、點、長條的格式
    private XYMultipleSeriesRenderer buildRenderer(List<Integer> colors, /*PointStyle[] styles,*/ boolean fill) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        int length = colors.size();
        //折線
        if(length > 0) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(colors.get(0));
            r.setFillPoints(fill);
            renderer.addSeriesRenderer(r); //將座標變成線加入圖中顯示
        }

        //波峰點
        if (length > 1){
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setPointStyle(PointStyle.CIRCLE);
            r.setColor(colors.get(1));
            r.setFillPoints(true);
            r.setDisplayChartValues(true);
            r.setDisplayChartValuesDistance(0);
            r.setChartValuesFormat(new DecimalFormat("#"));
            r.setChartValuesTextSize(35);
            r.setChartValuesSpacing(10);
            renderer.addSeriesRenderer(r);
            renderer.setPointSize(5.0f);
        }

        return renderer;
    }

    // 資料處理
    private void buildDataset(List<double[]> xValues, List<double[]> yValues) {
        XYDataset.clear();

        int length = xValues.size(); // 資料集數量
        for (int i = 0; i < length; i++) {
            // XYseries對象,用於提供繪製的點集合的資料
            XYSeries series = new XYSeries("Dataset_" + i); // 依據每條線的名稱新增
            double[] xV = xValues.get(i); // 獲取第i條線的資料
            double[] yV = yValues.get(i);
            int seriesLength = xV.length; // 有幾個點

            for (int k = 0; k < seriesLength; k++) // 每條線裡有幾個點
                series.add(xV[k], yV[k]);
            XYDataset.addSeries(series);
        }
    }
}
