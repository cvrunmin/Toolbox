package io.github.cvrunmin.toolbox

import android.app.Activity
import android.provider.MediaStore

fun Activity.getRealPathFromUri(uri : android.net.Uri) : String
{
    var filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
    this.contentResolver.query(uri, filePathColumn, null, null, null).use {
        if(it != null){
            if (it.moveToFirst()){
                var columnIndex = it.getColumnIndex(filePathColumn[0])
                return it.getString(columnIndex)
            }
        }else {
            return ""
            //boooo, cursor doesn't have rows ...
        }
    }
    return ""
}

fun Activity.getRealPathFromUri(uri : String) : String{
    var column = arrayOf(MediaStore.Images.Media.DATA)
    var id = uri.split(':')[1]

    var sel = MediaStore.Images.ImageColumns._ID + "=?"
    this.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, arrayOf(id), null).use {
        var columnIndex = it.getColumnIndex(column[0])

        if(it.moveToFirst()){
            return it.getString(columnIndex)
        }else{
            return id
        }
    }
}

fun isExternalStorageDocument (uri : android.net.Uri) : Boolean
{
    return "com.android.externalstorage.documents".equals(uri.authority);
}
