package com.realityexpander.simplenetworkcalls

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.realityexpander.simplenetworkcalls.ui.theme.SimpleNetworkCallsTheme

// Setup for ProxyMan network monitoring.
// Be sure to setup proxy on the phone!
// Go to "help -> enter android" in ProxyMan for setup steps.

// Check this SO: https://stackoverflow.com/questions/3324717/sending-http-post-request-in-java

class MainActivity : ComponentActivity() {

    val viewModel by lazy { MainViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        StrictMode.enableDefaults()

        super.onCreate(savedInstanceState)
        setContent {
            SimpleNetworkCallsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val user0 by viewModel.user0.collectAsState()
                    val user1 by viewModel.user1.collectAsState()
                    val user2 by viewModel.user2.collectAsState()
                    val user3 by viewModel.user3.collectAsState()
                    val user4 by viewModel.user4.collectAsState()

                    Column {
                        ShowUser(user0)
                        ShowUser(user1)
                        ShowUser(user2)
                        ShowUser(user3)
                        ShowUser(user4)
                    }
                }
            }
        }
    }
}

@Composable
fun ShowUser(userEntity: UserEntity?) {
    if (userEntity != null) {
        Text(text = "User: ${userEntity.id} = ${userEntity.firstName} ${userEntity.lastName}")
    } else {
        Text(text = "Loading...")
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SimpleNetworkCallsTheme {
        ShowUser(
            UserEntity("0001",
                "Bob",
                "Smith",
                "bob@smith.com",
                "123-456-7890"
            )
        )
    }
}