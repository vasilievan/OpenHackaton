package sudo.openhackaton.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.dialog_check_if_text_correct.*
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