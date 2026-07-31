package com.example.sensordataapplication

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat


class SensorActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var selectedSensors = mutableListOf<Sensor>()
    private val sensorValuesMap = mutableStateMapOf<Sensor, FloatArray>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        selectedSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        enableEdgeToEdge()
        setContent {
            SensorScreen(selectedSensors, sensorValuesMap)
        }
    }

    override fun onResume() {
        super.onResume()
        selectedSensors.forEach { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            sensorValuesMap[it.sensor] = it.values.copyOf()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

/**
 * Composable function to display the list of sensors and their values.
 */
@Composable
fun SensorScreen(sensors: List<Sensor>, sensorValuesMap: SnapshotStateMap<Sensor, FloatArray>,
                 modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 45.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = APP_TITLE,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF08519C)
            )

            Spacer(modifier = Modifier.height(24.dp))

            SensorCards(sensors, sensorValuesMap)
        }
    }
}

/**
 * Composable function to display a list of sensor cards.
 */
@Composable
fun SensorCards(sensors: List<Sensor>, sensorValuesMap: SnapshotStateMap<Sensor, FloatArray>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sensors) { sensor ->
            val values = sensorValuesMap[sensor] ?: floatArrayOf()
            SensorCard(sensor.name, sensor.type, values)
        }
    }
}

/**
 * Composable function to display a single sensor card.
 */
@Composable
fun SensorCard(sensorName: String, sensorType: Int, sensorValues: FloatArray) {
    val labels = sensorLabel(sensorType, sensorValues)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9D00)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = sensorName,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge
            )
            
            if (sensorValues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                SensorData(labels, sensorValues)
            }
        }
    }
}

/**
 * Composable function to display sensor data.
 */
@Composable
fun SensorData(labels: List<String>, sensorValues: FloatArray) {
    // Table-like structure
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SensorKeys(labels)

        Spacer(modifier = Modifier.height(8.dp))

        SensorValues(sensorValues, labels)
    }
}

/**
 * Composable function to display sensor keys (headers).
 */
@Composable
fun SensorKeys(labels: List<String>) {
    // Keys as headers
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Composable function to display sensor values.
 */
@Composable
fun SensorValues(sensorValues: FloatArray, labels: List<String>) {
    // Values in cells
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val df = DecimalFormat("#.##")
        sensorValues.forEachIndexed { index, value ->
            if (index < labels.size) {
                Text(
                    text = df.format(value),
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Generate labels based on sensor type and values.
 */
fun sensorLabel(sensorType: Int, sensorValues: FloatArray): List<String> {
    val labels = when (sensorType) {
        Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_MAGNETIC_FIELD,
        Sensor.TYPE_GRAVITY, Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_ROTATION_VECTOR -> listOf("X", "Y", "Z")
        Sensor.TYPE_LIGHT -> listOf("lx")
        Sensor.TYPE_PRESSURE -> listOf("hPa")
        Sensor.TYPE_PROXIMITY -> listOf("cm")
        Sensor.TYPE_AMBIENT_TEMPERATURE -> listOf("°C")
        Sensor.TYPE_RELATIVE_HUMIDITY -> listOf("%")
        else -> List(sensorValues.size) { "V${it + 1}" }
    }
    return labels
}