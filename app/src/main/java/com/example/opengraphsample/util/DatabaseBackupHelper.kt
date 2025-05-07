package com.example.opengraphsample.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import com.example.opengraphsample.repository.room.MyRoomDatabase
import java.io.FileInputStream
import java.io.FileOutputStream

object DatabaseBackupHelper {
    private const val DATABASE_NAME = "OpenGraph.db"
    private const val BACKUP_FILE_NAME = "MyBookMark_BackUp.db"

    // 데이터베이스 백업 (Downloads 폴더)
    @RequiresApi(Build.VERSION_CODES.Q)
    fun backupDatabase(context: Context): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DATABASE_NAME)

            // MediaStore를 사용하여 Downloads 폴더에 파일 생성
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, BACKUP_FILE_NAME)
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return false

            resolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(dbFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 데이터베이스 복구 (Downloads 폴더에서 백업 파일 검색 후 복사)
    /**
     * @param context
     * @param uri SAF로부터 가져온 db파일의 Uri
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun restoreDatabase(context: Context, uri: Uri): Boolean {
        return try {
            if(DocumentFile.fromSingleUri(context, uri)?.name?.contains(".db") == false)
                return false

            val resolver = context.contentResolver

            val inputStream = resolver.openInputStream(uri)
            val outputFile = context.getDatabasePath(DATABASE_NAME)

            MyRoomDatabase.closeDatabase()

            if(outputFile.exists()) {
                outputFile.delete()
            }

            inputStream?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            }

            MyRoomDatabase.getInstance(context)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
