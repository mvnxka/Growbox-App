package com.example.growbox

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.text.SimpleDateFormat
import java.util.*

class TemperatureActivity : AppCompatActivity() {
    private lateinit var bluetoothService: BluetoothService
    private lateinit var temperatureGraph: GraphView
    private lateinit var temperatureValue: TextView
    private var series = LineGraphSeries<DataPoint>()
    private val handler = Handler(Looper.getMainLooper())
    private var lastUpdatedTime = 0L

    private val updateTextRunnable = object : Runnable {
        override fun run() {
            runOnUiThread {
                if (lastUpdatedTime != 0L) {
                    temperatureValue.text = "${series.highestValueY} °C"
                }
            }
            handler.postDelayed(this, 30000) // Aktualizacja co 30 sekund
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature)

        temperatureGraph = findViewById(R.id.temperatureGraph)
        temperatureValue = findViewById(R.id.temperatureValue)

        bluetoothService = BluetoothService(this) { data ->
            val temperature = data.toDoubleOrNull()
            temperature?.let {
                if (isValidData(it)) {
                    val timestamp = System.currentTimeMillis().toDouble()
                    series.appendData(DataPoint(timestamp, it), true, 100)
                    FileHelper.saveData(this, "temperature.csv", it)
                }
            }
        }

        temperatureGraph.addSeries(series)
        configureGraph()
        handler.post(updateTextRunnable)
    }

    private fun configureGraph() {
        val dateFormat = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())

        temperatureGraph.gridLabelRenderer.labelFormatter = object : DateAsXAxisLabelFormatter(this, dateFormat) {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    val date = Date(value.toLong())
                    dateFormat.format(date).replace(" ", "\n") // Nowa linia w dacie
                } else {
                    String.format(Locale.FRANCE, "%.1f", value)
                }
            }
        }

        temperatureGraph.gridLabelRenderer.numHorizontalLabels = 3
        temperatureGraph.gridLabelRenderer.numVerticalLabels = 5
        temperatureGraph.gridLabelRenderer.padding = 20

        temperatureGraph.viewport.isXAxisBoundsManual = true
        temperatureGraph.viewport.setMinX(System.currentTimeMillis().toDouble() - 60000)
        temperatureGraph.viewport.setMaxX(System.currentTimeMillis().toDouble())

        temperatureGraph.viewport.isYAxisBoundsManual = true
//        temperatureGraph.viewport.setMinY(0.0)
//        temperatureGraph.viewport.setMaxY(100.0)

        // Ustawienie pochylonego tekstu na osi X
        // temperatureGraph.gridLabelRenderer.horizontalLabelsAngle = 45f
    }

    private fun isValidData(newValue: Double): Boolean {
        return series.highestValueY == Double.NaN || kotlin.math.abs(series.highestValueY - newValue) <= 1
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTextRunnable)
    }
}
