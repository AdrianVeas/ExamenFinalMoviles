package com.example.examenfinal

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.examenfinal.databinding.ActivityMainBinding
import com.example.examenfinal.ml.ModelUnquant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.TensorImage.fromBitmap
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var tvOutput:TextView
    private val GALLERY_REQUEST_CODE =123
    var imageSize = 224
    private var outputFeature0 = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        imageView = binding.imageview
        button = binding.btnCaptureImage
        tvOutput = binding.tvoutput
        val buttonLoad= binding.btnLoadImage

        button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                takePicturePreview.launch(null)
            }else{
                requestPermission.launch(android.Manifest.permission.CAMERA)
            }
        }
        buttonLoad.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                onresult.launch(intent)
            }else{
                requestPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        val buttongetdata = binding.btnGetData
        buttongetdata.setOnClickListener {
            if (outputFeature0 != "")
            {
                val intent = Intent(this, ResponseActivity::class.java)
                intent.putExtra("output", outputFeature0)
                startActivity(intent)

            }else{
                Toast.makeText(this, "TAKE A PHOTO O LOAD IMAGE FOR YOU GALLERY", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private val requestPermission= registerForActivityResult(ActivityResultContracts.RequestPermission()){
        granted->
        if (granted){
            takePicturePreview.launch(null)
        }else{
            Toast.makeText(this, "Permission Denied!! Try again", Toast.LENGTH_SHORT).show()
        }
    }
    private val takePicturePreview= registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
        bitmap->
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap)
        }
    }
    private val onresult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->
        Log.i("TAG", "This is the result: ${result.data} ${result.resultCode}")
        onResultReceived(GALLERY_REQUEST_CODE, result)
    }
    private fun onResultReceived(requestCode: Int, result: ActivityResult?){
        when(requestCode){
            GALLERY_REQUEST_CODE->{
                if(result?.resultCode == Activity.RESULT_OK){
                    result.data?.data?.let{ uri->
                        Log.i("TAG", "onResultReceived: $uri")
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                        imageView.setImageBitmap(bitmap)
                        outputGenerator(bitmap)
                    }
                }else{
                    Log.e("TAG","onActivityResult: error in selecting image")
                }
            }
        }
    }
    private fun outputGenerator(bitmap: Bitmap){
        val model = ModelUnquant.newInstance(this)
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())



        // get 1D array of 224 * 224 pixels in image
        val intValues = IntArray(imageSize * imageSize)
        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

        // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
        var pixel = 0
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val `val` = intValues[pixel++] // RGB
                byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 255f))
                byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 255f))
                byteBuffer.putFloat((`val` and 0xFF) * (1f / 255f))
            }
        }
        inputFeature0.loadBuffer(byteBuffer)





        outputFeature0 = outputs.outputFeature0AsTensorBuffer
        model.close()
    }
}

