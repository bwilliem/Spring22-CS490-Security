package com.example.securitytest

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Trace
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.TimingLogger
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File


class MainActivity : AppCompatActivity() {
    var fileUris: ArrayList<String>? = null
    val PICK_PDF_FILE = 2
    var fileNames = ArrayList<String>()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Helps with seeing the text box when keyboard used in emulator
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        val permBtn = findViewById<Button>(R.id.permissionBtn)
        val pdfBtn = findViewById<Button>(R.id.pdfBtn)
        val list = findViewById<ListView>(R.id.fileList)
        val addImageBtn = findViewById<Button>(R.id.addImageBtn)
        val addPdfBtn = findViewById<Button>(R.id.addPdfBtn)
        val addName = findViewById<EditText>(R.id.newFileName)
        val openImageBtn = findViewById<Button>(R.id.openImageBtn)

        // All Files Access Permission prompt dialog
//        if (Build.VERSION.SDK_INT >= 30) {
//            val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
//
//            startActivity(
//                Intent(
//                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
//                    uri
//                )
//            )
//        }

        // Read External Storage Permission prompt dialog
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                    ),
//                    1
//                )
//            }
//

        // Manage Media Request prompt dialog
//        if (SDK_INT >= Build.VERSION_CODES.S) {
//            startActivity(
//                Intent(
//                    Settings.ACTION_REQUEST_MANAGE_MEDIA,
//                    uri,
//                ),
//            )
//        }

        permBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 30 && Environment.isExternalStorageManager()) {
                Toast.makeText(this, R.string.hasPermission, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.noPermission, Toast.LENGTH_LONG).show()
            }

            val fileNames = getFiles()
            val adapt = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                fileNames
            )
            list.adapter = adapt

            list.setOnItemLongClickListener { parent, view, position, id ->
                val selectedFile = fileNames[position]
                val dialogBuilder = AlertDialog.Builder(this)

                dialogBuilder.setMessage("Do you want to open file $selectedFile?")
                    .setCancelable(false)
                    .setPositiveButton("Open", DialogInterface.OnClickListener {
                            dialog, id -> openImage(selectedFile)
                    })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, which -> dialog.cancel()
                    })

                val alert = dialogBuilder.create()

                alert.show()
                true
            }
        }

        pdfBtn.setOnClickListener{
            openFile(MediaStore.Downloads.INTERNAL_CONTENT_URI)
        }

        addImageBtn.setOnClickListener{
            addImage(addName.text.toString())
        }

        addPdfBtn.setOnClickListener{
            addPdf(addName.text.toString())
        }

        openImageBtn.setOnClickListener{
            openImage(addName.text.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getFiles():ArrayList<String> {
        Log.d("MyFile", "enter getfiles()")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
//            MediaStore.Files.FileColumns._ID
        )
//        val uriFile = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uriFile = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

//        val projection = arrayOf(
//            MediaStore.Images.Media.DISPLAY_NAME,
////            MediaStore.Images.Media._ID
//        )
//        val uriFile = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        val cursor = contentResolver.query(uriFile, projection, null, null, null)
//        val displayNameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
//        val idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
        val displayNameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

        fileNames = ArrayList<String>()
//        fileUris = ArrayList<String>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val displayName = displayNameColumn?.let { cursor.getString(it) }
                if (displayName != null) {
                    fileNames.add(displayName)
                }

//                var photoUri: Uri = Uri.withAppendedPath(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    idColumn?.let { cursor.getString(it) }
//                )

                // Get location data using the Exifinterface library.
//                // Exception occurs if ACCESS_MEDIA_LOCATION permission isn't granted.
//                photoUri = MediaStore.setRequireOriginal(photoUri)
//                contentResolver.openInputStream(photoUri)?.use { stream ->
//                    ExifInterface(stream).run {
//                        // If lat/long is null, fall back to the coordinates (0, 0).
//                        val latLong = doubleArrayOf(0.0, 0.0)
//                    }
//                }
//
//                Log.d("MyFile", "photo uri $photoUri")
            }
        }

        Log.d("MyFile", "Filenames: $fileNames")
        return fileNames
    }

    private fun openFile(pickerInitialUri: Uri) {
        Log.d("MyFile", "In Open File")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        startActivityForResult(intent, PICK_PDF_FILE)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun addImage(filename: String) {
        Log.d("MyFile", "New File Added $filename")
        var startTime =   System.nanoTime()

        // Add a specific media item.
        val resolver = applicationContext.contentResolver

        // Find all audio files on the primary external storage device.
        val imageCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

        Log.d("MyTimer", "Get URI took :" + ((System.nanoTime()-startTime)/1000000)+ "mS\n")
        startTime =   System.nanoTime()
        val source = BitmapFactory.decodeResource(resources, R.drawable.rand2)

        Log.d("MyTimer", "Get source took :" + ((System.nanoTime()-startTime)/1000000)+ "mS\n")
        startTime =   System.nanoTime()
        // Publish a new song.
        val newImageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            put(MediaStore.Images.Media.TITLE, filename);
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            // Add the date meta data to ensure the image is added at the front of the gallery
            // Time still not working in file data
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        }
        Log.d("MyTimer", "Make Image took :" + ((System.nanoTime()-startTime)/1000000)+ "mS\n")
        startTime =   System.nanoTime()

        val url = resolver.insert(imageCollection, newImageDetails)

        Log.d("MyTimer", "Insert Image took :" + ((System.nanoTime()-startTime)/1000000)+ "mS\n")
        startTime =   System.nanoTime()

        val imageOut = url?.let { resolver.openOutputStream(it) }
        try {
            source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut)
        } finally {
            imageOut?.close()
        }

        Log.d("MyTimer", "Last Stretch took :" + ((System.nanoTime()-startTime)/1000000)+ "mS\n")
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun addPdf(filename: String) {
        val pageHeight = 1120
        val pagewidth = 792

        var bmp: Bitmap
        var scaledbmp: Bitmap

        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.lorem);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);

        val pdfDocument = PdfDocument()

        // two variables for paint "paint" is used
        // for drawing shapes and we will use "title"
        // for adding text in our PDF file.
        val paint = Paint()
        val title = Paint()


        // we are adding page info to our PDF file
        // in which we will be passing our pageWidth,
        // pageHeight and number of pages and after that
        // we are calling it to create our PDF.
        val mypageInfo = PageInfo.Builder(pagewidth, pageHeight, 1).create()


        // below line is used for setting
        // start page for our PDF file.
        val myPage = pdfDocument.startPage(mypageInfo)

        // creating a variable for canvas
        // from our page of PDF.
        val canvas: Canvas = myPage.canvas

        // below line is used to draw our image on our PDF file.
        // the first parameter of our drawbitmap method is
        // our bitmap
        // second parameter is position from left
        // third parameter is position from top and last
        // one is our variable for paint.
        canvas.drawBitmap(scaledbmp, 56F, 40F, paint)


        // below line is used for adding typeface for
        // our text which we will be adding in our PDF file.
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))

        // below line is used for setting text size
        // which we will be displaying in our PDF file.
        title.setTextSize(15F)

        // below line is sued for setting color
        // of our text inside our PDF file.
        title.setColor(ContextCompat.getColor(this, R.color.purple_200))

        // below line is used for setting
        // our text to center of PDF.
        title.setTextAlign(Paint.Align.CENTER)
        canvas.drawText("This is sample document which we have created.", 396F, 560F, title)

        // after adding all attributes to our
        // PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage)

        val resolver = applicationContext.contentResolver

        val pdfCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        Log.d("MyFile", "Path for add pdf $pdfCollection")
        val newPdfDetails = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, filename);
            put(MediaStore.Files.FileColumns.TITLE, filename);
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/vnd.android.package-archive");
            put(MediaStore.Files.FileColumns.DATE_ADDED, System.currentTimeMillis());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Files.FileColumns.DATE_TAKEN, System.currentTimeMillis())
            };
        }

        val url = resolver.insert(pdfCollection, newPdfDetails)

        val pdfOut = url?.let { resolver.openOutputStream(it) }
        try {
            pdfDocument.writeTo(pdfOut)
        } finally {
            pdfOut?.close()
        }
        pdfDocument.close()
    }

    private fun openImage(filename: String) {
        Log.d("MyFile", "to open $filename")
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val file = File("/storage/emulated/0/Pictures/$filename")
        val uri = FileProvider.getUriForFile(
            this@MainActivity,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
        intent.setDataAndType(uri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent)
    }
}