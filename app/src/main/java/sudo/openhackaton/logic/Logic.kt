package sudo.openhackaton.logic

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.AsyncTask
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.googlecode.tesseract.android.TessBaseAPI
import sudo.openhackaton.logic.Constants.DATA_FILE
import sudo.openhackaton.logic.Constants.IMAGE_DOCUMENT_TYPE
import sudo.openhackaton.logic.Constants.JPG
import sudo.openhackaton.logic.Constants.LANGUAGE
import sudo.openhackaton.logic.Constants.MODE_READ
import sudo.openhackaton.logic.Constants.PICTURE_WASNT_TAKEN
import sudo.openhackaton.logic.Constants.RECOGNITION_DIDNT_SUCCEED
import sudo.openhackaton.logic.Constants.RECOGNITION_IN_PROGRESS
import sudo.openhackaton.logic.Constants.REQUEST_CODE_PERMISSIONS
import sudo.openhackaton.logic.Constants.REQUEST_CODE_PICTURE
import sudo.openhackaton.logic.Constants.REQUEST_TAKE_A_PHOTO
import sudo.openhackaton.logic.Constants.ROOT
import sudo.openhackaton.logic.Constants.TESS_DIR
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt

class Logic {

    private var imageCapture: ImageCapture? = null


    fun askForPermissions(context: Context, activity: Activity) {
        if ((ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    fun beginning(assets: AssetManager, context: Context) {
        MAIN_DIR = context.getExternalFilesDir(null).toString()
        beginning(assets)
    }

    private fun beginning(assets: AssetManager) {
        val myDir = File("$MAIN_DIR/$ROOT/$TESS_DIR")
        if (!myDir.exists()) {
            myDir.mkdirs()
        }

        val dataFile = File(myDir, DATA_FILE)
        if (!dataFile.exists()) {
            dataFile.createNewFile()
            fulFillDataFile(assets, dataFile)
        }
    }

    private fun fulFillDataFile(assets: AssetManager, dataFile: File) {
        val fous = FileOutputStream(dataFile)
        fous.write(assets.open("$TESS_DIR/$DATA_FILE").readBytes())
        fous.flush()
        fous.close()
    }

    private fun deleteBrokenFile() {
        val brokenFile = File("$MAIN_DIR/$ROOT/$TESS_DIR/$DATA_FILE")
        if (brokenFile.exists()) {
            brokenFile.delete()
        }
    }

    fun recognizeText(assets: AssetManager, bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        val tessBaseAPI = TessBaseAPI()
        try {
            tessBaseAPI.init("$MAIN_DIR/$ROOT", LANGUAGE)
        } catch (e: RuntimeException) {
            deleteBrokenFile()
            beginning(assets)
            tessBaseAPI.init("$MAIN_DIR/$ROOT", LANGUAGE)
        }
        //tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, ALLOWED_SYMBOLS)
        tessBaseAPI.setImage(bitmap)
        val result = tessBaseAPI.utF8Text
        tessBaseAPI.end()
        return result
    }

    fun performFileSearch(activity: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = IMAGE_DOCUMENT_TYPE
        startActivityForResult(activity, intent, REQUEST_CODE_PICTURE, null)
    }

    private fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri?): Bitmap? {
        if (uri == null) return null
        val parcelFileDescriptor: ParcelFileDescriptor = contentResolver.openFileDescriptor(uri, MODE_READ)
            ?: return null
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun getBitmapFromAbsolutePath(path: String?): Bitmap? {
        try {
            val options = BitmapFactory.Options()
            return BitmapFactory.decodeFile(path, options)
        } catch (e: IOException) {
        }
        return null
    }


    fun takePictures(context: Context, activity: Activity, frame: View, reference: View) {
        val imageCapture = imageCapture ?: return

        val photoFile: File = try {
            createImageFile("") ?: return
        } catch (ex: IOException) {
            return
        }

        val croppedPhotoFile: File = try {
            createImageFile("-CROPPED") ?: return
        } catch (ex: IOException) {
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("TAG", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val data = Intent()
                    data.data = savedUri
                    val bm = getBitmapFromAbsolutePath(currentPhotoPath)!!
                    val bmCropped = cropImage(bm, frame, reference)
                    val fout = FileOutputStream(croppedPhotoFile)
                    bmCropped.compress(Bitmap.CompressFormat.JPEG, 100, fout)
                    fout.flush()
                    fout.close()
                    activity.setResult(RESULT_OK)
                    activity.finish()
                }
            })


        /* Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            context,
                            "sudo.openhackaton.provider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(activity, takePictureIntent, REQUEST_TAKE_A_PHOTO, null)
                }
            }
        } */
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

            imageCapture = ImageCapture.Builder()
                .build()

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    activity, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e("TAG", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(activity))
    }


    private fun createImageFile(suffix: String): File? {
        val time = Date().time.toString()
        val file = File("$MAIN_DIR/$ROOT/$time$suffix$JPG")
        return try {
            file.createNewFile()
            if (suffix.isEmpty()) {
                currentPhotoPath = file.absolutePath
            } else {
                currentCroppedPhotoPath = file.absolutePath
            }
            file
        } catch (e: IOException) {
            null
        }
    }

    fun doTask(
        context: Context,
        assets: AssetManager,
        contentResolver: ContentResolver,
        requestCode: Int,
        indicator: TextView,
        resultCode: Int,
        resultData: Intent?
    ) {
        if (resultCode == RESULT_OK) {
            Toast.makeText(context, RECOGNITION_IN_PROGRESS, Toast.LENGTH_LONG).show()
            var bm: Bitmap? = null
            if (requestCode == REQUEST_CODE_PICTURE) {
                if (resultData != null) {
                    bm = getBitmapFromUri(contentResolver, resultData.data)
                }
            } else if (requestCode == REQUEST_TAKE_A_PHOTO) {
                bm = getBitmapFromAbsolutePath(currentCroppedPhotoPath)
                if (currentCroppedPhotoPath != null) {
                    //File(currentCroppedPhotoPath!!).delete()
                } else {
                    Toast.makeText(context, PICTURE_WASNT_TAKEN, Toast.LENGTH_LONG).show()
                }
                currentCroppedPhotoPath = null
            }
            @SuppressLint("StaticFieldLeak")
            val asyncTask = object : AsyncTask<Any?, Any?, Any?>() {
                var temp: String? = null
                override fun doInBackground(vararg p0: Any?) {
                    temp = recognizeText(assets, bm)
                }

                override fun onPostExecute(result: Any?) {
                    super.onPostExecute(result)
                    indicator.text = if (temp == null || temp!!.isEmpty()) {
                        RECOGNITION_DIDNT_SUCCEED
                    } else {
                        temp
                    }
                }
            }
            asyncTask.execute()
        }
    }

    companion object {
        private var currentPhotoPath: String? = null
        private var currentCroppedPhotoPath: String? = null

        private lateinit var MAIN_DIR: String
    }
}