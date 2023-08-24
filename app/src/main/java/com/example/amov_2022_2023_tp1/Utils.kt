package com.example.amov_2022_2023_tp1

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaDrm
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

// Logs
val TAG : String = "LOG"

//--------------------------------------------------------------
// Permissions
//--------------------------------------------------------------
val PERMISSION_REQUEST_CODE: Int = 101

fun checkPermissions(context: Context) : Boolean {
    Log.d(TAG,"Checking permissions...")
    return if (hasPermissions(context)) {
        Log.d(TAG,"App has all the permissions!")
        true
    } else {
        Log.d(TAG,"App doesn't have all the permissions!")
        false
    }
}
fun hasPermissions(context : Context): Boolean {
    return (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED                    &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED     &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED    &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
            )
}
fun requestPermissions(context : Context) {
    Log.d(TAG,"Requesting permissions...")
    return ActivityCompat.requestPermissions(
        context as Activity, arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        ), PERMISSION_REQUEST_CODE)
}

//--------------------------------------------------------------
// Language Change
//--------------------------------------------------------------
fun selectLang(context: Context, language: String) : String {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.createConfigurationContext(config)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    return language
}

//--------------------------------------------------------------
// DATE
//--------------------------------------------------------------
@SuppressLint("SimpleDateFormat")
fun getDateTime() : String {
    val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss.SSS")
    return sdf.format(Date())
}

//--------------------------------------------------------------
// Encoding & Decoding (camera bitmap)
//--------------------------------------------------------------
fun encodeToBase64(image: Bitmap): String {
    val baos = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val b: ByteArray = baos.toByteArray()
    val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
    Log.d(TAG, "Image was encoded!")
    return imageEncoded
}

fun decodeBase64(input: String?): Bitmap {
    val decodedByte = Base64.decode(input, 0)
    Log.d(TAG, "Image was decoded!")
    return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
}

//--------------------------------------------------------------
// Generating a Unique ID
//--------------------------------------------------------------
fun byteArrayToHex(byteArray: ByteArray): String {
    var result : String = ""
    for (b in byteArray) {
        val st = String.format("%02X", b)
        result += st
    }
    return result
}

fun getUniqueID() : String {
    val wideVineUuid : UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
    return try {
        val wvDrm : MediaDrm = MediaDrm(wideVineUuid);
        val wideVineId : ByteArray = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);

        val s : String = byteArrayToHex(wideVineId);
        s;
    } catch (e : Exception) {
        "UUID=ERROR"
    }
    // Close resources with close() or release() depending on platform API
    // Use ARM on Android P platform or higher, where MediaDrm has the close() method
}

//--------------------------------------------------------------
// Documents - Firebase
//--------------------------------------------------------------
fun createEmptyUser(){
    val db = Firebase.firestore
    val user = hashMapOf(
        "id" to getUniqueID(),
        "username" to  getUniqueID(),
        "highscore" to  0,
        "created"  to  getDateTime()
    )
    if(getUniqueID().length <= 1) {
        Log.d(TAG, "Error creating user!")
        return
    } else {
        // Add a new document with a ID that is linked to device!
        db.collection("users").document(getUniqueID()).set(user)
    }
    Log.d(TAG,user["username"].toString())
}

fun updateUsername(username: String){
    val db = Firebase.firestore
    if (getUniqueID().length <= 1) {
        Log.d(TAG, "Error updating highscore!")
    } else {
        db.collection("users").document(getUniqueID()).update("username", username)
        Log.d(TAG, "Username updated!")
    }
}

fun updateHighscore(score : Int) {
    val db = Firebase.firestore
    if (getUniqueID().length <= 1) {
        Log.d(TAG, "Error updating highscore!")
        return
    } else {
        if (score > getUserHighscore()) {
            db.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        if (document.id == getUniqueID()) {
                            db.collection("users").document(getUniqueID())
                                .update("highscore", score)
                        }
                    }
                    Log.d(TAG, "Highscore updated!")
                }
        }
    }
}

fun getUserHighscore() : Int {
    val db = Firebase.firestore
    var highscore : Int = -1
    if (getUniqueID().length <= 1) {
        Log.d(TAG, "Error updating highscore!")
    } else {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.id == getUniqueID()) {
                        highscore = document.get("highscore").toString().toInt()
                    }
                }
                Log.d(TAG, "${getUniqueID()} has highscore of: $highscore")
            }
    }
    return highscore
}

fun getUsername(): Deferred<String> {
    val db = Firebase.firestore
    return GlobalScope.async {
        if (getUniqueID().length <= 1) {
            Log.d(TAG, "Error updating highscore!")
            return@async "user"
        }
        val document = db.collection("users").document(getUniqueID()).get().await()
        return@async document.get("username").toString()
    }
}

fun getUserHighscore(u : String) : Int {
    val db = Firebase.firestore
    var highscore : Int = -1
    if (u.length <= 1) {
        Log.d(TAG, "Error updating highscore!")
    } else {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.get("username").toString() == u) {
                        highscore = document.get("highscore").toString().toInt()
                    }
                }
                Log.d(TAG, "$u has highscore of: $highscore")
            }
    }
    return highscore
}

fun getHighestHighscore(myCallback: (String) -> Unit) {
    val db = Firebase.firestore
    var highscore : Int = -1
    var username : String = "username"
    var res : String = ""
    db.collection("users")
        .get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    if (document.get("highscore").toString().toInt() > highscore) {
                        highscore = document.get("highscore").toString().toInt()
                        username = document.get("username").toString()
                        res = "$username : $highscore"
                    }
                }
            }
            myCallback(res)
        }
}

fun getSecondHighestHighscore(myCallback: (String) -> Unit) {
    val db = Firebase.firestore
    var highscore : Int = -1
    var secondHighscore: Int = -1
    var username : String = "username"
    var secondUsername: String = "username"
    var res : String = ""
    db.collection("users")
        .get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    val score = document.get("highscore").toString().toInt()
                    if (score > highscore) {
                        secondHighscore = highscore
                        secondUsername = username
                        highscore = score
                        username = document.get("username").toString()
                    } else if (score > secondHighscore) {
                        secondHighscore = score
                        secondUsername = document.get("username").toString()
                    }
                }
            }
            res = "$secondUsername : $secondHighscore"
            myCallback(res)
        }
}

fun getThirdHighestHighscore(myCallback: (String) -> Unit) {
    val db = Firebase.firestore
    var highscore : Int = -1
    var secondHighscore: Int = -1
    var thirdHighscore: Int = -1
    var username : String = "username"
    var secondUsername: String = "username"
    var thirdUsername: String = "username"
    var res : String = ""
    db.collection("users")
        .get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    val score = document.get("highscore").toString().toInt()
                    if (score > highscore) {
                        thirdHighscore = secondHighscore
                        thirdUsername = secondUsername
                        secondHighscore = highscore
                        secondUsername = username
                        highscore = score
                        username = document.get("username").toString()
                    } else if (score > secondHighscore) {
                        thirdHighscore = secondHighscore
                        thirdUsername = secondUsername
                        secondHighscore = score
                        secondUsername = document.get("username").toString()
                    } else if (score > thirdHighscore) {
                        thirdHighscore = score
                        thirdUsername = document.get("username").toString()
                    }
                }
            }
            res = "$thirdUsername : $thirdHighscore"
            myCallback(res)
        }
}

//--------------------------------------------------------------
// Storage - Firebase
//--------------------------------------------------------------
fun uploadImageToStorage(bitmap: Bitmap) {
    // Create a storage reference from our app
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    // Create a reference to "mountains.jpg"
    val fileImage = storageRef.child(getUniqueID() + ".jpg")
    // Output Stream
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val data = baos.toByteArray()
    // Firestore Upload
    var uploadTask = fileImage.putBytes(data)
    uploadTask.addOnFailureListener {
        Log.d(TAG, "Upload failed!")
    }.addOnSuccessListener { taskSnapshot ->
        Log.d(TAG, "Upload successful!")
    }
}

//----------------------------------------------------------
// Clipboard
//----------------------------------------------------------
fun copyMyID(context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", getUniqueID())
    clipboard.setPrimaryClip(clip)
}

//----------------------------------------------------------
// DialogBox
//----------------------------------------------------------
fun showDialogMsg(title: String?, activity: Activity, context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setCancelable(false);

    if(title == "Language") {
        builder.setTitle("Select Language")
        builder.setMessage("Selecionar Linguagem")
        builder.setPositiveButton(
            "Português"
        ) { dialog, which ->
            selectLang(context, "pt")
            activity.finish()
            activity.startActivity(Intent(activity.intent))
        }
        builder.setNegativeButton("English") { dialog, which ->
            selectLang(context,"en")
            activity.finish()
            activity.startActivity(Intent(activity.intent))
        }.show()
    }
    else {
        builder.setTitle(R.string.game_end_title)
        builder.setMessage(R.string.game_end_msg)
        builder.setPositiveButton(
            "OK"
        ) { dialog, which ->
            activity.finish()
        }.show()
    }
}
