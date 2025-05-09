package com.example.quanlyamthuc.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.quanlyamthuc.databinding.FragmentChiTietMonAnBinding
import com.example.quanlyamthuc.model.MonAn

class ChiTietMonAnFragment : Fragment() {

    private var _binding: FragmentChiTietMonAnBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(monAn: MonAn): ChiTietMonAnFragment {
            val fragment = ChiTietMonAnFragment()
            val bundle = Bundle()
            bundle.putSerializable("monAn", monAn)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChiTietMonAnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val monAn = arguments?.getSerializable("monAn") as? MonAn ?: return

        val danhSachAnh = arrayListOf<SlideModel>()
        monAn.hinhanh?.let { danhSachAnh.add(SlideModel(it, ScaleTypes.FIT)) }
        monAn.hinhanh2?.let { danhSachAnh.add(SlideModel(it, ScaleTypes.FIT)) }
        monAn.hinhanh3?.let { danhSachAnh.add(SlideModel(it, ScaleTypes.FIT)) }

        binding.imageSliderChiTiet.setImageList(danhSachAnh, ScaleTypes.FIT)
        binding.imageSliderChiTiet.startSliding(1500) // Tự động chuyển ảnh mỗi 1.5s

        binding.tvTenMonChiTiet.text = monAn.tenma
        binding.tvTinhChiTiet.text = "${monAn.idtt}"
        binding.tvGioiThieuChiTiet.text = monAn.gioithieu
        binding.tvGiaCaChiTiet.text = "  Giao động từ: ${monAn.giaca}"
        binding.tvDiaChiChiTiet.text = "  Địa chỉ quán gợi ý: ${monAn.diachi}"
        binding.tvMoTaChiTiet.text = "\n${monAn.mota}"

        // Hiển thị "Xem địa chỉ" nhưng mở link khi nhấn
        binding.tvXemDiaChiChiTiet.text = "  Xem địa chỉ"
        binding.tvXemDiaChiChiTiet.setOnClickListener {
            val link = monAn.duonglink_diachi
            if (!link.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
