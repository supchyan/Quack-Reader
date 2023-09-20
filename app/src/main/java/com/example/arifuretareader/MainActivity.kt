package com.example.arifuretareader

import android.Manifest.permission.INTERNET
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : ComponentActivity() {

    private lateinit var parent: LinearLayout
    private lateinit var header: LinearLayout

    private lateinit var scrollBar: ScrollView
    private lateinit var scrollContainer: LinearLayout
    private lateinit var chapterShownUI: LinearLayout

    private lateinit var floatingWindow: LinearLayout
    private lateinit var centeredBlock: RelativeLayout

    private lateinit var chapterScrollBar: HorizontalScrollView
    private lateinit var chapterScrollContainer: LinearLayout
    private lateinit var chapterScrollBlock: LinearLayout

    private lateinit var chapterUIscrollBar: ScrollView
    private lateinit var chapterUIscrollContainer: LinearLayout
    private lateinit var chapterUIscrollBlock: LinearLayout

    private lateinit var ranobeName: TextView
    private var ranobeTitle = "Арифурэта: С простейшей профессией к Сильнейшему в мире"

    private val chScrollContainerArr = mutableListOf<TextView>()
    private val chUIscrollContainerArr = mutableListOf<TextView>()

    private var isChUIshown = false

    private lateinit var closeFw: ImageView
    private lateinit var openChUIbtn: ImageView
    private lateinit var duckCustomizerBtn: ImageView
    private lateinit var delRanobeBtn: ImageView
    private lateinit var nextBtn: ImageView
    private lateinit var backBtn: ImageView
    private lateinit var toBmBtn: ImageView
    private lateinit var swapThemeBtn: ImageView

    private var duckLayout = mutableListOf<LinearLayout>() // duck's layout for properly work stuff below
    private var duckArr = mutableListOf<ImageView>() // duck bookmarks

    private val paragraphArr = mutableListOf<TextView>() // text of whole paragraphs in chapter
    private var paragraphCount = 0
    private var paragraphIndex = 0
    private lateinit var paragraphBuffer: Job
    private lateinit var chJob: Job
    private lateinit var chUIjob: Job

    private var scrollPos = 0
    private var chScrollPos = 0

    private var bookMarked = false

    private var lightTheme = false

    private var delPressCount = 5 // before delete chapters from app, you pressing btn 5 times
    private var errPressCount = 5 // before tell advice to use, he tries 5 time by himself

    private var appFolderName = "Quack Re. Data"

    private var bMdataFileName = "storedData.txt"
    private var storedBmDataArr = mutableListOf<String>()

    private var chDataFileName = "chStoredData.txt"
    private var storedChDataArr = mutableListOf<String>()

    private var tempChaptersFolderName = "Temp Chapters"
    private lateinit var ranobeURL : URL

    private var cantConnect  = false
    private var successConnect = false

    private var chFolderName = "Chapters"
    private var chIndex = 0
    private var chText = mutableListOf<String>() // paragraphs content
    private var chNumArr = mutableListOf<String>() // names of all chapters files

    private val PERMISSION_CODE = 100

    @SuppressLint("RestrictedApi", "SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        // creates any UI when onCreate() has been awaken
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //

        // searching for main blocks and containers ids
        parent = findViewById(R.id._parent)
        header = findViewById(R.id._header)
        centeredBlock = findViewById(R.id._centered_block)
        ranobeName = findViewById(R.id._ranobeName)
        chapterShownUI = findViewById(R.id._chapterShownUI)
        scrollContainer = findViewById(R.id._scrollContainer)
        scrollBar = findViewById(R.id._scrollBar)
        chapterScrollBar = findViewById(R.id._chapterScrollBar)
        chapterScrollContainer = findViewById(R.id._chapterScrollContainer)
        chapterScrollBlock = findViewById(R.id._chapterScrollBlock)
        chapterUIscrollBar = findViewById(R.id._chapterUIscrollBar)
        chapterUIscrollContainer = findViewById(R.id._chapterUIscrollContainer)
        chapterUIscrollBlock = findViewById(R.id._chapterUIscrollBlock)
        openChUIbtn = findViewById(R.id._toCurrentChapter)
        duckCustomizerBtn = findViewById(R.id._duckCustomizerBtn)
        delRanobeBtn = findViewById(R.id._delRanobeBtn)
        nextBtn = findViewById(R.id._next)
        backBtn = findViewById(R.id._back)
        toBmBtn = findViewById(R.id._toBookmark)
        swapThemeBtn = findViewById(R.id._swapTheme)
        //

        // generates 'condition container' to make sure that app can work properly when executing code below
        fun AllowPermissionBlock() {
            RequestPermission()

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

        if (!CheckPermission()) {
            AllowPermissionBlock()
            return // returns this, because code below is requires all of those permissions from above
        }

        // creates app folders and data files
        CreateContentStorage()
        //

        // searching chapter files inside certain folder adds it's names to chNumArr[] and sorting it inside from !min to max!
        ChNumsSearchAndSort()
        //

        // if previous fun doesn't find any shit inside, generate 'greetings container'
        if (chNumArr.isEmpty()) {
            WelcomeViewBlock()
            return // returning this, because code below can be executed only if user has one or more chapters in certain folder
        }
        //

        // connect this to some data file in future and to theme changer
        swapThemeBtn.setImageDrawable(getDrawable(R.drawable.dark_theme))
        //

        // Block with chapter's content
        @SuppressLint("ClickableViewAccessibility")
        fun ReadingBlock(scope: CoroutineScope) {

            centeredBlock.visibility = View.GONE

            LoadBookMarkData()
            LoadChapterData()

            // novel name
            ranobeName.text = ranobeTitle
            //

            // duck bookmark
            val duckView = layoutInflater.inflate(R.layout.duck_toast, null)
            //

            paragraphCount = chText.lastIndex

            for (i in 0..paragraphCount) {
                paragraphArr += TextView(this)
                duckLayout += LinearLayout(this)
                duckArr += ImageView(this)
            }
            paragraphBuffer = scope.launch {
                for (paragraph in paragraphArr) {

                    if (paragraphArr.indexOf(paragraph) < paragraphArr.size) {

                        var k = paragraphArr.indexOf(paragraph)

                        // paragraph
                        paragraph.text = chText[k]
                        paragraph.setTextColor(getColor(R.color._textColor))
                        paragraph.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                        if (paragraphArr.indexOf(paragraph) != paragraphArr.lastIndex)
                            paragraph.setPadding(dpToFloat(30).toInt(), 0, dpToFloat(30).toInt(), 0)
                        else
                            paragraph.setPadding(
                                dpToFloat(30).toInt(),
                                0,
                                dpToFloat(30).toInt(),
                                dpToFloat(60).toInt()
                            )

                        scrollContainer.addView(paragraph)

                        var animation = AnimationUtils.loadAnimation(
                            applicationContext,
                            R.anim.simple_appearing
                        )
                        paragraph.animation = animation
                        //

                        var duckLayoutId = duckLayout[paragraphArr.indexOf(paragraph)]
                        duckLayoutId.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        duckLayoutId.gravity = Gravity.LEFT

                        // duck bookmark
                        var duckId = duckArr[paragraphArr.indexOf(paragraph)]

                        duckId.setImageDrawable(getDrawable(R.drawable.duckbookmark))
                        duckId.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        duckId.scaleX = 1.3f
                        duckId.scaleY = 1.3f
                        duckId.setPadding(0, dpToFloat(10).toInt(), 0, dpToFloat(10).toInt())
                        duckId.visibility = View.INVISIBLE
                        duckId.foregroundGravity = Gravity.LEFT
                        //

                        // append to duckLayout duck image view and then append it in scroll container
                        duckLayoutId.addView(duckId)
                        scrollContainer.addView(duckLayoutId)
                        //

                        paragraph.setOnClickListener {

                            if (isChUIshown) {
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
                                scrollPos = scrollBar.scrollY

                                SaveData()

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

                            if (isChUIshown) {
                                return@setOnClickListener
                            }

                            bookMarked = !bookMarked
                            paragraphIndex = paragraphArr.indexOf(paragraph)
                            scrollPos = scrollBar.scrollY

                            SaveData()

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
                            }
                        }
                        if (!bookMarked) {
                            for (i in 0..paragraphArr.lastIndex) {
                                duckArr[i].visibility = View.INVISIBLE
                            }
                            TextColorNormalize()
                        } else {
                            duckArr[paragraphIndex].visibility = View.VISIBLE
                            for (i in 0..paragraphIndex) {
                                paragraphArr[i].setTextColor(getColor(R.color._sideColor))
                            }
                        }
                    }
                    delay(1)
                }
            }
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
                    var context = chNumArr[chScrollContainerArr.indexOf(v)].replace(".txt", "")
                    var tomeStr = context.substring(0, 3)
                    var chapterStr = context.substring(3, context.length)

                    var tome = ""
                    var chapter = ""

                    var tempTome = tomeStr.toDouble().toInt().toDouble()
                    var tempChapter = chapterStr.toDouble().toInt().toDouble()

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
                        var separator = layoutInflater.inflate(R.layout.separator_line, null)
                        if (chScrollContainerArr.indexOf(v) != 0)
                            chapterScrollContainer.addView(separator)
                        chapterScrollContainer.addView(v)
                    } else {
                        tomeOld = tome.toDouble()

                        var separator = layoutInflater.inflate(R.layout.separator_tri, null)
                        if (chScrollContainerArr.indexOf(v) != 0)
                            chapterScrollContainer.addView(separator)
                        chapterScrollContainer.addView(v)
                    }

                    v.setOnClickListener {

                        paragraphBuffer.cancel()
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

                        chIndex = chScrollContainerArr.indexOf(v)

                        scrollBar.scrollTo(0, 0)

                        SaveData()
                        ClearReadingBlock()
                        EraseBookMarkData()

                        ReadingBlock(lifecycleScope)
                    }
                    delay(1)
                }
            }
            chUIjob = scope.launch {
                for (vUI in chUIscrollContainerArr) {
                    // name of the chapter in chapter's select mode
                    var context = chNumArr[chUIscrollContainerArr.indexOf(vUI)].replace(".txt", "")
                    var tomeStr = context.substring(0, 3)
                    var chapterStr = context.substring(3, context.length)

                    var tome = ""
                    var chapter = ""

                    var tempTome = tomeStr.toDouble().toInt().toDouble()
                    var tempChapter = chapterStr.toDouble().toInt().toDouble()

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

                    vUI.setPadding(0, dpToFloat(25).toInt(), 0, dpToFloat(25).toInt())

                    if (chUIscrollContainerArr.indexOf(vUI) == chIndex) {
                        vUI.setTextColor(getColor(R.color._sideColor))
                    } else {
                        vUI.setTextColor(getColor(R.color._textColor))
                    }

                    vUI.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)

                    var separator = layoutInflater.inflate(R.layout.separator_tome, null)
                    var tomeText = separator.findViewById<TextView>(R.id._tome_chUI_text)

                    tomeText.textAlignment = View.TEXT_ALIGNMENT_CENTER

                    tomeText.setPadding(0, dpToFloat(15).toInt(), 0, dpToFloat(15).toInt())

                    if (tome.toDouble() != tomeOldUI) {
                        tomeOldUI = tome.toDouble()
                        tomeText.text = "Том $tome"
                        chapterUIscrollContainer.addView(separator)
                    }
                    chapterUIscrollContainer.addView(vUI)

                    vUI.setOnClickListener {

                        paragraphBuffer.cancel()
                        chJob.cancel()
                        chUIjob.cancel()

                        for (i in 0..chUIscrollContainerArr.lastIndex) {
                            chUIscrollContainerArr[i].setTextColor(getColor(R.color._textColor))
                        }
                        for (i in 0..chScrollContainerArr.lastIndex) {
                            chScrollContainerArr[i].setTextColor(getColor(R.color._textColor))
                        }

                        vUI.setTextColor(getColor(R.color._sideColor))
                        chScrollContainerArr[chUIscrollContainerArr.indexOf(vUI)].setTextColor(
                            getColor(R.color._sideColor)
                        )

                        chScrollPos = chScrollContainerArr[chUIscrollContainerArr.indexOf(vUI)].left

                        chIndex = chUIscrollContainerArr.indexOf(vUI)

                        scrollBar.scrollTo(0, 0)
                        chapterScrollBar.scrollTo(chScrollPos, 0)

                        SaveData()
                        ClearReadingBlock()
                        EraseBookMarkData()

                        isChUIshown = !isChUIshown

                        openChUIbtn.startAnimation(
                            AnimationUtils.loadAnimation(
                                applicationContext,
                                R.anim.rotate_from_180_fa
                            )
                        )

                        delRanobeBtn.visibility = View.GONE
                        duckCustomizerBtn.visibility = View.VISIBLE

                        chapterScrollBar.visibility = View.VISIBLE
                        chapterScrollBar.animate().alpha(1f).setDuration(250)
                            .setInterpolator(AccelerateDecelerateInterpolator()).start()

                        scrollContainer.visibility = View.VISIBLE
                        scrollContainer.animate().alpha(1f).setDuration(250)
                            .setInterpolator(AccelerateDecelerateInterpolator()).start()

                        chapterShownUI.animate().translationY(dpToFloat(500)).setDuration(250)
                            .setInterpolator(AccelerateDecelerateInterpolator()).start()

                        ReadingBlock(lifecycleScope)
                    }
                    delay(1)
                }
            }
        }
        //

        // 'to current chapter in menu' button activity on click
        openChUIbtn.setOnClickListener {

            isChUIshown = !isChUIshown

            if (!isChUIshown) {
                openChUIbtn.startAnimation(
                    AnimationUtils.loadAnimation(
                        this,
                        R.anim.rotate_from_180_fa
                    )
                )
                delRanobeBtn.visibility = View.GONE
                duckCustomizerBtn.visibility = View.VISIBLE

                chapterScrollBar.visibility = View.VISIBLE
                chapterScrollBar.animate().alpha(1f).setDuration(250)
                    .setInterpolator(AccelerateDecelerateInterpolator()).start()

                scrollContainer.animate().alpha(1f).setDuration(250)
                    .setInterpolator(AccelerateDecelerateInterpolator()).start()

                chapterShownUI.animate().translationY(dpToFloat(500)).setDuration(250)
                    .setInterpolator(AccelerateDecelerateInterpolator()).start()
            } else {
                openChUIbtn.startAnimation(
                    AnimationUtils.loadAnimation(
                        this,
                        R.anim.rotate_to_180_fa
                    )
                )
                delRanobeBtn.visibility = View.VISIBLE
                duckCustomizerBtn.visibility = View.GONE

                chapterScrollBar.animate().alpha(0f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
                if (chapterScrollBar.alpha == 0f)
                    chapterScrollBar.visibility = View.INVISIBLE

                scrollContainer.animate().alpha(0.4f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()

                chapterShownUI.animate().translationY(0f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        }

        duckCustomizerBtn.setOnClickListener {
            duckCustomizerBtn.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.rotate_like_rotor
                )
            )
        }
        delRanobeBtn.setOnClickListener {

            delRanobeBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.less_bouncing))

            delPressCount--

            if (delPressCount != 0) {
                coolToast("Нажмите ещё $delPressCount раз, чтобы удалить все главы")
            } else {
                delPressCount = 5
                paragraphBuffer.cancel()
                chJob.cancel()
                chUIjob.cancel()

                WelcomeViewBlock()

                var appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)
                val chFolder = File(appFolder, chFolderName)
                for (chapters in chFolder.listFiles()) {
                    chapters.delete()
                }

                coolToast("Удаление успешно")
            }
        }

        // 'back to done content' button activity on click
        toBmBtn.setOnClickListener {
            toBmBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_to_180))
            scrollBar.smoothScrollTo(0, scrollPos)
            chapterScrollBar.smoothScrollTo(chScrollPos, 0)
            if (scrollBar.scrollY == scrollPos && chapterScrollBar.scrollX == chScrollPos) coolToast(
                "В прошлый раз вы остановились в этом месте"
            )
            else CheckScrollPos(lifecycleScope)
        }
        //

        // 'change theme' button activity on click
        swapThemeBtn.setOnClickListener {

            lightTheme = !lightTheme

            swapThemeBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_360))

            if (!lightTheme) {
                swapThemeBtn.setImageDrawable(getDrawable(R.drawable.dark_theme))
            } else {
                swapThemeBtn.setImageDrawable(getDrawable(R.drawable.light_theme))
            }
        }
        //

        // 'next chapter' button activity on click
        nextBtn.setOnClickListener {

            paragraphBuffer.cancel()
            chJob.cancel()
            chUIjob.cancel()

            if (chIndex == chNumArr.lastIndex || chIndex == chNumArr.lastIndex) {
                val animator = ValueAnimator.ofFloat(0f, -20f, 0f).apply {
                    addUpdateListener { animation ->
                        nextBtn.translationX = animation.animatedValue as Float
                    }
                    duration = 250
                    start()
                }
                coolToast("Там ничего нет")
                return@setOnClickListener
            }

            chIndex += 1

            nextBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.less_bouncing))

            scrollBar.scrollTo(0, 0)

            chScrollPos = chScrollContainerArr[chIndex].left

            chScrollContainerArr.clear()
            chapterScrollContainer.removeAllViews()
            chUIscrollContainerArr.clear()
            chapterUIscrollContainer.removeAllViews()
            ChaptersSelectBlock(lifecycleScope)

            chapterScrollBar.scrollTo(chScrollPos, 0)

            SaveData()

            ClearReadingBlock()
            EraseBookMarkData()

            ReadingBlock(lifecycleScope)

        }
        //

        // 'previous chapter' button activity on click
        backBtn.setOnClickListener {

            paragraphBuffer.cancel()
            chJob.cancel()
            chUIjob.cancel()

            if (chIndex == 0) {
                val animator = ValueAnimator.ofFloat(0f, 20f, 0f).apply {
                    addUpdateListener { animation ->
                        backBtn.translationX = animation.animatedValue as Float
                    }
                    duration = 250
                    start()
                }
                coolToast("Там ничего нет")
                return@setOnClickListener
            }

            chIndex -= 1

            backBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.less_bouncing))

            scrollBar.scrollTo(0, 0)

            chScrollPos = chScrollContainerArr[chIndex].left

            chScrollContainerArr.clear()
            chapterScrollContainer.removeAllViews()
            chUIscrollContainerArr.clear()
            chapterUIscrollContainer.removeAllViews()
            ChaptersSelectBlock(lifecycleScope)

            chapterScrollBar.scrollTo(chScrollPos, 0)

            SaveData()

            ClearReadingBlock()
            EraseBookMarkData()

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
        } catch (e: Exception) {

            // make everything null
            EraseBookMarkData()
            EraseChData()
            //

            // save it
            SaveData()
            //

            // try to generate again
            ReadingBlock(lifecycleScope)
            ChaptersSelectBlock(lifecycleScope)
            //
        }
    }
    //

    // converts dp to float (my project uses dp values as hell)
    private fun dpToFloat(sizeInDp: Int): Float {
        var screenOffset = resources.displayMetrics.density
        return (sizeInDp * screenOffset)
    }
    //

    // generates toast message on call
    private fun coolToast(text: String) {
        var toast = Toast.makeText(applicationContext, " ", Toast.LENGTH_SHORT)
        var toastView = layoutInflater.inflate(R.layout.duck_toast, null)
        var toastTextId = toastView.findViewById<TextView>(R.id._toast_text)
        toastTextId.text = text
        toast.view = toastView
        toast.show()
    }
    //

    // checks current scroll position /-> needed only on starting application when paragraphs are still loading to buffer
    private fun CheckScrollPos(scope: CoroutineScope) {
        var job = scope.launch {
            delay(600)
            if (scrollBar.scrollY != scrollPos) {
                coolToast("Глава ещё прогружается, пожалуйста подождите...")
            }
        }
    }
    //

    // requiers permissions on different apis
    @SuppressLint("RestrictedApi")
    private fun RequestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11(R) or above
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            //Android is below 11(R)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(INTERNET, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
                PERMISSION_CODE
            )
        }
    }
    //

    // checks permissions on different apis
    private fun CheckPermission(): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11(R) or above
            Environment.isExternalStorageManager()
        } else {
            //Android is below 11(R)
            val internet = ContextCompat.checkSelfPermission(this, INTERNET)
            val write = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
            internet == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }
    //

    // idk what is this lol
    @SuppressLint("RestrictedApi")
    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            //here we will handle the result of our intent
            if (SDK_INT >= Build.VERSION_CODES.R) {
                //Android is 11(R) or above
                if (Environment.isExternalStorageManager()) {
                    //Manage External Storage Permission is granted
                } else {
                    //Manage External Storage Permission is denied....
                }
            } else {
                //Android is below 11(R)
            }
        }
    //

    // same as with above
    @SuppressLint("RestrictedApi")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                //check each permission if granted or not
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                val internet = grantResults[2] == PackageManager.PERMISSION_GRANTED
                if (write && read && internet) {
                    //External Storage Permission granted
                } else {
                    //External Storage Permission denied...
                }
            }
        }
    }
    //

    // creates certain folders and data files
    private fun CreateContentStorage() {
        // software folder
        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)
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

        // creates bookmarks data
        if (!File(appFolder, bMdataFileName).exists()) {
            val readerData = File(appFolder, bMdataFileName)
            val writerData = FileWriter(readerData)
            writerData.append("false\n0\n0")
            writerData.flush()
            writerData.close()
        }
        //

        // creates paused chapter data if not exists
        if (!File(appFolder, chDataFileName).exists()) {
            val readerChData = File(appFolder, chDataFileName)
            val writerChData = FileWriter(readerChData)
            writerChData.append("0\n0")
            writerChData.flush()
            writerChData.close()
        }
        //
    }

    // saves (writes) data in files
    private fun SaveData() {

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        // creates file to store current bookmark's state
        val readerData = File(appFolder, bMdataFileName)
        val writerData = FileWriter(readerData)
        writerData.append("$bookMarked\n$paragraphIndex\n$scrollPos")
        writerData.flush()
        writerData.close()
        //

        // creates file to store current chapter's number
        val readerChData = File(appFolder, chDataFileName)
        val writerChData = FileWriter(readerChData)
        writerChData.append("$chIndex\n$chScrollPos")
        writerChData.flush()
        writerChData.close()
        //
    }
    //

    // loads bookmark data from storedBmDataArr[]
    private fun LoadBookMarkData() {

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        // saves file data into storedBmDataArr[]
        val dataFile = File("$appFolder/$bMdataFileName")
        for (line in dataFile.readLines()) {
            storedBmDataArr += line
        }
        //
        // if 'chStoredData.txt' is strange
        try {
            bookMarked = storedBmDataArr[0].toBoolean()
            paragraphIndex = storedBmDataArr[1].toInt()
            scrollPos = storedBmDataArr[2].toInt()
        } catch (e: Exception) {

            bookMarked = false
            paragraphIndex = 0
            scrollPos = 0

            SaveData()

            coolToast("Ошибка: B0M1DU6K")
        }
    }
    //

    // loads chapter data to chText[] (this array's 'i' is 'paragraphs' in the future)
    @SuppressLint("RestrictedApi")
    private fun LoadChapterData() {

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        // saves file data into storedChArr[]
        val dataChFile = File("$appFolder/$chDataFileName")
        for (line in dataChFile.readLines()) {
            storedChDataArr += line
        }
        //

        // if 'chStoredData.txt' is strange
        try {
            chIndex = storedChDataArr[0].toInt()
            chScrollPos = storedChDataArr[1].toInt()
        } catch (e: Exception) {

            chIndex = 0
            chScrollPos = 0

            SaveData()

            coolToast("Ошибка: CH3RDU6K")
        }

        // number of array equals selected chapter
        var chName = chNumArr[chIndex]
        //

        // if some loaded 'chapter name' from 'chapter's array' is no longer available, returns fun with an error
        if (!File("$appFolder/$chFolderName/$chName").exists()) {
            coolToast("Ошибка: N0DU6K")
            return
        }
        //

        // if certain chapter has been found, adds it's 'paragraphs content' into chText[]
        val chContent = File("$appFolder/$chFolderName/$chName")
        for (line in chContent.readLines()) {
            chText += line
        }
        //
    }
    //

    // deletes all data from storedData.txt
    private fun EraseBookMarkData() {

        bookMarked = false
        paragraphIndex = 0
        scrollPos = 0
        storedBmDataArr.clear()

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        val readerData = File(appFolder, bMdataFileName)
        val writerData = FileWriter(readerData)
        writerData.append("$bookMarked\n$paragraphIndex\n$scrollPos")
        writerData.flush()
        writerData.close()

    }
    //

    // deletes all data from chStoredData.txt
    private fun EraseChData() {

        chIndex = 0
        chScrollPos = 0

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        val readerChData = File(appFolder, chDataFileName)
        val writerChData = FileWriter(readerChData)
        writerChData.append("$chIndex\n$chScrollPos")
        writerChData.flush()
        writerChData.close()
    }
    //

    //
    private fun TextColorNormalize() {
        // gives to whole text normal (un-bookmarked) color
        for (i in 0..paragraphCount) {
            paragraphArr[i].setTextColor(getColor(R.color._textColor))
            scrollPos = 0
        }
        //
    }

    // searching for any chapter inside 'Chapters' folder
    private fun ChNumsSearchAndSort() {

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        val chFolder = File("$appFolder/$chFolderName")

        for (file in chFolder.list()) {
            var cell = file
            chNumArr += cell
        }
        chNumArr.sort()
    }
    //

    // clears stored content inside 'ReadingBlock'
    private fun ClearReadingBlock() {

        chText.clear()
        paragraphArr.clear()
        duckArr.clear()
        duckLayout.clear()
        storedChDataArr.clear()
        scrollContainer.removeAllViews()

    }
    //

    private fun WelcomeViewBlock() {

        EraseChData()
        EraseBookMarkData()
        SaveData()

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
        val darkDuck = welcomeView.findViewById<ImageView>(R.id._darkDuck)
        val urlSearch = welcomeView.findViewById<EditText>(R.id._urlSearch)
        //
        // generates welcomeView
        centeredBlock.addView(welcomeView)
        //
        ranobeInput.visibility = View.GONE
        hideRanobeBtn.visibility = View.GONE

        // generates onStart() animations
        val anim = AnimationUtils.loadAnimation(this, R.anim.less_to_max)
        anim.startOffset = 100
        val anim1 = AnimationUtils.loadAnimation(this, R.anim.less_to_max)
        anim1.startOffset = 200
        val anim2 = AnimationUtils.loadAnimation(this, R.anim.less_to_max)
        anim2.startOffset = 250
        val anim3 = AnimationUtils.loadAnimation(this, R.anim.less_to_max)
        anim3.startOffset = 300
        val anim4 = AnimationUtils.loadAnimation(this, R.anim.duck_in)
        anim4.startOffset = 1000
        anim4.duration = 700
        //

        // starts animations from above
        nothingHere.startAnimation(anim)
        helpInfo.startAnimation(anim1)
        gitLinkBtn.startAnimation(anim2)
        addRanobeBtn.startAnimation(anim2)
        darkDuck.startAnimation(anim4)
        //

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

        // 'git link' button activity on click
        gitLinkBtn.setOnClickListener {
            gitLinkBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.less_bouncing))
            val url = "https://github.com/supchyan/quackreader"

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

                addRanobeBtn.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_45_to)

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

                addRanobeBtn.animation = AnimationUtils.loadAnimation(this, R.anim.rotate_45_from)

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
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        Thread {
                            //Do some Network Request
                            DownloadRanobe("${urlSearch.text}")
                            runOnUiThread {
                                //Update UI
                                if(cantConnect) {

                                    RedImpulse(urlSearch, ranobeInput)

                                    if(errPressCount > 0) coolToast("Неправильная ссылка")
                                    else if (errPressCount > -15) coolToast("Если возникли трудности с загрузкой глав, посетите GitHub проекта")
                                    else if (errPressCount > -30) coolToast("Классная анимация, мне тоже нравится")
                                    else coolToast("Достижение получено!\nКлац клац клац")

                                    errPressCount--

                                    cantConnect = false
                                }
                                if(successConnect) {
                                    GreenImpulse(urlSearch, ranobeInput)
                                    successConnect = false
                                }
                            }
                        }.start()
                    }
                    else -> return false
                }
                return true
            }
        })
        //
    }

    // this is for... when error in url line (see welcome block)
    private fun RedImpulse(view : EditText, animatedView : LinearLayout) {

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
    private fun GreenImpulse(view : EditText, animatedView : LinearLayout) {

        val backgroundDrawable = DrawableCompat.wrap(view.background).mutate()

        DrawableCompat.setTint(backgroundDrawable, Color.parseColor("#3CFF42"))
        view.setTextColor(getColor(R.color._darkerMainColor))
        view.setHintTextColor(getColor(R.color._darkerMainColor))
    }
    //

    @SuppressLint("RestrictedApi")
    private fun DownloadRanobe(url : String) {

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)
        val tempChapters = File(appFolder, tempChaptersFolderName)
        if (!tempChapters.exists()) {
            tempChapters.mkdirs()
        }

        // converting string to URL
        try {
            ranobeURL = URL(url)
        }
        catch (e: Exception) {
            cantConnect = true
            return
        }
        //

        var ch = 0.0
        var tempURL: URL
        // массив для ссылкок, потом в цикл запихнуть массив и в цикле прогонять запись в файлы
        var job = lifecycleScope.launch {
            for(i in 1 .. 5) {
                for (k in 0.. 5) {

                    Log.i("cock", "{$ranobeURL/v$i/c$k}")

                    if(ch == 0.0) ch.toInt()

                    tempURL = URL("$ranobeURL/v$i/c$k")

                    val urlConnection = tempURL.openConnection() as HttpURLConnection

                    try {
                        val text = urlConnection.inputStream.bufferedReader().readText()

                        if(!File(tempChapters, "v$i/c$k.txt").exists()) {
                            val readerData = File(tempChapters, "v$i/c$k.txt")
                            val writerData = FileWriter(readerData)
                            writerData.append(text)
                            writerData.flush()
                            writerData.close()
                        }
                        successConnect = true
                    }
                    catch (e : Exception) {
                    }
                    finally {
                        urlConnection.disconnect()
                    }
                }
            }
        }
    }
}