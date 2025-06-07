package vcmsa.projects.loginpage.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: CloudinaryService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/") // base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudinaryService::class.java)
    }
}
