package com.example.udos_wg_tohuwabohu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.dataclasses.DBWriter
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.example.udos_wg_tohuwabohu.dataclasses.FinanceEntry
import com.github.tehras.charts.bar.BarChart
import com.github.tehras.charts.bar.BarChartData
import com.github.tehras.charts.bar.renderer.bar.SimpleBarDrawer
import com.github.tehras.charts.bar.renderer.label.SimpleValueDrawer
import com.github.tehras.charts.bar.renderer.yaxis.SimpleYAxisDrawer
import com.github.tehras.charts.piechart.animation.simpleChartAnimation
import java.util.*
import kotlin.math.abs

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

    fun formatDate(date: Date): String {
        return "" + formatNumber(date.date) + "." + formatNumber(date.month+1) + "." + formatNumber(1900+date.year)
    }

    fun formatNumber(n: Int): String {
        Log.d("FinanceFragmentFormatNumber", "Got number: $n")
        return if (n > 9) "" + n else "0" + n
    }

    @Composable
    fun FinanceView() {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(UdoDarkGray)
            ) {
                ChartPane()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp, 0.dp, 15.dp, 0.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                dataHandler.financeEntries.forEach { f ->
                    Spacer(modifier = Modifier.height(10.dp))
                    FinanceCard(financeEntry = f)
                }
            }
        }
    }

    @Composable
    fun FinanceCard(financeEntry: FinanceEntry) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .clip(RoundedCornerShape(10.dp))
                .background(UdoLightBlue)
        ) {
            Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp)) {
                Log.d("FinanceChar", financeEntry.toString())
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = financeEntry.description ?: "unknown",
                        style = UdoFinanceTextFieldTypographie()
                    )
                    Text(
                        text = Math.round(
                            (financeEntry.price?.times(100) ?: 0.0)
                        ).div(100).toString() + " €",
                        modifier = Modifier.align(androidx.compose.ui.Alignment.CenterEnd),
                        color = UdoOrange
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ListNamesSimpleRow(
                        description = "Gönner",
                        dataHandler.getRoommate(financeEntry.benefactor?.id)?.username
                            ?: "unknown"
                    )
                    ListNamesSimpleRow(
                        description = "Schnorrer",
                        nameList = financeEntry.moucherList?.map { moucher ->
                            dataHandler.getRoommate(moucher.id)?.username ?: "unknown"
                        }?.toTypedArray() ?: arrayOf("unknown")
                    )
                }
                Text(
                    text = financeEntry.timestamp?.let { formatDate(it)} ?: "unknown"
                )
            }
        }
    }

    @Composable
    fun ListNamesSimpleRow(description: String, vararg nameList: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = description)
            Spacer(modifier = Modifier.width(5.dp))
            Column {
                nameList.forEach { name ->
                    WhiteSimpleText(
                        text = name
                    )
                }
            }
        }
    }

    @Composable
    fun WhiteSimpleText(text: String) {
        Text(
            text = text,
            color = UdoWhite
        )
    }

    @Composable
    fun ChartPane() {
        val chartHeight = 300.dp
        val udoColors = listOf(UdoOrange, UdoRed, UdoLightBlue, UdoDarkBlue)
        var index = 0;
        Column(modifier = Modifier.padding(0.dp, 15.dp, 0.dp, 15.dp)) {
            // Positive bars
            BarChart(
                barChartData = BarChartData(
                    bars = dataHandler.roommateList.values.filter { r -> (r.balance ?: 0.0) >= 0 }
                        .map { r ->
                            if (index == udoColors.size) index = 0
                            BarChartData.Bar(
                                label = (r.username ?: "unknown"),
                                value = (r.balance ?: 0.0).toFloat() + 5,
                                color = udoColors[index++]
                            )
                        },
                    startAtZero = true
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight.div(2))
                    .background(Color.Transparent),
                animation = simpleChartAnimation(),
                barDrawer = SimpleBarDrawer(),
                yAxisDrawer = SimpleYAxisDrawer(),
                labelDrawer = SimpleValueDrawer()
            )
            index = udoColors.size - 1
            // Negative bars
            BarChart(
                barChartData = BarChartData(
                    bars = dataHandler.roommateList.values.filter { r -> (r.balance ?: 0.0) < 0 }
                        .map { r ->
                            if (index < 0) index = (udoColors.size - 1)
                            BarChartData.Bar(
                                label = (r.username ?: "unknown"),
                                value = abs((r.balance ?: 0.0).toFloat()),
                                color = udoColors[index--]
                            )
                        },
                    startAtZero = true
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight.div(2))
                    .background(Color.Transparent)
                    .rotate(180f)
                    .offset((-40).dp, 2.dp),
                animation = simpleChartAnimation(),
                barDrawer = SimpleBarDrawer(),
                labelDrawer = SimpleValueDrawer()
            )
        }

    }
}