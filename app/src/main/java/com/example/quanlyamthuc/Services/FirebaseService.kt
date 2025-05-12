package com.example.quanlyamthuc.services

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.MultipartBody

object FirebaseService {
    private const val CLOUD_NAME = "de2jmalxk" // Thay bằng cloud name của bạn
    private const val UPLOAD_PRESET = "myduyen" // Thay bằng upload preset của bạn

    fun uploadImage(context: Context, uri: Uri, callback: (String?) -> Unit) {
        val file = getFileFromUri(context, uri) ?: run {
            callback(null)
            return
        }

        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Cloudinary", "Upload failed", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val json = response.body?.string()
                    val jsonObject = JSONObject(json)
                    val secureUrl = jsonObject.getString("secure_url")
                    callback(secureUrl)
                } catch (e: Exception) {
                    Log.e("Cloudinary", "Error parsing response", e)
                    callback(null)
                }
            }
        })
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File.createTempFile("upload", ".jpg", context.cacheDir)
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            file
        } catch (e: Exception) {
            Log.e("Cloudinary", "Error creating file", e)
            null
        }
    }
}

//object FirebaseService {
//
//    // Lấy file thực từ URI
//    private fun getFileFromUri(context: Context, uri: Uri): File? {
//        return try {
//            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
//            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
//            tempFile.outputStream().use { fileOut ->
//                inputStream.copyTo(fileOut)
//            }
//            tempFile
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    // Hàm tải ảnh lên Cloudinary
//    fun uploadToCloudinary(context: Context, uri: Uri, callback: (String?) -> Unit) {
//        val file = getFileFromUri(context, uri)
//        if (file == null) {
//            callback(null)
//            return
//        }
//
//        // Tạo request để upload
//        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
//        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
//
//        // Dùng upload preset "myduyen"
//        val uploadPreset = "myduyen".toRequestBody("text/plain".toMediaTypeOrNull())
//
//        // Khởi tạo Retrofit và gọi API Cloudinary
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://api.cloudinary.com/v1_1/de2jmalxk/") // Cloud name của bạn
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        val service = retrofit.create(CloudinaryService::class.java)
//
//        // Thực hiện request upload ảnh
//        service.uploadImage(body, uploadPreset).enqueue(object : Callback<CloudinaryResponse> {
//            override fun onResponse(call: Call<CloudinaryResponse>, response: Response<CloudinaryResponse>) {
//                if (response.isSuccessful) {
//                    callback(response.body()?.secure_url) // Trả về URL ảnh sau khi upload thành công
//                } else {
//                    callback(null) // Nếu có lỗi xảy ra
//                }
//            }
//
//            override fun onFailure(call: Call<CloudinaryResponse>, t: Throwable) {
//                callback(null) // Nếu có lỗi kết nối
//            }
//        })
//    }
//
//}