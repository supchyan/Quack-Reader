package com.example.arifuretareader

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
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
import android.os.Environment
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
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.example.arifuretareader.CoolToast.showToast
import com.example.arifuretareader.CoolToast.toastContainer
import com.example.arifuretareader.DataSaver.bookMarked
import com.example.arifuretareader.DataSaver.chFolderName
import com.example.arifuretareader.DataSaver.chIndex
import com.example.arifuretareader.DataSaver.chNumArr
import com.example.arifuretareader.DataSaver.chScrollPos
import com.example.arifuretareader.DataSaver.chText
import com.example.arifuretareader.DataSaver.chUIscrollPos
import com.example.arifuretareader.DataSaver.cleanData
import com.example.arifuretareader.DataSaver.createContentEnv
import com.example.arifuretareader.DataSaver.loadData
import com.example.arifuretareader.DataSaver.paragraphIndex
import com.example.arifuretareader.DataSaver.paragraphScrollPos
import com.example.arifuretareader.DataSaver.picFolderName
import com.example.arifuretareader.DataSaver.ranobeTitle
import com.example.arifuretareader.DataSaver.saveData
import com.example.arifuretareader.DataSaver.tempText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.net.URL


class MainActivity : ComponentActivity() {

    lateinit var parent: LinearLayout
    lateinit var header: LinearLayout

    lateinit var scrollBar: ScrollView
    lateinit var scrollContainer: LinearLayout
    lateinit var chapterShownUI: LinearLayout

    lateinit var centeredBlock: RelativeLayout

    lateinit var picContainer: FrameLayout
    lateinit var picGalleryScrollBar: HorizontalScrollView
    lateinit var picGalleryScrollLayout: LinearLayout
    lateinit var picGalleryScrollContainer: LinearLayout
    lateinit var picGalleryHelpingArrow: ImageView

    lateinit var customScrollBar: FrameLayout
    lateinit var customScrollBarRed: LinearLayout
    lateinit var customScrollBarYellow: LinearLayout
    lateinit var customScrollBarBlue: LinearLayout
    lateinit var customScrollBarCyan: LinearLayout
    lateinit var customScrollBarWhite: LinearLayout
    lateinit var customScrollBarText: TextView

    lateinit var chapterScrollBar: HorizontalScrollView
    lateinit var chapterScrollContainer: LinearLayout
    lateinit var chapterScrollBlock: LinearLayout
    lateinit var chapterUIsvg: ImageView

    lateinit var chapterUIscrollBar: ScrollView
    lateinit var chapterUIscrollContainer: LinearLayout
    lateinit var chapterUIscrollBlock: LinearLayout

    lateinit var ranobeNameScrollBar: HorizontalScrollView
    lateinit var ranobeName: TextView

    val chScrollContainerArr = mutableListOf<TextView>()
    val chUIscrollContainerArr = mutableListOf<TextView>()

    var isChUIshown = false
    var welcomeViewAppended = false

    lateinit var closeFw: ImageView
    lateinit var openChUIbtn: ImageView
    lateinit var duckCustomizerBtn: ImageView
    lateinit var delRanobeBtn: ImageView
    lateinit var nextBtn: ImageView
    lateinit var backBtn: ImageView
    lateinit var toBmBtn: ImageView
    lateinit var swapThemeBtn: ImageView

    var duckLayout = mutableListOf<LinearLayout>() // duck's layout for properly work stuff below
    var duckArr = mutableListOf<ImageView>() // duck bookmarks

    val paragraphArr = mutableListOf<TextView>() // text of whole paragraphs in chapter
    var paragraphCount = 0
    lateinit var paragraphBuffer: Job
    lateinit var chJob: Job
    lateinit var chUIjob: Job

    var imageArr = mutableListOf<ImageView>() // preview of certain chapter
    var imagesArr = mutableListOf<ImageView>() // pictures of certain chapter
    lateinit var picBuffer: Job

    lateinit var customScrBbuffer: Job
    var selectedChH = 0

    // locals that moved to globals for some reasons
    var chUImidSvg = listOf<Int>(
        R.drawable._f302,R.drawable._602,R.drawable._614,R.drawable._f327,
        R.drawable._615,R.drawable._f9cb,R.drawable._f37f,R.drawable._f369,
        R.drawable._f30c,R.drawable._f303,R.drawable._f309,R.drawable._f306
    )
    var c = 1
    var of = 0
    var oldScr = 0
    var oldOf = 0
    //

    var lightTheme = false

    var delPressCount = 5 // before delete chapters from app, you pressing btn 5 times
    var errPressCount = 5 // before tell advice to use, he tries 5 time by himself

    var appFolderName = "Quack Re."
    var dataFileName = "applicationData.\uD83E\uDD86"

    lateinit var inputURL : URL
    lateinit var nextChapterFromHTML : String
    var wrongInputURL  = false // send when link isn't valid
    var successConnect = false // send when link is valid
    var parsingError = false // send when somehow can't download chapters
    var doesLastChapterDownloaded = false // send when last chapter has been parsed and saved
    var isDownloading = false // send when can't press enter btn to download chapters from ranobelib
    var chaptersAmount = 0

    val STORAGE_PERMISSION_CODE = 100
    val INTERNET_PERMISSION_CODE = 101
    val NOTIFICATIONS_PERMISSION_CODE = 102

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "channelID"
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("RestrictedApi", "SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        // creates any UI when onCreate() has been awaken
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //

        // searching for main blocks and containers ids
        toastContainer = findViewById(R.id._toastContainer)
        parent = findViewById(R.id._parent)
        header = findViewById(R.id._header)
        centeredBlock = findViewById(R.id._centered_block)
        ranobeName = findViewById(R.id._ranobeName)
        ranobeNameScrollBar = findViewById(R.id._ranobeNameScrollBar)
        chapterShownUI = findViewById(R.id._chapterShownUI)
        picContainer = findViewById(R.id._picture_container)
        picGalleryScrollLayout = findViewById(R.id._picture_gallery_scroll_layout)
        picGalleryScrollBar = findViewById(R.id._picture_gallery_scroll_bar)
        picGalleryScrollContainer = findViewById(R.id._picture_gallery_scroll_container)
        picGalleryHelpingArrow = findViewById(R.id._picture_gallery_helping_arrow)
        customScrollBar = findViewById(R.id._customScrollBar)
        customScrollBarRed = findViewById(R.id._customScrollBarRed)
        customScrollBarYellow = findViewById(R.id._customScrollBarYellow)
        customScrollBarBlue = findViewById(R.id._customScrollBarBlue)
        customScrollBarCyan = findViewById(R.id._customScrollBarCyan)
        customScrollBarWhite = findViewById(R.id._customScrollBarWhite)
        customScrollBarText = findViewById(R.id._customScrollBarText)
        scrollContainer = findViewById(R.id._scrollContainer)
        scrollBar = findViewById(R.id._scrollBar)
        chapterScrollBar = findViewById(R.id._chapterScrollBar)
        chapterScrollContainer = findViewById(R.id._chapterScrollContainer)
        chapterScrollBlock = findViewById(R.id._chapterScrollBlock)
        chapterUIscrollBar = findViewById(R.id._chapterUIscrollBar)
        chapterUIscrollContainer = findViewById(R.id._chapterUIscrollContainer)
        chapterUIsvg = findViewById(R.id._chapter_UI_svg)
        chapterUIscrollBlock = findViewById(R.id._chapterUIscrollBlock)
        openChUIbtn = findViewById(R.id._toCurrentChapter)
        duckCustomizerBtn = findViewById(R.id._duckCustomizerBtn)
        delRanobeBtn = findViewById(R.id._delRanobeBtn)
        nextBtn = findViewById(R.id._next)
        backBtn = findViewById(R.id._back)
        toBmBtn = findViewById(R.id._toBookmark)
        swapThemeBtn = findViewById(R.id._swapTheme)
        //

        ranobeNameScrollBar.setOnTouchListener { v, event ->
            true
        }

        CreateNotificationChannel()

        if (!CheckPermission()) {
            AllowPermissionBlock()
            RequestPermission()
            CheckPermission()
            return
        }
        // creates app folders and data files
        createContentEnv()
        //
        // searching chapter files inside certain folder adds it's names to chNumArr[] and sorting it inside from !min to max!
        ChNumsSearchAndSort()
        //

        val toastPos = lifecycleScope.launch {
            while (true) {
                if(isChUIshown || welcomeViewAppended) {
                    toastContainer.setPadding(0,0,0,0)
                }
                else toastContainer.setPadding(0,0,0,dpToFloat(60).toInt())
                delay(1)
            }
        }

        // if previous fun doesn't find any shit inside, generate 'greetings container'
        if (chNumArr.isEmpty()) {
            WelcomeViewBlock()
            return // returning this, because code below can be executed only if user has one or more chapters in certain folder
        }
        //
        // connect this to some data file in future and to theme changer
        swapThemeBtn.setImageDrawable(getDrawable(R.drawable.dark_theme))
        //
        // 'to current chapter in menu' button activity on click
        openChUIbtn.setOnClickListener {

            isChUIshown = !isChUIshown

            if (!isChUIshown) {
                openChUIbtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_from_180_fa))
                delRanobeBtn.visibility = View.GONE
                chapterUIsvg.visibility = View.GONE
                duckCustomizerBtn.visibility = View.VISIBLE

                chapterScrollBar.visibility = View.VISIBLE

                scrollContainer.animate().alpha(1f).setDuration(250)
                    .setInterpolator(AccelerateDecelerateInterpolator()).start()

                picContainer.animate().alpha(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()

                chapterShownUI.animate().translationY(dpToFloat(500)).setDuration(250)
                    .setInterpolator(AccelerateDecelerateInterpolator()).start()
            } else {
                openChUIbtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_to_180_fa))
                delRanobeBtn.visibility = View.VISIBLE
                duckCustomizerBtn.visibility = View.GONE
                chapterScrollBar.visibility = View.GONE
                chapterUIsvg.visibility = View.VISIBLE


                scrollContainer.animate().alpha(0.4f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()

                picContainer.animate().alpha(0.4f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()

                chapterShownUI.animate().translationY(0f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        }
        //
        // 'change bookmark visuals' button activity on click
        duckCustomizerBtn.setOnClickListener {
            duckCustomizerBtn.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.rotate_like_rotor
                )
            )
            showToast(applicationContext, lifecycleScope, R.drawable.open_gallery, "Данная кнопка позволяет настроить тему закладке, но пока что тут ничего нет.", 10000)
        }
        //
        // 'delete ranobe chapters' button activity on click
        delRanobeBtn.setOnClickListener {

            delRanobeBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.less_bouncing))

            delPressCount--

            if (delPressCount != 0) {
                showToast(applicationContext, lifecycleScope, R.drawable.trash,"Нажмите ещё $delPressCount раз, чтобы удалить все главы")
            } else {

                delPressCount = 5
                customScrBbuffer.cancel()
                paragraphBuffer.cancel()
                picBuffer.cancel()
                
                chJob.cancel()
                chUIjob.cancel()

                WelcomeViewBlock()

                DeleteChapters()

                ranobeTitle = "Нет названия"

                showToast(applicationContext, lifecycleScope, R.drawable.duck_customizer,"Удаление успешно")
            }
        }
        //
        // 'back to done content' button activity on click
        toBmBtn.setOnClickListener {
            toBmBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_like_rotor))
            scrollBar.smoothScrollTo(0, paragraphScrollPos)
            chapterScrollBar.smoothScrollTo(chScrollPos, 0)
            if(isChUIshown) {
                chapterUIscrollBar.smoothScrollTo(0, chUIscrollPos)
            }
            if (scrollBar.scrollY == paragraphScrollPos && chapterScrollBar.scrollX == chScrollPos) {
                showToast(applicationContext, lifecycleScope, R.drawable.tobookmark,"В прошлый раз вы остановились в этом месте.")
            }
            else CheckScrollPos(lifecycleScope)
        }
        //
        // 'swap theme' button activity on click
        swapThemeBtn.setOnClickListener {

            lightTheme = !lightTheme

            swapThemeBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_360))

            if (!lightTheme) {
                swapThemeBtn.setImageDrawable(getDrawable(R.drawable.dark_theme))
            } else {
                swapThemeBtn.setImageDrawable(getDrawable(R.drawable.light_theme))
            }

                showToast(applicationContext, lifecycleScope, R.drawable.dark_theme, "В данный момент сменить тему нельзя.")
        }
        //
        // 'next chapter' button activity on click
        nextBtn.setOnClickListener {

            if (chIndex == chNumArr.lastIndex || chIndex == chNumArr.lastIndex) {
                val animator = ValueAnimator.ofFloat(0f, -20f, 0f).apply {
                    addUpdateListener { animation ->
                        nextBtn.translationX = animation.animatedValue as Float
                    }
                    duration = 250
                    start()
                }
                showToast(applicationContext, lifecycleScope, R.drawable.duck_customizer,"Вы находитесь на последней главе.")
                return@setOnClickListener
            }

            customScrBbuffer.cancel()
            paragraphBuffer.cancel()
            picBuffer.cancel()
            chJob.cancel()
            chUIjob.cancel()

            nextBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.less_bouncing))

            scrollBar.scrollTo(0, 0)

            chIndex += 1
            chScrollPos = chScrollContainerArr[chIndex].left
            chUIscrollPos = chUIscrollContainerArr[chIndex].top - selectedChH
            chScrollContainerArr.clear()
            chapterScrollContainer.removeAllViews()
            chUIscrollContainerArr.clear()
            chapterUIscrollContainer.removeAllViews()
            ChaptersSelectBlock(lifecycleScope)
            ClearReadingBlock()
            saveData()
            ReadingBlock(lifecycleScope)

        }
        //
        // 'previous chapter' button activity on click
        backBtn.setOnClickListener {

            if (chIndex == 0) {
                val animator = ValueAnimator.ofFloat(0f, 20f, 0f).apply {
                    addUpdateListener { animation ->
                        backBtn.translationX = animation.animatedValue as Float
                    }
                    duration = 250
                    start()
                }
                showToast(applicationContext, lifecycleScope, R.drawable.duck_customizer,"Вы находитесь на первой главе.")
                return@setOnClickListener
            }

            customScrBbuffer.cancel()
            paragraphBuffer.cancel()
            picBuffer.cancel()
            chJob.cancel()
            chUIjob.cancel()

            backBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.less_bouncing))

            scrollBar.scrollTo(0, 0)

            chIndex -= 1
            chScrollPos = chScrollContainerArr[chIndex].left
            chUIscrollPos = chUIscrollContainerArr[chIndex].top - selectedChH
            chScrollContainerArr.clear()
            chapterScrollContainer.removeAllViews()
            chUIscrollContainerArr.clear()
            chapterUIscrollContainer.removeAllViews()
            ChaptersSelectBlock(lifecycleScope)
            ClearReadingBlock()
            saveData()
            ReadingBlock(lifecycleScope)

        }
        //
        try {
            // generates block with paragraphs when onCreate() has been awaken
            ReadingBlock(lifecycleScope)
            //

            // generates block with chapters selection
            ChaptersSelectBlock(lifecycleScope)
            //

            val titleAnim = lifecycleScope.launch {
                while(true) {
                    val time: Long = 25000
                    while(true) {
                        ranobeName.translationX = resources.displayMetrics.widthPixels.toFloat()
                        ranobeName.animate().translationX(-resources.displayMetrics.widthPixels-getViewWidth(ranobeName).toFloat()).setDuration(time).setInterpolator(LinearInterpolator()).start()
                        delay(time)
                    }
                }
            }

        } catch (e: Exception) {
            // make everything null
            cleanData()
            //

            // try to generate again
            ReadingBlock(lifecycleScope)
            ChaptersSelectBlock(lifecycleScope)
            //

            val titleAnim = lifecycleScope.launch {
                while(true) {
                    val time: Long = 25000
                    while(true) {
                        ranobeName.translationX = resources.displayMetrics.widthPixels.toFloat()
                        ranobeName.animate().translationX(-resources.displayMetrics.widthPixels-getViewWidth(ranobeName).toFloat()).setDuration(time).setInterpolator(LinearInterpolator()).start()
                        delay(time)
                    }
                }
            }
        }
    }
    //
    @SuppressLint("SuspiciousIndentation")
    override fun onStop() {
        super.onStop()
        if(isDownloading)
        ShowNotification("Приложение свёрнуто", "Загрузка происходит в фоновом режиме.")
    }
    // Block with chapter's content
    @SuppressLint("ClickableViewAccessibility")
    fun ReadingBlock(scope: CoroutineScope) {

        centeredBlock.visibility = View.GONE

        loadData()

        // novel name
        ranobeName.text = ranobeTitle
        //

        paragraphCount = chText.lastIndex

        for (i in 0..paragraphCount) {
            paragraphArr += TextView(this)
            duckLayout += LinearLayout(this)
            duckArr += ImageView(this)
        }

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)
        val picFolder = File(appFolder, picFolderName)

        // generates solo picture as chapter's preview?
        picBuffer = scope.launch {
            if(File(picFolder, chNumArr[chIndex].substringBefore(".txt")).exists()) {

                val chPicFolder = File(picFolder, chNumArr[chIndex].substringBefore(".txt"))

                for (i in 0..chPicFolder.listFiles().size) {
                    imageArr += ImageView(applicationContext)
                    imagesArr += ImageView(applicationContext)
                }

                for (pics in chPicFolder.listFiles()) {

                    val img = imagesArr[chPicFolder.listFiles().indexOf(pics)]

                    val bitmap = BitmapFactory.decodeFile(pics.absolutePath)
                    val roBitmap = ImageHelper.getRoundedCornerBitmap(bitmap, dpToFloat(30).toInt())

                    var bmW = roBitmap.width
                    var bmH = roBitmap.height

                    while(bmW > dpToFloat(370).toInt()) {
                        bmW = (bmW/1.1).toInt()
                        bmH = (bmH/1.1).toInt()
                    }
                    while(bmW < dpToFloat(360).toInt()) {
                        bmW = (bmW*1.1).toInt()
                        bmH = (bmH*1.1).toInt()
                    }

                    img.setImageBitmap(Bitmap.createScaledBitmap(roBitmap, bmW, bmH, false))
                    if(imagesArr.size > 2) {

                        img.setPadding(
                            dpToFloat(10).toInt(),
                            dpToFloat(10).toInt(),
                            dpToFloat(10).toInt(),
                            dpToFloat(10).toInt()
                        )
                    }
                    else {
                        img.setPadding(
                            (resources.displayMetrics.widthPixels - getViewWidth(img))/2,
                            0,
                            (resources.displayMetrics.widthPixels - getViewWidth(img))/2,
                            0
                        )
                    }
                    picGalleryScrollContainer.addView(img)
                }

                // generates pic as a chapter's preview
                for (pic in chPicFolder.listFiles()) {

                    val img = imageArr[chPicFolder.listFiles().indexOf(pic)]

                    val bitmap = BitmapFactory.decodeFile(pic.absolutePath)
                    val roBitmap = ImageHelper.getRoundedCornerBitmap(bitmap, dpToFloat(30)
                        .toInt())

                    var bmW = roBitmap.width
                    var bmH = roBitmap.height

                    while(bmW > dpToFloat(230).toInt()) {
                        bmW = (bmW/1.1).toInt()
                        bmH = (bmH/1.1).toInt()
                    }
                    while(bmW < dpToFloat(230).toInt()) {
                        bmW = (bmW*1.1).toInt()
                        bmH = (bmH*1.1).toInt()
                    }

                    img.setImageBitmap(Bitmap.createScaledBitmap(roBitmap, bmW, bmH, false))

                    img.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    img.setPadding(
                        dpToFloat(0).toInt(),
                        dpToFloat(5).toInt(),
                        dpToFloat(0).toInt(),
                        dpToFloat(30).toInt())
                    img.foregroundGravity = Gravity.CENTER
                    img.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.simple_appearing))

                    picContainer.addView(img)

                    val dot = ImageView(applicationContext)
                    dot.setImageResource(R.drawable.open_gallery)
                    val animatable = dot.drawable as Animatable
                    animatable.start()

                    dot.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    dot.setPadding(
                        0,
                        getViewHeight(img) - dpToFloat(30).toInt() - getViewHeight(dot)/2,
                        0,
                        0
                    )
                    dot.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.simple_appearing))

                    img.setOnClickListener {

                        if(isChUIshown) {
                            ClearChSelBlock()
                            return@setOnClickListener
                        }

                        customScrollBar.visibility = View.GONE
                        scrollBar.visibility = View.GONE
                        header.visibility = View.GONE
                        chapterShownUI.visibility = View.GONE

                        picGalleryScrollLayout.visibility = View.VISIBLE
                        picGalleryScrollContainer.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.simple_appearing))

                        val job = lifecycleScope.launch {

                            if(imagesArr.size > 2) {
                                picGalleryHelpingArrow.visibility = View.VISIBLE
                                picGalleryHelpingArrow.animation = AnimationUtils.loadAnimation(applicationContext, R.anim.helping_arrow)
                            }
                            else picGalleryHelpingArrow.visibility = View.GONE
                            delay(10)
                            picGalleryScrollBar.smoothScrollTo(dpToFloat(60).toInt(),0)
                            delay(500)
                            picGalleryScrollBar.smoothScrollTo(dpToFloat(0).toInt(),0)
                        }
                    }
                    // returns process if pics in chapter's folder more than 1
                    if(chPicFolder.listFiles().size > 1) {
                        picContainer.addView(dot)
                        return@launch
                    }
                    //
                }
                //
            }
        }
        //

        // block to create UI with chapter's images
        // ...
        //

        paragraphBuffer = scope.launch {
            var posOff = 0
            for (paragraph in paragraphArr) {

                // back position of scroll bar to bottom horizontal chapter's bar
                if(posOff != 10) {
                    chapterScrollBar.scrollTo(chScrollPos, 0) // this is here, because of multiple updates...hahahn't
                    posOff += 1
                }
                //

                if (paragraphArr.indexOf(paragraph) < tempText.size) {

                    val k = paragraphArr.indexOf(paragraph)

                    // paragraph
                    paragraph.text = tempText[k]
                    paragraph.setTextColor(getColor(R.color._textColor))
                    paragraph.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                    if (paragraphArr.indexOf(paragraph) != tempText.lastIndex)
                        paragraph.setPadding(
                            dpToFloat(30).toInt(), 0, dpToFloat(30).toInt(), 0)
                    else
                        paragraph.setPadding(
                            dpToFloat(30).toInt(),
                            0,
                            dpToFloat(30).toInt(),
                            dpToFloat(60).toInt()
                        )

                    scrollContainer.addView(paragraph)

                    val animation = AnimationUtils.loadAnimation(
                        applicationContext,
                        R.anim.simple_appearing
                    )
                    paragraph.animation = animation
                    //

                    val duckLayoutId = duckLayout[paragraphArr.indexOf(paragraph)]
                    duckLayoutId.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    duckLayoutId.gravity = Gravity.LEFT

                    // duck bookmark
                    val duckId = duckArr[paragraphArr.indexOf(paragraph)]

                    duckId.setImageDrawable(getDrawable(R.drawable.duckbookmark))
                    duckId.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    duckId.scaleX = 1.3f
                    duckId.scaleY = 1.3f
                    duckId.setPadding(0, dpToFloat(10).toInt(), 0, dpToFloat(10)
                        .toInt())
                    duckId.visibility = View.INVISIBLE
                    duckId.foregroundGravity = Gravity.LEFT
                    //

                    // append to duckLayout duck image view and then append it in scroll container
                    duckLayoutId.addView(duckId)
                    scrollContainer.addView(duckLayoutId)
                    //

                    paragraph.setOnClickListener {

                        if(isChUIshown) {
                            ClearChSelBlock()
                            return@setOnClickListener
                        }

                        if (paragraph.text.toString().contains("*")) {

                            bookMarked = false

                            if (!bookMarked) {
                                for (i in 0..paragraphArr.lastIndex) {
                                    if (duckArr[i].visibility == View.VISIBLE) {
                                        duckArr[i].animation = AnimationUtils.loadAnimation(
                                            applicationContext,
                                            R.anim.duck_out
                                        )
                                        Handler().postDelayed({
                                            duckArr[i].visibility = View.INVISIBLE
                                        }, 300)
                                    }
                                    duckArr[i].visibility = View.INVISIBLE
                                }
                                TextColorNormalize()
                            }

                            paragraphIndex = paragraphArr.indexOf(paragraph) - 1
                            bookMarked = paragraphIndex > 0
                            paragraphScrollPos = scrollBar.scrollY

                            saveData()

                            toBmBtn.setColorFilter(getColor(R.color._duckBodyColor), PorterDuff.Mode.SRC_IN)
                            toBmBtn.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.less_bouncing))

                            scrollBar.smoothScrollTo(0, scrollContainer.height)

                            for (i in 0..paragraphArr.lastIndex) {
                                paragraphArr[i].setTextColor(getColor(R.color._textColor))
                            }
                            for (i in 0..paragraphIndex) {
                                if (paragraphIndex > 0)
                                    paragraphArr[i].setTextColor(getColor(R.color._sideColor))
                            }
                        }
                    }

                    // 'duck bookmark' button activity on click
                    duckLayoutId.setOnClickListener {

                        if(isChUIshown) {
                            ClearChSelBlock()
                            return@setOnClickListener
                        }

                        bookMarked = !bookMarked
                        paragraphIndex = paragraphArr.indexOf(paragraph)
                        paragraphScrollPos = scrollBar.scrollY

                        saveData()

                        if (!bookMarked) {
                            for (i in 0..paragraphArr.lastIndex) {
                                if (duckArr[i].visibility == View.VISIBLE) {
                                    duckArr[i].animation = AnimationUtils.loadAnimation(
                                        applicationContext,
                                        R.anim.duck_out
                                    )
                                    Handler().postDelayed({
                                        duckArr[i].visibility = View.INVISIBLE
                                    }, 300)
                                }
                                duckArr[i].visibility = View.INVISIBLE
                            }
                            toBmBtn.setColorFilter(getColor(R.color._sideColor), PorterDuff.Mode.SRC_IN)
                            toBmBtn.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.less_bouncing))
                            TextColorNormalize()
                        } else {
                            for (i in 0..paragraphArr.lastIndex) {
                                if (i == paragraphIndex) duckArr[i].visibility = View.VISIBLE
                                else duckArr[i].visibility = View.INVISIBLE
                            }
                            duckId.animation =
                                AnimationUtils.loadAnimation(applicationContext, R.anim.duck_in)
                            for (i in 0..paragraphIndex) {
                                paragraphArr[i].setTextColor(getColor(R.color._sideColor))
                            }
                            toBmBtn.setColorFilter(getColor(R.color._duckBodyColor), PorterDuff.Mode.SRC_IN)
                            toBmBtn.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.less_bouncing))
                        }
                    }
                    if (!bookMarked) {
                        for (i in 0..paragraphArr.lastIndex) {
                            duckArr[i].visibility = View.INVISIBLE
                        }
                        toBmBtn.setColorFilter(getColor(R.color._sideColor), PorterDuff.Mode.SRC_IN)
                        toBmBtn.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.less_bouncing))
                        TextColorNormalize()
                    } else {
                        duckArr[paragraphIndex].visibility = View.VISIBLE
                        for (i in 0..paragraphIndex) {
                            paragraphArr[i].setTextColor(getColor(R.color._sideColor))
                        }
                        toBmBtn.setColorFilter(getColor(R.color._duckBodyColor), PorterDuff.Mode.SRC_IN)
                        toBmBtn.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.less_bouncing))
                    }
                }
                delay(1)
            }
        }

        customScrBbuffer = scope.launch {

            var lerp = 0f

            while (true) {

                val sbH = scrollBar.bottom.toFloat()
                val scP = scrollContainer.bottom.toFloat() - scrollBar.scrollY.toFloat()
                val oldScP = scrollContainer.bottom.toFloat()
                val min = 0f
                val max = 1f

                if(oldScP - sbH != 0f) lerp = ((scP - sbH) / (oldScP - sbH)) * (min - max) + max

                val offset = (scrollBar.bottom.toFloat() - getViewHeight(customScrollBarWhite)) * lerp

                customScrollBarRed.translationY = offset * 0.995f
                customScrollBarYellow.translationY = offset * 0.997f
                customScrollBarBlue.translationY = offset * 1.006f
                customScrollBarCyan.translationY = offset * 1.003f
                customScrollBarWhite.translationY = offset

                customScrollBarText.translationY = dpToFloat(50) + offset
                customScrollBarText.translationX = dpToFloat(8)
                customScrollBarText.pivotX = getViewWidth(customScrollBarText) / 2f
                customScrollBarText.pivotY = getViewHeight(customScrollBarText) / 2f
                customScrollBarText.rotation = 90f

                delay(1)
            }
        }
    }
    //
    // clears stored content inside 'ReadingBlock'
    fun ClearReadingBlock() {
        bookMarked = false
        paragraphIndex = 0
        paragraphScrollPos = 0
        chText.clear()
        paragraphArr.clear()
        duckArr.clear()
        imageArr.clear()
        imagesArr.clear()
        picContainer.removeAllViews()
        picGalleryScrollContainer.removeAllViews()
        duckLayout.clear()
        scrollContainer.removeAllViews()
        tempText.clear()
    }
    //
    // resets text color of certain content to normal
    private fun TextColorNormalize() {
        // gives to whole text normal (un-bookmarked) color
        for (i in 0..paragraphCount) {
            paragraphArr[i].setTextColor(getColor(R.color._textColor))
            paragraphScrollPos = 0
        }
        //
    }
    //
    // Block with chapters selections
    @SuppressLint("ResourceAsColor")
    fun ChaptersSelectBlock(scope: CoroutineScope) {

        var tomeOld = 0.0
        var tomeOldUI = 0.0

        for (i in 0..chNumArr.lastIndex) {
            chScrollContainerArr += TextView(this)
            chUIscrollContainerArr += TextView(this)
        }
        chJob = scope.launch {
            for (v in chScrollContainerArr) {

                // name of the chapter in chapter's select mode
                val context = chNumArr[chScrollContainerArr.indexOf(v)].replace(".txt", "")
                val tomeStr = context.substring(0, 3)
                val chapterStr = context.substring(3, context.length)

                var tome = ""
                var chapter = ""

                val tempTome = tomeStr.toDouble().toInt().toDouble()
                val tempChapter = chapterStr.toDouble().toInt().toDouble()

                // filter system to make 000102304230 looks like Tome 3, Chapter 1 *stone face with sparkles*
                if (tomeStr.toDouble() != tempTome) {
                    tome = tomeStr.toDouble().toString()
                } else {
                    tome = tomeStr.toDouble().toInt().toString()
                }
                if (chapterStr.toDouble() != tempChapter) {
                    chapter = chapterStr.toDouble().toString()
                } else {
                    chapter = chapterStr.toDouble().toInt().toString()
                }

                v.text = "Том $tome, Глава $chapter"

                if (chScrollContainerArr.indexOf(v) == chIndex) {
                    v.setTextColor(getColor(R.color._sideColor))
                } else {
                    v.setTextColor(getColor(R.color._textColor))
                }

                v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)

                if (tome.toDouble() == tomeOld) {
                    val separator = layoutInflater.inflate(R.layout.separator_line, null)
                    if (chScrollContainerArr.indexOf(v) != 0)
                        chapterScrollContainer.addView(separator)
                    chapterScrollContainer.addView(v)
                } else {
                    tomeOld = tome.toDouble()

                    val separator = layoutInflater.inflate(R.layout.separator_tri, null)
                    if (chScrollContainerArr.indexOf(v) != 0)
                        chapterScrollContainer.addView(separator)
                    chapterScrollContainer.addView(v)
                }

                v.setOnClickListener {

                    customScrBbuffer.cancel()
                    paragraphBuffer.cancel()
                    picBuffer.cancel()
                    
                    chJob.cancel()
                    chUIjob.cancel()

                    for (i in 0..chScrollContainerArr.lastIndex) {
                        chScrollContainerArr[i].setTextColor(getColor(R.color._textColor))
                    }
                    for (i in 0..chUIscrollContainerArr.lastIndex) {
                        chUIscrollContainerArr[i].setTextColor(getColor(R.color._textColor))
                    }

                    v.setTextColor(getColor(R.color._sideColor))
                    chUIscrollContainerArr[chScrollContainerArr.indexOf(v)].setTextColor(
                        getColor(R.color._sideColor)
                    )

                    chScrollPos = v.left
                    chUIscrollPos = chUIscrollContainerArr[chScrollContainerArr.indexOf(v)].top - selectedChH

                    chIndex = chScrollContainerArr.indexOf(v)

                    scrollBar.scrollTo(0, 0)
                    chScrollContainerArr.clear()
                    chapterScrollContainer.removeAllViews()
                    chUIscrollContainerArr.clear()
                    chapterUIscrollContainer.removeAllViews()
                    ChaptersSelectBlock(lifecycleScope)
                    ClearReadingBlock()
                    saveData()
                    ReadingBlock(lifecycleScope)
                }
            }
        }
        var text = ""
        chUIjob = scope.launch {

            var height = mutableListOf<Int>()

            for (vUI in chUIscrollContainerArr) {
                // name of the chapter in chapter's select mode
                val context = chNumArr[chUIscrollContainerArr.indexOf(vUI)].replace(".txt", "")
                val tomeStr = context.substring(0, 3)
                val chapterStr = context.substring(3, context.length)

                var tome = ""
                var chapter = ""

                val tempTome = tomeStr.toDouble().toInt().toDouble()
                val tempChapter = chapterStr.toDouble().toInt().toDouble()

                // filter system to make 000102304230 looks like Tome 3, Chapter 1 *stone face with sparkles*
                if (tomeStr.toDouble() != tempTome) {
                    tome = tomeStr.toDouble().toString()
                } else {
                    tome = tomeStr.toDouble().toInt().toString()
                }
                if (chapterStr.toDouble() != tempChapter) {
                    chapter = chapterStr.toDouble().toString()
                } else {
                    chapter = chapterStr.toDouble().toInt().toString()
                }

                vUI.text = "Том $tome, Глава $chapter"

                vUI.gravity = Gravity.CENTER

                if (chUIscrollContainerArr.indexOf(vUI) == chIndex) {

                    vUI.setPadding(0, dpToFloat(24).toInt(), 0, dpToFloat(24).toInt())

                    vUI.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
                    vUI.setTextColor(getColor(R.color._sideColor))
                } else {

                    vUI.setPadding(0, dpToFloat(25).toInt(), 0, dpToFloat(25).toInt())

                    vUI.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
                    vUI.setTextColor(getColor(R.color._textColor))
                }


                val separator = layoutInflater.inflate(R.layout.separator_tome, null)
                val tomeText = separator.findViewById<TextView>(R.id._tome_chUI_text)

                tomeText.textAlignment = View.TEXT_ALIGNMENT_CENTER

                tomeText.setPadding(0, dpToFloat(15).toInt(), 0, dpToFloat(20).toInt())

                var sepIndex = 0
                if (tome.toDouble() != tomeOldUI) {
                    tomeOldUI = tome.toDouble()
                    tomeText.text = "Том $tome"
                    chapterUIscrollContainer.addView(separator)

                    sepIndex = chapterUIscrollContainer.indexOfChild(separator)

                    height += getViewHeight(chapterUIscrollContainer)

                }
                chapterUIscrollContainer.addView(vUI)

                selectedChH = getViewHeight(vUI)*3

                vUI.setOnClickListener {

                    customScrBbuffer.cancel()
                    paragraphBuffer.cancel()
                    picBuffer.cancel()
                    
                    chJob.cancel()
                    chUIjob.cancel()

                    for (i in 0..chUIscrollContainerArr.lastIndex) {
                        chUIscrollContainerArr[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                        chUIscrollContainerArr[i].setTextColor(getColor(R.color._textColor))
                    }
                    for (i in 0..chScrollContainerArr.lastIndex) {
                        chScrollContainerArr[i].setTextColor(getColor(R.color._textColor))
                    }

                    vUI.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    vUI.setTextColor(getColor(R.color._sideColor))
                    chScrollContainerArr[chUIscrollContainerArr.indexOf(vUI)].setTextColor(
                        getColor(R.color._sideColor)
                    )

                    chScrollPos = chScrollContainerArr[chUIscrollContainerArr.indexOf(vUI)].left
                    chUIscrollPos = vUI.top - selectedChH

                    chIndex = chUIscrollContainerArr.indexOf(vUI)

                    scrollBar.scrollTo(0, 0)
                    chapterScrollBar.scrollTo(chScrollPos, 0)

                    chScrollContainerArr.clear()
                    chapterScrollContainer.removeAllViews()
                    chUIscrollContainerArr.clear()
                    chapterUIscrollContainer.removeAllViews()
                    ChaptersSelectBlock(lifecycleScope)
                    ClearReadingBlock()
                    ClearChSelBlock()
                    saveData()
                    ReadingBlock(lifecycleScope)
                }
            }
            var switch = 0
            while (true) {

                val curScr = chapterUIscrollBar.scrollY
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                fun tomeAnim() {
                    chapterUIsvg.alpha = 0f
                    chapterUIsvg.scaleX = 0.4f
                    chapterUIsvg.scaleY = 0.4f
                    chapterUIsvg.animate().alpha(1f).setDuration(150).start()
                    chapterUIsvg.animate().scaleX(0.6f).setDuration(150).start()
                    chapterUIsvg.animate().scaleY(0.6f).setDuration(150).start()
                }

                if (curScr < oldScr && of == 0) {
                    oldScr = height[of]
                    if(oldOf != of) {
                        chapterUIsvg.setImageResource(chUImidSvg.random())
                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                        oldOf = of
                    }
                }
                else if (curScr < oldScr && of != 0) {
                    oldScr = height[of]
                    of-=1
                    if(oldOf != of) {
                        chapterUIsvg.setImageResource(chUImidSvg.random())
                        vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
                        tomeAnim()
                        oldOf = of
                    }
                }
                else if(curScr >= height[of] && of != height.lastIndex) {
                    oldScr = height[of]
                    of+=1
                    if(oldOf != of) {
                        chapterUIsvg.setImageResource(chUImidSvg.random())
                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                        tomeAnim()
                        oldOf = of
                    }
                }
                else if (curScr >= height[of] && of == height.lastIndex) {
                    oldScr = height[of]
                    if(oldOf != of+1) {
                        chapterUIsvg.setImageResource(chUImidSvg.random())
                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                        tomeAnim()
                        oldOf = of+1
                    }
                }
                delay(1)
            }
        }
    }
    fun ClearChSelBlock() {
        isChUIshown = false

        openChUIbtn.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_from_180_fa))
        delRanobeBtn.visibility = View.GONE
        duckCustomizerBtn.visibility = View.VISIBLE
        chapterUIsvg.visibility = View.GONE

        chapterScrollBar.visibility = View.VISIBLE

        scrollContainer.animate().alpha(1f).setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator()).start()
        picContainer.animate().alpha(1f).setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator()).start()

        chapterShownUI.animate().translationY(dpToFloat(500)).setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator()).start()
    }
    //
    fun getViewHeight(view: View): Int {
        val wm = view.context.getSystemService(WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val deviceWidth: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val size = Point()
            display.getSize(size)
            size.x
        } else {
            display.width
        }
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthMeasureSpec, heightMeasureSpec)
        return view.measuredHeight //        view.getMeasuredWidth();
    }
    fun getViewWidth(view: View): Int {
        val wm = view.context.getSystemService(WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val deviceWidth: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val size = Point()
            display.getSize(size)
            size.x
        } else {
            display.width
        }
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthMeasureSpec, heightMeasureSpec)
        return view.measuredWidth //        view.getMeasuredWidth();
    }
    // Block with welcome / download menu
    fun WelcomeViewBlock() {

        welcomeViewAppended = true
        cleanData()

        // clears UI
        header.visibility = View.GONE
        chapterScrollBlock.visibility = View.GONE
        chapterUIscrollBlock.visibility = View.GONE
        //

        centeredBlock.visibility = View.VISIBLE

        val welcomeView = layoutInflater.inflate(R.layout.welcome_view, null)

        // searching elements id inside the welcomeView
        val nothingHere = welcomeView.findViewById<TextView>(R.id._nothingHere)
        val helpInfo = welcomeView.findViewById<TextView>(R.id._helpInfo)
        val addRanobeBtn = welcomeView.findViewById<ImageView>(R.id._addRanobeBtn)
        val gitLinkBtn = welcomeView.findViewById<ImageView>(R.id._gitLinkBtn)
        val ranobeInput = welcomeView.findViewById<LinearLayout>(R.id._ranobeInput)
        val hideRanobeBtn = welcomeView.findViewById<ImageView>(R.id._hideRanobeBtn)
        val bigDuck = welcomeView.findViewById<ImageView>(R.id._bigDuck)
        val urlSearch = welcomeView.findViewById<EditText>(R.id._urlSearch)
        //
        // generates welcomeView
        centeredBlock.addView(welcomeView)
        //
        ranobeInput.visibility = View.GONE
        hideRanobeBtn.visibility = View.GONE

        // winking cursor animation
        val animator = ValueAnimator.ofFloat(0f, 2f).apply {
            addUpdateListener { animation ->
                if (animation.animatedValue as Float <= 1f) nothingHere.text =
                    "Здесь пока ничего нет_"
                else nothingHere.text = "Здесь пока ничего нет"
            }
            duration = 1000
            repeatCount = Animation.INFINITE
            start()
        }
        //
        // 'to ranobelib page' button activity on click
//        helpInfo.setOnClickListener {
//            val url = "https://ranobelib.me"
//            val i = Intent(Intent.ACTION_VIEW)
//            i.data = Uri.parse(url)
//            startActivity(i)
//        }
        //

        // 'git link' button activity on click
        gitLinkBtn.setOnClickListener {
            gitLinkBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.less_bouncing))
            val url = "https://github.com/supchyan/QuackReader"

            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
        //

        // 'parse link from ranobelib.com' button activity on click
        addRanobeBtn.setOnClickListener {

            if (gitLinkBtn.visibility == View.VISIBLE) {

                gitLinkBtn.visibility = View.GONE
                ranobeInput.visibility = View.VISIBLE

                addRanobeBtn.animate().scaleX(0.8f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
                addRanobeBtn.animate().scaleY(0.8f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()

                addRanobeBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_45_to))

                ranobeInput.alpha = 0f
                ranobeInput.animate().alpha(1f).setDuration(250).start()

                ranobeInput.scaleX = 0.8f
                ranobeInput.scaleY = 0.8f

                ranobeInput.animate().scaleX(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
                ranobeInput.animate().scaleY(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
            } else {
                gitLinkBtn.visibility = View.VISIBLE
                ranobeInput.visibility = View.GONE

                addRanobeBtn.animate().scaleX(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
                addRanobeBtn.animate().scaleY(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()

                addRanobeBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_45_from))

                gitLinkBtn.alpha = 0f
                gitLinkBtn.animate().alpha(1f).setDuration(250).start()

                gitLinkBtn.scaleX = 1.2f
                gitLinkBtn.scaleY = 1.2f

                gitLinkBtn.animate().scaleX(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
                gitLinkBtn.animate().scaleY(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        }
        //

        // parse url input into temp folder
        urlSearch.setOnKeyListener(object : View.OnKeyListener {
            @SuppressLint("RestrictedApi")
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        if(!isDownloading) {
                            Thread {
                                DownloadRanobe("${urlSearch.text}")
                                runOnUiThread {
                                    //Update UI
                                    if(wrongInputURL) {

                                        RedImpulse(urlSearch, ranobeInput)
                                        when (c) {
                                            1 -> {
                                                showToast(applicationContext, lifecycleScope, R.drawable.duck_customizer,"Приложение поддерживает только ссылки с ranobelib.me.")
                                                c = 2
                                            }
                                            2 -> {
                                                showToast(applicationContext, lifecycleScope, R.drawable.duck_customizer,"Для дополнительной информации посетите Github.")
                                                c = 1
                                            }
                                        }
                                        wrongInputURL = false
                                    }
                                    if(successConnect) {

                                        GreenImpulse(urlSearch, ranobeInput)

                                        successConnect = false
                                    }
                                    if(parsingError) {

                                        DeleteChapters()

                                        val cancelAfterDelay = lifecycleScope.launch {

                                            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                                            notificationManager.cancel(NOTIFICATION_ID)

                                            ShowNotification(
                                                "Ошибка",
                                                "Что-то пошло не так, попробуйте загрузить главы по новой."
                                            )

                                            RedImpulse(urlSearch, ranobeInput)

                                            parsingError = false

                                            isDownloading = false

                                            delay(1000)
                                        }


                                    }
                                    if(doesLastChapterDownloaded) {

                                        //change it on notification

                                        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                                        notificationManager.cancel(NOTIFICATION_ID)

                                        ShowNotification(
                                            "Загрузка завершена",
                                            "Загружено ${chaptersAmount} глав(ы). Если число не сходится с сайтом, попробуйте снова или посетите GitHub проекта."
                                        )

                                        // hide keyboard on main block's load
                                        fun View.hideKeyboard() {
                                            val inputManager = context.getSystemService(
                                                INPUT_METHOD_SERVICE
                                            ) as InputMethodManager
                                            inputManager.hideSoftInputFromWindow(windowToken, 0)
                                        }
                                        scrollContainer.hideKeyboard()
                                        //
                                        doesLastChapterDownloaded = false

                                        isDownloading = false

                                        val restartAfterDelay = lifecycleScope.launch {
                                            delay(10)
                                            RestartApp(applicationContext)
                                        }
                                    }
                                }
                            }.start()
                        }
                        else {
                            showToast(applicationContext, lifecycleScope, R.drawable.duck_customizer,"Скачивание уже началось, ожидайте")
                        }
                    }
                    else -> return false
                }
                return true
            }
        })
        //
    }
    //
    // generates 'condition container' to make sure that app can work properly when executing code below
    fun AllowPermissionBlock() {

        centeredBlock.visibility = View.VISIBLE

        val permView = layoutInflater.inflate(R.layout.perm_view, null)
        // clears UI
        header.visibility = View.GONE
        chapterScrollBlock.visibility = View.GONE
        chapterUIscrollBlock.visibility = View.GONE
        //

        // generates 'request view'
        centeredBlock.addView(permView)
        //

        // winking cursor animation
        val nothingHere = permView.findViewById<TextView>(R.id._permText)
        val animator = ValueAnimator.ofFloat(0f, 2f).apply {
            addUpdateListener { animation ->
                if (animation.animatedValue as Float <= 1f) {
                    nothingHere.text =
                        "Откройте приложению доступ к файлам и перезагрузите его, чтобы продолжить использование_"
                } else nothingHere.text =
                    "Откройте приложению доступ к файлам и перезагрузите его, чтобы продолжить использование"
            }
            duration = 1000
            repeatCount = Animation.INFINITE
            start()
        }
        //
    }
    //
    // searching for any chapter inside 'Chapters' folder
    fun ChNumsSearchAndSort() {

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        val chFolder = File("$appFolder/${chFolderName}")

        for (file in chFolder.list()) {
            chNumArr += file
        }
        chNumArr.sort()
    }
    //
    // deletes all content from app folders
    fun DeleteChapters() {
        var appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)
        val chFolder = File(appFolder, chFolderName)
        val picFolder = File(appFolder, picFolderName)
        for (chapters in chFolder.listFiles()) {
            chapters.delete()
        }
        for (f in picFolder.listFiles()) {
            for( p in f.listFiles()) {
                p.delete()
            }
            f.delete()
        }
    }
    //
    // notification channel (since oreo did)
    fun CreateNotificationChannel() {
        val name = "Quack!"
        val descriptionText = "Утка села на шпагат, вот и весь анекдот"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = descriptionText
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    //
    // notification, that might be closed
    @SuppressLint("MissingPermission")
    fun ShowNotification(title: String, text: String) {
        val builder = NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setColor(Color.TRANSPARENT)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle())

        with(NotificationManagerCompat.from(this)) {
            notify(MainActivity.NOTIFICATION_ID, builder.build())
        }
    }
    //
    // notification, that can't be closed
    @SuppressLint("MissingPermission")
    fun ShowImmortalNotification(title: String, text: String) {
        val builder = NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setOngoing(true)
            .setColor(Color.TRANSPARENT)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle())

        with(NotificationManagerCompat.from(this)) {
            notify(MainActivity.NOTIFICATION_ID, builder.build())
        }
    }
    //
    // checks permissions on different apis
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun CheckPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11(R) or above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf<String>(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATIONS_PERMISSION_CODE
                )
            }
            val note = (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
            return Environment.isExternalStorageManager() && note
        } else {
            //Android is below 11(R)
            val note = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
            val internet = ContextCompat.checkSelfPermission(
                this, Manifest.permission.INTERNET
            )
            val write = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val read = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            )
            return (
                note == PackageManager.PERMISSION_GRANTED &&
                internet == PackageManager.PERMISSION_GRANTED &&
                write == PackageManager.PERMISSION_GRANTED &&
                read == PackageManager.PERMISSION_GRANTED
            )
        }
    }
    //
    // checks avaliability of the internet connection
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }
    //
    // downloads whole content from needed links
    @SuppressLint("RestrictedApi")
    fun DownloadRanobe(url: String) {

        var finurl = url
        // checking state of availability url address
        try {
            finurl = if(!finurl.contains("https://")) {
                "https://$url"
            } else url
            isDownloading = true
            inputURL = URL(finurl)

            if(!finurl.contains("ranobelib.me")) {
                wrongInputURL = true
                isDownloading = false
                return
            } else successConnect = true
        } catch (e: Exception) {
            wrongInputURL = true
            isDownloading = false
            return
        }
        //

        // this block it's start point of parser. here program tries to find сhapters
        nextChapterFromHTML = "$finurl"
            .substringBeforeLast("?ui")
            .substringBeforeLast("&ui")
            .substringBeforeLast("?page")

        var isSecondParse = false

        while (isDownloading) {

            if(!isNetworkAvailable(applicationContext)) {
                ShowNotification(
                    "Нет подключения к интернету",
                    "Проверьте подключение к интернету, чтобы продолжить"
                )
            }
            else {
                var noteText = "v${nextChapterFromHTML.substringAfterLast("v").substringBefore("?bid")}"
                    .replace("v", "Том ")
                    .replace("/c", ", Глава ")

                if(!isSecondParse)
                    ShowImmortalNotification(
                        "Идёт загрузка глав...",
                        "$noteText"
                    )
                if(isSecondParse)
                    ShowImmortalNotification("Проверка глав...", "$noteText")

                try {
                    val appFolder = File(Environment.getExternalStorageDirectory(),
                        appFolderName
                    )
                    val anotherFolder = File(appFolder, chFolderName)

                    Log.i("TEXT", "${nextChapterFromHTML}")

                    val doc = Jsoup.connect(nextChapterFromHTML).userAgent("Mozilla").get()
                    val html = doc.outerHtml()
                    var getText = ""

                    if (html.contains("reader-header-action__text text-truncate")) {
                        ranobeTitle = html
                            .substringAfterLast("<div class=\"reader-header-action__text text-truncate\">")
                            .substringBefore("</div>")
                            .replace("       ", "")
                            .replace("\n","")
                            .replace("      ","")
                        saveData()
                    }

                    getText = html
                        .substringAfter("<div class=\"reader-container container container_center\">")
                        .substringBefore("</div> <!-- --> <!-- -->")
                        .replace("<p>","")
                        .replace("</p>","")
                        .replace("&nbsp;","")
                        .replace("    ","")
                        .substringAfter("\n")
                        .substringBeforeLast("\n")

                    var volume = nextChapterFromHTML
                        .substringAfterLast("/v")
                        .substringBeforeLast("/c")
                        .substringBefore(".0")

                    if (volume.toDouble() < 10) {
                        volume = "00$volume"
                    }
                    else if(volume.toDouble() < 100) {
                        volume = "0$volume"
                    }

                    var chapter = nextChapterFromHTML
                        .substringAfterLast("/c")
                        .substringBefore(".0")
                        .substringBefore("?bid")

                    if (chapter.toDouble() < 10) {
                        chapter = "00$chapter"
                    }
                    else if(chapter.toDouble() < 100) {
                        chapter = "0$chapter"
                    }

                    for (line in html.lines()) {

                        if(line.contains("<img class=\"lazyload\"")) {
                            val picture = line
                                .substringAfter("<div class=\"reader-container container container_center\">")
                                .substringBefore("</div> <!-- --> <!-- -->")
                                .substringAfter("<img class=\"lazyload\" data-background=\"\" data-src=\"")
                                .substringBefore("\">")
//                            Log.i("PICTURE", "$picture")
                            val picurl = URL(picture)
                            val image = BitmapFactory.decodeStream(picurl.openConnection().getInputStream())
                            SaveImage("$volume$chapter", "${picture.substringAfterLast("/").substringBeforeLast(".jpg").substringBeforeLast(".png")}", image)
                        }
                        else {
//                            Log.i("0", "0")
                        }

                        val readerChData = File(anotherFolder, "$volume$chapter.txt")
                        val writerChData = FileWriter(readerChData)
                        writerChData.append("$getText")
                        writerChData.flush()
                        writerChData.close()

                        // this downloading process works twice, because of my crab hands...
                        // somehow one or couple chapters doesn't downloads if this operation goes once
                        if(line.contains("Последняя глава прочитана") && isSecondParse) {
                            chaptersAmount = anotherFolder.listFiles().size
                            doesLastChapterDownloaded = true
                            isDownloading = false
                            return
                        }
                        else if (line.contains("Последняя глава прочитана") && !isSecondParse) {
                            nextChapterFromHTML = "$finurl"
                                .substringBeforeLast("?ui")
                                .substringBeforeLast("&ui")
                                .substringBeforeLast("?page")
                            isSecondParse = true
                        }
                        //
                        if(line.contains("class=\"reader-next__btn button text-truncate button_label button_label_right\"")) {
                            nextChapterFromHTML = line
                                .substringAfterLast("<a class=\"reader-next__btn button text-truncate button_label button_label_right\" href=\"")
                                .substringBefore("\" tabindex=\"-1\">")
                                .substringBeforeLast("?ui")
                                .substringBeforeLast("&ui")
                                .substringBeforeLast("?page")
                        }
                        else {
//                            Log.i("0", "0")
                        }
                        //
                    }
                } catch (_: Exception) {

                }
                //
            }
        }
    }
    //
    // saves imgs from urls (not mine code)
    @Throws(IOException::class)
    fun SaveImage(folderName : String, imgName: String, bm: Bitmap) {

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)
        val picFolder = File(appFolder, picFolderName)
        // pic folder
        val chPicFolder = File(picFolder, folderName)
        if (!chPicFolder.exists()) {
            chPicFolder.mkdirs()
        }
        //

        val imageFile = File(chPicFolder, "$imgName.png")
        val out = FileOutputStream(imageFile)

        try {
            bm.compress(Bitmap.CompressFormat.PNG, 100, out) // Compress Image
            out.flush()
            out.close()

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(applicationContext, arrayOf<String>(imageFile.absolutePath), null) { path, uri ->

            }
        } catch (e: java.lang.Exception) {
            throw IOException()
        }
    }
    //
    // restarts app, when needed
    fun RestartApp(context : Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
    //
    // requiers permissions on different apis
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("RestrictedApi")
    fun RequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11(R) or above
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT > 32) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf<String>(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATIONS_PERMISSION_CODE
                    )
                }
            }
        } else {
            //Android is below 11(R)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.INTERNET
                ),
                INTERNET_PERMISSION_CODE
            )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                NOTIFICATIONS_PERMISSION_CODE
            )
        }
    }
    //
    // idk what is this lol
    val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    //

    // converts dp to float (my project uses dp values as hell)
    fun dpToFloat(sizeInDp: Int): Float {
        val screenOffset = resources.displayMetrics.density
        return (sizeInDp * screenOffset)
    }
    //

    // this is for... when error in url line (see welcome block)
    fun RedImpulse(view : EditText, animatedView : LinearLayout) {

        val backgroundDrawable = DrawableCompat.wrap(view.background).mutate()

        DrawableCompat.setTint(backgroundDrawable, Color.parseColor("#FF3C3C"))
        val boxPos = ValueAnimator.ofFloat(0f, 30f, -30f, 0f).apply {
            addUpdateListener { animation ->
                animatedView.translationX = animation.animatedValue as Float
            }
            duration = 220
            start()
        }
        view.setTextColor(Color.parseColor("#ffffff"))
        view.setHintTextColor(Color.parseColor("#ffffff"))
    }
    //

    // this is for... when success in url line (see welcome block)
    fun GreenImpulse(view : EditText, animatedView : LinearLayout) {

        val backgroundDrawable = DrawableCompat.wrap(view.background).mutate()

        DrawableCompat.setTint(backgroundDrawable, Color.parseColor("#3CFF42"))
        view.setTextColor(getColor(R.color._darkerMainColor))
        view.setHintTextColor(getColor(R.color._darkerMainColor))
    }
    //
    // checks current scroll position /-> needed only on starting application when paragraphs are still loading to buffer
    fun CheckScrollPos(scope: CoroutineScope) {
        var job = scope.launch {
            delay(600)
            if (scrollBar.scrollY != paragraphScrollPos) {
                showToast(applicationContext, lifecycleScope, R.drawable.tobookmark,"Глава ещё прогружается, пожалуйста подождите...")
            }
        }
    }
    //
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        //replaces the default 'Back' button action
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(centeredBlock.visibility == View.VISIBLE) {
                return true
            }
            if(isChUIshown) {
                ClearChSelBlock()
            }
            customScrollBar.visibility = View.VISIBLE
            scrollBar.visibility = View.VISIBLE
            header.visibility = View.VISIBLE
            chapterShownUI.visibility = View.VISIBLE

            if(picGalleryScrollLayout.visibility == View.VISIBLE) {
                picGalleryScrollLayout.visibility = View.GONE
                for(v in imageArr) {
                    v.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.simple_appearing))
                }
                val job = lifecycleScope.launch {
                    delay(1)
                    picGalleryScrollBar.scrollTo(dpToFloat(0).toInt(),0)
                }
            }
        }
        return true
    }
}