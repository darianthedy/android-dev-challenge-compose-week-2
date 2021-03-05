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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassDisabled
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androiddevchallenge.ui.theme.MyTheme

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
class MainActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel> {
        MainViewModelProviderFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val timerFocusRequester = TimerFocusRequester(
            FocusRequester(),
            FocusRequester(),
            FocusRequester()
        )

        setContent {
            MyTheme {
                MyApp(mainViewModel, timerFocusRequester)
            }
        }

        mainViewModel.onViewCreated()

        observeViewModel(timerFocusRequester)
    }

    private fun observeViewModel(timerFocusRequester: TimerFocusRequester) {
        mainViewModel.changeFocusToHours.observe(this) {
            if (it) timerFocusRequester.hoursFocusRequester.requestFocus()
        }

        mainViewModel.changeFocusToMinutes.observe(this) {
            if (it) timerFocusRequester.minutesFocusRequester.requestFocus()
        }

        mainViewModel.changeFocusToSeconds.observe(this) {
            if (it) timerFocusRequester.secondsFocusRequester.requestFocus()
        }
    }
}

class MainViewModelProviderFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            MainViewModel(TimerImpl()) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
}

// Start building your app here!
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@Composable
fun MyApp(
    mainViewModel: MainViewModel,
    timerFocusRequester: TimerFocusRequester,
) {
    Surface(color = MaterialTheme.colors.background) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderView(
                Modifier.align(Alignment.CenterHorizontally),
                mainViewModel,
                timerFocusRequester
            )

            Spacer(Modifier.size(24.dp))

            TimerView(mainViewModel)
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun HeaderView(
    modifier: Modifier,
    mainViewModel: MainViewModel,
    timerFocusRequester: TimerFocusRequester
) {
    val isPauseStopButtonVisible by mainViewModel.isPauseStopVisible.observeAsState(false)

    Box(modifier) {
        AnimatedVisibility(
            visible = isPauseStopButtonVisible,
        ) {
            StopTimerView(mainViewModel)
        }

        AnimatedVisibility(
            visible = !isPauseStopButtonVisible,
        ) {
            InputTimerView(mainViewModel, timerFocusRequester)
        }
    }
}

@Composable
private fun StopTimerView(mainViewModel: MainViewModel) {
    val isRunning by mainViewModel.isRunning.observeAsState(true)

    Row(
        Modifier.width(250.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (isRunning)
            HourglassRotating()
        else
            HourglassDisabled()

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = { mainViewModel.onStopButtonPressed() },
            modifier = Modifier.height(50.dp)
        ) {
            Text(text = "Stop")
        }
    }
}

@Composable
private fun InputTimerView(
    mainViewModel: MainViewModel,
    timerFocusRequester: TimerFocusRequester,
) {
    val hours by mainViewModel.hoursText.observeAsState("")
    val minutes by mainViewModel.minutesText.observeAsState("")
    val seconds by mainViewModel.secondsText.observeAsState("")

    Row(
        Modifier.width(250.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        TextField(
            modifier = Modifier
                .width(60.dp)
                .focusRequester(timerFocusRequester.hoursFocusRequester),
            value = hours,
            label = { Text("hh") },
            onValueChange = { mainViewModel.onInputHours(it) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
        )

        Spacer(Modifier.width(16.dp))

        TextField(
            modifier = Modifier
                .width(60.dp)
                .focusRequester(timerFocusRequester.minutesFocusRequester),
            value = minutes,
            label = { Text("mm") },
            onValueChange = { mainViewModel.onInputMinutes(it) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
        )

        Spacer(Modifier.width(16.dp))

        TextField(
            modifier = Modifier
                .width(60.dp)
                .focusRequester(timerFocusRequester.secondsFocusRequester),
            value = seconds,
            label = { Text("ss") },
            onValueChange = { mainViewModel.onInputSeconds(it) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
        )
    }
}

@Composable
private fun HourglassRotating() {
    val infiniteTransition = rememberInfiniteTransition()
    val degrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                360f at 800
            },
            repeatMode = RepeatMode.Restart
        )
    )

    Image(
        modifier = Modifier
            .size(50.dp)
            .rotate(degrees),
        imageVector = Icons.Default.HourglassTop,
        contentDescription = "Hourglass rotating",
    )
}

@Composable
private fun HourglassDisabled() {
    Image(
        modifier = Modifier.size(50.dp),
        imageVector = Icons.Default.HourglassDisabled,
        contentDescription = "Hourglass rotating",
    )
}

@ExperimentalComposeUiApi
@Composable
private fun TimerView(mainViewModel: MainViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        contentAlignment = Alignment.Center
    ) {
        val progress by mainViewModel.progress.observeAsState(0f)

        CircularProgressIndicator(
            modifier = Modifier.size(250.dp),
            progress = progress,
            color = MaterialTheme.colors.secondary
        )

        Button(
            modifier = Modifier.size(242.dp),
            shape = CircleShape,
            onClick = {
                keyboardController?.hideSoftwareKeyboard()
                mainViewModel.onTimerButtonPressed()
            }
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val remainingTime by mainViewModel.remainingTime.observeAsState(0f)
                if (remainingTime > 0f) {
                    val remainingTimeSecMs = (remainingTime / 1_000)
                    val remainingTimeSecs = remainingTimeSecMs.toInt()
                    val remainingTimeMs = ((remainingTimeSecMs - remainingTimeSecs) * 1_000).toInt()

                    Row {
                        Text(
                            remainingTimeSecs.toString(),
                            style = MaterialTheme.typography.h5,
                            modifier = Modifier.align(Alignment.Bottom),
                        )
                        Text(
                            ".$remainingTimeMs",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .padding(bottom = 2.dp),
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }

                val isRunning by mainViewModel.isRunning.observeAsState(false)
                val buttonText = if (isRunning) "Pause" else "Start"
                Text(buttonText, style = MaterialTheme.typography.h6)
            }
        }
    }
}

@ExperimentalComposeUiApi
@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        Row(
            Modifier.width(250.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Timer in secs: ",
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(Modifier.width(16.dp))

            val focusRequester = remember { FocusRequester() }

            TextField(
                modifier = Modifier
                    .width(100.dp)
                    .focusRequester(focusRequester),
                value = "",
                onValueChange = { },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
            )
        }
    }
}

@ExperimentalComposeUiApi
@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        Row(
            Modifier.width(250.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Timer in secs: ",
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(Modifier.width(16.dp))

            TextField(
                modifier = Modifier
                    .width(100.dp),
                value = "",
                onValueChange = { },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
            )
        }
    }
}
