package sudo.openhackaton.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import sudo.openhackaton.R
import sudo.openhackaton.logic.Logic


class MainActivity : AppCompatActivity() {
    private val logic = Logic()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logic.askForPermissions(this, this)
        logic.beginning(assets)
    }

    fun chosen(v: View) {
        logic.performFileSearch(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 42 && resultCode == RESULT_OK) {
            var chosenDocument: Uri? = null
            if (resultData != null) {
                chosenDocument = resultData.data
            }
            val bm = logic.getBitmapFromUri(contentResolver, chosenDocument)
            @SuppressLint("StaticFieldLeak")
            val asyncTask = object : AsyncTask<Any?, Any?, Any?>() {
                var temp: String? = null
                override fun doInBackground(vararg p0: Any?) {
                    temp = logic.recognizeText(assets, bm)
                }
                override fun onPostExecute(result: Any?) {
                    super.onPostExecute(result)
                    indicator.text = temp ?: "Text wasn't recognized. :( Try again."
                }
            }
            asyncTask.execute()
        }
    }
}