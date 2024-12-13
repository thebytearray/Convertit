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
    import com.nasahacker.convertit.ui.composable.AboutAppBar
    import com.nasahacker.convertit.ui.screen.AboutScreen
    import com.nasahacker.convertit.ui.theme.AppTheme

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