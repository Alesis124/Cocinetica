package dam.moviles.cocinetica.modelo

import androidx.navigation.NavController
import androidx.navigation.NavDirections

interface NavegadorError{
    fun getNavController(): NavController
    fun getFlechaNavegacionPantallaError(m: String): NavDirections
    fun navegarPantallaError(m: String){
        val nc = getNavController()
        val flecha = getFlechaNavegacionPantallaError(m)
        nc.navigate(flecha)
    }
}