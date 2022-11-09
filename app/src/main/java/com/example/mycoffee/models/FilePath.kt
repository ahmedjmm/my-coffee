package com.example.mycoffee.models

data class FilePath(val filePath: String, val fileSeparator: Int) {

    companion object{
        var initialFilesPathList: MutableList<FilePath> = mutableListOf()
    }
}