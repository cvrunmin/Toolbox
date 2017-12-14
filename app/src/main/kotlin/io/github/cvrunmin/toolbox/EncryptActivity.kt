package io.github.cvrunmin.toolbox

import android.content.ComponentName
import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.nfc.FormatException
import android.os.Bundle
import android.preference.EditTextPreference
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputType
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.view.Gravity
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Switch
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener
import org.jetbrains.anko.support.v4.drawerLayout
import io.github.cvrunmin.crmkjk.*
import java.util.*

class EncryptActivity : AppCompatActivity(){
    lateinit var drawerLayout : DrawerLayout
    val encodeTypes = arrayOf("base64", "crmkjk", "unicode")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drawerLayout = drawerLayout {
            id = R.id.drawer_layout
            fitsSystemWindows = true

            //android:fitsSystemWindows = true //not support attribute
            //tools:openDrawer = start //not support attribute
            var linearLayout = verticalLayout {
                var toolbar = toolbar {
                    id = R.id.toolbar
                    setTheme(R.style.ThemeOverlay_AppCompat_Dark_ActionBar)
                    popupTheme = R.style.ThemeOverlay_AppCompat_Light
                    var toggle = ActionBarDrawerToggle(this@EncryptActivity, this@drawerLayout, this, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
                    this@drawerLayout.setDrawerListener(toggle)
                    toggle.syncState()
                }.lparams(width = matchParent){
                    elevation = 4f
                    minimumHeight = R.attr.actionBarSize
                }
                setSupportActionBar(toolbar)
                relativeLayout {
                    var t1 = textView {
                        id = R.id.textView1
                        textAppearance = android.R.style.TextAppearance_DeviceDefault_Medium
                        text = "Original Text"
                    }.lparams{
                        //topMargin = dip(60)
                        alignWithParent = true
                        alignParentLeft()
                        alignParentTop()
                    }
                    var decodeText = editText {
                        id = R.id.editTextO
                        inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    }.lparams(height = dip(100)){
                        alignWithParent = true
                        alignParentLeft()
                        alignParentRight()
                        below(t1)
                    }

                    var butEncode = button {
                        id = R.id.butEncode
                        text = "Encode"
                    }.lparams{
                        alignWithParent = true
                        alignParentLeft()
                        below(decodeText)
                    }
                    var encodeType = spinner {
                        id = R.id.spinnerET
                        adapter = ArrayAdapter<String>(this@EncryptActivity, android.R.layout.simple_spinner_dropdown_item, encodeTypes)
                    }.lparams{
                        alignWithParent = true
                        alignParentRight()
                        below(decodeText)
                    }
                    lateinit var switchFU : Switch
                    lateinit var switchETE : Switch
                    var extraArg = scrollView {
                        visibility = GONE

                        id = R.id.list_view
                        var vl = verticalLayout {
                            switchFU = switch {
                                id = R.id.switchFU
                                text = "Force Unicode Mode"
                            }.lparams{
                                padding = dip(10)
                            }
                            switchETE = switch {
                                id = R.id.switchETE
                                text = "Encode the key"
                            }.lparams {
                                padding = dip(10)
                            }
                        }.lparams(width = matchParent, height = matchParent){
                            minimumWidth = 25
                            minimumHeight = 25
                        }

                    }.lparams{
                        minimumWidth = 25
                        minimumHeight = 25
                        below(butEncode)
                    }
                    encodeType.onItemSelectedListener{
                        onItemSelected { adapterView, view, i, l ->
                            if (i == 1) {
                                extraArg.visibility = VISIBLE
                            } else extraArg.visibility = GONE
                        }
                    }
                    var butDecode = button {
                        id = R.id.butDecode
                        text = "Decode"
                    }.lparams{
                        alignWithParent = true
                        alignParentRight()
                        below(extraArg)
                    }

                    var t2 = textView {
                        id = R.id.textView2
                        textAppearance = android.R.style.TextAppearance_DeviceDefault_Medium
                        text = "Encoded Text"
                    }.lparams{
                        below(butDecode)
                    }
                    var encodeText = editText {
                        id = R.id.editTextE
                        inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    }.lparams{
                        width = matchParent
                        height = dip(100)
                        below(t2)
                    }
                    butEncode.onClick {
                        when (encodeTypes[encodeType.selectedItemPosition]) {
                            "crmkjk" -> encodeText.setText(encode(decodeText.text.toString(), (if (switchFU.isChecked) UNICODE else 0) or if (switchETE.isChecked) ENCODE_TEXT_BASE64_ENCODE else 0))
                            "base64" -> {
                                if (!decodeText.text.isNullOrBlank())
                                    encodeText.setText(Base64.encodeToString((decodeText.text.toString()).toByteArray(), Base64.DEFAULT))
                            }
                            "unicode" -> encodeText.setText(decodeText.text.toString().escapeToUnicode())
                            else -> {
                            }
                        }
                    }

                    butDecode.onClick {
                        when(encodeTypes[encodeType.selectedItemPosition])
                        {
                            "crmkjk" ->
                            try
                            {
                                decodeText.setText(decode(encodeText.text.toString()))
                            }
                            catch (e : UnexpectedCRMKJKEncodeException)
                            {
                                toast("ERROR: unexpected crmkjk encode")
                            }
                            "base64"->
                            if (!encodeText.text.isNullOrBlank())
                            try
                            {
                                decodeText.setText(Base64.decode(encodeText.text.toString(), Base64.DEFAULT).toString(Charsets.UTF_8))
                            }
                            catch (e : IllegalArgumentException)
                            {
                                toast("ERROR: unexpected base64 encode")
                            }
                            "unicode"->
                            if (!encodeText.text.isNullOrBlank())
                            try
                            {
                                decodeText.setText(encodeText.text.toString().trapToUnicode())
                            }
                            catch (e : IllegalArgumentException)
                            {
                                toast("ERROR: unexpected unicode")
                            }
                        }
                    }

                    var butED = button {
                        id = R.id.butEDImgs
                        text = "Encode/Decode images"

                        onClick {
                            alert {
                                positiveButton("Encrypt") {
                                    buildChooser(2333)
                                }

                                negativeButton("Decrypt") {
                                    buildChooser(6666)
                                }
                            }.show()
                        }
                    }.lparams{
                        below(encodeText)
                    }
                }
            }.lparams(width = matchParent, height = matchParent)
            navigationView {
                id = R.id.nav_view
                inflateMenu(R.menu.menuintegrate)
                //android:fitsSystemWindows = true //not support attribute
                //app:headerLayout = @layout/nav_header_main //not support attribute
                //app:menu = @menu/menuintegrate //not support attribute

                setNavigationItemSelectedListener({ item: MenuItem ->
                    val id = item.itemId
                    var intent : Intent = Intent()

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

    private fun buildChooser(code : Int) {
        var smfi = Intent(Intent.ACTION_OPEN_DOCUMENT)
        smfi.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        smfi.type = "*/*"
        smfi.addCategory(Intent.CATEGORY_OPENABLE)
        var selectFolderIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        var a = packageManager.queryIntentActivities(smfi, 0)
        var a1 = packageManager.queryIntentActivities(selectFolderIntent, 0)

        var usefolderone = false
        if (a.size == 0) usefolderone = true
        if (usefolderone && a1.size == 0) {
            alert("No selectors found").show()
            return
        }

        var chooserIntent: Intent
        chooserIntent = Intent(Intent.ACTION_CHOOSER)
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Open as...")
        var forEditing = SpannableString(" (selecting multiple files)")
        forEditing.setSpan(ForegroundColorSpan(Color.BLACK), 0, forEditing.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        var extraIntents = arrayOfNulls<Intent>(a.size)
        for (i in a.indices) {
            var ri = a[i]
            var packageName = ri.activityInfo.packageName
            var intent = Intent()
            intent.component = ComponentName(packageName, ri.activityInfo.name)
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            var label = TextUtils.concat(ri.loadLabel(packageManager), forEditing.subSequence(0, forEditing.length))
            extraIntents[i] = LabeledIntent(intent, packageName, label, ri.icon)
        }

        forEditing = SpannableString(" (selecting folder)")
        forEditing.setSpan(ForegroundColorSpan(Color.BLACK), 0, forEditing.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        var extraIntents1 = arrayOfNulls<Intent>(a1.size)
        for (i in a1.indices) {
            var ri = a1[i]
            var packageName = ri.activityInfo.packageName
            var intent = Intent()
            intent.component = ComponentName(packageName, ri.activityInfo.name)
            intent.action = Intent.ACTION_OPEN_DOCUMENT_TREE
            var label = TextUtils.concat(ri.loadLabel(packageManager), forEditing.subSequence(0, forEditing.length))
            extraIntents1[i] = LabeledIntent(intent, packageName, label, ri.icon)
        }

        var final: ArrayList<Intent>
        if (!usefolderone) {
            final = extraIntents.plus(extraIntents1).toMutableList() as ArrayList<Intent>
        } else {
            final = extraIntents1.toMutableList() as ArrayList<Intent>
        }
        if (final.size != 0) {
            chooserIntent.putExtra(Intent.EXTRA_INTENT, final[0])
        }
        if (final.size > 1) {
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, final.drop(1).toTypedArray())
        }
        startActivityForResult(chooserIntent, code)
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
        if(item?.itemId == android.R.id.home) {
            val drawer = drawerLayout
            drawer.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == -1){
                startService(intentFor<CryptImagesService>("dir" to data?.data, "files" to data?.clipData, "decrypt" to (requestCode == 6666)))
        }
    }
}