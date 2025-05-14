package com.example.quanlyamthuc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.quanlyamthuc.admin.BlockFragment
import com.example.quanlyamthuc.admin.HomeAdminActivity
import com.example.quanlyamthuc.admin.HomeFragment
import com.example.quanlyamthuc.admin.ProvinceFragment
import com.example.quanlyamthuc.admin.ReviewFragment
import com.example.quanlyamthuc.admin.UserFragment
import com.example.quanlyamthuc.fragment.DangBaiFragment
import com.example.quanlyamthuc.fragment.DishFragment
import com.example.quanlyamthuc.fragment.LienHeFragment
import com.example.quanlyamthuc.fragment.MonAnFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.example.quanlyamthuc.fragment.TrangChuFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

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
            .replace(R.id.fragment_container, TrangChuFragment())
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

                R.id.nav_lien_he -> replaceFragment(LienHeFragment())
                R.id.nav_dang_bai -> replaceFragment(DangBaiFragment())
                R.id.nav_nguoi_dung -> replaceFragment(ProfileFragment())

            }
            true
        }
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_trang_chu -> replaceFragment(TrangChuFragment())
                R.id.drawer_mon_an -> replaceFragment(MonAnFragment())
                R.id.drawer_dang_bai -> replaceFragment(DangBaiFragment())
                R.id.drawer_lien_he -> replaceFragment(LienHeFragment())
//                R.id.drawer_dang_nhap -> replaceFragment(ReviewFragment())
//
//                R.id.drawer_dang_ky -> replaceFragment(UserFragment())
                R.id.drawer_dang_xuat -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // nếu muốn thoát khỏi Activity hiện tại
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Xử lý menu drawer
//        navigationView.setNavigationItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_home -> replaceFragment(HomeFragment())
//                R.id.nav_province -> replaceFragment(ProvinceFragment())
//                R.id.nav_dish -> replaceFragment(DishFragment())
//                R.id.nav_block -> replaceFragment(BlockFragment())
//                R.id.nav_review -> replaceFragment(ReviewFragment())
//
//                R.id.nav_user -> replaceFragment(UserFragment())
//                R.id.nav_exit -> {
//                    val intent = Intent(this, LoginActivity::class.java)
//                    startActivity(intent)
//                    finish() // nếu muốn thoát khỏi Activity hiện tại
//                }
//            }
//            drawerLayout.closeDrawer(GravityCompat.START)
//            true
//        }

    }

    // Hàm thay fragment trong FrameLayout
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

