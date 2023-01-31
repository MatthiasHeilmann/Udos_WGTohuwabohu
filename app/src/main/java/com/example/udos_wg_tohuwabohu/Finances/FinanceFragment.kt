package com.example.udos_wg_tohuwabohu.Finances

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.*
import com.example.udos_wg_tohuwabohu.dataclasses.DBWriter
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.example.udos_wg_tohuwabohu.dataclasses.FinanceEntry
import java.util.*
import kotlin.math.roundToInt
import kotlin.streams.toList

class FinanceFragment : Fragment() {

    private lateinit var mainActivity: MainActivity

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
                FinanceFab()
            }
        }
    }


    fun formatDate(date: Date): String {
        return "" + formatNumber(date.date) + "." + formatNumber(date.month + 1) + "." + formatNumber(
            1900 + date.year
        )
    }

    fun formatNumber(n: Int): String {
        return if (n > 9) "" + n else "0" + n
    }

    @Composable
    fun FinanceView() {
        Column {
            Box(
                modifier = Modifier.background(UdoDarkBlue)
            ) {
                BalanceList()
            }
            Column(
                modifier = Modifier
                    .padding(15.dp, 0.dp, 15.dp, 0.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Letzte Einträge:",
                    modifier = Modifier.padding(10.dp),
                    color = UdoWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
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
                    text = financeEntry.timestamp?.let { formatDate(it) } ?: "unknown"
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
    fun BalanceList() {
        val sortedList = dataHandler.roommateList.values.stream().sorted { r1, r2 ->
            return@sorted if ((r1.balance ?: 0f).toFloat() <= (r2.balance ?: 0f).toFloat()) 1
            else -1
        }.toList()
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Eure Kontostände:",
                modifier = Modifier.padding(10.dp),
                color = UdoWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            sortedList.forEach { roommate ->
                Card(
                    modifier = Modifier
                        .padding(60.dp, 5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(UdoLightBlue)
                    ) {
                        Column() {
                            Text(
                                text = roommate.username ?: "unknown",
                                modifier = Modifier.padding(14.dp, 7.dp),
                                color = UdoWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.weight(0.5f))
                        Column {
                            val b = (roommate.balance ?: 0f).toFloat()
                            val color = if (b > 0f) UdoGreen else if (b == 0f) UdoWhite else UdoRed
                            Text(
                                text = "" + ((roommate.balance?.times(100) ?: 0f) as Double)
                                    .roundToInt().div(100f) + "€",
                                color = color,
                                modifier = Modifier.padding(14.dp, 7.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun FinanceFab() {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.padding(10.dp)
        ) {
            FloatingActionButton(
                onClick = { mainActivity.openAddFinanceFragment() },
                modifier = Modifier
                    .requiredHeight(60.dp)
                    .requiredWidth(60.dp),
                shape = CircleShape,
                containerColor = UdoOrange
            ) { Text("+", color = UdoDarkBlue, fontSize = 30.sp) }
        }
    }

    @Composable
    fun WhiteSimpleText(text: String) {
        Text(
            text = text,
            color = UdoWhite
        )
    }

    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
    }
}
