package uz.fath.filecrypto.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View


fun View.visible(){
    this.visibility = View.VISIBLE
}

fun View.gone(){
    this.visibility = View.GONE
}

fun Uri.getName(context: Context): String {
    val returnCursor : Cursor= context.contentResolver.query(this, null, null, null, null)!!
    val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor.moveToFirst()
    val fileName = returnCursor.getString(nameIndex)
    returnCursor.close()
    return fileName
}