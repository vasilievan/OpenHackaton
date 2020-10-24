package sudo.openhackaton.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import sudo.openhackaton.R
import sudo.openhackaton.logic.Constants.INDICATION
import sudo.openhackaton.logic.Constants.INDICATION_VALUE
import sudo.openhackaton.logic.Constants.SERIAL_NUMBER
import sudo.openhackaton.logic.Constants.SERIAL_NUMBER_VALUE
import java.util.*

class CheckingDialogFragment : DialogFragment() {
    private var serialNumberString: String? = null
    private var indicationString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        indicationString = arguments!!.getString(INDICATION)
        serialNumberString = arguments!!.getString(SERIAL_NUMBER)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.alert_background)
        val view = inflater.inflate(R.layout.dialog_check_if_text_correct, container, false)
        val indication = view.findViewById<TextView>(R.id.indication)
        indication.text = indicationString
        indication.addTextChangedListener {
            INDICATION_VALUE = indication.text.toString()
        }
        val serialNumber = view.findViewById<TextView>(R.id.serialNumber)
        serialNumber.text = serialNumberString
        serialNumber.addTextChangedListener {
            SERIAL_NUMBER_VALUE = serialNumber.text.toString()
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.30).toInt()
        dialog!!.window?.setLayout(width, height)
    }

    companion object {
        private var q: CheckingDialogFragment? = null
        fun newInstance(serialNumber: String?, indication: String?): DialogFragment {
            val fragment = CheckingDialogFragment()
            q = fragment
            val args = Bundle()
            args.putString(SERIAL_NUMBER, serialNumber)
            SERIAL_NUMBER_VALUE = serialNumber
            args.putString(INDICATION, indication)
            INDICATION_VALUE = indication
            fragment.arguments = args
            return fragment
        }

        fun close() { q?.dismiss() }
    }
}
