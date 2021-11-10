package com.example.sample.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.sample.R
import com.example.sample.ui.theme.SampleAppTheme


class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SampleApp() }
    }

}

@Composable
fun SampleApp(){
    SampleAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                SmallTopAppBar(title = {
                    Text(text = stringResource(R.string.app_name))
                }, navigationIcon = {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "")
                })

                Text(text = "What?")
            }
        }
    }
}

@Composable
fun FloatingButton(){

}

@Preview(showBackground = true)
@Composable
fun SampleAppPreview(){
    SampleApp()
}