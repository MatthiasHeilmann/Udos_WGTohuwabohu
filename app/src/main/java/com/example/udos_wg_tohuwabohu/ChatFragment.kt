package com.example.udos_wg_tohuwabohu

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColor
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.dataclasses.ChatMessage
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import java.util.*
import kotlin.collections.ArrayList

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_CHAT_LIST = "chatList"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {

    private val dataHandler = DataHandler.getInstance()
    private var chatList: Array<ChatMessage>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        var messages by remember { mutableStateOf(dataHandler.getChat()) }
        println("Updated Messages:")
        messages.forEach { msg -> println(msg.message) }


        Column(
            modifier = Modifier
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(
                    enabled = true,
                    state = ScrollState(0),
                    reverseScrolling = true
                )
                .weight(1f, false)
            ) {
                messages.forEach { msg ->
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
        var text by remember { mutableStateOf(TextFieldValue("")) }

        var backgroundColor: Color = com.google.android.material.R.attr.colorOnSecondary.toColor();

        Box(){
            TextField(
                value = text,
                onValueChange = {
                    text = it
                },
                label = { Text(text = "Message") },
                placeholder = { Text(text = "Type a message") },
                modifier = Modifier.background(ComposeColor(backgroundColor.toArgb()))
            )

            Button(
                modifier = Modifier.align(CenterEnd),
                onClick = {
                    Toast.makeText(
                        activity,
                        text.text,
                        Toast.LENGTH_SHORT
                    ).show()
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
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
        }
    }

    @Composable
    fun MessageCard(msg: ChatMessage) {
        Row(
            modifier = Modifier.padding(all = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colors.secondary, CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                Text(
                    text = dataHandler.getRoommate(msg.user.toString())?.username ?: "unknown",
                    color = MaterialTheme.colors.secondaryVariant,
                    style = MaterialTheme.typography.subtitle2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(shape = MaterialTheme.shapes.medium, elevation = 5.dp) {
                    Text(
                        text = msg.message ?: "",
                        modifier = Modifier.padding(all = 4.dp),
                        style = MaterialTheme.typography.body2
                    )
                }

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = formatDate(msg.timestamp!!),
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        textAlign = TextAlign.Right,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Preview(showBackground = true)
    @Composable
    fun GreetingBoxPreview() {
        val msgArr = arrayOf(
            ChatMessage("", "Hello there", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Jo was geht", Date(), null),
            ChatMessage("", "Hello there", Date(), null),
            ChatMessage("", "Hello there", Date(), null),
            ChatMessage("", "Hello there", Date(), null),
            ChatMessage("", "Hello there", Date(), null),
            ChatMessage("", "Hello there", Date(), null),
            ChatMessage("", "Hello there", Date(), null),
            ChatMessage("", "Hello there", Date(), null),
            ChatMessage("", "Hello there", Date(), null),
            ChatMessage("", "Hello there", Date(), null),
            ChatMessage("", "Hello there", Date(), null),
            ChatMessage("", "Hello there", Date(), null)
        )

        dataHandler.addChatMessage(*msgArr)

        ChatBox()
    }
}