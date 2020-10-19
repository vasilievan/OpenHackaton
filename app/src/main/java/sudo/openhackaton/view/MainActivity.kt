package sudo.openhackaton.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_main.*
import sudo.openhackaton.R
import sudo.openhackaton.logic.Constants.REQUEST_TAKE_A_PHOTO
import sudo.openhackaton.logic.Logic

class MainActivity : AppCompatActivity() {
    private val logic = Logic()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logic.askForPermissions(this, this)
        logic.beginning(assets, this)
    }

    fun chosen(v: View) {
        indicator.text = ""
        logic.performFileSearch(this)
    }

    fun takeAPhoto(v: View) {
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, REQUEST_TAKE_A_PHOTO, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        logic.doTask(
            this,
            assets,
            contentResolver,
            requestCode,
            indicator,
            resultCode,
            resultData
        )
    }
}