package com.example.shifumi

import android.content.Context
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shifumi.ui.theme.ShifumiTheme
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShifumiTheme {
                MaterialTheme{
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("playHome") {
            PlayScreen(navController = navController)
        }
    }
}

@Composable
fun PlayScreen(navController: NavController) {
    val imageList = listOf(
        R.drawable.pierre,
        R.drawable.feuille,
        R.drawable.ciseaux
    )

    var randomImage by remember { mutableStateOf(imageList[Random.nextInt(imageList.size)]) }
    var shakeCount by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    DisposableEffect(Unit) {
        val sensorEventListener = object : SensorEventListener {
            private var lastTime: Long = 0
            private var lastX: Float = 0f
            private var lastY: Float = 0f
            private var lastZ: Float = 0f
            private val shakeThreshold = 800

            override fun onSensorChanged(event: SensorEvent) {
                val curTime = System.currentTimeMillis()
                if ((curTime - lastTime) > 100) {
                    val diffTime = curTime - lastTime
                    lastTime = curTime

                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val speed = sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)) / diffTime * 10000

                    if (speed > shakeThreshold) {
                        shakeCount++
                        if (shakeCount == 3) {
                            randomImage = imageList.random()
                            shakeCount = 0
                        }
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Page de jeu")
        Image(
            painter = painterResource(id = randomImage),
            contentDescription = "Image aléatoire",
            modifier = Modifier.size(200.dp)
        )
        Text("Secouez le téléphone 3 fois pour changer l'image")
        Button(onClick = { navController.navigate("home") }) {
            Text(text = "Home")
        }
    }
}


@Composable
fun HomeScreen(navController: NavController){
    Column (
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "ShiFuMi",
            style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigate("playHome") }) {
            Text(text = "Play")
        }
    }
}
