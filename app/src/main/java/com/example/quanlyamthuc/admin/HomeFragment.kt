package com.example.quanlyamthuc.admin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.quanlyamthuc.LoginActivity
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.databinding.FragmentHomeBinding
import com.example.quanlyamthuc.fragment.DishFragment
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")

        setupImageSlider()
        setupClickListeners()
        setupBarChart() // Khởi tạo biểu đồ trước khi load dữ liệu
        loadStatisticsData()
    }

    private fun setupImageSlider() {
        val imageList = listOf(
            SlideModel(R.drawable.bn11, ScaleTypes.FIT),
            SlideModel(R.drawable.bn222, ScaleTypes.FIT),
            SlideModel(R.drawable.banner3, ScaleTypes.FIT),
            SlideModel(R.drawable.banner4, ScaleTypes.FIT)
        )

        binding.imageSlider.apply {
            setImageList(imageList)
            setItemClickListener(object : ItemClickListener {
                override fun doubleClick(position: Int) {}
                override fun onItemSelected(position: Int) {
                    Toast.makeText(requireContext(), "Selected image $position", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            dishLayout.setOnClickListener { openFragment(DishFragment()) }
            provinceLayout.setOnClickListener { openFragment(ProvinceFragment()) }
            blockLayout.setOnClickListener { openFragment(BlockFragment()) }
            reviewLayout.setOnClickListener { openFragment(ReviewFragment()) }
            userLayout.setOnClickListener { openFragment(UserFragment()) }
            exitLayout.setOnClickListener {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
        }
    }

    private fun loadStatisticsData() {
        val statsMap = mutableMapOf<String, Int>()

        lifecycleScope.launch {
            try {
                // Lấy dữ liệu đồng thời từ nhiều nguồn
                val (userCount, dishCount, provinceCount, reviewCount, blockCount) = awaitAll(
                    async { getFirestoreCount("nguoidung") },
                    async { getRealtimeCount(database.getReference("10/data")) }, // Món ăn
                    async { getRealtimeCount(database.getReference("14/data")) }, // Tỉnh thành
                    async { getRealtimeCount(database.getReference("5/data")) },  // Đánh giá
                    async { getRealtimeCount(database.getReference("2/data")) }   // Bài đăng
                )

                statsMap.apply {
                    put("Người dùng", userCount)
                    put("Món ăn", dishCount)
                    put("Tỉnh", provinceCount)
                    put("Đánh giá", reviewCount)
                    put("Bài đăng", blockCount)
                }

                withContext(Dispatchers.Main) {
                    updateBarChart(statsMap)
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading stats", e)
                showErrorChart()
            }
        }
    }

    private suspend fun getFirestoreCount(collection: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                firestore.collection(collection).get().await().size()
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching $collection", e)
                0
            }
        }
    }

    private suspend fun getRealtimeCount(ref: DatabaseReference): Int {
        return withContext(Dispatchers.IO) {
            try {
                ref.get().await().children.count()
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching ${ref.path}", e)
                0
            }
        }
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.text = "."
            description.textSize = 5f
            setNoDataText("Đang tải dữ liệu...")
            setNoDataTextColor(Color.GRAY)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisLeft.granularity = 1f
            axisRight.isEnabled = false
            legend.isEnabled = true

            legend.isWordWrapEnabled = true
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.yEntrySpace = 10f            // 🔸 Tăng khoảng cách giữa các dòng
            legend.formToTextSpace = 10f        // 🔸 Tăng khoảng cách giữa biểu tượng và text
            legend.textSize = 12f               // 🔸 Tăng cỡ chữ cho dễ nhìn

            setExtraBottomOffset(20f)
        }
    }

    private fun updateBarChart(statsMap: Map<String, Int>) {
        if (statsMap.values.all { it == 0 }) {
            showErrorChart()
            return
        }

        val labels = statsMap.keys.toList()
        val values = statsMap.values.toList()
        val colors = listOf(Color.GREEN, Color.YELLOW, Color.RED, Color.BLUE, Color.CYAN) // hoặc dùng ColorTemplate

        val dataSets = mutableListOf<IBarDataSet>()

        for (i in values.indices) {
            val entry = BarEntry(i.toFloat(), values[i].toFloat())
            val dataSet = BarDataSet(listOf(entry), labels[i])
            dataSet.color = colors[i % colors.size]
            dataSet.valueTextColor = Color.BLACK
            dataSet.valueTextSize = 10f
            dataSets.add(dataSet)
        }

        binding.barChart.apply {
            data = BarData(dataSets)
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f
            xAxis.setCenterAxisLabels(false)
            xAxis.setDrawGridLines(false)
            setFitBars(true)
            animateY(800)
            invalidate()
        }
    }


    private fun showErrorChart() {
        binding.barChart.apply {
            clear()
            setNoDataText("Không có dữ liệu hoặc lỗi kết nối")
            setNoDataTextColor(Color.RED)
        }
        Toast.makeText(requireContext(), "Lỗi khi tải dữ liệu thống kê", Toast.LENGTH_SHORT).show()
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)
            .addToBackStack(null)
            .commit()
    }
}

//
//class HomeFragment : Fragment() {
//    private lateinit var binding: FragmentHomeBinding
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//         binding = FragmentHomeBinding.inflate(inflater, container, false)
//        return binding.root
//
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val imageList = ArrayList<SlideModel>()
//        imageList.add(SlideModel(R.drawable.bn11, ScaleTypes.FIT))
//        imageList.add(SlideModel(R.drawable.bn222, ScaleTypes.FIT))
//        imageList.add(SlideModel(R.drawable.banner3, ScaleTypes.FIT))
//        imageList.add(SlideModel(R.drawable.banner4, ScaleTypes.FIT))
//
//        val imageSlider = binding.imageSlider
//        imageSlider.setImageList(imageList)
//        imageSlider.setImageList(imageList, ScaleTypes.FIT)
//
//        imageSlider.setItemClickListener(object :ItemClickListener{
//            override fun doubleClick(position: Int) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onItemSelected(position: Int) {
//                val itemPos = imageList[position]
//                val itemMess = "Select image $position"
//                Toast.makeText(requireContext(), itemMess, Toast.LENGTH_SHORT).show()
//            }
//        })
//        // Bắt sự kiện click vào các layout chức năng
//        binding.dishLayout.setOnClickListener { openFragment(DishFragment()) }
//        binding.provinceLayout.setOnClickListener { openFragment(ProvinceFragment()) }
//        binding.blockLayout.setOnClickListener { openFragment(BlockFragment()) }
//        binding.reviewLayout.setOnClickListener { openFragment(ReviewFragment()) }
//        binding.userLayout.setOnClickListener { openFragment(UserFragment()) }
//        binding.exitLayout.setOnClickListener {
//            val intent = Intent(requireContext(), LoginActivity::class.java)
//            startActivity(intent)
//            requireActivity().finish()
//        }
//    }
//
//    private fun openFragment(fragment: Fragment) {
//        parentFragmentManager.beginTransaction()
//            .replace(R.id.main_content, fragment)
//            .addToBackStack(null)
//            .commit()
//    }
//}
//
