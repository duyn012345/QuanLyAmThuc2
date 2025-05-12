package com.example.quanlyamthuc.fragment

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.MonAnAdapter
import com.example.quanlyamthuc.databinding.FragmentDanhSachMonAnTheoTinhBinding
import com.example.quanlyamthuc.model.MonAn
import com.google.firebase.database.*

class DanhSachMonAnTheoTinhFragment : Fragment() {

    private var _binding: FragmentDanhSachMonAnTheoTinhBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(idtt: String, tenTinh: String): DanhSachMonAnTheoTinhFragment {
            val fragment = DanhSachMonAnTheoTinhFragment()
            val args = Bundle()
            args.putString("idtt", idtt)
            args.putString("tentinh", tenTinh)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDanhSachMonAnTheoTinhBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idtt = arguments?.getString("idtt")
        val tentinh = arguments?.getString("tentinh")

        binding.txtTenTinh.text = " $tentinh"

        if (idtt != null) {
            taiMonAnTheoTinh(idtt)
        }
    }

    private fun taiMonAnTheoTinh(idtt: String) {
        val db = FirebaseDatabase
            .getInstance("https://quanlyamthuc-tpmd-default-rtdb.asia-southeast1.firebasedatabase.app")
        val dbRef = db.getReference("10/data")

        val danhSach = mutableListOf<MonAn>()

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (monAnSnap in snapshot.children) {
                    val monAn = monAnSnap.getValue(MonAn::class.java)
                    if (monAn != null && monAn.idtt == idtt) {
                        danhSach.add(monAn)
                    }
                }

                if (danhSach.isEmpty()) {
                    Toast.makeText(requireContext(), "Không có món ăn nào!", Toast.LENGTH_SHORT).show()
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
                    layoutId = R.layout.item_mon_an_noi_tieng,
                    mapTenTinh = mapOf(idtt to binding.txtTenTinh.text.toString())
                )

                binding.recyclerDanhSachMonAn.layoutManager = GridLayoutManager(requireContext(), 2)
                binding.recyclerDanhSachMonAn.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi Firebase", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
