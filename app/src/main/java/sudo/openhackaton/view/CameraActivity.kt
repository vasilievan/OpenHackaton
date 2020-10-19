package sudo.openhackaton.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera.*
import sudo.openhackaton.R
import sudo.openhackaton.logic.Logic

class CameraActivity : AppCompatActivity(){
    private val logic = Logic()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        logic.startCamera(viewFinder, this)
    }

    fun takeAPhoto(view: View) {
        logic.takePictures(this, this, rectangle, viewFinder)
    }
}

