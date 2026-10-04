package com.github.leemoon228.calc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.leemoon228.calc.ui.theme.CalcTheme

@Composable
fun App(modifier: Modifier = Modifier) {
    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center,

        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text("Hello", )
        Text(
            "Hello",
            Modifier.size(200.dp, Dp.Unspecified)
        )
    }
}

@Preview
@Composable
fun AppPreview() {
    CalcTheme() {
        App()
    }
}