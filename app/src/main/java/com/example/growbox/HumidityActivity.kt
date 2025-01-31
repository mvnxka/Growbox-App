package com.example.growbox

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.text.SimpleDateFormat
import java.util.*

class HumidityActivity : AppCompatActivity() {
    private lateinit var bluetoothService: BluetoothService
    private lateinit var humidityGraph: GraphView
    private lateinit var humidityValue: TextView
    private var series = LineGraphSeries<DataPoint>()
    private val handler = Handler(Looper.getMainLooper())
    private var lastUpdatedTime = 0L

    private val updateTextRunnable = object : Runnable {
        override fun run() {
            runOnUiThread {
                if (lastUpdatedTime != 0L) {
                    humidityValue.text = "${series.highestValueY} %"
                }
            }
            handler.postDelayed(this, 30000) // Aktualizacja co 30 sekund
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_humidity)

        humidityGraph = findViewById(R.id.humidityGraph)
        humidityValue = findViewById(R.id.humidityValue)

        bluetoothService = BluetoothService(this) { data ->
            val humidity = data.toDoubleOrNull()
            humidity?.let {
                if (isValidData(it)) {
                    val timestamp = System.currentTimeMillis().toDouble()
                    series.appendData(DataPoint(timestamp, it), true, 100)
                    FileHelper.saveData(this, "humidity.csv", it)
                }
            }
        }

        humidityGraph.addSeries(series)
        configureGraph()
        handler.post(updateTextRunnable)
    }

    private fun configureGraph() {
        val dateFormat = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())

        humidityGraph.gridLabelRenderer.labelFormatter = object : com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter(this, dateFormat) {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    val date = Date(value.toLong())
                    dateFormat.format(date).replace(" ", "\n") // Nowa linia w dacie
                } else {
                    String.format(Locale.FRANCE, "%.1f", value)
                }
            }
        }

        humidityGraph.gridLabelRenderer.numHorizontalLabels = 3
        humidityGraph.gridLabelRenderer.numVerticalLabels = 5
        humidityGraph.gridLabelRenderer.padding = 20

        humidityGraph.viewport.isXAxisBoundsManual = true
        humidityGraph.viewport.setMinX(System.currentTimeMillis().toDouble() - 60000)
        humidityGraph.viewport.setMaxX(System.currentTimeMillis().toDouble())

        humidityGraph.viewport.isYAxisBoundsManual = true
//        humidityGraph.viewport.setMinY(0.0)
//        humidityGraph.viewport.setMaxY(100.0)
    }

    private fun isValidData(newValue: Double): Boolean {
        return series.highestValueY == Double.NaN || kotlin.math.abs(series.highestValueY - newValue) <= 1
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTextRunnable)
    }
}
