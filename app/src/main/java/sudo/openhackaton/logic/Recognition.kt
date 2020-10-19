package sudo.openhackaton.logic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.widget.TextView
import android.widget.Toast
import com.googlecode.tesseract.android.TessBaseAPI
import sudo.openhackaton.logic.Constants.LANGUAGE
import sudo.openhackaton.logic.Constants.MAIN_DIR
import sudo.openhackaton.logic.Constants.PICTURE_WASNT_TAKEN
import sudo.openhackaton.logic.Constants.RECOGNITION_DIDNT_SUCCEED
import sudo.openhackaton.logic.Constants.RECOGNITION_IN_PROGRESS
import sudo.openhackaton.logic.Constants.ROOT
import java.io.File

class Recognition(private val filesLogic: FilesLogic) {
    fun recognizeText(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        val tessBaseAPI = TessBaseAPI()
        try {
            tessBaseAPI.init("$MAIN_DIR/$ROOT", LANGUAGE)
        } catch (e: RuntimeException) {
            filesLogic.deleteBrokenFile()
            filesLogic.loadServiceFiles()
            tessBaseAPI.init("$MAIN_DIR/$ROOT", LANGUAGE)
        }
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, Constants.ALLOWED_SYMBOLS)
        tessBaseAPI.setImage(bitmap)
        val result = tessBaseAPI.utF8Text
        tessBaseAPI.end()
        return result
    }

    fun doTask(
        contentResolver: ContentResolver,
        requestCode: Int,
        indicator: TextView,
        resultCode: Int,
        resultData: Intent?
    ) {
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(filesLogic.context, RECOGNITION_IN_PROGRESS, Toast.LENGTH_LONG).show()
            var bm: Bitmap? = null
            if (requestCode == Constants.REQUEST_CODE_PICTURE) {
                if (resultData != null) {
                    bm = filesLogic.getBitmapFromUri(contentResolver, resultData.data)
                }
            } else if (requestCode == Constants.REQUEST_TAKE_A_PHOTO) {
                bm = filesLogic.getBitmapFromAbsolutePath(filesLogic.currentCroppedPhotoPath)
                if (filesLogic.currentCroppedPhotoPath == null) {
                    Toast.makeText(filesLogic.context, PICTURE_WASNT_TAKEN, Toast.LENGTH_LONG).show()
                }
            }
            @SuppressLint("StaticFieldLeak")
            val asyncTask = object : AsyncTask<Any?, Any?, Any?>() {
                var temp: String? = null
                override fun doInBackground(vararg p0: Any?) {
                    temp = recognizeText(bm)
                }

                override fun onPostExecute(result: Any?) {
                    super.onPostExecute(result)
                    indicator.text = if (temp == null || temp!!.isEmpty()) {
                        RECOGNITION_DIDNT_SUCCEED
                    } else {
                        if (filesLogic.currentCroppedPhotoPath != null) {
                            File(filesLogic.currentCroppedPhotoPath!!).delete()
                            filesLogic.currentCroppedPhotoPath = null
                        }
                        temp
                    }
                }
            }
            asyncTask.execute()
        }
    }
}