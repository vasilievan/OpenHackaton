package sudo.openhackaton.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import sudo.openhackaton.R
import sudo.openhackaton.logic.Constants.RECOGNITION_DIDNT_SUCCEED
import sudo.openhackaton.logic.Constants.REQUEST_CODE_PICTURE
import sudo.openhackaton.logic.Logic
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    private val logic = Logic()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logic.askForPermissions(this, this)
        logic.beginning(assets)
    }

    fun chosen(v: View) {
        indicator.text = ""
        logic.performFileSearch(this)
    }

    fun takeAPhoto(v: View) {
        logic.takePictures(packageManager, this, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        logic.chosenFromFileSystem(this, assets, contentResolver, requestCode, indicator, resultCode, resultData)
    }
}