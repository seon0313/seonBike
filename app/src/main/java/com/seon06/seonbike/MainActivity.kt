package com.seon06.seonbike

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.seon06.seonbike.ui.theme.SeonBikeTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    private var isPermissionGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            isPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }

        enableEdgeToEdge()
        setContent {
            SeonBikeTheme {
                // Scaffold를 사용하되 컨텐츠에는 패딩을 강제하지 않음 (지도가 전체 화면을 쓰기 위함)
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    if (isPermissionGranted) {
                        MainView()
                    }
                }
            }
        }
    }
}

@Composable
fun MainView() {
    Box(modifier = Modifier.fillMaxSize()) {
        MapView(modifier = Modifier.fillMaxSize())
        FloatingBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onSearch = { q ->
                Log.i("MainActivity", "Search: $q")
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainViewPreview() {
    SeonBikeTheme {
        MainView()
    }
}
