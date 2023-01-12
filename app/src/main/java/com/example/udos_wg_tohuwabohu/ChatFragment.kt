package com.example.udos_wg_tohuwabohu

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.dataclasses.ChatMessage
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_CHAT_LIST = "chatList"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private val dataHandler = DataHandler.getInstance()
    private var chatList: Array<ChatMessage>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // TODO Somehow get an array from the arguments
            chatList = it.getSerializable(ARG_CHAT_LIST) as Array<ChatMessage>?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return ComposeView(requireContext()).apply {
            setContent {
                Text(text = "Hello world.")
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
        fun newInstance(vararg chatMessages: ChatMessage) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CHAT_LIST, chatMessages)
                }
            }
    }

    @Composable
    fun chatFragment(messages: Array<ChatMessage>){
        messages.forEach { msg ->
            MessageCard(msg)
        }
    }

    @Composable
    fun MessageCard(msg: ChatMessage) {
        Row(modifier = Modifier.padding(all = 8.dp)) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colors.secondary, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = dataHandler.getRoommate(msg.user)?.username?: "",
                    color = MaterialTheme.colors.secondaryVariant,
                    style = MaterialTheme.typography.subtitle2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(shape = MaterialTheme.shapes.medium, elevation = 1.dp) {
                    Text(
                        text = msg.message?: "",
                        modifier = Modifier.padding(all = 4.dp),
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }

    @Composable
    fun GreetingBox(msg: ChatMessage){
        Box(modifier = Modifier.wrapContentSize()){
            Column(modifier = Modifier.wrapContentSize()){

                Text(
                    text = dataHandler.getRoommate(msg.user)?.username?: "",
                    color = MaterialTheme.colors.secondaryVariant,
                    style = MaterialTheme.typography.subtitle2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(elevation = 10.dp) {
                    Text(
                        text = msg.message?: "",
                        modifier = Modifier.padding(all = 4.dp),
                        style = MaterialTheme.typography.body2
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Box(){
                    Text(
                        text= msg.timestamp.toString(),
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                    Text(
                        "Leo",
                        modifier = Modifier
                            .padding(end = 150.dp)
                    )
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingBoxPreview() {

    }

}