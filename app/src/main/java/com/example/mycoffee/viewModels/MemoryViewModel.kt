package com.example.mycoffee.viewModels

import android.app.Application
import android.content.Context
import android.os.Environment.*
import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mycoffee.MyApplication
import com.example.mycoffee.R
import com.example.mycoffee.models.File.Companion.currentFolder
import com.example.mycoffee.models.FilePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow


class MemoryViewModel(val app: Application): AndroidViewModel(app) {
    private var rootDirectory: File? = null

    private val context = getApplication<Application>().applicationContext

    private val files: MutableList<com.example.mycoffee.models.File?> = mutableListOf()
    private var filesPaths: MutableList<FilePath> = mutableListOf()

    var filesListMutableLiveData: MutableLiveData<MutableList<com.example.mycoffee.models.File?>> = MutableLiveData()
    private var pathListMutableLiveData: MutableLiveData<MutableList<FilePath>> = MutableLiveData()

    init {
        viewModelScope.launch {
            initializeMemoryFragment()
        }
    }

    private suspend fun initializeMemoryFragment() = withContext(Dispatchers.IO) {
        rootDirectory = getExternalStorageDirectory()
        currentFolder = rootDirectory
        filesPaths.add(FilePath(currentFolder!!.path, R.drawable.ic_path_seperator))
        val filesList = rootDirectory?.listFiles()
        for (position in filesList!!.indices)
            if (filesList[position].isDirectory) {
                files.add(
                    com.example.mycoffee.models.File(
                        filesList[position], readableFileSize(fileSize(filesList[position]))
                    , getSubFoldersQuantity(context, filesList[position]))
                )
            } else
                files.add(
                    com.example.mycoffee.models.File(
                        filesList[position], readableFileSize(fileSize(filesList[position]))
                    , getSubFoldersQuantity(context, filesList[position]))
                )
        com.example.mycoffee.models.File.initialFilesList = files
        FilePath.initialFilesPathList = filesPaths
        filesListMutableLiveData.postValue(files)
        pathListMutableLiveData.postValue(filesPaths)
    }

    var fileSize: (File) -> Long = size@{
        if(!it.exists())
            return@size 0
        if(!it.isDirectory)
            return@size it.length()
        val dirs: MutableList<File> = LinkedList()
        dirs.add(it)
        var result: Long = 0
        while (dirs.isNotEmpty()) {
            val dir = dirs.removeAt(0)
            if (!dir.exists()) continue
            val listFiles = dir.listFiles()
            if (listFiles == null || listFiles.isEmpty()) continue
            for (child in listFiles) {
                result += child.length()
                if (child.isDirectory) dirs.add(child)
            }
        }
        return@size result
    }

    private fun getFolderPath(pathList: List<String?>, position: Int): String {
        val path = StringBuilder()
        for (x in 0..position) {
            path.append("/")
            path.append(pathList[x])
        }
        return path.toString()
    }

    fun deleteFileOrDirectory(foldersList: List<com.example.mycoffee.models.File>) {
        for (x in foldersList.indices.reversed())
            foldersList[x].file.deleteRecursively()
    }

    fun moveFileOrDirectory(sourceFile: File, destinationFile: File) {
        sourceFile.copyRecursively(destinationFile, false, onError = { _, ioException -> throw ioException })
        sourceFile.deleteRecursively()
    }

    fun copyFileOrDirectory(sourceFile: File, destinationFile: File) {
        sourceFile.copyRecursively(destinationFile, false, onError = { _, ioException -> throw ioException })
    }

    fun renameFileOrFolder(file: File, string: String){
        file.renameTo(File(string))
    }

    suspend fun sort(sort: String, isReverse: Boolean) = withContext(Dispatchers.IO){
        when (sort) {
            "small to large" -> sortFromSmallToLarge(isReverse)
//            "old to new" -> sortFromOldToNew(isReverse)
//            "a to z" -> sortFromAToZ(isReverse)
        }
    }

    private fun sortFromAToZ(foldersList: List<com.example.mycoffee.models.File>, isReverse: Boolean) {
        if (!isReverse) {
//            Collections.sort(foldersList) { folders, f2 -> folders.file.name.lowercase()
//                .compareTo(f2.file.name.lowercase())
//            }
            foldersList.sortedWith { f1, f2 -> f1.file.name.lowercase().compareTo(f2.file.name.lowercase()) }
        }
        else {
            foldersList.sortedWith{ f1, f2 -> f1.file.name.lowercase().compareTo(f2.file.name.lowercase()) }
                .reversed()
//            Collections.sort(foldersList) { folders, f2 -> folders.file.name.lowercase()
//                    .compareTo(f2.file.name.lowercase())
//            }
//            Collections.reverse(foldersList)
//            foldersList.reversed()
        }
    }

    private fun sortFromOldToNew(foldersList: MutableList<com.example.mycoffee.models.File>, isReverse: Boolean) {
        if (!isReverse)
            foldersList.sortWith { folders, f2 -> folders.file.lastModified().compareTo(f2.file.lastModified()) }
        else {
            foldersList.sortWith { folders, f2 ->
                folders.file.lastModified().compareTo(f2.file.lastModified())
            }
            foldersList.reverse()
        }
    }

    private fun sortFromSmallToLarge(isReverse: Boolean) {
        if (!isReverse)
            filesListMutableLiveData.postValue(files.sortedWith { f1, f2 -> f1?.size!!.compareTo(f2!!.size)}.toMutableList())
//            Collections.sort(foldersList) { folders, f2 -> java.lang.Long.compare(folders.size, f2.size) }
        else {
            filesListMutableLiveData.postValue(files.sortedWith { f1, f2 -> f1?.size!!.compareTo(f2!!.size)}.reversed().toMutableList())
//            Collections.sort(foldersList) { folders, f2 ->
//                folders.size.compareTo(f2.size)
//            }
//            Collections.reverse(foldersList)
        }
    }

    suspend fun backToFolder(backToPathList: MutableList<FilePath>, file: File, showHidden: Boolean) =
        withContext(Dispatchers.IO) {
            filesPaths = backToPathList.toMutableList()
            pathListMutableLiveData.postValue(backToPathList)
            val newFoldersList = mutableListOf<com.example.mycoffee.models.File>()
            val newFolders = file.listFiles()
            if (newFolders != null)
                if (showHidden)
                    for (newFolder in newFolders) {
                        if (newFolder.isDirectory)
                            newFoldersList.add(com.example.mycoffee.models.File(newFolder,
                                readableFileSize(fileSize(newFolder)), getSubFoldersQuantity(context, newFolder)))
                        else
                            newFoldersList.add(com.example.mycoffee.models.File(newFolder,
                                readableFileSize(fileSize(newFolder)), getSubFoldersQuantity(context, newFolder)))
                    }
                else
                    for (folder in newFolders) {
                        if (!folder.name.startsWith("."))
                            if (folder.isDirectory) newFoldersList.add(
                                com.example.mycoffee.models.File(folder,
                                    readableFileSize(fileSize(folder)), getSubFoldersQuantity(context, folder)))
                            else
                                newFoldersList.add(com.example.mycoffee.models.File(folder,
                                    readableFileSize(fileSize(folder)), getSubFoldersQuantity(context, folder)))
                    }
            filesListMutableLiveData.postValue(newFoldersList.toMutableList())
        }

    suspend fun newFolders(file: File, showHidden: Boolean) =  withContext(Dispatchers.IO){
        if (file.isDirectory) {
            val newFilesList = mutableListOf<com.example.mycoffee.models.File>()
            val newFolders = file.listFiles()
            if (newFolders != null)
                if (showHidden)
                    for (newFolder in newFolders) {
                        if (newFolder.isDirectory)
                            newFilesList.add(com.example.mycoffee.models.File(newFolder,
                                readableFileSize(fileSize(newFolder)), getSubFoldersQuantity(context, newFolder)))
                        else
                            newFilesList.add(com.example.mycoffee.models.File(newFolder,
                                readableFileSize(fileSize(newFolder)), getSubFoldersQuantity(context, newFolder)))
                    }
                else
                    for (folder in newFolders) {
                        if (!folder.name.startsWith("."))
                            if (folder.isDirectory) newFilesList.add(
                                com.example.mycoffee.models.File(folder,
                                    readableFileSize(fileSize(folder)), getSubFoldersQuantity(context, folder)))
                            else
                                newFilesList.add(com.example.mycoffee.models.File(folder,
                                    readableFileSize(fileSize(folder)), getSubFoldersQuantity(context, folder)))
                    }
            filesListMutableLiveData.postValue(newFilesList.toMutableList())
            currentFolder = file
            filesPaths.add(FilePath(file.name, R.drawable.ic_path_seperator))
            pathListMutableLiveData.postValue(filesPaths.toMutableList())
        }
    }

    private fun readableFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble()))
            .toString() + " " + units[digitGroups]
    }

    companion object{
        @JvmStatic
        @BindingAdapter("getDateModified")
        fun getDateModified(textView: TextView, file: Long) {
            val date = Date(file)
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
            textView.text = simpleDateFormat.format(date)
        }
    }

//    suspend fun getCheckedFoldersNames(foldersList: List<com.example.mycoffee.models.File?>?): MutableList<String?>? {
//        for (folders in foldersList!!)
//            if (folders!!.isSelected)
//                foldersName!!.add(folders.file.name)
//        return foldersName
//    }

//    suspend fun getCheckedFoldersPaths(foldersList: List<com.example.mycoffee.models.File?>?): MutableList<String?>? {
//        for (folders in foldersList!!)
//            if (folders!!.isSelected) {
//                foldersPath!!.add(folders.file.absolutePath)
//            }
//        return foldersPath
//    }


    private fun getSubFoldersQuantity(context: Context, file: File): String {
        if (file.isDirectory) {
            val subFoldersFiles = file.listFiles()
            if (subFoldersFiles != null) return subFoldersFiles.size.toString() + " " + context.resources
                .getString(R.string.folders_files_quantity)
        }
        return ""
    }

    fun searchFiles(newText: String?) {
        val listToFilter = filesListMutableLiveData.value
        var filteredList: MutableList<com.example.mycoffee.models.File?>? = null
        listToFilter?.apply {
            filteredList = filter { it?.file?.name?.lowercase()?.contains(newText.toString().lowercase())!! }.toMutableList()
        }
        filesListMutableLiveData.postValue(filteredList?.toMutableList())
    }

    fun getMyFilesListMutableLiveData() = filesListMutableLiveData
    fun getPathsListMutableLiveData() = pathListMutableLiveData
}