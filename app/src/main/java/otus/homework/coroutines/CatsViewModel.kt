package otus.homework.coroutines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.net.SocketTimeoutException

class CatsViewModel(
    private val catsService: CatsService,
    private val avatarService: CatsAvatarService,
    private val errorReceived: ErrorReceived,
    private val view: ICatsView
) : ViewModel() {

    private val mutablePictureLiveData = MutableLiveData<ApiResult<PictureLink>>()
    private val mutableFactLiveData = MutableLiveData<ApiResult<Fact>>()
    val catsData: LiveData<ApiResult<PictureLink>> get() = mutablePictureLiveData
    val factData: LiveData<ApiResult<Fact>> get() = mutableFactLiveData

    init {
        attachView()
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            when (throwable) {
                is SocketTimeoutException -> {
                    errorReceived.error("Не удалось получить ответ от сервера")
                }
                else -> {
                    CrashMonitor.trackWarning(throwable)
                    throwable.localizedMessage?.let { errorReceived.error(it) }
                    throwable.printStackTrace()
                }
            }
        }) {
            onInitComplete()
        }
    }

    private var _catsView: ICatsView? = null

    private val job = Job()

    private suspend fun onInitComplete() {

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
        mutablePictureLiveData.value = avatarResult
        mutableFactLiveData.value = factResult

        _catsView?.populate(fact, pic)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
        _catsView = null
    }

    private fun attachView() {
        _catsView = view
    }
}