package dam.moviles.cocinetica.vista

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.google.firebase.BuildConfig
import dam.moviles.cocinetica.databinding.FragmentAyudaBinding
import java.io.BufferedReader
import java.io.InputStreamReader

class AyudaFragment : Fragment() {

    private var _binding: FragmentAyudaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAyudaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureWebView()
        loadLocalHtml()

        binding.btnVolverAyuda.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun configureWebView() {
        binding.webViewAyuda.settings.apply {
            javaScriptEnabled = true // Si usas JS
            domStorageEnabled = true // Para HTML5 localStorage
            allowFileAccess = true  // Para acceder a archivos locales
            allowContentAccess = true
            setSupportZoom(true)     // Si necesitas zoom
        }

        // Habilita depuración en modo desarrollo
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

    private fun loadLocalHtml() {
        try {
            // Método más eficiente que garantiza carga de recursos
            binding.webViewAyuda.loadUrl("file:///android_asset/ayuda/index.html")

        } catch (e: Exception) {
            e.printStackTrace()
            showErrorPage(e)
        }
    }

    private fun showErrorPage(error: Exception) {
        val errorHtml = """
            <html>
                <body>
                    <h1>Error al cargar la ayuda</h1>
                    <p>${error.localizedMessage}</p>
                    <p>Versión de la app: ${BuildConfig.VERSION_NAME}</p>
                </body>
            </html>
        """.trimIndent()

        binding.webViewAyuda.loadData(errorHtml, "text/html", "UTF-8")
    }

    override fun onDestroyView() {
        binding.webViewAyuda.stopLoading()
        binding.webViewAyuda.destroy()
        _binding = null
        super.onDestroyView()
    }
}