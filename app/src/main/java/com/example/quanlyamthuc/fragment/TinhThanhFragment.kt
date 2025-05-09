package com.example.quanlyamthuc.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.adapter.TinhThanhAdapter
import com.example.quanlyamthuc.model.TinhThanh
import com.google.firebase.database.*

class TinhThanhFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dbRef: DatabaseReference
    private lateinit var tinhList: MutableList<TinhThanh>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tinh_thanh, container, false)

        recyclerView = view.findViewById(R.id.recyclerTinh)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        tinhList = mutableListOf()
        dbRef = FirebaseDatabase.getInstance().getReference("data")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tinhList.clear()
                for (tinhSnapshot in snapshot.children) {
                    val tinh = tinhSnapshot.getValue(TinhThanh::class.java)
                    if (tinh != null) {
                        tinhList.add(tinh)
                    }
                }
                recyclerView.adapter = TinhThanhAdapter(tinhList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần
            }
        })

        return view
    }
}
