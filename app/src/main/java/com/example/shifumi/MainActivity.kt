package com.example.shifumi

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.DisposableEffectScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shifumi.ui.theme.ShifumiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
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
        composable("play") {
            PlayScreen(navController = navController)
        }
        composable("playRandom") {
            RandomBotScreen(navController = navController)
        }
    }
}

@Composable
fun ConfettiEffect(show: Boolean) {
    if (show) {
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = listOf(
                Party(
                    speed = 30f,
                    maxSpeed = 50f,
                    damping = 0.9f,
                    spread = 360,
                    colors = listOf(0xff33e0, 0xff3333, 0xbb33ff, 0xf9ff33, 0xffac33),
                    emitter = Emitter(duration = 3, TimeUnit.SECONDS).perSecond(100),
                    position = Position.Relative(0.5, 0.0)
                )
            )
        )
    }
}

@Composable
fun PlayScreen(navController: NavController) {
    val context = LocalContext.current
    var playerChoice by remember { mutableStateOf<Int?>(null) }
    var botChoice by remember { mutableStateOf<Int?>(null) }
    var resultMessage by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf(0) }
    var consecutiveWins by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(0) }
    val highScoreFlow = context.highScoreFlow.collectAsState(initial = 0)
    var showConfetti by remember { mutableStateOf(false) }
    var choiceCounter by remember { mutableStateOf(0) }

    LaunchedEffect(highScoreFlow.value) {
        highScore = highScoreFlow.value
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Page de jeu contre un bot stratégique")

        Spacer(modifier = Modifier.height(16.dp))

        Text("Faites votre choix :")

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(R.drawable.pierre, R.drawable.feuille, R.drawable.ciseaux).forEach { imageRes ->
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp)
                        .clickable {
                            playerChoice = imageRes
                            countdown = 3
                            choiceCounter++
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (countdown > 0) {
            Text("Décompte : $countdown")
        } else if (playerChoice != null && botChoice != null) {
            Text("Votre choix : ${getChoiceName(playerChoice)}")
            Text("Choix du bot : ${getChoiceName(botChoice)}")
            Text(resultMessage)
            Text("Victoires consécutives : $consecutiveWins")
            Text("Meilleur score : $highScore")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.navigate("home") }) {
            Text(text = "Accueil")
        }
    }

    LaunchedEffect(choiceCounter) {
        if (playerChoice != null) {
            startCountdown(
                listOf(R.drawable.pierre, R.drawable.feuille, R.drawable.ciseaux),
                playerChoice!!,
                onCountdownTick = { value ->
                    countdown = value
                },
                onFinish = { botResult, result ->
                    botChoice = botResult
                    resultMessage = result
                    countdown = 0

                    if (result == "Vous avez gagné !") {
                        consecutiveWins++
                        if (consecutiveWins > highScore) {
                            CoroutineScope(Dispatchers.Main).launch {
                                context.updateHighScore(consecutiveWins)
                            }
                            highScore = consecutiveWins
                            triggerWowEffect(context)
                            showConfetti = true
                        }
                    } else {
                        consecutiveWins = 0
                    }
                }
            )
        }
    }

    LaunchedEffect(showConfetti) {
        if (showConfetti) {
            delay(2000)
            showConfetti = false
        }
    }

    ConfettiEffect(show = showConfetti)
}



fun triggerWowEffect(context: Context) {
    val mediaPlayer = MediaPlayer.create(context, R.raw.victory_sound)
    mediaPlayer.start()

    Toast.makeText(context, "Nouveau record battu !", Toast.LENGTH_LONG).show()
}


suspend fun startCountdown(
    imageList: List<Int>,
    playerChoice: Int,
    onCountdownTick: (Int) -> Unit,
    onFinish: (Int, String) -> Unit
) {
    var countdownValue = 3

    while (countdownValue > 0) {
        onCountdownTick(countdownValue)
        delay(1000L)
        countdownValue--
    }

    val botChoiceResult = calculateBotChoice(imageList)
    val resultMessageResult = determineResult(playerChoice, botChoiceResult)

    onFinish(botChoiceResult, resultMessageResult)
}


fun calculateBotChoice(imageList: List<Int>): Int {
    return imageList.random()
}

fun determineResult(playerChoice: Int, botChoice: Int): String {
    return when {
        playerChoice == botChoice -> "Égalité !"
        (playerChoice == R.drawable.pierre && botChoice == R.drawable.ciseaux) ||
                (playerChoice == R.drawable.feuille && botChoice == R.drawable.pierre) ||
                (playerChoice == R.drawable.ciseaux && botChoice == R.drawable.feuille) -> "Vous avez gagné !"
        else -> "Le bot a gagné !"
    }
}

fun getChoiceName(choice: Int?): String {
    return when (choice) {
        R.drawable.pierre -> "Pierre"
        R.drawable.feuille -> "Feuille"
        R.drawable.ciseaux -> "Ciseaux"
        else -> "Aucun"
    }
}

@Composable
fun RandomBotScreen(navController: NavController) {
    val imageList = listOf(
        R.drawable.pierre,
        R.drawable.feuille,
        R.drawable.ciseaux
    )

    var randomImage by remember { mutableStateOf<Int?>(null) }
    var randomImageBot by remember { mutableStateOf<Int?>(null) }
    var shakeCount by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val updatedRandomImage = rememberUpdatedState(randomImage)
    val updatedRandomImageBot = rememberUpdatedState(randomImageBot)
    val updatedShakeCount = rememberUpdatedState(shakeCount)

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

                    val speed =
                        sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)) / diffTime * 10000

                    if (speed > shakeThreshold) {
                        shakeCount++
                        if (shakeCount == 3) {
                            randomImage = imageList.random()
                            randomImageBot = imageList.random()
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

        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Page de jeu (mode aléatoire)")

        if (randomImage != null && randomImageBot != null) {
            Image(
                painter = painterResource(id = randomImage!!),
                contentDescription = "Image aléatoire",
                modifier = Modifier.size(200.dp)
            )
            Image(
                painter = painterResource(id = randomImageBot!!),
                contentDescription = "Image aléatoire bot",
                modifier = Modifier.size(200.dp)
            )
        } else {
            Text("Secouez le téléphone 3 fois pour jouer")
        }

        Button(onClick = { navController.navigate("home") }) {
            Text(text = "Accueil")
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
        Button(onClick = { navController.navigate("play") }) {
            Text(text = "Jouer")
        }
        Button(onClick = { navController.navigate("playRandom") }) {
            Text(text = "Jouer (mode aléatoire)")
        }
    }
}