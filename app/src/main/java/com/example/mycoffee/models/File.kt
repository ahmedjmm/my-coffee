package com.example.mycoffee.models

import java.io.File


data class File(val file: File, var size: String, val subFiles: String){
    companion object{
        var currentFolder: File? = null
        lateinit var sortType: String
        var isReverse = false
        var showHidden = null
        var initialFilesList: MutableList<com.example.mycoffee.models.File?> = mutableListOf()
    }
}