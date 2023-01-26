package com.example.udos_wg_tohuwabohu

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.dataclasses.ChatMessage
import com.example.udos_wg_tohuwabohu.dataclasses.DBWriter
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_CHAT_LIST = "chatList"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param chatMessages list of all chat messages that shall be shown
         * @return A new instance of fragment ChatFragment.
         */
        @JvmStatic
        fun newInstance(): Fragment {
            return ChatFragment()
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

        println("Got bound Messages")
        println(dataHandler.chat.joinToString { it.message + ", " + it.timestamp?.toString() + ", " + it.user.toString() + "\n\t" })

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
            ) {
                dataHandler.chat.forEach { msg ->
                    MessageCard(msg)
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

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MessageInput() {
        // TODO get input event and upload to database
        var textFieldValue by remember { mutableStateOf("") }
        var test by remember { mutableStateOf(ArrayList<String>()) }

        Box {
            TextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                },
                label = { Text(text = "Message") },
                placeholder = { Text(text = "Type a message") },
                modifier = Modifier.background(UdoWhite),
                textStyle = TextStyle(color = UdoDarkGray)
            )

            Button(
                modifier = Modifier.align(CenterEnd),
                colors = UdoChatButtonTheme(),
                onClick = {
                    if (textFieldValue.trim() != "") {
                        uploadMessage(textFieldValue)
                    } else {
                        Toast.makeText(
                            activity,
                            "Pls insert a valid Message",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    textFieldValue = ""
                },
                // Uses ButtonDefaults.ContentPadding by default
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 12.dp,
                    end = 20.dp,
                    bottom = 12.dp
                )
            ) {
                // Inner content including an icon and a text label
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Send Message",
                    modifier = Modifier.size(ButtonDefaults.IconSize.times(1.5f))
                )
            }
        }
    }

    @Composable
    fun MessageCard(msg: ChatMessage) {
        // TODO correct Theme and Get colors for own Messages
        val thisUser = (msg.user?.id == dataHandler.user?.docID) || false;
        //if(thisUser) ""
        //                        else
        val username = dataHandler.getRoommate(msg.user?.id)?.username ?: "unknown"
        val alignment = if (thisUser) Alignment.CenterEnd else Alignment.CenterStart
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(alignment)
                    .padding(all = 8.dp)
            ) {
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    Text(
                        text = username,
                        color = UdoOrange,
                        modifier= Modifier.align(Alignment.Start),
                        style = MaterialTheme.typography.subtitle2,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        elevation = 5.dp
                    ) {
                        Text(
                            text = msg.message ?: "",
                            modifier = Modifier.background(UdoLightBlue).padding(all = 4.dp),
                            color = UdoWhite,
                            style = MaterialTheme.typography.body2
                        )
                    }

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = formatDate(msg.timestamp!!),
                        modifier = Modifier.fillMaxWidth(),
                        color = UdoGray,
                        style = TextStyle(
                            textAlign = TextAlign.Right,
                            fontSize = 12.sp
                        )
                    )
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