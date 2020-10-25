package sudo.openhackaton.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import sudo.openhackaton.R
import sudo.openhackaton.logic.CameraLogic
import sudo.openhackaton.logic.Constants.FROM_WHERE
import sudo.openhackaton.logic.Constants.INDICATION_VALUE
import sudo.openhackaton.logic.Constants.REQUEST_TAKE_A_PHOTO
import sudo.openhackaton.logic.Constants.SERIAL_NUMBER_VALUE
import sudo.openhackaton.logic.Constants.cameraLogic
import sudo.openhackaton.logic.FilesLogic
import sudo.openhackaton.logic.Network
import sudo.openhackaton.logic.Recognition
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private lateinit var recognition: Recognition
    private lateinit var filesLogic: FilesLogic
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        filesLogic = FilesLogic(this, this)
        cameraLogic = CameraLogic(filesLogic)
        recognition = Recognition(filesLogic)
        filesLogic.askForPermissions()
        filesLogic.beginning()
        openServer()
    }

    private fun openServer() {
        thread {
            embeddedServer(Netty, 8080) {
                install(ContentNegotiation) {
                    gson {}
                }
                routing {
                    post("/api/uploadimage") {
                        val parameters = call.parameters
                        val file = parameters["filePath"]
                        val result = recognition.serverDoTask(file)
                        if (result?.second == null) {
                            call.respond(HttpStatusCode.InternalServerError)
                        } else {
                            call.respond(mapOf(result.first to result.second))
                        }
                    }
                    get("/api/uploadimage") {
                        val parameters = call.parameters
                        val file = parameters["filePath"]
                        val result = recognition.serverDoTask(file)
                        if (result?.second == null) {
                            call.respond(HttpStatusCode.InternalServerError)
                        } else {
                            call.respond(mapOf(result.first to result.second))
                        }
                    }
                }
            }.start(wait = true)
        }
    }

    fun chosen(v: View) {
        filesLogic.performFileSearch()
    }

    fun takeAPhoto(v: View) {
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, REQUEST_TAKE_A_PHOTO, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        recognition.doTask(this, contentResolver, resultCode, resultData)
    }

    fun apply(v: View) {
        Network.workForIlya(SERIAL_NUMBER_VALUE, INDICATION_VALUE)
        SERIAL_NUMBER_VALUE = null
        INDICATION_VALUE = null
        close(v)
    }

    fun close(v: View) { CheckingDialogFragment.close() }

    fun backToFromYouAre(v: View) {
        close(v)
        if (FROM_WHERE) {
            takeAPhoto(v)
        } else {
            chosen(v)
        }
    }
}