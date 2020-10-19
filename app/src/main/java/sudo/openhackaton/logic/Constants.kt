package sudo.openhackaton.logic

object Constants {
    const val LANGUAGE = "eng"
    const val ALLOWED_SYMBOLS = "1234567890"
    const val ROOT = "sudo"
    const val TESS_DIR = "tessdata"
    const val DATA_FILE = "eng.traineddata"
    const val IMAGE_DOCUMENT_TYPE = "image/*"
    const val RECOGNITION_DIDNT_SUCCEED = "Text wasn't recognized. :( Try again."
    const val PICTURE_WASNT_TAKEN = "Something went wrong while taking a picture. :("
    const val MODE_READ = "r"
    const val RECOGNITION_IN_PROGRESS = "Recognition is in process..."
    const val REQUEST_CODE_PERMISSIONS = 314
    const val REQUEST_CODE_PICTURE = 44
    const val REQUEST_TAKE_A_PHOTO = 45
    const val JPG = ".jpg"
    const val EMPTY_STRING = ""
    const val CROPPED_SUFFIX = "-CROPPED"
    const val CAPTURE_FAILED = "Photo capture failed: "
    const val TAG = "TAG"
    const val BINDING_FAILED = "Use case binding failed"
    lateinit var cameraLogic: CameraLogic
    lateinit var MAIN_DIR: String
}