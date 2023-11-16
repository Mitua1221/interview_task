package com.arjental.arch.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

fun <T> produceSharedFlow(
    replay: Int = 1,
    extraBufferCapacity: Int = 0,
    bufferOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST
) =
    MutableSharedFlow<T>(replay = replay, extraBufferCapacity = extraBufferCapacity, onBufferOverflow = bufferOverflow)

fun <T> produceChannel(
    capacity: Int = 1,
    bufferOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST,
    onUndeliveredElement: ((T) -> Unit)? = null
) =
    Channel<T>(capacity = capacity, onBufferOverflow = bufferOverflow, onUndeliveredElement = onUndeliveredElement)

open class BaseViewModel<State, Effect>(
    initialState: State,
) : ViewModel() {

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State>
        get() = _state

    private val _effect = produceChannel<Effect>(capacity = 64)
    val effect = _effect.receiveAsFlow()

    protected fun postState(state: State) {
        _state.value = state
    }

    protected fun postEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    protected fun modifyState(mutator: (prevState: State) -> State): State {
        return _state.updateAndGet { prevState ->
            mutator(prevState)
        }
    }

    protected fun launchAll(
        context: CoroutineContext = Dispatchers.Main,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        loadingHandle: (Boolean) -> Unit = { handleLoading(it) },
        errorHandle: (Throwable) -> Unit = { handleError(it) },
        lambda: suspend CoroutineScope.() -> Unit
    ): Job {
        return viewModelScope.launch(
            context = context,
            start = start,
        ) {
            try {
                // Wrapping with coroutineScope to avoid uncaught exceptions from nested coroutines.
                coroutineScope {
                    loadingHandle(true)
                    lambda(this)
                }
            } catch (t: Throwable) {
                errorHandle(t)
            } finally {
                loadingHandle(false)
            }
        }
    }

    protected fun handleLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    open fun handleError(t: Throwable) {

    }

}
