package uz.fath.filecrypto

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import com.rockaport.alice.Alice
import com.rockaport.alice.AliceContext
import com.rockaport.alice.AliceContextBuilder
import uz.fath.filecrypto.databinding.ActivityMainBinding
import uz.fath.filecrypto.utils.getName
import uz.fath.filecrypto.utils.gone
import uz.fath.filecrypto.utils.visible
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var loadData: ByteArray? = null
    private var cryptoFileName = ""
    private var cryptoData: ByteArray? = null
    private var encryption = true


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val aliceContext = AliceContextBuilder()
            .setAlgorithm(AliceContext.Algorithm.AES)
            .setMode(AliceContext.Mode.GCM)
            .setIvLength(16)
            .setGcmTagLength(AliceContext.GcmTagLength.BITS_128)
            .build()

        val alice = Alice(aliceContext)


        binding.apply {

            fun visible() {
                correctLayout.visible()
                saveLayout.visible()
                fileNameLayout.visible()
            }

            fun gone() {
                correctLayout.gone()
                saveLayout.gone()
                fileNameLayout.gone()
                selectTv.setText("Select a file")
                keyTv.setText("")
                fileNameTv.setText("")
            }

            segmented2.setOnCheckedChangeListener { _, p1 ->
                if (p1 == binding.button1.id) {
                    encryptBtn.text = "Encrypt"
                    encryption = true
                    gone()

                } else {
                    encryptBtn.text = "Decrypt"
                    encryption = false
                    gone()
                }
            }

            selectTv.setOnClickListener {
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "*/*"
                resultLauncher.launch(intent)
            }

            encryptBtn.setOnClickListener {

                permissionsBuilder(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE).build().send { result ->
                    if (result.allGranted()) {
                        if (loadData?.isNotEmpty() == true) {
                            val key = keyTv.text.toString()
                            if (key != "") {
                                cryptoData = if (encryption) {
                                    alice.encrypt(loadData, key.toCharArray())
                                } else {
                                    alice.decrypt(loadData, key.toCharArray())
                                }
                                visible()
                                fileNameTv.setText(cryptoFileName)
                            } else {
                                Toast.makeText(this@MainActivity, "Enter key", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "Load file", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }


            }

            saveBtn.setOnClickListener {
                saveFileStorage(fileNameTv.text.toString(), cryptoData)
                Toast.makeText(this@MainActivity, "File Saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveFileStorage(fileName: String, fileData: ByteArray?) {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val file = File(path, fileName)
        val bos = BufferedOutputStream(FileOutputStream(file))
        bos.write(fileData)
        bos.flush()
        bos.close()
    }


    private val resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Uri = result.data?.data ?: Uri.EMPTY
            val fileName = data.getName(this)
            val inputStream = contentResolver.openInputStream(data)
            Toast.makeText(this, data.path, Toast.LENGTH_SHORT).show()

            loadData = inputStream?.readBytes()

            cryptoFileName = if (encryption) {
                "enc-$fileName"
            } else {
                fileName.substring(4)
            }
            binding.selectTv.setText(fileName)
        }
    }


}