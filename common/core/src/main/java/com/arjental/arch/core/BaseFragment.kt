package com.arjental.arch.core

import android.databinding.tool.writer.ViewBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.databinding.tool.writer.ViewBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.guavapay.common.manager.NotificationClickManager
import com.guavapay.common.util.applyAdjustResize
import com.guavapay.common.util.setOnApplyWindowInsetsListenerCompat
import com.guavapay.core.R
import com.guavapay.core.extension.DeeplinkNavigationTypes
import com.guavapay.core.tools.NavigationCommand
import com.guavapay.core.tools.deeplink.DeepLinkNavigationManager
import com.guavapay.core.tools.deeplink.DeeplinkNavigation
import com.guavapay.core.tools.deeplink.DeeplinkNavigationHandlers
import com.guavapay.domain.entity.analytics.product.payment.request.AcceptRequestFriendEvent
import com.guavapay.ui_toolkit.toolbar.GuavaToolbar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import com.guavapay.core.base.letBinding as letBindingExt
import com.guavapay.core.base.withBinding as withBindingExt

abstract class BaseFragment<State, Effect, vm : BaseViewModel<State, Effect>> :
    Fragment() {

    private var collectJob: Job? = null

    @Suppress("SpellCheckingInspection")
    val viewmodel: ViewModel by lazy {
        val owner = requireActivity()
        val defaultViewModelProviderFactory = screen.getViewModelProviderFactory()
            ?: (owner as HasDefaultViewModelProviderFactory)
                .defaultViewModelProviderFactory
        val wrappedViewModelProviderFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras,
            ): T {
                val viewModel = defaultViewModelProviderFactory.create(modelClass, extras) as ViewModel
                return viewModel as T
            }
        }
        ViewModelProvider(
            owner = owner,
            factory = wrappedViewModelProviderFactory
        )[screen.getViewModelClass()]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectJob?.cancel()
        collectJob = this.lifecycleScope.launch {
            launch {
                vm.state.collectLatest { state -> screen.observeState(state) }
            }
            launch {
                viewmodel.effect.collect { effect -> screen.observeEffect(effect) }
            }
        }
    }


}
