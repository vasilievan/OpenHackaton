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
import android.net.Uri
import android.os.AsyncTask
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.googlecode.tesseract.android.TessBaseAPI
import sudo.openhackaton.logic.Constants.DATA_FILE
import sudo.openhackaton.logic.Constants.IMAGE_DOCUMENT_TYPE
import sudo.openhackaton.logic.Constants.JPG
import sudo.openhackaton.logic.Constants.LANGUAGE
import sudo.openhackaton.logic.Constants.MAIN_DIR
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

class Logic {
    private var currentPhotoPath: String? = null

    fun askForPermissions(context: Context, activity: Activity) {
        if ((ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)) {
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

    fun beginning(assets: AssetManager) {
        val myDir = File("$MAIN_DIR/$ROOT/$TESS_DIR")
        if (!myDir.exists()) {
            myDir.mkdirs()
        }
        val dataFile = File("$MAIN_DIR/$ROOT/$TESS_DIR/$DATA_FILE")
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
        val parcelFileDescriptor: ParcelFileDescriptor = contentResolver.openFileDescriptor(uri, MODE_READ) ?: return null
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun getBitmapFromAbsolutePath(): Bitmap? {
        try {
            val options = BitmapFactory.Options()
            return BitmapFactory.decodeFile(currentPhotoPath, options)
        } catch (e: IOException) {
        }
        return null
    }

    fun takePictures(packageManager: PackageManager, context: Context, activity: Activity) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
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
        }
    }

    private fun createImageFile(): File? {
        val time = Date().time.toString()
        val file = File("$MAIN_DIR/$ROOT/$time$JPG")
        return try {
            file.createNewFile()
            currentPhotoPath = file.absolutePath
            file
        } catch (e: IOException) {
            null
        }
    }

    fun doTask(context: Context,
                             assets: AssetManager,
                             contentResolver: ContentResolver,
                             requestCode: Int,
                             indicator: TextView,
                             resultCode: Int,
                             resultData: Intent?) {
        if (resultCode == RESULT_OK) {
            Toast.makeText(context, RECOGNITION_IN_PROGRESS, Toast.LENGTH_LONG).show()
            var bm: Bitmap? = null
            if (requestCode == REQUEST_CODE_PICTURE) {
                if (resultData != null) {
                    bm = getBitmapFromUri(contentResolver, resultData.data)
                }
            } else if (requestCode == REQUEST_TAKE_A_PHOTO) {
                bm = getBitmapFromAbsolutePath()
                if (currentPhotoPath != null) File(currentPhotoPath!!).delete()
                else Toast.makeText(context, PICTURE_WASNT_TAKEN, Toast.LENGTH_LONG).show()
                currentPhotoPath = null
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
}