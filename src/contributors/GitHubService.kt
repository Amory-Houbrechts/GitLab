package contributors

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.Base64

interface GitHubService {
    @GET("projects/622148/members/all/")
    fun getOrgReposCall(): Call<List<Repo>>

    @GET("projects/622148/members/all/")
    fun getRepoContributorsCall(): Call<List<User>>
}

@Serializable
data class Repo(
    val id: Long,
    val name: String
)

@Serializable
data class User(
    val access_level: Int,
    val avatar_url: String,
    val created_at: String,
    val id: Int,
    val membership_state: String,
    val name: String,
    val state: String,
    val username: String,
    val web_url: String
)

@Serializable
data class RequestData(
    val username: String,
    val password: String,
    val org: String
)

@OptIn(ExperimentalSerializationApi::class)
fun createGitHubService(password: String): GitHubService {
    val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Authorization", "Bearer $password")
            val request = builder.build()
            chain.proceed(request)
        }
        .build()

    val contentType = "application/json".toMediaType()
    val retrofit = Retrofit.Builder()
        .baseUrl("https://gitlab.com/api/v4/")
        .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(httpClient)
        .build()
    return retrofit.create(GitHubService::class.java)
}
