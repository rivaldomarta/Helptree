package com.dicoding.picodiploma.helptree.ui.picture

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.helptree.R
import com.dicoding.picodiploma.helptree.databinding.ActivityTakePictureBinding
import com.dicoding.picodiploma.helptree.ml.CNNBiasa2
import com.dicoding.picodiploma.helptree.ui.chatbot.ChatbotActivity
import com.dicoding.picodiploma.helptree.ui.information.InfoActivity
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class TakePictureActivity : AppCompatActivity() {

    private var _activityTakePictureBinding:ActivityTakePictureBinding? = null
    private val activityTakePictureBinding get() = _activityTakePictureBinding
    private lateinit var bitmap: Bitmap
    private lateinit var imgView: ImageView
    private var nameDisease: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _activityTakePictureBinding = ActivityTakePictureBinding.inflate(layoutInflater)
        setContentView(activityTakePictureBinding?.root)

        imgView = activityTakePictureBinding?.resultImg!!
        val filename = "labels.txt"
        val filenamedetail = "details.txt"
        val inputString = application.assets.open(filename).bufferedReader().use { it.readText() }
        val inputStringdetail = application.assets.open(filenamedetail).bufferedReader().use { it.readText() }
        val diseaselist = inputString.split("\n")
        val diseaseDetails = inputStringdetail.split("\n")

        val select = activityTakePictureBinding?.btnSelect
        val reset = activityTakePictureBinding?.btnReset
        val solusi = activityTakePictureBinding?.btnSolusi
        val info = activityTakePictureBinding?.btnInfoDetail

        info?.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        solusi?.setOnClickListener {
            if (activityTakePictureBinding?.tvPrediction?.text != "Nama Penyakit") {
                val intent = Intent(this, ChatbotActivity::class.java)
                intent.putExtra("DISEASE_NAME", nameDisease)
                startActivity(intent)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Silahkan Pilih Gambar Terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        reset?.setOnClickListener {
            activityTakePictureBinding?.tvPrediction?.text = getString(R.string.tvpred)
            activityTakePictureBinding?.resultImg?.setImageResource(0)
        }

        select?.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"

            startActivityForResult(intent, 100)
        }

        val predict = activityTakePictureBinding?.btnPredict
        val tvPredict = activityTakePictureBinding?.tvPrediction
        val tvDetail = activityTakePictureBinding?.tvDetails

        predict?.setOnClickListener {
            if (activityTakePictureBinding?.resultImg?.drawable != null) {
                val imageProcessor = ImageProcessor.Builder()
                    .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                    .build()

                var tImage = TensorImage(DataType.FLOAT32)

                tImage.load(bitmap)
                tImage = imageProcessor.process(tImage)

                val probabilityProcessor =
                    TensorProcessor.Builder().add(NormalizeOp(0f, 255f)).build()

                val model = CNNBiasa2.newInstance(this@TakePictureActivity)
                val outputs =
                    model.process(probabilityProcessor.process(tImage.tensorBuffer))
                val outputBuffer = outputs.outputFeature0AsTensorBuffer

                val max = getMax(outputBuffer.floatArray)
                tvPredict?.text = diseaselist[max]
                tvDetail?.text = diseaseDetails[max]
                nameDisease = diseaselist[max]
                model.close()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Silahkan Pilih Gambar Terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        imgView.setImageURI(data?.data)
        val uri: Uri? = data?.data

        try {
            if (Build.VERSION.SDK_INT >= 29) {
                val source = ImageDecoder.createSource(this.contentResolver, uri!!)
                bitmap = ImageDecoder.decodeBitmap(source)
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri!!)
            }
        } catch (e: Exception) {
            println("Could not convert image to BitMap")
            e.printStackTrace()
        }
    }

    private fun getMax(arr: FloatArray):Int{
        var idx = 0
        var min = 0.0f

        for (i in 0..37){
            if(arr[i]>min){
                idx = i
                min = arr[i]
            }
        }
        return idx
    }
}