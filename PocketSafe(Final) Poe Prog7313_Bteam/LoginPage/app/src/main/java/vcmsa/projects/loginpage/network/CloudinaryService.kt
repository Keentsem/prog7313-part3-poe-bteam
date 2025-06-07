package vcmsa.projects.loginpage.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface CloudinaryService {
    @Multipart
    @POST("v1_1/dn3fsiep0/image/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: String = "PocketSafe B Team"
    ): Response<CloudinaryResponse>
}
