package sudo.openhackaton.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera.*
import sudo.openhackaton.R
import sudo.openhackaton.logic.Constants.cameraLogic

class CameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        cameraLogic.setCameraActivity(this)
        cameraLogic.startCamera(viewFinder, this)
    }

    fun takeAPhoto(v: View) {
        cameraLogic.takePictures()
        v.isClickable = false
    }
}