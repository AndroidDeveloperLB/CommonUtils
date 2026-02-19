package com.lb.common_utils.sample

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lb.common_utils.BoundActivity
import com.lb.common_utils.sample.databinding.ActivityMainBinding

class MainActivity: BoundActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(0))
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            binding.appBarLayout.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        setSupportActionBar(binding.toolbar)
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.addSubMenu("More info").let { subMenu ->
                    subMenu.setIcon(android.R.drawable.ic_menu_info_details)
                    subMenu.item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    subMenu.add("Repository website").setOnMenuItemClickListener(
                        createUrlMenuItemClickListener("https://github.com/AndroidDeveloperLB/CommonUtils")
                    )
                    subMenu.add("All my repositories").setOnMenuItemClickListener(
                        createUrlMenuItemClickListener("https://github.com/AndroidDeveloperLB")
                    )
                    subMenu.add("All my apps").setOnMenuItemClickListener(
                        createUrlMenuItemClickListener("https://play.google.com/store/apps/developer?id=AndroidDeveloperLB")
                    )
                }
            }

            private fun createUrlMenuItemClickListener(url: String): MenuItem.OnMenuItemClickListener {
                return MenuItem.OnMenuItemClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    @Suppress("DEPRECATION")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    startActivity(intent)
                    true
                }
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return true
            }
        })
    }
}
