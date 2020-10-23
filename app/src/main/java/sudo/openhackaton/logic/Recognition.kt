package sudo.openhackaton.logic

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import sudo.openhackaton.logic.Constants.RECOGNITION_DIDNT_SUCCEED
import sudo.openhackaton.logic.Constants.RECOGNITION_IN_PROGRESS

class Recognition(private val filesLogic: FilesLogic) {
    private lateinit var inputImage: InputImage
    private val recognizer = TextRecognition.getClient()

    fun doTask(contentResolver: ContentResolver,
               indicator: TextView,
               resultCode: Int,
               resultData: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(filesLogic.context, RECOGNITION_IN_PROGRESS, Toast.LENGTH_LONG).show()

            inputImage = if (resultData == null) {
                if (filesLogic.bitmap != null) InputImage.fromBitmap(filesLogic.bitmap!!, 0)
                else InputImage.fromFilePath(filesLogic.context, Uri.fromFile(filesLogic.lastCreated))
            } else {
                val bm = filesLogic.getBitmapFromUri(contentResolver, resultData.data) ?: return
                InputImage.fromBitmap(bm, 0)
            }

            recognizer.process(inputImage).addOnCompleteListener {
                indicator.text = if (it.result == null || it.result.toString().isEmpty()) {
                    RECOGNITION_DIDNT_SUCCEED
                } else {
                    if (filesLogic.lastCreated != null) {
                        filesLogic.lastCreated!!.delete()
                    }
                    it.result!!.text
                }
            }
        }
    }
}