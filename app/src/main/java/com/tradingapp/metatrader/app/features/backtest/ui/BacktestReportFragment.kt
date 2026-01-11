package com.tradingapp.metatrader.app.features.backtest.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.tradingapp.metatrader.app.databinding.FragmentBacktestReportBinding
import com.tradingapp.metatrader.app.features.backtest.BacktestViewModel
import com.tradingapp.metatrader.app.features.backtest.export.html.BacktestHtmlReportBuilder
import kotlinx.coroutines.flow.collectLatest

class BacktestReportFragment : Fragment() {

    private var _binding: FragmentBacktestReportBinding? = null
    private val binding get() = _binding!!

    private val vm: BacktestViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBacktestReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.reportWebView.settings.javaScriptEnabled = false
        binding.reportWebView.settings.domStorageEnabled = false
        binding.reportWebView.webChromeClient = WebChromeClient()
        binding.reportWebView.webViewClient = WebViewClient()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { render() }
        }
    }

    private fun render() {
        val res = vm.state.value.result ?: run {
            binding.reportWebView.loadDataWithBaseURL(
                null,
                "<html><body style='background:#0b1220;color:#d1d4dc;font-family:Arial;padding:18px;'>No report yet.</body></html>",
                "text/html",
                "utf-8",
                null
            )
            return
        }

        val html = BacktestHtmlReportBuilder.build(res)
        binding.reportWebView.loadDataWithBaseURL(
            null,
            html,
            "text/html",
            "utf-8",
            null
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
