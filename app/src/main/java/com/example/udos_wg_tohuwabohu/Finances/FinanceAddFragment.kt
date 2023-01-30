package com.example.udos_wg_tohuwabohu.Finances

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.example.udos_wg_tohuwabohu.R
import com.example.udos_wg_tohuwabohu.databinding.FragmentFinanceAddBinding

class FinanceAddFragment : Fragment() {
    private lateinit var _binding: FragmentFinanceAddBinding
    private lateinit var composeView: ComposeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanceAddBinding.inflate(inflater,container,false)
        val view = _binding.root





        composeView = view.findViewById(R.id.compose_view)
        composeView.setContent {
            // COMPOSE
        }
        return view
    }
}