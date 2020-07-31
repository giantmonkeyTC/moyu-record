package cn.troph.tomon.ui.chat.fragments

import OnStampClickListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.cruxlab.sectionedrecyclerview.lib.PositionManager
import com.cruxlab.sectionedrecyclerview.lib.SectionDataManager
import kotlinx.android.synthetic.main.fragment_stamp.*

class StampFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mSectionDataManager: SectionDataManager
    private lateinit var mGridLayoutManager: GridLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun loadStamp() {
        val stampClickListener = object : OnStampClickListener {
            override fun onStampSelected(stampCode: String) {

            }

            override fun onSystemStampSelected(unicode: String) {

            }

        }
        mSectionDataManager = SectionDataManager()
        mGridLayoutManager = GridLayoutManager(requireContext(), 7)
        val positionManager: PositionManager = mSectionDataManager
        mGridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (positionManager.isHeader(position)) {
                    return mGridLayoutManager.spanCount
                } else {
                    return 1
                }
            }
        }
        stamp_rr.layoutManager = mGridLayoutManager

    }

}