package sudo.openhackaton.logic

import android.os.Environment

object Constants {
    val MAIN_DIR = Environment.getExternalStorageDirectory().toString()
    val LANGUAGE = "eng"
    const val ALLOWED_SYMBOLS = "1234567890"
    const val TESS_DIR = "tessdata"
    const val DATA_FILE = "eng.traineddata"
    const val IMAGE_DOCUMENT_TYPE = "image/*"

    const val REQUEST_CODE_PERMISSIONS = 314
    const val REQUEST_CODE_PICTURE = 42
}