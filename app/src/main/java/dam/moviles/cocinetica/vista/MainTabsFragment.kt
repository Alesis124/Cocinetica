package dam.moviles.cocinetica.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import dam.moviles.cocinetica.R

class MainTabsFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_main_tabs, container, false)
        viewPager = view.findViewById(R.id.viewPager)
        bottomNav = view.findViewById(R.id.bottomNav)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            private val fragments = listOf(
                InicioFragment(),
                BusquedaFragment(),
                GuardadosFragment(),
                CuentaFragment()
            )
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        // Al seleccionar una pestaña en BottomNavigationView, cambia ViewPager
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> viewPager.currentItem = 0
                R.id.nav_search -> viewPager.currentItem = 1
                R.id.nav_book -> viewPager.currentItem = 2
                R.id.nav_profile -> viewPager.currentItem = 3
                else -> false
            }
            true
        }

        // Al hacer swipe en ViewPager, cambia la selección del BottomNavigationView
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNav.menu.getItem(position).isChecked = true
            }
        })

        // Opcional: empieza mostrando la pestaña Inicio
        bottomNav.selectedItemId = R.id.nav_home
    }
}
