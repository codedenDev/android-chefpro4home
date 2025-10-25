package com.chefpro4home.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloudflare Image Loader for Android
 * 
 * This class handles loading images from Cloudflare R2 storage with caching,
 * providing better performance and reliability than WordPress images.
 */
@Singleton
class CloudflareImageLoader @Inject constructor(
    private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    
    companion object {
        private const val CLOUDFLARE_IMAGE_BASE_URL = "https://recipes-api.recipedos.workers.dev/api/images"
        private const val CACHE_SIZE = 50 // Maximum number of images in memory cache
        private const val DISK_CACHE_SIZE = 100 * 1024 * 1024 // 100MB disk cache
    }
    
    // Memory cache for images
    private val memoryCache = LruCache<String, Bitmap>(CACHE_SIZE)
    
    // Disk cache directory
    private val diskCacheDir = context.cacheDir.resolve("cloudflare_images")
    
    init {
        // Create disk cache directory if it doesn't exist
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs()
        }
    }
    
    /**
     * Load image from Cloudflare R2 with caching
     */
    suspend fun loadImage(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Convert WordPress URL to Cloudflare R2 URL if needed
            val cloudflareUrl = convertToCloudflareUrl(imageUrl)
            
            // Check memory cache first
            memoryCache.get(cloudflareUrl)?.let { cachedBitmap ->
                println("📱 Image loaded from memory cache: $cloudflareUrl")
                return@withContext cachedBitmap
            }
            
            // Check disk cache
            val cachedFile = getCachedFile(cloudflareUrl)
            if (cachedFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(cachedFile.absolutePath)
                if (bitmap != null) {
                    memoryCache.put(cloudflareUrl, bitmap)
                    println("💾 Image loaded from disk cache: $cloudflareUrl")
                    return@withContext bitmap
                }
            }
            
            // Load from Cloudflare R2
            loadImageFromCloudflare(cloudflareUrl)
            
        } catch (e: Exception) {
            println("❌ Error loading image: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Convert WordPress URL to Cloudflare R2 URL
     */
    private fun convertToCloudflareUrl(originalUrl: String): String {
        // If it's already a Cloudflare URL, return as-is
        if (originalUrl.contains("recipes-api.recipedos.workers.dev")) {
            return originalUrl
        }
        
        // If it's a WordPress URL, convert to Cloudflare R2
        if (originalUrl.contains("recipedos.com/wp-content/uploads/")) {
            val filename = originalUrl.substringAfterLast("/")
            return "$CLOUDFLARE_IMAGE_BASE_URL/$filename"
        }
        
        // If it's a relative path or just filename, assume it's already migrated
        if (!originalUrl.contains("http")) {
            return "$CLOUDFLARE_IMAGE_BASE_URL/$originalUrl"
        }
        
        // For any other URL, try to extract filename and use Cloudflare
        val filename = originalUrl.substringAfterLast("/")
        return "$CLOUDFLARE_IMAGE_BASE_URL/$filename"
    }
    
    /**
     * Load image from Cloudflare R2
     */
    private suspend fun loadImageFromCloudflare(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            println("🌐 Loading image from Cloudflare R2: $url")
            
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "image/*")
                .addHeader("User-Agent", "ChefPro4Home-Android/1.0")
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val inputStream: InputStream? = response.body?.byteStream()
                if (inputStream != null) {
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    
                    if (bitmap != null) {
                        // Cache the image
                        memoryCache.put(url, bitmap)
                        saveToDiskCache(url, bitmap)
                        println("✅ Image loaded from Cloudflare R2: $url")
                        return@withContext bitmap
                    }
                }
            } else {
                println("❌ Failed to load image from Cloudflare R2: ${response.code} - ${response.message}")
            }
            
        } catch (e: IOException) {
            println("❌ Network error loading image from Cloudflare R2: ${e.message}")
        } catch (e: Exception) {
            println("❌ Error loading image from Cloudflare R2: ${e.message}")
        }
        
        null
    }
    
    /**
     * Save image to disk cache
     */
    private fun saveToDiskCache(url: String, bitmap: Bitmap) {
        try {
            val cachedFile = getCachedFile(url)
            cachedFile.outputStream().use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            println("💾 Image saved to disk cache: $url")
        } catch (e: Exception) {
            println("❌ Error saving image to disk cache: ${e.message}")
        }
    }
    
    /**
     * Get cached file path
     */
    private fun getCachedFile(url: String): java.io.File {
        val filename = url.hashCode().toString()
        return java.io.File(diskCacheDir, filename)
    }
    
    /**
     * Clear memory cache
     */
    fun clearMemoryCache() {
        memoryCache.evictAll()
        println("🧹 Memory cache cleared")
    }
    
    /**
     * Clear disk cache
     */
    fun clearDiskCache() {
        try {
            diskCacheDir.listFiles()?.forEach { file ->
                file.delete()
            }
            println("🧹 Disk cache cleared")
        } catch (e: Exception) {
            println("❌ Error clearing disk cache: ${e.message}")
        }
    }
    
    /**
     * Clear all caches
     */
    fun clearAllCaches() {
        clearMemoryCache()
        clearDiskCache()
        println("🧹 All caches cleared")
    }
    
    /**
     * Get cache size info
     */
    fun getCacheInfo(): CacheInfo {
        val memorySize = memoryCache.size()
        val diskSize = diskCacheDir.listFiles()?.sumOf { it.length() } ?: 0L
        
        return CacheInfo(
            memoryCacheSize = memorySize,
            diskCacheSize = diskSize,
            memoryCacheMaxSize = CACHE_SIZE,
            diskCacheMaxSize = DISK_CACHE_SIZE.toLong()
        )
    }
    
    data class CacheInfo(
        val memoryCacheSize: Int,
        val diskCacheSize: Long,
        val memoryCacheMaxSize: Int,
        val diskCacheMaxSize: Long
    )
}
