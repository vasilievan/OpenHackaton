package sudo.openhackaton.logic

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import sudo.openhackaton.R
import sudo.openhackaton.logic.Constants.RECOGNITION_DIDNT_SUCCEED
import sudo.openhackaton.logic.Constants.RECOGNITION_IN_PROGRESS
import sudo.openhackaton.logic.Constants.TEXT_FROM_RECOGNITION
import sudo.openhackaton.view.CheckingDialogFragment


class Recognition(private val filesLogic: FilesLogic) {
    private var inputImage: InputImage? = null
    private val recognizer = TextRecognition.getClient()
    private lateinit var context: AppCompatActivity

    fun doTask(context: AppCompatActivity,
               contentResolver: ContentResolver,
               indicator: TextView,
               resultCode: Int,
               resultData: Intent?) {
        this.context = context
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(filesLogic.context, RECOGNITION_IN_PROGRESS, Toast.LENGTH_LONG).show()

            inputImage = if (resultData == null) {
                if (filesLogic.bitmap != null) InputImage.fromBitmap(filesLogic.bitmap!!, 0)
                else InputImage.fromFilePath(filesLogic.context, Uri.fromFile(filesLogic.lastCreated))
            } else {
                val bm = filesLogic.getBitmapFromUri(contentResolver, resultData.data) ?: return
                InputImage.fromBitmap(bm, 0)
            }

            if (inputImage == null) {
                indicator.text = RECOGNITION_DIDNT_SUCCEED
                return
            }

            recognizer.process(inputImage!!).addOnCompleteListener {
                indicator.text = if (it.result == null || it.result!!.text.isEmpty()) {
                    CheckingDialogFragment.newInstance(RECOGNITION_DIDNT_SUCCEED).show(context.supportFragmentManager, TEXT_FROM_RECOGNITION)
                    RECOGNITION_DIDNT_SUCCEED
                } else {
                    if (filesLogic.lastCreated != null) {
                        filesLogic.lastCreated!!.delete()
                    }
                    CheckingDialogFragment.newInstance(it.result!!.text).show(context.supportFragmentManager, TEXT_FROM_RECOGNITION)
                    it.result!!.text
                }
            }
        }
    }
}