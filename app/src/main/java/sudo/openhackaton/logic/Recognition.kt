package sudo.openhackaton.logic

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import sudo.openhackaton.logic.Constants.RECOGNITION_DIDNT_SUCCEED
import sudo.openhackaton.logic.Constants.RECOGNITION_IN_PROGRESS
import java.io.FileOutputStream
import java.io.IOException

class Recognition(private val filesLogic: FilesLogic) {
    private var inputImage: InputImage? = null
    private val recognizer = TextRecognition.getClient()

    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun prepateData(contentResolver: ContentResolver, uri: Uri?) {
        try {
            val bm = filesLogic.getBitmapFromUri(contentResolver, uri) ?: return
            val rotated = rotateBitmap(bm, 90f) ?: return
            filesLogic.createImageFile()
            val fos = FileOutputStream(filesLogic.lastCreated)
            rotated.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            return
        }
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
            val src = Imgcodecs.imread(filesLogic.lastCreated!!.absolutePath, 0)
            val dst = Mat()
            Imgproc.adaptiveThreshold(
                    src, dst, 125.0, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                    Imgproc.THRESH_BINARY, 11, 12.0
            )
            Imgcodecs.imwrite(filesLogic.lastCreated!!.absolutePath, dst)
        } catch (e: Exception) {
            return
        }
    }

    fun doTask(
            contentResolver: ContentResolver,
            indicator: TextView,
            resultCode: Int,
            resultData: Intent?
    ) {
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(filesLogic.context, RECOGNITION_IN_PROGRESS, Toast.LENGTH_LONG).show()
            inputImage = if (resultData == null) {
                if (filesLogic.bitmap != null) InputImage.fromBitmap(filesLogic.bitmap!!, 0)
                else InputImage.fromFilePath(
                        filesLogic.context,
                        Uri.fromFile(filesLogic.lastCreated)
                )
            } else {
                prepateData(contentResolver, resultData.data)
                val bm = filesLogic.getBitmapFromUri(contentResolver, resultData.data) ?: return
                InputImage.fromBitmap(bm, 0)
            }

            if (inputImage == null) {
                indicator.text = RECOGNITION_DIDNT_SUCCEED
                return
            }

            recognizer.process(inputImage!!).addOnCompleteListener {
                indicator.text = if (it.result == null || it.result!!.text.isEmpty()) {
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