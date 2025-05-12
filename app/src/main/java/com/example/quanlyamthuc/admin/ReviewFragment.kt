package com.example.quanlyamthuc.admin

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.ReviewAdapter
import com.example.quanlyamthuc.model.ReviewModel
import com.google.firebase.database.*

class ReviewFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private var reviewList = mutableListOf<ReviewModel>()
    private lateinit var txtTongSoBaiDang: TextView
    private lateinit var containerThongKeMon: LinearLayout
    private lateinit var txtXemThemThongKe: TextView
    private lateinit var searchView: EditText
    private val tenMonAnMap = mutableMapOf<String, String>()
    private val soDongHienThiToiDa = 5
    private var isThongKeMoRong = false
    private var thongKeMonList = listOf<Pair<String, Int>>()
    private lateinit var txtXemThemTren: TextView
    private lateinit var txtXemThemDuoi: TextView
    private lateinit var scrollViewThongKe: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewDanhGia)
        txtTongSoBaiDang = view.findViewById(R.id.txtTongSoBaiDang)
        containerThongKeMon = view.findViewById(R.id.containerThongKeMon)
      //  txtXemThemThongKe = view.findViewById(R.id.txtXemThemThongKe)
        searchView = view.findViewById(R.id.searchView)
        txtXemThemTren = view.findViewById(R.id.txtXemThemTren)
        txtXemThemDuoi = view.findViewById(R.id.txtXemThemDuoi)
        scrollViewThongKe = view.findViewById(R.id.scrollViewThongKe)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        reviewAdapter = ReviewAdapter(requireContext(), reviewList) { review ->
            AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn xóa đánh giá này không?")
                .setPositiveButton("Xóa") { _, _ -> xoaDanhGia(review) }
                .setNegativeButton("Hủy", null)
                .show()
        }
        recyclerView.adapter = reviewAdapter


        // Thiết lập sự kiện click
        val clickListener = View.OnClickListener {
            isThongKeMoRong = !isThongKeMoRong
            capNhatGiaoDienThongKe()
            if (isThongKeMoRong) {
                scrollViewThongKe.post {
                    scrollViewThongKe.smoothScrollTo(0, 0)
                }
            }
        }
        txtXemThemTren.setOnClickListener(clickListener)
        txtXemThemDuoi.setOnClickListener(clickListener)
        searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val filtered = reviewList.filter {
                    it.noi_dung?.contains(s.toString(), ignoreCase = true) == true
                }
                reviewAdapter.updateList(filtered)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        loadMonAn()
    }
    private fun loadMonAn() {
        val foodRef = FirebaseDatabase
            .getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("10/data")
        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tenMonAnMap.clear()
                for (foodSnapshot in snapshot.children) {
                    val idma = foodSnapshot.child("idma").getValue(String::class.java) ?: continue
                    val tenma = foodSnapshot.child("tenma").getValue(String::class.java) ?: "Không rõ"
                    tenMonAnMap[idma] = tenma
                }
                reviewAdapter.setTenMonAnMap(tenMonAnMap)
                docDanhSachDanhGia()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun docDanhSachDanhGia() {
        val ref = FirebaseDatabase
            .getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("5/data")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reviewList.clear()
                for (child in snapshot.children) {
                    val review = child.getValue(ReviewModel::class.java)
                    review?.key = child.key
                    review?.let { reviewList.add(it) }
                }
                reviewAdapter.updateList(reviewList)
                txtTongSoBaiDang.text = "Tổng số đánh giá: ${reviewList.size}"
                hienThiThongKeTheoMon(reviewList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi đọc dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun hienThiThongKeTheoMon(danhSach: List<ReviewModel>) {
        val thongKeMap = mutableMapOf<String, Int>()
        for (review in danhSach) {
            val tenMon = tenMonAnMap[review.idma] ?: "Không rõ"
            thongKeMap[tenMon] = thongKeMap.getOrDefault(tenMon, 0) + 1
        }

        thongKeMonList = thongKeMap.entries
            .map { it.key to it.value }
            .sortedByDescending { it.second }

        capNhatGiaoDienThongKe()
    }

    private fun capNhatGiaoDienThongKe() {
        containerThongKeMon.removeAllViews()

        val soLuongHienThi = if (isThongKeMoRong) thongKeMonList.size else minOf(thongKeMonList.size, soDongHienThiToiDa)

        for (i in 0 until soLuongHienThi) {
            val pair = thongKeMonList[i]
            val textView = TextView(requireContext()).apply {
                text = "- ${pair.first}: ${pair.second} bài"
                setTextColor(Color.BLACK)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 4.dpToPx())
                }
            }
            containerThongKeMon.addView(textView)
        }

        if (thongKeMonList.size > soDongHienThiToiDa) {
            val text = if (isThongKeMoRong) "Thu gọn" else "Xem thêm..."

            // Chỉ hiển thị 1 nút ở vị trí phù hợp
            if (isThongKeMoRong) {
                // Khi đang mở rộng: hiển thị nút "Thu gọn" ở trên
                txtXemThemTren.text = text
                txtXemThemTren.visibility = View.VISIBLE
                txtXemThemDuoi.visibility = View.GONE
            } else {
                // Khi đang thu gọn: hiển thị nút "Xem thêm" ở dưới
                txtXemThemDuoi.text = text
                txtXemThemTren.visibility = View.GONE
                txtXemThemDuoi.visibility = View.VISIBLE
            }

            // Tự động scroll lên đầu khi mở rộng
            if (isThongKeMoRong) {
                scrollViewThongKe.post {
                    scrollViewThongKe.smoothScrollTo(0, 0)
                }
            }
        } else {
            // Nếu ít mục thì ẩn cả 2 nút
            txtXemThemTren.visibility = View.GONE
            txtXemThemDuoi.visibility = View.GONE
        }
    }
    // Extension function để convert dp to px
    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()



//    private fun hienThiThongKeTheoMon(danhSach: List<ReviewModel>) {
//        val thongKeMap = mutableMapOf<String, Int>()
//        for (review in danhSach) {
//            val tenMon = tenMonAnMap[review.idma] ?: "Không rõ"
//            thongKeMap[tenMon] = thongKeMap.getOrDefault(tenMon, 0) + 1
//        }
//
//        thongKeMonList = thongKeMap.entries
//            .map { it.key to it.value }
//            .sortedByDescending { it.second }
//
//        containerThongKeMon.removeAllViews()
//        thongKeMonList.forEachIndexed { index, pair ->
//            val textView = TextView(requireContext()).apply {
//                text = "- ${pair.first}: ${pair.second} bài"
//                setTextColor(Color.BLACK)
//                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
//                if (index >= soDongHienThiToiDa) visibility = View.GONE
//            }
//            containerThongKeMon.addView(textView)
//        }
//
//        if (thongKeMonList.size > soDongHienThiToiDa) {
//            txtXemThemThongKe.visibility = View.VISIBLE
//            txtXemThemThongKe.text = "Xem thêm..."
//            txtXemThemThongKe.setOnClickListener {
//                if (!isThongKeMoRong) {
//                    hienThiDialogThongKe()
//                }
//            }
//        } else {
//            txtXemThemThongKe.visibility = View.GONE
//        }
//    }

//    private fun hienThiDialogThongKe() {
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setTitle("Thống kê đánh giá theo món ăn")
//
//        val textView = TextView(requireContext()).apply {
//            text = thongKeMonList.joinToString("\n") { "- ${it.first}: ${it.second} bài" }
//            setPadding(16, 16, 16, 16)
//            movementMethod = ScrollingMovementMethod()
//        }
//
//        val scrollView = ScrollView(requireContext()).apply {
//            addView(textView)
//        }
//
//        builder.setView(scrollView)
//        builder.setPositiveButton("Đóng", null)
//        builder.show()
//    }

    private fun xoaDanhGia(review: ReviewModel) {
        val key = review.key ?: return
        val ref = FirebaseDatabase
            .getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("5/data")
        ref.removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Đã xóa đánh giá", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show()
            }
    }
}


//package com.example.quanlyamthuc.admin
//
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.util.TypedValue
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import android.widget.LinearLayout
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.quanlyamthuc.R
//import com.example.quanlyamthuc.adapter.ReviewAdapter
//import com.example.quanlyamthuc.model.ReviewModel
//import com.google.firebase.database.*
//import com.google.firebase.firestore.FirebaseFirestore
//
//class ReviewFragment : Fragment() {
//
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var reviewAdapter: ReviewAdapter
//    private var reviewList = mutableListOf<ReviewModel>()
//    private lateinit var txtTongSoBaiDang: TextView
//    private lateinit var containerThongKeMon: LinearLayout
//    private lateinit var txtXemThemThongKe: TextView
//    private lateinit var searchView: EditText
//    private val tenMonAnMap = mutableMapOf<String, String>()
//    private val tenNguoiDungMap = mutableMapOf<String, String>()
//    private val soDongHienThiToiDa = 5
//    private var isThongKeMoRong = false
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_review, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        recyclerView = view.findViewById(R.id.recyclerViewDanhGia)
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        txtTongSoBaiDang = view.findViewById(R.id.txtTongSoBaiDang)
//        containerThongKeMon = view.findViewById(R.id.containerThongKeMon)
//        txtXemThemThongKe = view.findViewById(R.id.txtXemThemThongKe)
//        searchView = view.findViewById(R.id.searchView)
//
//        reviewAdapter = ReviewAdapter(requireContext(), reviewList) { review ->
//            AlertDialog.Builder(requireContext())
//                .setTitle("Xác nhận")
//                .setMessage("Bạn có chắc chắn muốn xóa đánh giá này không?")
//                .setPositiveButton("Xóa") { _, _ -> xoaDanhGia(review) }
//                .setNegativeButton("Hủy", null)
//                .show()
//        }
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        recyclerView.adapter = reviewAdapter
//
//        searchView.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                val filtered = reviewList.filter {
//                    it.noi_dung?.contains(s.toString(), ignoreCase = true) == true
//                }
//                reviewAdapter.updateList(filtered)
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })
//        loadMonAn()
//
//    }
//
//    private fun loadMonAn() {
//        val foodRef = FirebaseDatabase.getInstance().getReference("10/data")
//        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                tenMonAnMap.clear()
//                for (foodSnapshot in snapshot.children) {
//                    val idma = foodSnapshot.child("idma").getValue(String::class.java) ?: continue
//                    val tenma = foodSnapshot.child("tenma").getValue(String::class.java) ?: "Không rõ"
//                    tenMonAnMap[idma] = tenma
//                }
//                reviewAdapter.setTenMonAnMap(tenMonAnMap)
//                docDanhSachDanhGia()
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
//    }
//
//    private fun docDanhSachDanhGia() {
//        val ref = FirebaseDatabase.getInstance().getReference("5/data")
//        ref.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                reviewList.clear()
//                for (child in snapshot.children) {
//                    val review = child.getValue(ReviewModel::class.java)
//                    review?.key = child.key
//                    review?.let { reviewList.add(it) }
//                }
//                reviewAdapter.updateList(reviewList)
//                txtTongSoBaiDang.text = "Tổng số đánh giá: ${reviewList.size}"
//                hienThiThongKeTheoMon(reviewList)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(requireContext(), "Lỗi đọc dữ liệu", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun hienThiThongKeTheoMon(danhSach: List<ReviewModel>) {
//        val thongKeMap = mutableMapOf<String, Int>()
//        for (review in danhSach) {
//            val tenMon = tenMonAnMap[review.idma] ?: "Không rõ"
//            thongKeMap[tenMon] = thongKeMap.getOrDefault(tenMon, 0) + 1
//        }
//        hienThiThongKeMonAn(thongKeMap)
//    }
//
//    private fun hienThiThongKeMonAn(thongKeMap: Map<String, Int>) {
//        containerThongKeMon.removeAllViews()
//        val danhSachMon = thongKeMap.entries.sortedByDescending { it.value }
//
//        danhSachMon.forEachIndexed { index, entry ->
//            val textView = TextView(requireContext())
//            textView.text = "- ${entry.key}: ${entry.value} bài"
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
//            if (index >= soDongHienThiToiDa && !isThongKeMoRong) {
//                textView.visibility = View.GONE
//            }
//            containerThongKeMon.addView(textView)
//        }
//
//        if (danhSachMon.size > soDongHienThiToiDa) {
//            txtXemThemThongKe.visibility = View.VISIBLE
//            txtXemThemThongKe.text = "Xem thêm..."
//
//            txtXemThemThongKe.setOnClickListener {
//                isThongKeMoRong = !isThongKeMoRong
//                for (i in soDongHienThiToiDa until containerThongKeMon.childCount) {
//                    val view = containerThongKeMon.getChildAt(i)
//                    view?.visibility = if (isThongKeMoRong) View.VISIBLE else View.GONE
//                }
//                txtXemThemThongKe.text = if (isThongKeMoRong) "Thu gọn" else "Xem thêm..."
//            }
//        } else {
//            txtXemThemThongKe.visibility = View.GONE
//        }
//    }
//
//    private fun xoaDanhGia(review: ReviewModel) {
//        val key = review.key ?: return
//        val ref = FirebaseDatabase.getInstance().getReference("5/data").child(key)
//        ref.removeValue()
//            .addOnSuccessListener {
//                Toast.makeText(requireContext(), "Đã xóa đánh giá", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener {
//                Toast.makeText(requireContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show()
//            }
//    }
//}
