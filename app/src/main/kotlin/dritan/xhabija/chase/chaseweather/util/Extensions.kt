package dritan.xhabija.chase.chaseweather.util

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Extension function that enables performing a `launch {..}` in a safe manner where exceptions
 * inside the coroutine don't crash the app. The returned [LaunchException] enables listening for
 * an exception via ` .onException {..}` after the launch block.
 *
 * ex:
 * launchSafely {
 *      // do work
 *      // exception is thrown and stored in a [LaunchException]
 * }.onException { exception-> // thrown exception is caught here
 *      // safe launch
 * }
 */
fun CoroutineScope.launchSafely(
    work: suspend (() -> Unit)
): LaunchException {
    val launchException = LaunchException(this)

    launch(
        CoroutineExceptionHandler { _, exception ->
            println("@@@@@@@@EXCEPTION running coroutine::: $exception")
            launch {
                launchException.setException(exception)
            }
        }
    ) {
        work.invoke()
    }
    return launchException
}

/**
 * Object containing the exception that's thrown during a `launch{..}` task.
 * This object is returned when calling [launchSafely] so we can be notified if an exception is thrown in the coroutine task.
 */
class LaunchException(private val coroutineScope: CoroutineScope) {
    private var throwable: Throwable? = null
    private var callback: (suspend (Throwable) -> Unit)? = null

    fun setException(throwable: Throwable): LaunchException {
        this.throwable = throwable
        notifyException()
        return this
    }

    fun onException(callback: suspend ((Throwable) -> Unit)): LaunchException {
        this.callback = callback
        return this
    }

    private fun notifyException() {
        throwable?.let { exception ->
            callback?.run {
                coroutineScope.launch {
                    invoke(exception)
                    end()
                }
            }
        }
    }

    fun end() {
        throwable = null
        callback = null
    }
}
