package com.dicoding.picodiploma.helptree.ui.picture

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import com.dicoding.picodiploma.helptree.R
import com.dicoding.picodiploma.helptree.databinding.ActivityTakePictureBinding
import com.dicoding.picodiploma.helptree.ml.AlexQuant
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _activityTakePictureBinding = ActivityTakePictureBinding.inflate(layoutInflater)
        setContentView(activityTakePictureBinding?.root)

        imgView = activityTakePictureBinding?.resultImg!!
        val filename = "labels.txt"
        val inputString = application.assets.open(filename).bufferedReader().use { it.readText() }
        val townlist = inputString.split("\n")

        val select = activityTakePictureBinding?.btnSelect
        val reset = activityTakePictureBinding?.btnReset
        val solusi = activityTakePictureBinding?.btnSolusi

        reset?.setOnClickListener(View.OnClickListener {
            activityTakePictureBinding?.tvPrediction?.text = getString(R.string.tvpred)
            activityTakePictureBinding?.resultImg?.setImageResource(0)
        })

        select?.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"

            startActivityForResult(intent, 100)
        })

        val predict = activityTakePictureBinding?.btnPredict
        val tvPredict = activityTakePictureBinding?.tvPrediction
        predict?.setOnClickListener(View.OnClickListener {
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                .build()

            var tImage = TensorImage(DataType.FLOAT32)

            tImage.load(bitmap)
            tImage = imageProcessor.process(tImage)

            val probabilityProcessor =
                TensorProcessor.Builder().add(NormalizeOp(0f, 255f)).build()

            val model = AlexQuant.newInstance(this@TakePictureActivity)
            val outputs =
                model.process(probabilityProcessor.process(tImage.tensorBuffer))
            val outputBuffer = outputs.outputFeature0AsTensorBuffer

            val max = getMax(outputBuffer.floatArray)
            tvPredict?.text = townlist[max]
            model.close()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imgView.setImageURI(data?.data)

        var uri:Uri? = data?.data
        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

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