package com.example.udos_wg_tohuwabohu

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.dataclasses.ChatMessage
import com.example.udos_wg_tohuwabohu.dataclasses.DBWriter
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import java.text.DateFormatSymbols
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_CHAT_LIST = "chatList"


class ChatFragment : Fragment() {

    private val dataHandler = DataHandler.getInstance()
    private val dbWriter = DBWriter.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ChatBox()
            }
        }
    }

    fun formatDate(date: Date): String {
        return "" + formatNumber(date.hours) + ":" + formatNumber(date.minutes)
    }

    fun formatNumber(n: Int): String {
        return if (n > 9) "" + n else "0" + n
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun ChatBox() {
        Column(
            modifier = Modifier
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(
                        enabled = true,
                        state = ScrollState(0),
                        reverseScrolling = true
                    )
                    .weight(1f, false)
                    .padding(bottom = 10.dp)
            ) {
                var date:Int = 0
                val c = Calendar.getInstance()
                val currentDay = c.get(Calendar.DATE)
                val currentMonth = c.get(Calendar.MONTH)+1
                val currentYear = c.get(Calendar.YEAR)
                var msgBefore:ChatMessage? = null
                dataHandler.chat.forEach { msg ->
                    if(msg.timestamp?.date!=date){
                        val day = msg.timestamp!!.date
                        val month = msg.timestamp!!.month+1
                        val year = msg.timestamp!!.year+1900
                        date = day
                        val dateText:String
                        dateText = if(day==currentDay&&year==currentYear&&month==currentMonth){
                            "Heute"
                        }else if(day+1==currentDay&&year==currentYear&&month==currentMonth){
                            "Gestern"
                        }else{
                            "$day.$month.$year"
                        }
                        androidx.compose.material3.Text(
                            text = dateText, textAlign = TextAlign.Center, color = UdoWhite, modifier = Modifier.padding(top = 10.dp, start =9.dp)
                        )
                        if(date!=0){
                            val mod = if(msg.user?.id == dataHandler.user?.docID) Modifier.padding(bottom = 10.dp) else Modifier.padding(all = 0.dp)
                            Divider(thickness= 1.dp,color= UdoWhite, modifier = mod)
                            msgBefore = null
                        }
                    }
                    MessageCard(msg,msgBefore)
                    msgBefore = msg
                }
            }
            Box(
                modifier = Modifier
                    .align(CenterHorizontally)
            ) {
                MessageInput()
            }
        }
    }

    @Composable
    fun MessageInput() {
        var textFieldValue by remember { mutableStateOf("") }

        Row (modifier = Modifier.background(UdoWhite)) {
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .background(UdoWhite)
                    .heightIn(0.dp, 100.dp),
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = { Text(text = "Nachricht") },
                placeholder = { Text(text = "Schreibe eine Nachricht") },
                textStyle = TextStyle(color = UdoDarkGray),
                colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = UdoDarkBlue, unfocusedBorderColor = UdoWhite, focusedLabelColor = UdoDarkBlue, cursorColor = UdoDarkBlue )
            )
            Button(
                modifier = Modifier
                    .padding(all = 3.dp)
                    .height(50.dp)
                    .width(50.dp)
                    .shadow(elevation = 0.dp),
                onClick = {
                    Log.d("[CHAT]",textFieldValue.trim())
                    val message =textFieldValue.trim{it <= ' '}.trim{it <= '\n'}
                    Log.d("[CHAT]",message)
                    if (message != "") {
                        uploadMessage(message)
                    } else {
                        Toast.makeText(
                            activity,
                            "Bitte gib eine gültige Nachricht ein!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    textFieldValue = ""
                },
                colors = androidx.compose.material.ButtonDefaults.buttonColors(backgroundColor = UdoDarkBlue),
                shape = RoundedCornerShape(50.dp),
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Send Message",
                    modifier = Modifier.size(ButtonDefaults.IconSize.times(2f)).padding(all = 0.dp),
                    tint = UdoBeige
                )
            }
        }
    }

    @Composable
    fun MessageCard(msg: ChatMessage, msgbefore:ChatMessage?) {
        val thisUser = msg.user?.id == dataHandler.user?.docID && msg.user?.id!= null
        val username = dataHandler.getRoommate(msg.user?.id)?.username ?: "Unbekannt"
        val alignment = if (thisUser) Alignment.CenterEnd else Alignment.CenterStart
        val alignmentText = if (thisUser) Alignment.End else Alignment.Start
        val cardColor = if (thisUser) UdoBeige else UdoLightBlue
        val cardTextColor = if (thisUser) UdoDarkGray else UdoWhite
        val cardPadding = if(thisUser) Modifier.padding(start = 20.dp) else Modifier.padding(end = 20.dp)
        val sameUserAsBefore = msg.user?.id == msgbefore?.user?.id
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(alignment)
                    .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
            ) {
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {

                    if(!sameUserAsBefore && !thisUser) {
                        Text(
                            text = username,
                            color = UdoOrange,
                            modifier = Modifier.align(alignmentText).padding(top = 5.dp, bottom = 2.dp),
                            style = MaterialTheme.typography.subtitle2,
                        )
                    }
//                    Spacer(modifier = Modifier.height(1.dp))

                    Surface(
//                        modifier = Modifier.fillMaxWidth(),
                        modifier = cardPadding,
                        shape = MaterialTheme.shapes.medium,
                        elevation = 5.dp
                    ) {
                        Column (modifier = Modifier.background(color = cardColor)) {

                            Text(
                                text = msg.message ?: "",
                                modifier = Modifier
                                    .background(cardColor)
                                    .padding(all = 7.dp),
                                color = cardTextColor,
                                style = MaterialTheme.typography.body2
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = formatDate(msg.timestamp!!),
                                modifier = Modifier.background(cardColor).padding(start = 7.dp, end = 20.dp, bottom = 2.dp),
                                color = cardTextColor,
                                style = TextStyle(
                                    textAlign = TextAlign.Right,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }




                }
            }
        }
    }

    fun uploadMessage(text: String) {
        dbWriter.createChatMessage(text, Date(), dataHandler.user)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Preview(showBackground = true)
    @Composable
    fun GreetingBoxPreview() {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        fun randomString() = List(10) { charPool.random() }.joinToString("")
        val msgArr = arrayOf(
            ChatMessage(randomString(), "Hello there", Date(), null),
            ChatMessage(randomString(), "Jo was geht", Date(), null),
            ChatMessage(randomString(), "Jo was geht", Date(), null),
            ChatMessage(randomString(), "Jo was geht", Date(), null),
            ChatMessage(randomString(), "Jo was geht", Date(), null),
            ChatMessage(randomString(), "Jo was geht", Date(), null),
            ChatMessage(randomString(), "Jo was geht", Date(), null),
            ChatMessage(randomString(), "Jo was geht", Date(), null),
            ChatMessage(randomString(), "Jo was geht", Date(), null),
            ChatMessage(randomString(), "Jo was geht", Date(), null),
            ChatMessage(randomString(), "Hehehehehehdddasdasd", Date(), null),
            ChatMessage(randomString(), "Hehehehehehdddasdasd", Date(), null),
            ChatMessage(randomString(), "Hehehehehehdddasdasd", Date(), null),
            ChatMessage(randomString(), "Hehehehehehdddasdasd", Date(), null),
            ChatMessage(randomString(), "Hehehehehehdddasdasd", Date(), null),
            ChatMessage(randomString(), "Hehehehehehdddasdasd", Date(), null),
            ChatMessage(randomString(), "Hehehehehehdddasdasd", Date(), null),
            ChatMessage(randomString(), "Hehehehehehdddasdasd", Date(), null),
            ChatMessage(randomString(), "Hehehehehehdddasdasd", Date(), null),
        )

        dataHandler.addChatMessage(*msgArr)

        ChatBox()
    }
}