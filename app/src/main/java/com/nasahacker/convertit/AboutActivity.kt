    package com.nasahacker.convertit

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.compose.foundation.layout.padding
    import androidx.compose.material3.Scaffold
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.tooling.preview.Preview
    import com.nasahacker.convertit.ui.component.AboutAppBar
    import com.nasahacker.convertit.ui.screen.AboutScreen
    import com.nasahacker.convertit.ui.theme.AppTheme
    /**
     * @author      Tamim Hossain
     * @email       tamimh.dev@gmail.com
     * @license     Apache-2.0
     *
     * ConvertIt is a free and easy-to-use audio converter app.
     * It supports popular audio formats like MP3 and M4A.
     * With options for high-quality bitrates ranging from 128k to 320k,
     * ConvertIt offers a seamless conversion experience tailored to your needs.
     */

    class AboutActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContent {
                AppTheme {
                    Scaffold(
                        topBar = {
                            AboutAppBar(onBackPressed = {
                                onBackPressedDispatcher.onBackPressed()
                            })
                        }
                    ) { innerPadding ->
                        AboutScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun PreviewAboutScreen() {
        AppTheme {
            Scaffold(
                topBar = {
                    AboutAppBar(onBackPressed = {

                    })
                }
            ) { innerPadding ->
                AboutScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }