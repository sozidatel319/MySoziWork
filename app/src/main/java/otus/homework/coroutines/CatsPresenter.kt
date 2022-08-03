package otus.homework.coroutines

import kotlinx.coroutines.*
import java.net.SocketTimeoutException
import kotlin.Exception


class CatsPresenter(
    private val catsService: CatsService,
    private val avatarService: CatsAvatarService,
    private val errorReceived: ErrorReceived
) {

    private var _catsView: ICatsView? = null

    private val job = Job()

    private val presenterScope = CoroutineScope(
        job + Dispatchers.Main + CoroutineName("CatsCoroutine")
    )

    fun onInitComplete() {


        presenterScope.launch {


            try {
                val avatarResult = handleApi {
                    avatarService.getCatImageResult()
                }

                val pic: String? = when (avatarResult) {
                    is ApiSuccess -> avatarResult.data.file
                    is ApiError -> null
                    is ApiException -> null
                }

                val factResult = handleApi {
                    catsService.getCatFactResult()
                }

                val fact: String? = when (factResult) {
                    is ApiSuccess -> factResult.data.text
                    is ApiError -> null
                    is ApiException -> null
                }
                _catsView?.populate(fact, pic)

            } catch (e: Exception) {
                when (e) {
                    is SocketTimeoutException -> {
                        errorReceived.error("Не удалось получить ответ от сервера")
                    }
                    else -> {
                        CrashMonitor.trackWarning(e)
                        e.localizedMessage?.let { errorReceived.error(it) }
                        e.printStackTrace()
                        if (e is CancellationException) {
                            throw e
                        }
                    }
                }
            }
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        job.cancel()
        _catsView = null
    }
}