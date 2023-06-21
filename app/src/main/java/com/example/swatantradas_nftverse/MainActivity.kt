package com.example.swatantradas_nftverse

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.scanlibrary.ScanActivity
import com.scanlibrary.ScanConstants
import java.io.IOException
import java.io.OutputStream
import java.util.Objects

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var openGallery: View
    private lateinit var saveButton: ConstraintLayout
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private var writePermissionGranted = false
    var REQUEST_CODE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        openGallery = findViewById(R.id.btnOpenStorage)
        saveButton = findViewById(R.id.saveButton)

        openGallery.setOnClickListener {
            var preference  = ScanConstants.OPEN_MEDIA
            var intent = Intent(this,ScanActivity::class.java)
            intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE,preference)
            startActivityForResult(intent,REQUEST_CODE)
        }

        saveButton.setOnClickListener {
            if(imageView.drawable == null)
            {
                Toast.makeText(this,"Select an image first",Toast.LENGTH_SHORT).show()
            }
            else
            {
                updateOrRequestPermissions()
                saveImage()
            }
        }


    }

    private fun updateOrRequestPermissions()
    {
        val hasWritePermission = ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val minsdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        writePermissionGranted = hasWritePermission || minsdk29


        val permissionToRequest = mutableListOf<String>()
        if(!writePermissionGranted){
            permissionToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if(permissionToRequest.isNotEmpty())
        {
            permissionLauncher.launch(permissionToRequest.toTypedArray())
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 198)
        {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                saveImage()
            }
            else
            {
                Toast.makeText(this,"Please provide required permissions first",Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun saveImage() {

        var images: Uri
        var contentResolver: ContentResolver = contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            images = Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }else{
            images = Media.EXTERNAL_CONTENT_URI
        }

        var cv = ContentValues()
        cv.put(Media.DISPLAY_NAME,"${System.currentTimeMillis()}.jpg")
        cv.put(Media.MIME_TYPE, "images/*")

        var uri = contentResolver.insert(images,cv)

        try {
            var bitmapDrawable = imageView.drawable as BitmapDrawable
            var bitmap = bitmapDrawable.bitmap
            var outputStream = Objects.requireNonNull(uri)?.let { contentResolver.openOutputStream(it) } as OutputStream
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
            Objects.requireNonNull(outputStream)


            Toast.makeText(this,"Image saved successfully!",Toast.LENGTH_SHORT).show()

        }
        catch (e: Exception){
            Toast.makeText(this,"Failed to save image",Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            if(data!=null)
            {
                var uri: Uri = data.extras?.getParcelable(ScanConstants.SCANNED_RESULT)!!
                var bitmap: Bitmap
                try{
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver,uri)
                    contentResolver.delete(uri,null,null)
                    imageView.setImageBitmap(bitmap)
                    //var fos: FileOutputStream = FileOutputStream(bitmap)
                }catch (e: IOException)
                {
                    e.printStackTrace()
                }
            }

        }
    }
}