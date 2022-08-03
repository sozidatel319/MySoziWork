package otus.homework.coroutines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.net.SocketTimeoutException

class CatsViewModel(
    private val catsService: CatsService,
    private val avatarService: CatsAvatarService
) : ViewModel() {

    private val mutablePictureLiveData = MutableLiveData<ApiResult<PictureLink>>()
    private val mutableFactLiveData = MutableLiveData<ApiResult<Fact>>()
    private val mutableFinalLiveData = MutableLiveData<ApiResult<Pair<String?, String?>>>()
    val finalDataCats: LiveData<ApiResult<Pair<String?, String?>>> get() = mutableFinalLiveData
    private var scope: Job? = null

    init {
        scope = viewModelScope.launch {
            lateinit var factResult: Deferred<ApiResult<Fact>>
            lateinit var avatarResult: Deferred<ApiResult<PictureLink>>

            viewModelScope.apply {
                try {
                    factResult = async {
                        handleApi {
                            catsService.getCatFactResult()
                        }
                    }

                    avatarResult = async {
                        handleApi {
                            avatarService.getCatImageResult()
                        }
                    }

                } catch (exception: Exception) {
                    if (exception is SocketTimeoutException) {
                        mutableFinalLiveData.value =
                            ApiError(0, "Не удалось получить ответ от сервера")
                    } else {
                        CrashMonitor.trackWarning(exception)
                        mutableFinalLiveData.value = ApiException(exception)
                        exception.printStackTrace()
                    }
                }
            }
            mutableFinalLiveData.value = ApiSuccess(
                Pair(
                    (factResult.await() as ApiSuccess).data.text,
                    (avatarResult.await() as ApiSuccess).data.file
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope?.cancel()
    }
}