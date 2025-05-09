package com.example.quanlyamthuc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.quanlyamthuc.admin.HomeAdminActivity
import com.example.quanlyamthuc.fragment.MonAnFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.example.quanlyamthuc.fragment.TrangChuFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navigationView: NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.topAppBar)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        navigationView = findViewById(R.id.navigation_view)


    // Ánh xạ view
        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.topAppBar)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        navigationView = findViewById(R.id.navigation_view)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MonAnFragment())
            .commit()


        // Mở Navigation Drawer khi bấm icon menu
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Xử lý menu đáy
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_trang_chu -> replaceFragment(TrangChuFragment())
                R.id.nav_mon_an -> replaceFragment(MonAnFragment())

                R.id.nav_lien_he -> replaceFragment(MonAnFragment())
                R.id.nav_dang_bai -> replaceFragment(MonAnFragment())
                R.id.nav_nguoi_dung -> replaceFragment(MonAnFragment())

            }
            true
        }

        // Xử lý menu drawer
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_trang_chu -> replaceFragment(TrangChuFragment())
                R.id.drawer_mon_an -> replaceFragment(MonAnFragment())
                R.id.drawer_lien_he -> replaceFragment(MonAnFragment())
                R.id.drawer_dang_bai -> replaceFragment(MonAnFragment())
                R.id.drawer_dang_nhap -> replaceFragment(MonAnFragment()) // Thêm Fragment đăng nhập
                R.id.drawer_dang_ky -> replaceFragment(MonAnFragment())

            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    // Hàm thay fragment trong FrameLayout
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

