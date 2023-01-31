package com.example.udos_wg_tohuwabohu.Finances

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        Column (modifier = Modifier
            .padding(15.dp, 0.dp, 15.dp, 20.dp)
            .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        BalanceList()
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

    @Composable
    fun FinanceCard(financeEntry: FinanceEntry) {
        Box(modifier = Modifier.clip(shape = RoundedCornerShape(20.dp)),
        ) {
            Row(modifier = Modifier
                .background(UdoLightBlue)
                .padding(all = 4.dp)
                .clip(shape = RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(start = 5.dp)) {
                    Text(text = financeEntry.description ?: "Unbekannt",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = UdoWhite
                        )
                    Text(
                        text = "Für "+ Math.round(
                            (financeEntry.price?.times(100) ?: 0.0)
                        ).div(100).toString() + " €",
                        color = UdoOrange,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Am " + (financeEntry.timestamp?.let { formatDate(it) } ?: "Unbekannt"),
                        color = UdoWhite
                    )
//                    ListNamesSimpleRow(
//                        description = "Bezahlt von ",
//                        dataHandler.getRoommate(financeEntry.benefactor?.id)?.username
//                            ?: "Unbekannt"
//                    )
                    Text(
                        text = "Bezahlt von " + (dataHandler.getRoommate(financeEntry.benefactor?.id)?.username ?: "Unbekannt"),
                        color = UdoWhite
                    )
                }
                Spacer(modifier = Modifier.weight(0.5f))
                Column(modifier = Modifier
                    .padding(end = 5.dp)
                    .widthIn(160.dp, 200.dp)) {
                    ListNamesSimpleRow(
                        description = "Bezahlt für: ",
                        nameList = financeEntry.moucherList?.map { moucher ->
                            dataHandler.getRoommate(moucher.id)?.username ?: "Unbekannt"
                        }?.toTypedArray() ?: arrayOf("Unbekannt")
                    )
                }
            }
        }
    }

    @Composable
    fun ListNamesSimpleRow(description: String, vararg nameList: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = description, color = UdoWhite)
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
