package io.github.cvrunmin.toolbox

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageItemInfo
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.support.v4.drawerLayout
import org.jetbrains.anko.*
import android.content.pm.PackageManager
import android.widget.ListView
import org.jetbrains.anko.sdk25.coroutines.onItemLongClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick


class LaunchingActivity : AppCompatActivity() {
    lateinit var drawerLayout : DrawerLayout
    lateinit var listApp : ListView
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
                    var toggle = ActionBarDrawerToggle(this@LaunchingActivity, this@drawerLayout, this, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
                    this@drawerLayout.setDrawerListener(toggle)
                    toggle.syncState()
                }.lparams(width = matchParent){
                    elevation = 4f
                    minimumHeight = R.attr.actionBarSize
                }
                setSupportActionBar(toolbar)
                listApp = listView {
                    id = R.id.list_view
                    isFastScrollEnabled = true

                    onItemLongClick { p0, p1, p2, p3 ->
                        val info = (adapter as AppAdapter).getItem(p2)
                        val launchIntent = packageManager.getLaunchIntentForPackage(info.packageName)
                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        }
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

        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        packages.sortBy({ packageManager.getApplicationLabel(it) as String })
        listApp.adapter = AppAdapter(this@LaunchingActivity, 0, packages)
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
}

class AppAdapter(context: Context?, resource: Int, objects: MutableList<ApplicationInfo>) : ArrayAdapter<ApplicationInfo>(context, resource, objects) {
    var items : MutableList<ApplicationInfo> = objects

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = getItem(position)
        return with(parent!!.context){
            verticalLayout {
                relativeLayout {
                    var icon = imageView {
                        id = R.id.imgIcon
                        setImageDrawable(item.loadIcon(context.packageManager))
                    }.lparams(width = dip(45), height = dip(45))
                    var appName = textView {
                        id = R.id.txtApp
                        textAppearance = android.R.style.TextAppearance_DeviceDefault_Large
                    }.lparams {
                        rightOf(icon)
                    }
                    var packageName = textView {
                        id = R.id.txtPackage
                        textAppearance = android.R.style.TextAppearance_DeviceDefault_Small
                    }.lparams{
                        rightOf(icon)
                        below(appName)
                    }
                    val expectedName = context.packageManager.getApplicationLabel(item)
                    if (expectedName.isNullOrBlank()){
                        packageName.visibility = View.GONE;
                        appName.text = item.packageName;
                    }else{
                        appName.text = expectedName;
                        packageName.text = item.packageName;
                    }
                }
            }
        }
    }

}