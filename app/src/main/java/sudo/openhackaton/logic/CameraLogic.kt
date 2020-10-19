package sudo.openhackaton.logic

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import sudo.openhackaton.logic.Constants.BINDING_FAILED
import sudo.openhackaton.logic.Constants.CAPTURE_FAILED
import sudo.openhackaton.logic.Constants.CROPPED_SUFFIX
import sudo.openhackaton.logic.Constants.EMPTY_STRING
import sudo.openhackaton.logic.Constants.TAG
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt

class CameraLogic(val filesLogic: FilesLogic) {
    private var imageCapture: ImageCapture? = null
    private var cameraActivity: Activity? = null

    fun setCameraActivity(activity: Activity) { cameraActivity = activity }

    fun takePictures(frame: View, reference: View) {
        val imageCapture = imageCapture ?: return
        val photoFile: File = try {
            filesLogic.createImageFile(EMPTY_STRING) ?: return
        } catch (ex: IOException) {
            return
        }
        val croppedPhotoFile: File = try {
            filesLogic.createImageFile(CROPPED_SUFFIX) ?: return
        } catch (ex: IOException) {
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(filesLogic.context), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "$CAPTURE_FAILED${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val data = Intent()
                    data.data = savedUri
                    val bm = filesLogic.getBitmapFromAbsolutePath(filesLogic.currentPhotoPath)!!
                    val bmCropped = cropImage(bm, frame, reference)
                    val fout = FileOutputStream(croppedPhotoFile)
                    bmCropped.compress(Bitmap.CompressFormat.JPEG, 100, fout)
                    fout.flush()
                    fout.close()
                    cameraActivity!!.setResult(Activity.RESULT_OK)
                    cameraActivity!!.finish()
                }
            })
    }

    private fun cropImage(bitmap: Bitmap, frame: View, reference: View): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(90F)
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0,
            bitmap.width, bitmap.height, matrix, true
        )

        val koefX = rotatedBitmap.width.toFloat() / reference.width
        val koefY = rotatedBitmap.height.toFloat() / reference.height
        val x1: Int = frame.left
        val y1: Int = frame.top
        val x2: Int = frame.width
        val y2: Int = frame.height
        val cropStartX = (x1 * koefX).roundToInt()
        val cropStartY = (y1 * koefY).roundToInt()
        val cropWidthX = (x2 * koefX).roundToInt()
        val cropHeightY = (y2 * koefY).roundToInt()

        return Bitmap.createBitmap(
            rotatedBitmap,
            cropStartX, cropStartY, cropWidthX, cropHeightY
        )
    }

    fun startCamera(viewFinder: PreviewView, activity: AppCompatActivity) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    activity, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, BINDING_FAILED, exc)
            }

        }, ContextCompat.getMainExecutor(activity))
    }
}