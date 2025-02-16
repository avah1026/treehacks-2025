package tech.pacia.tinderswiper

import dadb.Dadb
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import maestro.Maestro
import maestro.drivers.AndroidDriver
import okio.Buffer
import java.io.IOException
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException

private val json = Json { ignoreUnknownKeys = true }

private lateinit var maestro: Maestro
private lateinit var gemini: Gemini
private var myOwnPreferences: MyOwnPreferences? = null

fun main() = runBlocking {
    val geminiApiKey = System.getenv("GEMINI_API_KEY") ?: throw IllegalStateException("GEMINI_API_KEY not set")
    gemini = Gemini(apiKey = geminiApiKey)

    val server = embeddedServer(CIO, 8080) {
        install(CORS) {
            anyHost() // Allows requests from any origin
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            allowCredentials = true
            allowNonSimpleContentTypes = true
            allowSameOrigin = true
            // anyMethod() // Allows all HTTP methods
        }

        routing {
            post("/my-data") {
                val jsonStr = call.receiveText()
                myOwnPreferences = json.decodeFromString<MyOwnPreferences>(jsonStr)

                log("Received data! MyOwnPreferences: $myOwnPreferences")

                val responseJSON = "\"msg\": \"You submitted data!\""
                call.respondText(responseJSON, contentType = ContentType.Application.Json)
            }
        }
    }

    val serverJob = launch {
        server.start(wait = false)
    }

    while (myOwnPreferences == null) {
        log("Did not receive user's preferences yet, waiting for it...")
        delay(1000)
    }

    log("Received user's preferences! Will start the main loop.")
    mainLoop()
    log("Main loop done!")
    serverJob.cancel(CancellationException("Main loop is done, server has no purpose to live anymore"))
}

private suspend fun mainLoop() {
    log("launching...")

    val dadb = Dadb.create("localhost", 5555)
    val driver = AndroidDriver(dadb = dadb)

    delay(timeMillis = 1000L)

    maestro = try {
        Maestro.android(driver)
    } catch (exception: IOException) {
        log("Error: IO exception!")
        exception.printStackTrace()
        return
    } catch (exception: TimeoutException) {
        log("Error: timed out!")
        exception.printStackTrace()
        return
    }

    log("ready")

    maestro.launchApp("com.tinder", stopIfRunning = false)

    log("launched the app, now will wait for 3s")
    delay(timeMillis = 3000L)
    log("done waiting for 3s!")

    var i = 0 // TODO: Loop infinitely until the "you're out of likes" message appears
    while (i < 3) {
        // TODO: Loop over all images in one's profile
        val imageBuffer = screenshot()

        val verdict = gemini.analyzeProfile(
            myOwnPrefs = myOwnPreferences!!,
            images = listOf(imageBuffer),
        )
        val like = verdict == 1 /* Random.nextBoolean() */
        log("Send a like? $like")
        if (like) {
            yay()
        } else {
            nay()
        }

        i++
    }

    log("before close???")

    maestro.close()
}

private suspend fun screenshot(): Buffer {
    log("will take a screenshot")

    val buffer = Buffer()
    maestro.takeScreenshot(buffer, false)

    log("took screenshot, now will wait 1s")
    delay(timeMillis = 1000L)

    return buffer
}

private suspend fun yay() {
    log("will swipe right :(")
    maestro.swipe(startRelative = "20%,50%", endRelative = "80%,50%", duration = 1000)

    log("swiped right, now will wait 1s")
    delay(timeMillis = 1000L)
}

private suspend fun nay() {
    log("will swipe left :(")
    maestro.swipe(startRelative = "80%,50%", endRelative = "20%,50%", duration = 1000)

    log("swiped left, now will wait 1s")
    delay(timeMillis = 1000L)
}

private fun log(message: Any?) {
    println("treehacks: $message")
}
