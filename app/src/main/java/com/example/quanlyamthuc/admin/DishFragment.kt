package com.example.quanlyamthuc.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.DishAdapter
import com.example.quanlyamthuc.databinding.FragmentDishBinding
import com.example.quanlyamthuc.model.DishModel
import com.example.quanlyamthuc.model.ProvinceModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.Normalizer


class DishFragment : Fragment() {
    private lateinit var binding: FragmentDishBinding
    private lateinit var monanRef: DatabaseReference
    private lateinit var dishAdapter: DishAdapter
    private val monAnList = mutableListOf<DishModel>()
    private var provinceIdtt: String? = null
    private lateinit var database: FirebaseDatabase
    private var searchQuery = ""
    private val fullList = mutableListOf<DishModel>() // Danh sách đầy đủ
    private val provinceCache = HashMap<String, String>() // Cache tên tỉnh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provinceIdtt = arguments?.getString("provinceIdtt")
        database = FirebaseDatabase.getInstance()
        val database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
        monanRef = database.getReference("10/data") // hoặc "monan/10/data"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        loadDishes()

        binding.fabAddDish.setOnClickListener {
            showDishDialog(null)
        }
        binding.btnRetry.setOnClickListener {
            loadDishes() // Load lại dữ liệu
        }
    }
    private fun updateTotalDishes(count: Int) {
        binding.txtTotalDishes.text = "Tổng: $count món"
    }
//    private fun updateTotalDishes(filteredCount: Int) {
//        val text = when {
//            searchQuery.isNotEmpty() -> "Đang hiển thị: $filteredCount/${fullList.size} món"
//            else -> "Tổng: ${fullList.size} món"
//        }
//        binding.txtTotalDishes.text = text
//    }
    private fun setupToolbar() {
        binding.toolbar.title = if (!provinceIdtt.isNullOrEmpty()) {
            "Món ăn theo tỉnh"
        } else {
            "Tất cả món ăn"
        }
    }
    // Cấu hình thanh tìm kiếm
    private fun setupSearch() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            private var lastText = ""
            private val handler = Handler(Looper.getMainLooper())
            private val runnable = Runnable {
                val currentText = binding.edtSearch.text.toString()
                if (currentText != lastText) {
                    lastText = currentText
                    searchQuery = lastText
                    filterDishes()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 300) // Delay 300ms
            }
        })
    }

    private fun removeAccent(s: String?): String {
        if (s.isNullOrEmpty()) return ""
        return Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
    }

        // Lọc dữ liệu theo tên món hoặc tỉnh
    private fun filterDishes() {
        Log.d("DishFragment", "Filter với từ khóa: '$searchQuery'")
        val searchText = removeAccent(searchQuery.trim())
        monAnList.clear()

        if (searchText.isEmpty()) {
            monAnList.addAll(fullList)
        } else {
            fullList.forEach { dish ->
                val dishName = removeAccent(dish.tenma?: "")
                val provinceName = removeAccent(getProvinceName(dish.idtt)) // Lấy tên tỉnh từ idtt

                if (dishName.contains(searchText) || provinceName.contains(searchText)) {
                    monAnList.add(dish)
                    Log.d("DishFragment", "Món phù hợp: ${dish.tenma}")
                }
            }
        }
            dishAdapter.updateList(monAnList)
            updateEmptyView(monAnList.isEmpty())
            updateTotalDishes(monAnList.size) // Thêm dòng này
            Log.d("DishFragment", "Số món sau filter: ${monAnList.size}")
        }


    // Hàm giả định để lấy tên tỉnh từ idtt (cần triển khai thực tế)
    private fun getProvinceName(idtt: String?): String {
        if (idtt.isNullOrEmpty()) return ""

        // Nếu đã có trong cache
        provinceCache[idtt]?.let { return it }

        // Nếu chưa có, thử lấy từ Firebase (đơn giản hóa)
        // Trong thực tế cần triển khai async
        return idtt // Tạm thời trả về idtt nếu chưa implement đầy đủ
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.dishRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }


    private fun setupRecyclerView() {
        binding.dishRecyclerView.layoutManager = LinearLayoutManager(context)
        dishAdapter = DishAdapter(
            monAnList,
            requireContext(),
            onEditClick = { showDishDialog(it) },
            onDeleteClick = { confirmDelete(it) }
        )
        binding.dishRecyclerView.adapter = dishAdapter
    }


    private fun loadDishes() {
        //val dishesRef = database.getReference("10/data")
        Log.d("DishFragment", "Bắt đầu load dữ liệu, provinceIdtt: $provinceIdtt")
        binding.progressBar.visibility = View.VISIBLE

        val query = if (!provinceIdtt.isNullOrEmpty()) {
            monanRef.orderByChild("idtt").equalTo(provinceIdtt)
        } else {
            monanRef
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullList.clear()
               // monAnList.clear()
                for (dishSnapshot in snapshot.children) {
                    val dish = dishSnapshot.getValue(DishModel::class.java)?.apply {
                        idma = dishSnapshot.key
                    }
                    dish?.let { fullList.add(it)
                        Log.d("DishFragment", "Đã thêm món: ${it.tenma}") }
                }
                updateTotalDishes(fullList.size) // Thêm dòng này
                binding.progressBar.visibility = View.GONE
                filterDishes()
//                dishAdapter.notifyDataSetChanged()
//                binding.emptyView.visibility = if (monAnList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("DishFragment", "DatabaseError: ${error.message}")
            }
        })
    }

    private fun showDishDialog(dishToEdit: DishModel?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_dish, null)
        val edtImg1 = dialogView.findViewById<EditText>(R.id.dishImage)
        val edtImg2 = dialogView.findViewById<EditText>(R.id.dishImage2)
        val edtImg3 = dialogView.findViewById<EditText>(R.id.dishImage3)
        val edtTenma = dialogView.findViewById<EditText>(R.id.edtTenMon)
        val edtGioithieu = dialogView.findViewById<EditText>(R.id.edtGioiThieu)
        val edtGiaca = dialogView.findViewById<EditText>(R.id.edtGia)
        val edtMota = dialogView.findViewById<EditText>(R.id.edtMoTa)
        val edtDiachi = dialogView.findViewById<EditText>(R.id.edtDiaChi)
        val edtLink = dialogView.findViewById<EditText>(R.id.edtLink)
        // Thêm Spinner chọn tỉnh
        val spinnerProvince = dialogView.findViewById<Spinner>(R.id.spinnerProvince)
        //khởi tạo snipper
        val provinceList = mutableListOf<ProvinceModel>()
        val provinceAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf<String>("Chọn tỉnh thành")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerProvince.adapter = provinceAdapter
// Logic hiển thị Spinner
        val isEditMode = dishToEdit != null
        val shouldShowProvinceSpinner = provinceIdtt == null && !isEditMode
        // Chỉ hiển thị Spinner khi thêm món từ trang "Tất cả món ăn"
        spinnerProvince.visibility = if  (shouldShowProvinceSpinner) View.VISIBLE else View.GONE

        // Load danh sách tỉnh từ Firebase
        if (spinnerProvince.visibility == View.VISIBLE) {
            val database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val ref = database.getReference("14/data")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    provinceList.clear()
                    provinceAdapter.clear()
                    provinceAdapter.add("Chọn tỉnh thành")

                    for (provinceSnapshot in snapshot.children) {
                        val province = provinceSnapshot.getValue(ProvinceModel::class.java)?.apply {
                            id = provinceSnapshot.key
                        }
                        province?.let {
                            provinceList.add(it)
                            provinceAdapter.add(it.tentinh ?: "Không tên")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Lỗi tải tỉnh thành: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }


        //  val isEdit = dishToEdit != null

        dishToEdit?.let { dish->
            edtImg1.setText(dish.hinhanh)
            edtImg2.setText(dish.hinhanh2)
            edtImg3.setText(dish.hinhanh3)
            edtTenma.setText(dish.tenma)
            edtDiachi.setText(dish.diachi)
            edtGioithieu.setText(dish.gioithieu)
            edtGiaca.setText(dish.giaca)
            edtMota.setText(dish.mota)
            edtLink.setText(dish.duonglink_diachi)
            if (spinnerProvince.visibility == View.VISIBLE && dish.idtt != null) {
                val position = provinceList.indexOfFirst { it.idtt == dish.idtt }
                if (position >= 0) spinnerProvince.setSelection(position + 1)
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (isEditMode) "Sửa món ăn" else "Thêm món ăn")
            .setView(dialogView)
            .setPositiveButton("Lưu", null)
            .setNegativeButton("Hủy", null)
            .create()

        dialog.setOnShowListener {
            val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSave.setOnClickListener {
                //validate
                val img1 = edtImg1.text.toString().trim()
                val img2 = edtImg2.text.toString().trim()
                val img3 = edtImg3.text.toString().trim()
                val ten = edtTenma.text.toString().trim()
                val dc = edtDiachi.text.toString().trim()
                val gt = edtGioithieu.text.toString().trim()
                val gia = edtGiaca.text.toString().trim()
                val mt = edtMota.text.toString().trim()
                val linkDC = edtLink.text.toString().trim()

                // Xử lý chọn tỉnh khi thêm từ trang "Tất cả món ăn"
                var selectedProvinceId: String? = provinceIdtt
                if (spinnerProvince.visibility == View.VISIBLE) {
                    if (spinnerProvince.selectedItemPosition == 0) {
                        Toast.makeText(context, "Vui lòng chọn tỉnh thành", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    selectedProvinceId = provinceList[spinnerProvince.selectedItemPosition - 1].idtt
                }

                if (img1.isEmpty() || ten.isEmpty() || dc.isEmpty() || gia.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val dishId = dishToEdit?.idma ?: monanRef.push().key!!

                val dish = dishToEdit ?: DishModel().apply {
                  //  idtt = selectedProvinceId
                    // Chỉ gán idtt khi thêm mới và đang ở chế độ xem theo tỉnh
//                    if (!provinceIdtt.isNullOrEmpty()) {
//                        idtt = provinceIdtt
//                    }
                    idtt = selectedProvinceId
                }

                dish.apply {
                    hinhanh = img1
                    hinhanh2 = img2.takeIf { it.isNotEmpty() }
                    hinhanh3 = img3.takeIf { it.isNotEmpty() }
                    idma = dishId
                    tenma = ten
                    diachi = dc
                    gioithieu = gt.takeIf { it.isNotEmpty() }
                    giaca = gia
                    mota = mt.takeIf { it.isNotEmpty() }
                    duonglink_diachi = linkDC.takeIf { it.isNotEmpty() }
                  //  idtt = provinceIdtt // Luôn gán idtt của tỉnh hiện tại
                }

                monanRef.child(dishId).setValue(dish)
                    .addOnSuccessListener {
                        Toast.makeText(context, if (isEditMode) "Cập nhật thành công" else "Thêm món thành công", Toast.LENGTH_SHORT).show()
                        Log.d("DishFragment", "Đã thêm món với idtt = ${dish.idtt}")
                        // Cập nhật lại danh sách
                        if (isEditMode) {
                            val index = fullList.indexOfFirst { it.idma == dishId }
                            if (index != -1) fullList[index] = dish
                        } else {
                            fullList.add(dish)
                        }
                        updateTotalDishes(fullList.size)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
                    }

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun confirmDelete(dish: DishModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa món ăn")
            .setMessage("Bạn có chắc muốn xóa món '${dish.tenma}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                val database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app/")
                val ref = database.getReference("10/data/${dish.idma}")

                ref.removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Xóa món thành công", Toast.LENGTH_SHORT).show()
                        // Cập nhật lại tổng số
                        fullList.removeIf { it.idma == dish.idma }
                        updateTotalDishes(fullList.size)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Lỗi khi xóa: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

}

//class DishFragment : Fragment() {
//
//    private lateinit var binding: FragmentDishBinding
//    private lateinit var monanRef: DatabaseReference
//    private lateinit var dishAdapter: DishAdapter
//    private val monAnList = mutableListOf<DishModel>()
//    private var provinceIdtt: String? = null
//    //private var valueEventListener: ValueEventListener? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        // Nhận tên tỉnh từ arguments
//        provinceIdtt = arguments?.getString("provinceIdtt")
//        Log.d("DishFragment", "Received provinceIdtt: $provinceIdtt")
//    }
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentDishBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.dishRecyclerView.layoutManager = LinearLayoutManager(context)
//        binding.dishRecyclerView.setHasFixedSize(true)
//
//        monanRef = FirebaseDatabase.getInstance().getReference("10/data")
//
//        dishAdapter = DishAdapter(
//            monAnList,
//            requireContext(),
//            onEditClick = { showDishDialog(it) },
//            onDeleteClick = { confirmDelete(it) }
//        )
//        binding.dishRecyclerView.adapter = dishAdapter
//
//        binding.fabAddDish.setOnClickListener {
//            showDishDialog(null)
//        }
//        // Tải danh sách tỉnh thành
//        getDishData()
//    }
//    private fun getDishData() {
//                monanRef = FirebaseDatabase.getInstance().getReference("10/data")
//                monanRef.addValueEventListener(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        monAnList.clear()
//                        for (monSnapshot in snapshot.children) {
//                            val monAn = monSnapshot.getValue(DishModel::class.java)
//                            monAn?.let { monAnList.add(it) }
//                        }
//                        dishAdapter.notifyDataSetChanged()
//                    }
//                    override fun onCancelled(error: DatabaseError) {
//                        Toast.makeText(context, "Lỗi tải món ăn: ${error.message}", Toast.LENGTH_SHORT).show()
//                    }
//                })
//            }
//
//    private fun showDishDialog(dishToEdit: DishModel?) {
//        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_dish, null)
//        val edtImg1 = dialogView.findViewById<EditText>(R.id.dishImage)
//        val edtImg2 = dialogView.findViewById<EditText>(R.id.dishImage2)
//        val edtImg3 = dialogView.findViewById<EditText>(R.id.dishImage3)
//        val edtTenma = dialogView.findViewById<EditText>(R.id.edtTenMon)
//        val edtGioithieu = dialogView.findViewById<EditText>(R.id.edtGioiThieu)
//        val edtGiaca = dialogView.findViewById<EditText>(R.id.edtGia)
//        val edtMota = dialogView.findViewById<EditText>(R.id.edtMoTa)
//        val edtDiachi = dialogView.findViewById<EditText>(R.id.edtDiaChi)
//        val edtLink = dialogView.findViewById<EditText>(R.id.edtLink)
//
//        val isEdit = dishToEdit != null
//
//        dishToEdit?.let {
//            edtImg1.setText(it.hinhanh)
//            edtImg2.setText(it.hinhanh2)
//            edtImg3.setText(it.hinhanh3)
//            edtTenma.setText(it.tenma)
//            edtDiachi.setText(it.diachi)
//            edtGioithieu.setText(it.gioithieu)
//            edtGiaca.setText(it.giaca)
//            edtMota.setText(it.mota)
//            edtLink.setText(it.duonglink_diachi)
//        }
//
//        val dialog = AlertDialog.Builder(requireContext())
//            .setTitle(if (isEdit) "Sửa món ăn" else "Thêm món ăn")
//            .setView(dialogView)
//            .setPositiveButton("Lưu", null)
//            .setNegativeButton("Hủy", null)
//            .create()
//
//        dialog.setOnShowListener {
//            val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
//            btnSave.setOnClickListener {
//                val img1 = edtImg1.text.toString().trim()
//                val img2 = edtImg2.text.toString().trim()
//                val img3 = edtImg3.text.toString().trim()
//                val ten = edtTenma.text.toString().trim()
//                val dc = edtDiachi.text.toString().trim()
//                val gt = edtGioithieu.text.toString().trim()
//                val gia = edtGiaca.text.toString().trim()
//                val mt = edtMota.text.toString().trim()
//                val linkDC = edtLink.text.toString().trim()
//
//                if (img1.isEmpty() || img2.isEmpty() || img3.isEmpty() || ten.isEmpty() || dc.isEmpty() || gt.isEmpty() || gia.isEmpty() || mt.isEmpty() || linkDC.isEmpty()) {
//                    Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//                val dishId = dishToEdit?.idma ?: monanRef.push().key!!
//                val dish =  dishToEdit ?: DishModel() // Nếu đang sửa thì dùng lại object cũ
//
//                dish.apply {
//                    hinhanh = img1
//                    hinhanh2 = img2
//                    hinhanh3 = img3
//                    idma = dishId
//                    tenma = ten
//                    diachi = dc
//                    gioithieu = gt
//                    giaca = gia
//                    mota = mt
//                    duonglink_diachi = linkDC
//                    idtt = dishToEdit?.idtt ?: provinceIdtt
//                }
//                monanRef.child(dishId).setValue(dish)
//                dialog.dismiss()
//            }
//        }
//
//        dialog.show()
//    }
//
//    private fun confirmDelete(dish: DishModel) {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Xóa món ăn")
//            .setMessage("Bạn có chắc muốn xóa món '${dish.tenma}' không?")
//            .setPositiveButton("Xóa") { _, _ ->
//                dish.idma?.let {
//                    monanRef.child(it).removeValue()
//                }
//            }
//            .setNegativeButton("Hủy", null)
//            .show()
//    }
//}


//    class DishFragment : Fragment() {
//
//        private lateinit var binding: FragmentDishBinding
//        private lateinit var monanRef: DatabaseReference
//        private lateinit var dishAdapter: DishAdapter
//        private val monAnList = mutableListOf<DishModel>()
//        private var provinceIdtt: String? = null
//
//        override fun onCreate(savedInstanceState: Bundle?) {
//            super.onCreate(savedInstanceState)
//            // Nhận tên tỉnh từ arguments
//            provinceIdtt = arguments?.getString("provinceIdtt")
//            Log.d("DishFragment", "Received provinceIdtt: $provinceIdtt")
//        }
//
//        override fun onCreateView(
//            inflater: LayoutInflater, container: ViewGroup?,
//            savedInstanceState: Bundle?
//        ): View {
//            binding = FragmentDishBinding.inflate(inflater, container, false)
//            return binding.root
//        }
//
//        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//            super.onViewCreated(view, savedInstanceState)
//
//            binding.dishRecyclerView.layoutManager = LinearLayoutManager(context)
//            binding.dishRecyclerView.setHasFixedSize(true)
//
//            monanRef = FirebaseDatabase.getInstance().getReference("10/data")
//
//            dishAdapter = DishAdapter(
//                monAnList,
//                requireContext(),
//                onEditClick = { showDishDialog(it) },
//                onDeleteClick = { confirmDelete(it) }
//            )
//
//            binding.dishRecyclerView.adapter = dishAdapter
//
//            binding.fabAddDish.setOnClickListener {
//                showDishDialog(null)
//            }
//
//            // Tải danh sách tỉnh thành
//            getDishData()
//        }
////        private fun getDishData() {
////            monanRef = FirebaseDatabase.getInstance().getReference("10/data")
////            monanRef.addValueEventListener(object : ValueEventListener {
////                override fun onDataChange(snapshot: DataSnapshot) {
////                    monAnList.clear()
////                    for (monSnapshot in snapshot.children) {
////                        val monAn = monSnapshot.getValue(DishModel::class.java)
////                        monAn?.let { monAnList.add(it) }
////                    }
////                    dishAdapter.notifyDataSetChanged()
////                }
////
////                override fun onCancelled(error: DatabaseError) {
////                    Toast.makeText(context, "Lỗi tải món ăn: ${error.message}", Toast.LENGTH_SHORT).show()
////                }
////            })
////        }
//
//        private fun getDishData() {
//            monanRef = FirebaseDatabase.getInstance().getReference("10/data")
//            monanRef.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    monAnList.clear()
//                    for (monSnapshot in snapshot.children) {
//                        val monAn = monSnapshot.getValue(DishModel::class.java)
//                        monAn?.let {
//                            // Gán key của node làm idma nếu chưa có
//                            if (it.idma == null) it.idma = monSnapshot.key
//                            // Lọc theo idtt nếu provinceIdtt không null
//                            if (provinceIdtt == null || it.idtt == provinceIdtt) {
//                                monAnList.add(it)
//                                Log.d("DishFragment", "Added dish: ${it.tenma}, idtt: ${it.idtt}")
//                            }
//                        }
//                    }
//                    Log.d("DishFragment", "Total dishes fetched: ${monAnList.size}")
//                    dishAdapter.notifyDataSetChanged()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Toast.makeText(context, "Lỗi tải món ăn: ${error.message}", Toast.LENGTH_SHORT).show()
//                    Log.e("DishFragment", "Error loading dishes: ${error.message}", error.toException())
//                }
//            })
//
//        }
//        private fun showDishDialog(dishToEdit: DishModel?) {
//            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_dish, null)
//            val edtImg1 = dialogView.findViewById<EditText>(R.id.dishImage)
//            val edtImg2 = dialogView.findViewById<EditText>(R.id.dishImage2)
//            val edtImg3 = dialogView.findViewById<EditText>(R.id.dishImage3)
//            val edtTenma = dialogView.findViewById<EditText>(R.id.edtTenMon)
//            val edtGioithieu = dialogView.findViewById<EditText>(R.id.edtGioiThieu)
//            val edtGiaca = dialogView.findViewById<EditText>(R.id.edtGia)
//            val edtMota = dialogView.findViewById<EditText>(R.id.edtMoTa)
//            val edtDiachi = dialogView.findViewById<EditText>(R.id.edtDiaChi)
//            val edtLink = dialogView.findViewById<EditText>(R.id.edtLink)
//
//            val isEdit = dishToEdit != null
//
//            dishToEdit?.let {
//                edtImg1.setText(it.hinhanh)
//                edtImg2.setText(it.hinhanh2)
//                edtImg3.setText(it.hinhanh3)
//                edtTenma.setText(it.tenma)
//                edtDiachi.setText(it.diachi)
//                edtGioithieu.setText(it.gioithieu)
//                edtGiaca.setText(it.giaca)
//                edtMota.setText(it.mota)
//                edtLink.setText(it.duonglink_diachi)
//            }
//
//            val dialog = AlertDialog.Builder(requireContext())
//                .setTitle(if (isEdit) "Sửa món ăn" else "Thêm món ăn")
//                .setView(dialogView)
//                .setPositiveButton("Lưu", null)
//                .setNegativeButton("Hủy", null)
//                .create()
//
//            dialog.setOnShowListener {
//                val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
//                btnSave.setOnClickListener {
//                    val img1 = edtImg1.text.toString().trim()
//                    val img2 = edtImg2.text.toString().trim()
//                    val img3 = edtImg3.text.toString().trim()
//                    val ten = edtTenma.text.toString().trim()
//                    val dc = edtDiachi.text.toString().trim()
//                    val gt = edtGioithieu.text.toString().trim()
//                    val gia = edtGiaca.text.toString().trim()
//                    val mt = edtMota.text.toString().trim()
//                    val linkDC = edtLink.text.toString().trim()
//
//                    if (img1.isEmpty() || img2.isEmpty() || img3.isEmpty() || ten.isEmpty() || dc.isEmpty() || gt.isEmpty() || gia.isEmpty() || mt.isEmpty() || linkDC.isEmpty()) {
//                        Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
//                        return@setOnClickListener
//                    }
//
//                    val dishId = dishToEdit?.idma ?: monanRef.push().key!!
//                    val dish = DishModel().apply {
//                        hinhanh = img1
//                        hinhanh2 = img2
//                        hinhanh3 = img3
//                        idma = dishId
//                        tenma = ten
//                        diachi = dc
//                        gioithieu = gt
//                        giaca = gia
//                        mota = mt
//                        duonglink_diachi = linkDC
////                        hinhanh = dishToEdit?.hinhanh
////                        hinhanh2 = dishToEdit?.hinhanh2
////                        hinhanh3 = dishToEdit?.hinhanh3
//                    }
//
//                    monanRef.child(dishId).setValue(dish)
//                    dialog.dismiss()
//                }
//            }
//
//            dialog.show()
//        }
//
//        private fun confirmDelete(dish: DishModel) {
//            AlertDialog.Builder(requireContext())
//                .setTitle("Xóa món ăn")
//                .setMessage("Bạn có chắc muốn xóa món '${dish.tenma}' không?")
//                .setPositiveButton("Xóa") { _, _ ->
//                    dish.idma?.let {
//                        monanRef.child(it).removeValue()
//                    }
//                }
//                .setNegativeButton("Hủy", null)
//                .show()
//        }
//    }





