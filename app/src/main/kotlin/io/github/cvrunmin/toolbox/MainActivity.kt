package io.github.cvrunmin.toolbox

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import android.widget.LinearLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.support.v4.drawerLayout

class MainActivity : AppCompatActivity(){
    lateinit var drawerLayout : DrawerLayout
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
                    var toggle = ActionBarDrawerToggle(this@MainActivity, this@drawerLayout, this, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
                    this@drawerLayout.setDrawerListener(toggle)
                    toggle.syncState()
                }.lparams(width = matchParent){
                    elevation = 4f
                    minimumHeight = R.attr.actionBarSize
                }
                setSupportActionBar(toolbar)
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
