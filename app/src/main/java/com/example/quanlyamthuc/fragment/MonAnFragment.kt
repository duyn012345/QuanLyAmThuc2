package com.example.quanlyamthuc.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.MonAnAdapter
import com.example.quanlyamthuc.databinding.FragmentMonAnBinding
import com.example.quanlyamthuc.model.MonAn
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MonAnFragment : Fragment() {

    private var _binding: FragmentMonAnBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonAnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Slider tổng hợp ảnh tĩnh
        val danhSachAnhTongHop = arrayListOf(
            SlideModel(R.drawable.bn4, ScaleTypes.FIT),
            SlideModel(R.drawable.menu2, ScaleTypes.FIT),
            SlideModel(R.drawable.menu3, ScaleTypes.FIT)

        )
        binding.imageSlider.setImageList(danhSachAnhTongHop, ScaleTypes.FIT)
        binding.imageSlider.startSliding(1500) // Bắt đầu auto slide 3s/lần

        TatCaMonAn()
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
                            binding.recyclerMonAn.layoutManager =
                                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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


    private fun TatCaMonAn() {
        val database = FirebaseDatabase.getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
        val danhSach = mutableListOf<MonAn>()
        val dbRef = database.getReference("10/data")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                danhSach.clear()
                for (monSnapshot in snapshot.children) {
                    val monAn = monSnapshot.getValue(MonAn::class.java)
                    if (monAn != null) {
                        danhSach.add(monAn)
                    }
                }

                val adapter = MonAnAdapter(
                    danhSach,
                    onItemClick = { monAn ->
                        val fragment = ChiTietMonAnFragment.newInstance(monAn)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    },
                    layoutId = R.layout.item_mon_an // <- truyền đúng layout cần dùng
                )

                binding.recyclerTatCaMonAn.layoutManager = GridLayoutManager(requireContext(), 3)
                binding.recyclerTatCaMonAn.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi Firebase", Toast.LENGTH_SHORT).show()
            }
        })
    }

//    private fun hienThiDialogThongTin(monAn: MonAn) {
//        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_thong_tin_mon_an, null)
//
//        val slider = dialogView.findViewById<com.denzcoskun.imageslider.ImageSlider>(R.id.imageSliderChiTiet)
//        val ten = dialogView.findViewById<TextView>(R.id.tvTenMonChiTiet)
//        val tinh = dialogView.findViewById<TextView>(R.id.tvTinhChiTiet)
//        val mota = dialogView.findViewById<TextView>(R.id.tvMoTaChiTiet)
//
//        val danhSachAnh = arrayListOf<SlideModel>()
//
//        monAn.hinhanh?.let { danhSachAnh.add(SlideModel(it, ScaleTypes.FIT)) }
//        monAn.hinhanh2?.let { danhSachAnh.add(SlideModel(it, ScaleTypes.FIT)) }
//        monAn.hinhanh3?.let { danhSachAnh.add(SlideModel(it, ScaleTypes.FIT)) }
//
//        slider.setImageList(danhSachAnh, ScaleTypes.FIT)
//
//        ten.text = monAn.tenma
//        tinh.text = "Tỉnh: ${monAn.idtt}"
//        mota.text = "Mô tả: ${monAn.mota}"
//
//        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
//        builder.setView(dialogView)
//            .setPositiveButton("Đóng") { dialog, _ -> dialog.dismiss() }
//            .show()
//    }




}