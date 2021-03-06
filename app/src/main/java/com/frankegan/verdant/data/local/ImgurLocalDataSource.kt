package com.frankegan.verdant.data.local

import android.support.annotation.VisibleForTesting
import com.frankegan.verdant.data.*
import com.frankegan.verdant.utils.AppExecutors
import kotlinx.coroutines.experimental.withContext

class ImgurLocalDataSource private constructor(
        val appExecutors: AppExecutors = AppExecutors(),
        val database: VerdantDatabase = VerdantDatabase.getInstance()
) : ImgurDataSource {
    override suspend fun getImage(id: String): Result<ImgurImage> =
            withContext(appExecutors.ioContext) {
                val response = database.imageDao().getImage(id)
                if (response != null) {
                    Result.Success(response)
                } else {
                    Result.Error(LocalDataNotFoundException())
                }
            }

    override suspend fun getImages(subreddit: String, page: Int): Result<List<ImgurImage>> =
            withContext(appExecutors.ioContext) {
                val response = database.imageDao().getAll()
                if (response.isNotEmpty()) {
                    Result.Success(response)
                } else {
                    Result.Error(LocalDataNotFoundException())
                }
            }

    override suspend fun favoriteImage(image: ImgurImage): Result<String> =
            withContext(appExecutors.ioContext) {
                database.imageDao().updateFavorited(image.id, image.favorite)
                if (image.favorite) {
                    Result.Success("favorite")
                } else {
                    Result.Success("unfavorite")
                }
            }

    override suspend fun getUsername(): Result<String> = withContext(appExecutors.ioContext) {
        Result.Success(database.userDao().getUsername())
    }

    override suspend fun saveUser(user: ImgurUser) = withContext(appExecutors.ioContext) {
        database.userDao().insertAll(user)
    }

    override suspend fun refreshImages() {
        // Not required because the {@link ImgurRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    override suspend fun deleteImages() = withContext(appExecutors.ioContext) {
        database.imageDao().deleteImages()
    }

    override suspend fun saveImages(images: List<ImgurImage>) = withContext(appExecutors.ioContext) {
        database.imageDao().insertAll(images = *images.toTypedArray())
    }

    companion object {
        private var INSTANCE: ImgurLocalDataSource? = null

        @JvmStatic
        fun getInstance(): ImgurLocalDataSource {
            if (INSTANCE == null) {
                synchronized(ImgurLocalDataSource::javaClass) {
                    INSTANCE = ImgurLocalDataSource()
                }
            }
            return INSTANCE!!
        }

        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }
}