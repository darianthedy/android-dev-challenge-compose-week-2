/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.androiddevchallenge.ui.theme.MyTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

private enum class HourglassState {
    Top, Bottom
}

// Start building your app here!
@Composable
fun MyApp() {
    Surface(color = MaterialTheme.colors.background) {
        var currentState by remember { mutableStateOf(HourglassState.Top) }
        val transition = updateTransition(currentState)

        Column {
            Image(imageVector = Icons.Default.Timer, contentDescription = null)
            Image(imageVector = Icons.Default.TimeToLeave, contentDescription = null)
            Image(imageVector = Icons.Default.Timelapse, contentDescription = null)
            Image(imageVector = Icons.Default.Timeline, contentDescription = null)
            Image(imageVector = Icons.Default.TimerOff, contentDescription = null)
            Image(imageVector = Icons.Default.HourglassFull, contentDescription = null)
            Image(imageVector = Icons.Default.HourglassBottom, contentDescription = null)
            Image(imageVector = Icons.Default.HourglassDisabled, contentDescription = null)
            Image(imageVector = Icons.Default.HourglassEmpty, contentDescription = null)
            Image(imageVector = Icons.Default.HourglassTop, contentDescription = null)
        }
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}
