package com.example.udos_wg_tohuwabohu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.KeyboardType.Companion.Text
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.dataclasses.DBWriter
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.model.BarData
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.dimens.ChartDimens

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [FinanceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FinanceFragment : Fragment() {

    private val dataHandler = DataHandler.getInstance()
    private val dbWriter = DBWriter.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return ComposeView(requireContext()).apply {
            setContent {
                FinanceView()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FinanceFragment.
         */
        @JvmStatic
        fun newInstance() = FinanceFragment()
    }

    @Composable
    fun FinanceView() {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .background(UdoWhite)
            ) {
                MyBarChartParent()
            }
        }
    }

    @Composable
    fun MyBarChartParent() {
        val udoColors = listOf(UdoOrange, UdoRed, UdoLightBlue, UdoDarkBlue)
        // TODO doesn't work use material shit
        BarChart(
            modifier = Modifier.fillMaxWidth().height(500.dp).background(UdoOrange),
            onBarClick = {},
            color = udoColors[3],
            chartDimens= ChartDimens(10.dp),
            axisConfig= AxisConfig(showAxis = true, showXLabels = true, showUnitLabels = true, textColor = UdoWhite, isAxisDashed = false, xAxisColor = UdoDarkBlue, yAxisColor = UdoLightBlue),
            barData = dataHandler.roommateList.values.map { m ->
                Log.d("FinanceChart", m.username + ": " + m.balance)
                BarData(m.username ?: "unknown", (m.balance ?: 0.0f).toFloat())
            }
        )
    }
}

