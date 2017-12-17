package io.github.cvrunmin.toolbox

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.RenderScript
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.drawerLayout
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.regex.Pattern
import kotlin.experimental.inv


class EditActivity : AppCompatActivity() {
    lateinit var drawerLayout : DrawerLayout
    var map : Bitmap? = null
    lateinit var layoutHandle : LinearLayout
    lateinit var uri : Uri
    lateinit var imageView : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        drawerLayout = drawerLayout {
            id = R.id.drawer_layout
            fitsSystemWindows = true
            var linearLayout = verticalLayout {
                var toolbar = toolbar {
                    id = R.id.toolbar
                    setTheme(R.style.ThemeOverlay_AppCompat_ActionBar)
                    popupTheme = R.style.ThemeOverlay_AppCompat_Light
                    var toggle = ActionBarDrawerToggle(this@EditActivity, this@drawerLayout, this, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
                    this@drawerLayout.setDrawerListener(toggle)
                    toggle.syncState()
                }.lparams(width = matchParent){
                    elevation = 4f
                    minimumHeight = R.attr.actionBarSize
                }
                setSupportActionBar(toolbar)
                relativeLayout {
                    imageView = imageView (R.drawable.ic_broken_image) {
                        scaleType = ImageView.ScaleType.FIT_XY
                        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    }.lparams(width = matchParent, height = dip(400))
                    horizontalScrollView {
                        layoutHandle = linearLayout {
                            visibility = GONE
                            imageButton(R.drawable.ic_grayscale) {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                onClick {
                                    modifyImageByMatrix(ColorMatrix(floatArrayOf(
                                            0.11f, 0.59f, 0.3f, 0f,0f,
                                            0.11f, 0.59f, 0.3f, 0f,0f,
                                            0.11f, 0.59f, 0.3f, 0f,0f,
                                            0f,0f,0f, 1f,0f
                                    )))
                                }
                                adjustViewBounds = true
                            }.lparams(width = dip(60), height = dip(60)){
                                setMargins(dip(5),dip(-5),dip(5), 0)
                                padding = 0
                            }
                            imageButton(R.drawable.ic_monochrome) {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                onClick {
                                    var dialog = SeekBarDialogFragment.newInstance(this@EditActivity, 0, 255, 128, R.string.DialogPickNoForThreshold, R.string.DialogThreshold, R.string.DialogThreshold, R.string.DialogThreshold)
                                    dialog.eventOkListener = (object : SeekBarDialogFragment.OnDialogOkListener{
                                        override fun onDialogOk(finProgress: Int) {
                                            modifyImageRS { alloOld, alloNew, renderScript ->
                                                var script = ScriptC_threshold(renderScript)
                                                script.set_threshold(finProgress)
                                                script.forEach_makeThreshold(alloOld, alloNew)
                                                alloNew
                                            }
                                        }
                                    })
                                    dialog.show(fragmentManager, "seekThreshold")
                                }
                                adjustViewBounds = true
                            }.lparams(width = dip(60), height = dip(60)){
                                setMargins(dip(5),dip(-5),dip(5), 0)
                                padding = 0
                            }
                            imageButton(R.drawable.ic_shiftbin) {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                onClick {
                                    var dialog = SeekBarDialogFragment.newInstance(this@EditActivity, -23, 23, 0,R.string.DialogPickNoForShift,R.string.DialogShiftBinLeft, R.string.DialogShiftDoNothing,R.string.DialogShiftBinRight)
                                    dialog.eventOkListener = (object : SeekBarDialogFragment.OnDialogOkListener{
                                        override fun onDialogOk(finProgress: Int) {
                                            modifyImageRS { alloOld, alloNew, renderScript ->
                                                var script = ScriptC_bitshift(renderScript)
                                                script._offsets = finProgress
                                                script.forEach_shiftBits(alloOld, alloNew)
                                                alloNew
                                            }
                                        }
                                    })
                                    dialog.show(fragmentManager, "seekShiftBin")
                                }
                                adjustViewBounds = true
                            }.lparams(width = dip(60), height = dip(60)){
                                setMargins(dip(5),dip(-5),dip(5), 0)
                                padding = 0
                            }
                            imageButton(R.drawable.ic_shifthex) {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                onClick {
                                    var dialog = SeekBarDialogFragment.newInstance(this@EditActivity, -5, 5, 0,R.string.DialogPickNoForShift,R.string.DialogShiftHexLeft, R.string.DialogShiftDoNothing,R.string.DialogShiftHexRight)
                                    dialog.eventOkListener = (object : SeekBarDialogFragment.OnDialogOkListener{
                                        override fun onDialogOk(finProgress: Int) {
                                            modifyImageRS { alloOld, alloNew, renderScript ->
                                                var script = ScriptC_bitshift(renderScript)
                                                script._offsets = finProgress * 4
                                                script.forEach_shiftBits(alloOld, alloNew)
                                                alloNew
                                            }
                                        }
                                    })
                                    dialog.show(fragmentManager, "seekShiftHex")
                                }
                                adjustViewBounds = true
                            }.lparams(width = dip(60), height = dip(60)){
                                setMargins(dip(5),dip(-5),dip(5), 0)
                                padding = 0
                            }
                            imageButton(R.drawable.ic_invert) {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                onClick {
                                    modifyImageByMatrix(ColorMatrix(floatArrayOf(
                                            -1f, 0f, 0f, 0f,255f,
                                            0f, -1f, 0f, 0f,255f,
                                            0f, 0f, -1f, 0f,255f,
                                            0f,0f,0f, 1f,0f
                                    )))
                                }
                            }.lparams(width = dip(60), height = dip(60)){
                                setMargins(dip(5),dip(-5),dip(5), 0)
                                padding = 0
                            }
                            imageButton(R.drawable.ic_rgbmono) {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                onClick {
                                    var dialog = SeekBarDialogFragment.newInstance(this@EditActivity, 0, 255, 105, R.string.DialogPickNoForThreshold, R.string.DialogThreshold, R.string.DialogThreshold, R.string.DialogThreshold)
                                    dialog.eventOkListener = (object : SeekBarDialogFragment.OnDialogOkListener{
                                        override fun onDialogOk(finProgress: Int) {
                                            modifyImageRS { alloOld, alloNew, renderScript ->
                                                var script = ScriptC_rgbthreshold(renderScript)
                                                script._threshold = finProgress
                                                script.forEach_makeThreshold(alloOld, alloNew)
                                                alloNew
                                            }
                                        }
                                    })
                                    dialog.show(fragmentManager, "seekRGBThreshold")
                                }
                                adjustViewBounds = true
                            }.lparams(width = dip(60), height = dip(60)){
                                setMargins(dip(5),dip(-5),dip(5), 0)
                                padding = 0
                            }
                            imageButton(R.drawable.ic_8bit) {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                onClick {
                                    modifyImageRS { alloOld, alloNew, renderScript ->
                                        var script = ScriptC_bitgroup(renderScript)
                                        script.forEach_groupBits(alloOld, alloNew)
                                        alloNew
                                    }
                                }
                                adjustViewBounds = true
                            }.lparams(width = dip(60), height = dip(60)){
                                setMargins(dip(5),dip(-5),dip(5), 0)
                                padding = 0
                            }
                        }.lparams (width = matchParent, height = matchParent){
                            padding = dip(10)
                        }
                    }.lparams(width = matchParent, height = dip(80)) {
                        alignParentBottom()
                    }
                }
            }.lparams(width = matchParent, height = matchParent)
            navigationView {
                id = R.id.nav_view
                inflateMenu(R.menu.menuintegrate)

                setNavigationItemSelectedListener({ item: MenuItem ->
                    val id = item.itemId
                    var intent: Intent

                    when(id){
                        R.id.nav_encrypt -> intent = intentFor<EncryptActivity>().clearTask().newTask()
                    //R.id.nav_namegen -> intent = intentFor<MonitorActivity>().clearTask().newTask()
                        R.id.nav_imgedit -> intent = intentFor<EditActivity>().clearTask().newTask()
                        R.id.nav_launch -> intent = intentFor<LaunchingActivity>().clearTask().newTask()
                        else -> return@setNavigationItemSelectedListener false
                    }

                    this@drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(intent)
                    true
                })
            }.lparams(height = matchParent){
                gravity = Gravity.START
                fitsSystemWindows = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    override fun onBackPressed() {
        val drawer = drawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId){
            android.R.id.home -> {
                val drawer = drawerLayout
                drawer.openDrawer(GravityCompat.START)
                return true
            }
            R.id.select->{
                var getIntent = Intent(Intent.ACTION_GET_CONTENT)
                getIntent.type = "image/*"

                var chooserIntent = Intent.createChooser(getIntent, getText(R.string.PickImage))
                startActivityForResult(chooserIntent, 2071)
                return true
            }
            R.id.reset -> {
                if(uri == null) return false
                map = android.provider.MediaStore.Images.Media.getBitmap(contentResolver,uri)
                imageView.setImageBitmap(map)
                return true
            }
            R.id.save -> saveImage()
            R.id.saveAsCopy -> saveImage(true)
            R.id.flipH -> {
                var matrix = Matrix()
                matrix.preScale(-1f,1f)
                var bm = Bitmap.createBitmap(map,0,0,map!!.width, map!!.height, matrix, true)
                map!!.recycle()
                map = bm
                imageView.setImageBitmap(map)
                return true
            }
            R.id.flipV -> {
                var matrix = Matrix()
                matrix.preScale(1f,-1f)
                var bm = Bitmap.createBitmap(map,0,0,map!!.width, map!!.height, matrix, true)
                map!!.recycle()
                map = bm
                imageView.setImageBitmap(map)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    var rotation = 0
    var flips = booleanArrayOf(false,false)
    fun setImage(url : Uri){
        uri = url
        try{
            var exif = ExifInterface(getRealPathFromUri(uri))
            var ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            rotation = 0
            flips.fill(false, 0, 1)
            when(ori){
                ExifInterface.ORIENTATION_NORMAL -> rotation = 0
                ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flips[0] = true
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> flips[1] = true
                ExifInterface.ORIENTATION_TRANSPOSE -> {rotation = 270; flips[0] = true}
                ExifInterface.ORIENTATION_TRANSVERSE -> {rotation = 90; flips[0] = true}
            }
        }catch (e : Exception){}
        imageView.setImageURI(uri)
        map = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.rotation = rotation.toFloat()
        if(flips.any { flag->flag }){
            imageView.scaleType = ImageView.ScaleType.MATRIX
            val matrix = Matrix()
            matrix.postScale(if(flips[0]) -1f else 1f,if(flips[1]) -1f else 1f)
            imageView.imageMatrix = matrix
        }
        layoutHandle.visibility = VISIBLE
    }

    fun createAgain(){
        imageView.buildDrawingCache(false)
        map = imageView.getDrawingCache(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        map?.recycle()
    }
    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 2071)
            if (resultCode == Activity.RESULT_OK)
            {
                setImage(data?.data!!)
            }
    }
    fun saveImage(preventOverride : Boolean = false):Boolean{
        var folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "image_editor")
        if(!folder.exists()) folder.mkdir()
        var filename = getRealPathFromUri(uri)
        filename = filename.substring(filename.lastIndexOf(File.separatorChar))
        var file = File(folder, filename)
        var count = 0
        if(preventOverride)
            while (file.exists()){
                count++
                file = File(folder, "${filename.substring(0, filename.lastIndexOf('.'))}($count)${filename.substring(filename.lastIndexOf('.'))}")
            }
        if(!file.exists()) file.createNewFile()
        var fullname = file.absolutePath
        try {
            var format = if(Pattern.matches("\\w+(.jpg|.jpeg|.jpe|.jfif)",filename.removePrefix("/"))) Bitmap.CompressFormat.JPEG else if(filename.endsWith(".png")) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.WEBP
            FileOutputStream(fullname).use {
                map!!.compress(format, 100, it)
            }
            if(format == Bitmap.CompressFormat.JPEG){
                var exif = ExifInterface(fullname)
                var attr = ExifInterface.ORIENTATION_UNDEFINED
                when{
                    (rotation == 0) and flips.none { flag->flag } -> attr = ExifInterface.ORIENTATION_NORMAL
                    (rotation == 0) and flips[0] -> attr = ExifInterface.ORIENTATION_FLIP_HORIZONTAL
                    (rotation == 0) and flips[1] -> attr = ExifInterface.ORIENTATION_FLIP_VERTICAL
                    (rotation == 90) and flips[0] -> attr = ExifInterface.ORIENTATION_TRANSVERSE
                    (rotation == 270) and flips[0] -> attr = ExifInterface.ORIENTATION_TRANSPOSE
                    (rotation == 270) and flips.none { flag->flag } -> attr = ExifInterface.ORIENTATION_ROTATE_270
                    (rotation == 180) and flips.none { flag->flag } -> attr = ExifInterface.ORIENTATION_ROTATE_180
                    (rotation == 90) and flips.none { flag->flag } -> attr = ExifInterface.ORIENTATION_ROTATE_90
                }
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, attr.toString())
                exif.saveAttributes()
            }
            var mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.setData(Uri.fromFile(file))
            sendBroadcast(mediaScanIntent)
            toast("Saved as " + fullname.substring(fullname.indexOf(Environment.DIRECTORY_PICTURES)))
            return true
        }catch (e : Exception){
            Log.w("Toolbox", e)
            toast("Unable to save image: ${e.message}")
            return false
        }
    }

    fun modifyImageRS(procedure : ((Allocation, Allocation, RenderScript) -> Allocation)){
        if(procedure == null)return
        if(map == null) createAgain()
        layoutHandle.visibility = GONE
        async {
            try {
                var bm = Bitmap.createBitmap(map!!.width, map!!.height, map!!.config)
                var rs = RenderScript.create(this@EditActivity)

                val aO = Allocation.createFromBitmap(rs, map)
                val aN = Allocation.createTyped(rs, aO.getType())
                procedure.invoke(aO, aN, rs).copyTo(bm)
                map!!.recycle()
                rs.destroy()
                aO.destroy()
                aN.destroy()
                map = bm
            }catch (e : Exception){

            }
        }.invokeOnCompletion {
            runOnUiThread {
                imageView.setImageBitmap(map)
                layoutHandle.visibility = VISIBLE
            }
        }
    }

    fun modifyImageByMatrix(matrix : ColorMatrix){
        var bm = Bitmap.createBitmap(map!!.width, map!!.height, map!!.config)
        var c = Canvas(bm)
        var p = Paint()
        p.setColorFilter(ColorMatrixColorFilter(matrix))
        c.drawBitmap(map,0f,0f,p)
        map!!.recycle()
        map = bm
        imageView.setImageBitmap(map)
    }

}