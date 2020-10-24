package sudo.openhackaton.logic

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import sudo.openhackaton.logic.Constants.RECOGNITION_DIDNT_SUCCEED
import sudo.openhackaton.logic.Constants.RECOGNITION_IN_PROGRESS
import sudo.openhackaton.logic.Constants.TEXT_FROM_RECOGNITION
import sudo.openhackaton.view.CheckingDialogFragment
import java.io.FileOutputStream
import java.io.IOException

class Recognition(private val filesLogic: FilesLogic) {
    private var inputImage: InputImage? = null
    private val recognizer = TextRecognition.getClient()
    private lateinit var context: AppCompatActivity

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun prepareData(contentResolver: ContentResolver, uri: Uri?) {
        try {
            val bm = filesLogic.getBitmapFromUri(contentResolver, uri) ?: return
            filesLogic.createImageFile()
            val fos = FileOutputStream(filesLogic.lastCreated)
            val rotated = rotateBitmap(bm,90f) ?: return
            rotated.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            return
        }
    }

    fun doTask(
            context: AppCompatActivity,
            contentResolver: ContentResolver,
            resultCode: Int,
            resultData: Intent?
    ) {
        this.context = context
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(filesLogic.context, RECOGNITION_IN_PROGRESS, Toast.LENGTH_LONG).show()
            inputImage = if (resultData == null) {
                if (filesLogic.bitmap != null) InputImage.fromBitmap(filesLogic.bitmap!!, 0)
                else InputImage.fromFilePath(
                        filesLogic.context,
                        Uri.fromFile(filesLogic.lastCreated)
                )
            } else {
                prepareData(contentResolver, resultData.data)
                val bm = filesLogic.getBitmapFromAbsolutePath(filesLogic.lastCreated!!.absolutePath) ?: return
                InputImage.fromBitmap(bm, 0)
            }

            if (inputImage == null) {
                CheckingDialogFragment.newInstance(RECOGNITION_DIDNT_SUCCEED)
                    .show(context.supportFragmentManager, TEXT_FROM_RECOGNITION)
                return
            }

            recognizer.process(inputImage!!).addOnCompleteListener {
               if (it.result == null || it.result!!.text.isEmpty()) {
                    CheckingDialogFragment.newInstance(RECOGNITION_DIDNT_SUCCEED)
                        .show(context.supportFragmentManager, TEXT_FROM_RECOGNITION)

                } else {
                    if (filesLogic.lastCreated != null) {
                        filesLogic.lastCreated!!.delete()
                    }
                    CheckingDialogFragment.newInstance(it.result!!.text)
                        .show(context.supportFragmentManager, TEXT_FROM_RECOGNITION)
                }
            }
        }
    }
}