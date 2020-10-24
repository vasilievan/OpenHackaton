package sudo.openhackaton.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import sudo.openhackaton.R
import sudo.openhackaton.logic.Constants.INDICATION
import sudo.openhackaton.logic.Constants.SERIAL_NUMBER
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
        val serialNumber = view.findViewById<TextView>(R.id.serialNumber)
        indication.text = indicationString
        serialNumber.text = serialNumberString
        return view
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.30).toInt()
        dialog!!.window?.setLayout(width, height)
    }

    companion object {
        private val q: Queue<CheckingDialogFragment> = LinkedList()
        fun newInstance(serialNumber: String?, indication: String?): DialogFragment {
            val fragment = CheckingDialogFragment()
            val args = Bundle()
            args.putString(SERIAL_NUMBER, serialNumber)
            args.putString(INDICATION, indication)
            fragment.arguments = args
            q.add(fragment)
            return fragment
        }

        fun close() {
            if (q.isNotEmpty()) {
                q.poll().dismiss()
            }
        }
    }
}
