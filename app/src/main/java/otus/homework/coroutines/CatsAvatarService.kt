package otus.homework.coroutines

import retrofit2.Response
import retrofit2.http.GET

interface CatsAvatarService {

    @GET("meow")
    suspend fun getCatImage(): PictureLink

    @GET("meow")
    suspend fun getCatImageResult(): Response<PictureLink>
}