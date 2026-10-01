package com.learnquest.mp.data.repository

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** A study file published in the repository's PKGS directory. */
data class RemotePackageFile(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long
)

/**
 * Reads the live PKGS directory from GitHub and downloads its files for offline use.
 *
 * Files are kept in the app's Downloads directory so the app can read them without
 * storage permissions. The Downloads screen also exposes this directory to the user.
 */
class PkgsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val appDownloadsDirectory =
        appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(appContext.filesDir, "downloads")

    suspend fun listPackageFiles(): Result<List<RemotePackageFile>> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = openConnection(PKGS_API_URL)
            try {
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    error("GitHub returned HTTP $responseCode")
                }

                val entries = JSONArray(connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() })
                buildList {
                    for (index in 0 until entries.length()) {
                        val entry = entries.getJSONObject(index)
                        val name = entry.optString("name")
                        val downloadUrl = entry.optString("download_url")
                        val type = entry.optString("type")
                        if (type == "file" && isStudyFile(name) && downloadUrl.isNotBlank()) {
                            add(
                                RemotePackageFile(
                                    name = name,
                                    downloadUrl = downloadUrl,
                                    sizeBytes = entry.optLong("size", 0L)
                                )
                            )
                        }
                    }
                }.sortedBy { it.name.lowercase() }
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun downloadPackage(packageFile: RemotePackageFile): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            appDownloadsDirectory.mkdirs()
            val target = File(appDownloadsDirectory, safeFileName(packageFile.name))
            val temporary = File(appDownloadsDirectory, ".${target.name}.part")
            val connection = openConnection(packageFile.downloadUrl)
            try {
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    error("Download failed with HTTP $responseCode")
                }
                connection.inputStream.use { input ->
                    temporary.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } finally {
                connection.disconnect()
            }
            if (!temporary.renameTo(target)) {
                temporary.copyTo(target, overwrite = true)
                temporary.delete()
            }
            target
        }
    }

    private fun openConnection(url: String): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 20_000
            instanceFollowRedirects = true
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json, text/plain, */*")
            setRequestProperty("User-Agent", "LearnQuest-MP-Android")
        }
    }

    private fun isStudyFile(name: String): Boolean {
        val lowerName = name.lowercase()
        return lowerName.endsWith(".md") || lowerName.endsWith(".txt")
    }

    private fun safeFileName(name: String): String =
        name.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"), "_")

    private companion object {
        const val PKGS_API_URL =
            "https://api.github.com/repos/manasvasaxena/STRAIGHT-B-mponline-hackathon/contents/PKGS?ref=main"
    }
}
