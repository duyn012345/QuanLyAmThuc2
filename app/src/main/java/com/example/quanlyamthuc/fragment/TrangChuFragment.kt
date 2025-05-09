package com.example.quanlyamthuc.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.MonAnAdapter
import com.example.quanlyamthuc.databinding.FragmentTrangChuBinding
import com.example.quanlyamthuc.model.MonAn
import com.google.firebase.database.*

class TrangChuFragment : Fragment() {

    private var _binding: FragmentTrangChuBinding? = null
    private val binding get() = _binding!!

    private lateinit var backgroundImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrangChuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        backgroundImage = view.findViewById(R.id.backgroundImage)
//        loadLocationImage()

        val danhSachAnhTongHop = arrayListOf(
            SlideModel(R.drawable.bn1, ScaleTypes.FIT),
            SlideModel(R.drawable.bn2, ScaleTypes.FIT),
            SlideModel(R.drawable.bn3, ScaleTypes.FIT),
            SlideModel(R.drawable.bn4, ScaleTypes.FIT)
        )
        binding.imageSlider.setImageList(danhSachAnhTongHop, ScaleTypes.FIT)

        // Slider tỉnh thành - lấy từ Firebase
        val imageSliderTinh = binding.imageSliderTinh
        val database =
            FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
        val dbRef = database.getReference("14/data")  // Lấy tất cả dữ liệu từ node "14/data"

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val slideList = ArrayList<SlideModel>()

                // Duyệt qua tất cả các mục trong node "14/data"
                for (dataSnap in snapshot.children) {
                    val imageUrl = dataSnap.child("hinhanh").getValue(String::class.java)
                    val description = dataSnap.child("tentinh").getValue(String::class.java)

                    // Nếu có ảnh, thêm vào danh sách slide
                    if (!imageUrl.isNullOrEmpty()) {
                        slideList.add(SlideModel(imageUrl, description ?: "", ScaleTypes.FIT))
                    }
                }

                // Set tất cả ảnh vào slider
                imageSliderTinh.setImageList(slideList, ScaleTypes.FIT)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi tải ảnh từ Firebase", Toast.LENGTH_SHORT)
                    .show()
            }

        })

// Bắt sự kiện click slider tỉnh thành
        imageSliderTinh.setItemClickListener(object : ItemClickListener {
            override fun doubleClick(position: Int) {}
            override fun onItemSelected(position: Int) {
                Toast.makeText(requireContext(), "Tỉnh thành: ảnh $position", Toast.LENGTH_SHORT)
                    .show()
            }
        })
         MonAnNoiTieng()
    }

    private fun MonAnNoiTieng() {
        val database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
        val danhSach = mutableListOf<MonAn>()
        val nodesToFetch = listOf("10/data/1", "10/data/3", "10/data/0", "10/data/6", "10/data/10", "10/data/12", "10/data/15", "10/data/16", "10/data/17")

        for (node in nodesToFetch) {
            val dbRef = database.getReference(node)

            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val monAn = snapshot.getValue(MonAn::class.java)
                    if (monAn != null) {
                        danhSach.add(monAn)
                        if (danhSach.size == nodesToFetch.size) {
                            val adapter = MonAnAdapter(
                                danhSach,
                                onItemClick = { monAn ->
                                    val fragment = ChiTietMonAnFragment.newInstance(monAn)
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, fragment)
                                        .addToBackStack(null)
                                        .commit()
                                },
                                layoutId = R.layout.item_mon_an_noi_tieng // dùng layout khác
                            )
                            binding.recyclerMonAn.layoutManager = GridLayoutManager(requireContext(), 3)
                            binding.recyclerMonAn.adapter = adapter
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Lỗi Firebase", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }



}
