package sudo.openhackaton.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import sudo.openhackaton.R
import sudo.openhackaton.logic.CameraLogic
import sudo.openhackaton.logic.Constants.REQUEST_TAKE_A_PHOTO
import sudo.openhackaton.logic.Constants.cameraLogic
import sudo.openhackaton.logic.FilesLogic
import sudo.openhackaton.logic.Recognition

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
    }

    fun chosen(v: View) { filesLogic.performFileSearch() }

    fun takeAPhoto(v: View) {
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, REQUEST_TAKE_A_PHOTO, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        recognition.doTask(this, contentResolver, resultCode, resultData)
    }

    fun apply(v: View) { close(v) }

    fun close(v: View) { CheckingDialogFragment.close() }

    fun backToCamera(v: View) {
        close(v)
        takeAPhoto(v)
    }
}