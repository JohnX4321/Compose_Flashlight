package com.thingsenz.flashlight

import android.Manifest
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import com.thingsenz.flashlight.ui.theme.FlashlightTheme

class MainActivity : ComponentActivity() {

    private var cameraManager: CameraManager? = null
    private var sosThread: Thread? = null
    private var sosInterrupt = true
    private var btnColor = mutableStateOf(Color.Red)
    private var btnText = mutableStateOf("OFF")
    private var sosColor = mutableStateOf(Color.Red)
    private var openPermDialog = mutableStateOf(false)
    private var openInfoDialog = mutableStateOf(false)
    private var msgType = mutableStateOf(0)
    private var cameraId = ""
    private var flashMode = false
    private var hasFlash = true

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            initCamera()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)) {
                msgType.value=0
            } else {
                msgType.value=1
            }
            openPermDialog.value=true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCamera()
        setContent {
            FlashlightTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(topBar = {
                    TopAppBar(title = {Text(text = "Flashlight",color = MaterialTheme.colors.secondary)},backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White,actions = {
                        Row(modifier = Modifier.padding(end = 16.dp)) {
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(onClick = { openInfoDialog.value=true }, modifier = Modifier
                                .then(Modifier.size(40.dp))
                                .border(1.dp, MaterialTheme.colors.secondary, shape = CircleShape)) {
                                Icon(Icons.Default.Info, contentDescription = "Info",tint = MaterialTheme.colors.secondary)
                            }
                        }
                    })
                }) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround,modifier = Modifier.fillMaxHeight()) {
                            OutlinedButton(
                                onClick = { try {
                                    if (!sosInterrupt) {
                                        sosMode()
                                    }
                                    toggleFlash()
                                } catch (e: Exception) {
                                    Toast.makeText(this@MainActivity,"An Unknown Error occurred. Kindly operate through device Flashlight feature",Toast.LENGTH_LONG).show()
                                    Log.e("MainActivity",e.message?: "")
                                } },
                                modifier = Modifier.size(100.dp),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, btnColor.value)
                            ) {
                                Text(text = btnText.value,style = TextStyle(color = btnColor.value,fontSize = 18.sp),maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { try {
                                    sosMode()
                                } catch (e: Exception) {
                                    Log.e("MainActivity",e.message?: "")
                                } },
                                modifier = Modifier.size(100.dp),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, sosColor.value)
                            ) {
                                Text(text = "SOS",style = TextStyle(color = sosColor.value,fontSize = 18.sp),maxLines = 1)
                            }

                            if (openInfoDialog.value) {
                                ShowInfoDialog()
                            }
                        }
                    }
                }

            }
        }
    }

    private fun initCamera() {
        if (cameraManager==null)
            cameraManager = getSystemService(CameraManager::class.java)
        cameraId = cameraManager!!.cameraIdList[0]
        val c = cameraManager!!.getCameraCharacteristics(cameraId)
        if (c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == false) {
            hasFlash=false
            Toast.makeText(this,"Selected Camera does not support Flash",Toast.LENGTH_SHORT).show()
        }
    }

    @Synchronized
    private fun toggleFlash() {
            if (!hasFlash) {
                Toast.makeText(this,"Selected Camera does not support Flash",Toast.LENGTH_SHORT).show()
                return
            }
            flashMode = if (flashMode) {
                if (sosInterrupt) {
                    btnColor.value = Color.Red
                    btnText.value = "OFF"
                }
                false
            } else {
                if (sosInterrupt) {
                    btnColor.value = Color.Green
                    btnText.value = "ON"
                }
                true
            }
            cameraManager?.setTorchMode(cameraId, flashMode)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun finish() {
        finishAndRemoveTask()
    }

    private fun sosMode() {
        if (sosThread==null) {
            sosInterrupt=false
            sosColor.value = Color.Green
            sosThread=Thread {
                while (!sosInterrupt) {
                    try {
                        if (flashMode)
                            toggleFlash()
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(1000L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(1000L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(1000L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(250L)
                        toggleFlash()
                        Thread.sleep(500L)
                    } catch (e: Exception) {
                        try {
                            if (flashMode)
                                toggleFlash()
                        } catch (ignored: Exception) {}
                        Log.e("Flashlight",e.message?: "")
                    }
                }
            }
            sosThread?.start()
        } else {
            try {
                sosInterrupt = true
                sosThread?.interrupt()
                sosThread = null
                sosColor.value = Color.Red
            } catch (e: Exception) {

            }
        }
    }

    private fun stroboscope() {
        //TODO(Implement)
    }

    override fun onDestroy() {
        if (sosThread!=null) {
            sosMode()
        }
        if (flashMode) {
            cameraManager?.setTorchMode(cameraId,false)
        }
        cameraManager = null
        super.onDestroy()
    }

    @Composable
    fun ShowPermDialog() {
        Dialog(onDismissRequest = {  }) {
            Card(shape = RoundedCornerShape(10.dp),modifier = Modifier.padding(10.dp,5.dp,10.dp,10.dp),elevation = 10.dp) {
                Column(modifier = Modifier.background(MaterialTheme.colors.background)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Camera Permission Required", textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(5.dp)
                                .fillMaxWidth(),style = MaterialTheme.typography.subtitle1,
                            overflow = TextOverflow.Ellipsis, color = if (isSystemInDarkTheme()) Color.White else Color.Black)
                        Text(text = if (msgType.value==0) "Camera Permission is required to use Flashlight. Clicking allow will request for permission again. Clicking Deny will close the app" else "Camera permission is required to use flashlight. Please grant from settings. Clicking Deny will close the app", color = if (isSystemInDarkTheme()) Color.White else Color.Black)
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(MaterialTheme.colors.background)) {
                        TextButton(onClick = { openPermDialog.value=false
                            finish()
                        }) {
                            Text(text = "Deny",fontWeight = FontWeight.SemiBold,color = Color.Blue,modifier = Modifier.padding(top = 5.dp,bottom = 5.dp))
                        }
                        TextButton(onClick = {
                            openPermDialog.value=false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },enabled = msgType.value==0) {
                            Text(text = "Allow",fontWeight = FontWeight.SemiBold,color = if (msgType.value==0) Color.Blue else Color.Gray,modifier = Modifier.padding(top = 5.dp,bottom = 5.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ShowInfoDialog() {
        Dialog(onDismissRequest = {  }) {
            Card(shape = RoundedCornerShape(10.dp),modifier = Modifier.padding(10.dp,5.dp,10.dp,10.dp),elevation = 10.dp) {
                Column(modifier = Modifier.background(MaterialTheme.colors.background)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "About", textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(5.dp)
                                .fillMaxWidth(),style = MaterialTheme.typography.subtitle1,
                            overflow = TextOverflow.Ellipsis)
                        Text(text = buildAnnotatedString {
                            append("Flashlight\n\n")
                            append("Version: ${BuildConfig.VERSION_NAME}\n")
                        })
                        ClickableText(text = AnnotatedString(text = "Code Repository",spanStyle = SpanStyle(color = Color.Blue,textDecoration = TextDecoration.Underline)), onClick = {
                            startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/JohnX4321/Compose_Flashlight")
                            ))
                        })
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(MaterialTheme.colors.background)) {
                        TextButton(onClick = { openInfoDialog.value=false
                        }) {
                            Text(text = "Close",fontWeight = FontWeight.SemiBold,color = Color.Blue,modifier = Modifier.padding(top = 5.dp,bottom = 5.dp))
                        }
                    }
                }
            }
        }
    }

}



@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FlashlightTheme {
        Greeting("Android")
    }
}

