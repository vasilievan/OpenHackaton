package sudo.openhackaton.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import sudo.openhackaton.R
import sudo.openhackaton.logic.CameraLogic
import sudo.openhackaton.logic.Constants
import sudo.openhackaton.logic.Constants.EMPTY_STRING
import sudo.openhackaton.logic.Constants.REQUEST_TAKE_A_PHOTO
import sudo.openhackaton.logic.Constants.cameraLogic
import sudo.openhackaton.logic.FilesLogic
import sudo.openhackaton.logic.Recognition
import java.io.Serializable

class MainActivity : AppCompatActivity() {
    private lateinit var recognition: Recognition
    private lateinit var filesLogic: FilesLogic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        filesLogic = FilesLogic(this, this)
        cameraLogic = CameraLogic(filesLogic)
        recognition = Recognition(filesLogic)
        filesLogic.askForPermissions()
        filesLogic.beginning()
    }

    fun chosen(v: View) {
        indicator.text = EMPTY_STRING
        filesLogic.performFileSearch()
    }

    fun takeAPhoto(v: View) {
        indicator.text = EMPTY_STRING
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, REQUEST_TAKE_A_PHOTO, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        recognition.doTask(this, contentResolver, indicator, resultCode, resultData)
    }

    fun apply(v: View) {
        close(v)
    }

    fun close(v: View) {
        CheckingDialogFragment.close()
    }

    fun backToCamera(v: View) {
        close(v)
        takeAPhoto(v)
    }
}