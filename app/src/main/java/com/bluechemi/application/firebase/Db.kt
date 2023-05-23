package com.bluechemi.application.firebase

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import android.widget.Toast
import com.bluechemi.application.AppSettings.Settings
import com.bluechemi.application.R
import com.bluechemi.application.utils.drawableToBitmap
import com.bluechemi.application.utils.makeStoragePath
import com.bluechemi.application.viewnotification.NotificationItem
import com.google.android.gms.tasks.Task
import com.google.common.collect.Queues
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.installations.Utils
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.lang.Integer.min
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object Db {

    val TAG = "firebaseDb"

    val pictureCache = mutableListOf<pictureCache>()

    fun QueryDeviceSettings(context: Context, querySnapshot: QuerySnapshot): HashMap<Any, HashMap<String, Any>>{

        var diaryHashMap:HashMap<Any, HashMap<String, Any>> = hashMapOf()

        querySnapshot.documents.forEach {
            val brightness = it.data?.getOrDefault(context.getString(R.string.db_brightness), 128) ?: 128
            val sensitivity = it.data?.getOrDefault(context.getString(R.string.db_sensitivity), 128) ?: 128
            val name = it.data?.getOrDefault(context.getString(R.string.db_username), "BlueChemi") ?: "BlueChemi"
            val uuid = it.id

            diaryHashMap.put(
                uuid,
                hashMapOf(
                    context.getString(R.string.db_username) to name,
                    context.getString(R.string.db_brightness) to brightness,
                    context.getString(R.string.db_sensitivity) to sensitivity
                )
            )
        }

        return diaryHashMap
    }
    fun ParseDeviceSettingsCache(context: Context, path: String): Task<QuerySnapshot> {
        return Firebase.firestore.collection(context.getString(R.string.db_root))
            .document(path)
            .collection(context.getString(R.string.db_doc_setting))
            .get(Source.CACHE)
    }
    fun UpdateDeviceSettings(context: Context, document: String, deviceId: String, settingValue: HashMap<String, Any>): Task<Void> {
        return Firebase.firestore.collection(context.getString(R.string.db_root))
            .document(document)
            .collection(context.getString(R.string.db_doc_setting))
            .document(deviceId)
            .set(settingValue)
    }

    fun QueryDiaries(context: Context, quarySnapShot: QuerySnapshot): ArrayList<NotificationItem>{

        var list = mutableListOf<NotificationItem>()

        quarySnapShot.documents.forEach {
            val date = it.data?.get(context.getString(R.string.db_key_diary_date))?: "unknown date"
            val time = it.data?.get(context.getString(R.string.db_key_diary_time))?: "unknown time"
            val id = it.data?.get(context.getString(R.string.db_key_diary_id))?: "unknown id"
            val address = it.data?.get(context.getString(R.string.db_key_diary_address))?: "unknown address"
            val sky = it.data?.get(context.getString(R.string.db_key_diary_sky))?: "unknown sky"
            val wav = it.data?.get(context.getString(R.string.db_key_diary_wav))?: "unknown wav"
            val note = it.data?.get(context.getString(R.string.db_key_diary_note))?: "unknown note"
            val uri = it.data?.get(context.getString(R.string.db_key_image_path))?: null

            list.add(
                NotificationItem(
                    Settings.APP_UUID,
                    date.toString(),
                    time.toString(),
                    address.toString(),
                    sky.toString(),
                    wav.toString(),
                    note.toString(),
                    null, null
                )
            )
        }

        return ArrayList(list)
    }
    fun ParseDiariesCache(context: Context, path: String): Task<QuerySnapshot> {
        return Firebase.firestore.collection(context.getString(R.string.db_root))
            .document(path)
            .collection(context.getString(R.string.db_diary))
            .get(Source.CACHE)
    }
    fun UploadDiary(context: Context, item: NotificationItem): Task<DocumentReference> {

        item.storagePath = makeStoragePath(item)

        // upload string data to firestore
        val map = hashMapOf(
            context.getString(R.string.db_key_diary_date) to item.date,
            context.getString(R.string.db_key_diary_id) to item.devUuid,
            context.getString(R.string.db_key_diary_address) to item.address,
            context.getString(R.string.db_key_diary_time) to item.time,
            context.getString(R.string.db_key_diary_sky) to item.weather,
            context.getString(R.string.db_key_diary_wav) to item.waveHeight,
            context.getString(R.string.db_key_diary_note) to item.message,
            context.getString(R.string.db_key_image_path) to item.storagePath
        )

        return Firebase.firestore.collection(context.getString(R.string.db_root))
            .document(Settings.APP_UUID)
            .collection(context.getString(R.string.db_diary))
            .add(map)

    }

    fun UploadPicture(context: Context, item: NotificationItem, quality: Int): UploadTask?{
        val bitmap = drawableToBitmap(item.drawable)
        bitmap?.let{
            val baos = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, min(quality, 100), baos)
            val data = baos.toByteArray()

            val uploadpath: String = makeStoragePath(item)

            return Firebase.storage.reference.child(uploadpath).putBytes(data)
        }

        return null
    }
    fun DownloadPictureFromCache(context: Context, path: String): ByteArray?{
        pictureCache.find { it.path == path }?.let {
            return it.byteArray
        }
        return null
    }
    fun UploadPictureToCache(context: Context, path: String, byteArray: ByteArray){
        pictureCache.add(
            pictureCache(path, byteArray)
        )
    }
    fun DownloadPicture(context: Context, path: String): Task<ByteArray>{
        val ONE_MEGABYTE: Long = 1024 * 1024
        return Firebase.storage.reference.child(path).getBytes(ONE_MEGABYTE)
    }
}

class pictureCache(
    val path: String,
    val byteArray: ByteArray
)