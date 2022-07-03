package com.thingsenz.flashlight

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.style.ClickableSpan
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
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.thingsenz.flashlight.ui.theme.FlashlightTheme

class MainActivity : ComponentActivity() {

    private var cameraManager: CameraManager? = null
    private var sosThread: Thread? = null
    private var sosInterrupt = false
    private var btnColor = mutableStateOf(Color.Red)
    private var btnText = mutableStateOf("OFF")
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
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) {
            initCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
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
                                Icon(Icons.Default.Info, contentDescription = "Info")
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
                                    toggleFlash()
                                } catch (e: Exception) {
                                    Log.e("Flashlight",e.message?: "")
                                } },
                                modifier = Modifier.size(100.dp),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, btnColor.value)
                            ) {
                                Text(text = btnText.value,style = TextStyle(color = btnColor.value,fontSize = 18.sp),maxLines = 1)
                            }
                            if (openPermDialog.value) {
                                showPermDialog()
                            }
                            if (openInfoDialog.value) {
                                showInfoDialog()
                            }
                        }
                    }
                }

            }
        }
    }

    private fun initCamera() {
       /* cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            Log.d("FL","Binding")
            val preview = androidx.camera.core.Preview.Builder().build()
            val camSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            val imageCapture = ImageCapture.Builder()
                .setFlashMode(flashMode).build()
            cameraProviderFuture.get().unbindAll()
            camera = cameraProviderFuture.get().bindToLifecycle(this,camSelector,preview,imageCapture)
            Log.d("FL","Done")
        },ContextCompat.getMainExecutor(this))*/
        if (cameraManager==null)
            cameraManager = getSystemService(CameraManager::class.java)
        cameraId = cameraManager!!.cameraIdList[0]
        val c = cameraManager!!.getCameraCharacteristics(cameraId)
        if (c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == false) {
            hasFlash=false
            Toast.makeText(this,"Selected Camera does not support Flash",Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFlash() {
        try {
            if (!hasFlash) {
                Toast.makeText(this,"Selected Camera does not support Flash",Toast.LENGTH_SHORT).show()
                return
            }
            flashMode = if (flashMode) {
                btnColor.value = Color.Red
                btnText.value = "OFF"
                false
            } else {
                btnColor.value = Color.Green
                btnText.value = "ON"
                true
            }
            cameraManager?.setTorchMode(cameraId, flashMode)
        } catch (e: Exception) {
            Log.e("Flashlight",e.message?: "")
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun finish() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            finishAndRemoveTask()
        else
            super.finish()
    }

    private fun sosMode() {
        if (sosThread==null) {
            sosInterrupt=false
            sosThread=Thread {
                while (!sosInterrupt) {
                    toggleFlash()
                    try {
                        Thread.sleep(1000L)
                    } catch (e: Exception) {

                    }
                }
            }
            sosThread?.start()
        } else {
            sosInterrupt=true
            sosThread?.interrupt()
            sosThread=null
        }
    }

    private fun stroboscope() {
        //TODO(Implement)
    }

    override fun onDestroy() {
        if (flashMode) {
            cameraManager?.setTorchMode(cameraId,false)
        }
        cameraManager = null
        super.onDestroy()
    }

    @Composable
    fun showPermDialog() {
        Dialog(onDismissRequest = {  }) {
            Card(shape = RoundedCornerShape(10.dp),modifier = Modifier.padding(10.dp,5.dp,10.dp,10.dp),elevation = 10.dp) {
                Column(modifier = Modifier.background(MaterialTheme.colors.background)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Camera Permission Required", textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(5.dp)
                                .fillMaxWidth(),style = MaterialTheme.typography.subtitle1,
                            overflow = TextOverflow.Ellipsis)
                        Text(text = if (msgType.value==0) "Camera Permission is required to use Flashlight. Clicking allow will request for permission again. Clicking Deny will close the app" else "Camera permission is required to use flashlight. Please grant from settings. Clicking Deny will close the app")
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
    fun showInfoDialog() {
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

