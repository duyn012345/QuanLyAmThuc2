package com.example.quanlyamthuc.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.quanlyamthuc.LoginActivity
import com.example.quanlyamthuc.R
import com.example.quanlyamthuc.databinding.FragmentHomeBinding
import com.example.quanlyamthuc.fragment.DishFragment


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(R.drawable.bn11, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.bn222, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner3, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner4, ScaleTypes.FIT))

        val imageSlider = binding.imageSlider
        imageSlider.setImageList(imageList)
        imageSlider.setImageList(imageList, ScaleTypes.FIT)

        imageSlider.setItemClickListener(object :ItemClickListener{
            override fun doubleClick(position: Int) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(position: Int) {
                val itemPos = imageList[position]
                val itemMess = "Select image $position"
                Toast.makeText(requireContext(), itemMess, Toast.LENGTH_SHORT).show()
            }
        })
        // Bắt sự kiện click vào các layout chức năng
        binding.dishLayout.setOnClickListener { openFragment(DishFragment()) }
        binding.provinceLayout.setOnClickListener { openFragment(ProvinceFragment()) }
        binding.blockLayout.setOnClickListener { openFragment(BlockFragment()) }
        binding.reviewLayout.setOnClickListener { openFragment(ReviewFragment()) }
        binding.userLayout.setOnClickListener { openFragment(UserFragment()) }
        binding.exitLayout.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)
            .addToBackStack(null)
            .commit()
    }
}

