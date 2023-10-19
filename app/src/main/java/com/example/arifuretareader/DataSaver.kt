package com.example.arifuretareader
import android.Manifest
import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.annotation.SuppressLint
import android.os.Environment
import java.io.File
import java.io.FileWriter
import com.example.arifuretareader.CoolToast.showToast

object DataSaver {

    var ranobeTitle = "Нет названия"
    var bookMarked = false
    var paragraphIndex = 0
    var paragraphScrollPos = 0
    var chIndex = 0
    var chScrollPos = 0
    var chUIscrollPos = 0

    const val chFolderName = "Chapters"
    const val picFolderName = ".Pictures"

    private const val appFolderName = "Quack Re."
    private const val dataFileName = "applicationData.\uD83E\uDD86"
    private val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)
    private val chFolder = File("$appFolder/$chFolderName")
    private val dataFile = File("$appFolder/$dataFileName")

    var tempData = mutableListOf<String>() // storedBmDataArr
    var tempText = mutableListOf<String>()
    var chText = mutableListOf<String>() // this appends text in the reading block
    var chNumArr = mutableListOf<String>() // names of all chapters files

    fun createContentEnv() {
        // software folder
        if (!appFolder.exists()) {
            appFolder.mkdirs()
        }
        //

        // chapters folder
        val chFolder = File(appFolder, chFolderName)
        if (!chFolder.exists()) {
            chFolder.mkdirs()
        }
        //

        // pics folder
        val picFolder = File(appFolder, picFolderName)
        if (!picFolder.exists()) {
            picFolder.mkdirs()
        }
        //

        // creates bookmarks data
        if (!File(appFolder, dataFileName).exists()) {
            val readerData = File(appFolder, dataFileName)
            val writerData = FileWriter(readerData)
            writerData.append(
                "bookMarked#0;\n" +
                "paragraphIndex#0;\n" +
                "paragraphScrollPos#0;\n" +
                "chIndex#0;\n" +
                "chScrollPos#0;\n" +
                "chUIscrollPos#0;\n" +
                "ranobeTitle#Нет названия;"
            )
            writerData.flush()
            writerData.close()
        }
        //
    }
    fun saveData() {
        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)
        val readerData = File(appFolder, dataFileName)
        val writerData = FileWriter(readerData)

        writerData.append(
            "bookMarked#$bookMarked;\n" +
            "paragraphIndex#$paragraphIndex;\n" +
            "paragraphScrollPos#$paragraphScrollPos;\n" +
            "chIndex#$chIndex;\n" +
            "chScrollPos#$chScrollPos;\n" +
            "chUIscrollPos#$chUIscrollPos;\n" +
            "ranobeTitle#$ranobeTitle;"
        )
        writerData.flush()
        writerData.close()
    }
    @SuppressLint("RestrictedApi")
    fun loadData() {
        try {
            for (line in dataFile.readLines()) {
                tempData += line

                if(line.contains("ranobeTitle")) {
                    ranobeTitle = line.substringAfter("#").substringBefore(";")
                }
                if(line.contains("bookMarked")) {
                    bookMarked = line.substringAfter("#").substringBefore(";").toBoolean()
                }
                if(line.contains("paragraphIndex")) {
                    paragraphIndex = line.substringAfter("#").substringBefore(";").toInt()
                }
                if(line.contains("paragraphScrollPos")) {
                    paragraphScrollPos = line.substringAfter("#").substringBefore(";").toInt()
                }
                if(line.contains("chIndex")) {
                    chIndex = line.substringAfter("#").substringBefore(";").toInt()
                }
                if(line.contains("chScrollPos")) {
                    chScrollPos = line.substringAfter("#").substringBefore(";").toInt()
                }
                if(line.contains("chUIscrollPos")) {
                    chUIscrollPos = line.substringAfter("#").substringBefore(";").toInt()
                }

                // number of array equals selected chapter
                val chNum = chNumArr[chIndex]
                //

                val chContent = File("$appFolder/${chFolderName}/$chNum")
                for (lines in chContent.readLines().filter {
                    !it.contains("<div class=\"article-image\">") &&
                            !it.contains("<img class=\"lazyload\"") &&
                            !it.contains("</div>")
                }) {
                    chText += line
                }

            }
            for(file in chFolder.listFiles()) {
                if(chFolder.listFiles().indexOf(file) == chIndex) {
                    for (line in file.readLines()) {
                        if(!line.contains("<div class=\"article-image\">")  && !line.contains("<img class=\"lazyload\"") && !line.contains("</div>")) {
                            tempText+=line
                        }
                    }
                }
            }
        } catch (e: Exception) {
            cleanData()
            saveData()
//            showToast(R.drawable.duck_customizer,"При попытке загрузки последнего сохранения возникли проблемы. Если ситуация повторяется, свяжитесь с разработчиком на странице проекта на GitHub.")
        }
    }
    fun cleanData() {
        bookMarked = false
        paragraphIndex = 0
        paragraphScrollPos = 0
        chIndex = 0
        chScrollPos = 0
        chUIscrollPos = 0

        saveData()
    }
}