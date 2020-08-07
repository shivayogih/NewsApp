package com.springr.newsapplication.ui.fragments

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment

import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.springr.newsapplication.R
import com.springr.newsapplication.models.Article
import com.springr.newsapplication.ui.NewsActivity
import com.springr.newsapplication.ui.NewsViewModel
import com.springr.newsapplication.util.AppAlerts
import com.springr.newsapplication.util.PermissionUtils
import kotlinx.android.synthetic.main.fragment_article.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

class ArticleFragment : Fragment(R.layout.fragment_article),EasyPermissions.PermissionCallbacks {

    lateinit var viewModel: NewsViewModel
    val args: ArticleFragmentArgs by navArgs()
     var article:Article?=null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        article = args.article


        initView(article!!)


        webView.apply {
            webViewClient = WebViewClient()
            article!!.url?.let { loadUrl(it) }
        }
        btnShare.setOnClickListener {
            shareTextUrl(article!!)
        }

        fab.setOnClickListener {
            viewModel.saveArticle(article!!)
            Snackbar.make(view, "Article saved successfully", Snackbar.LENGTH_SHORT).show()
        }

        download_btn.setOnClickListener {
            // After API 23 (Marshmallow) and lower Android 10 you need to ask for permission first before save an image
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

                requestPermissions()

            } else {
                downloadImage(article!!.urlToImage!!)
            }
        }
    }

    private fun initView(article: Article) {
        Glide.with(this)
            .load(article.urlToImage)
            .placeholder(R.drawable.loading_banner_image).into(imgNews)
       //  tvSource.text = article.source?.name
        tvSource.text = article.author
        tvTitle.text = article.title
        tvDescription.text = article.description
        tvPublishedAt.text = article.publishedAt
    }


    var msg: String? = ""
    var lastMsg = ""

    private fun downloadImage(url: String) {
        val directory = File(Environment.DIRECTORY_PICTURES)

        if (!directory.exists()) {
            directory.mkdirs()
        }
        val downloadManager = requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(url.substring(url.lastIndexOf("/") + 1))
                .setDescription("")
                .setDestinationInExternalPublicDir(
                    directory.toString(),
                    url.substring(url.lastIndexOf("/") + 1)
                )
        }

        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)
        Thread(Runnable {
            var downloading = true
            while (downloading) {
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                msg = statusMessage(url, directory, status)
                if (msg != lastMsg) {
                    requireActivity().runOnUiThread {
                        AppAlerts.showSnackbar(requireActivity(),msg!!)
                    }
                    lastMsg = msg ?: ""
                }
                cursor.close()
            }
        }).start()
    }

    private fun statusMessage(url: String, directory: File, status: Int): String? {
        var msg = ""
        msg = when (status) {
            DownloadManager.STATUS_FAILED -> "Download has been failed, please try again"
            DownloadManager.STATUS_PAUSED -> "Paused"
            DownloadManager.STATUS_PENDING -> "Pending"
            DownloadManager.STATUS_RUNNING -> "Downloading..."
            DownloadManager.STATUS_SUCCESSFUL ->{
                "Image Saved successfully in $directory" + File.separator + url.substring(url.lastIndexOf("/") + 1)
             }
            else -> "There's nothing to download"
        }
        return msg
    }


    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    }

    //===========================================================
    private fun requestPermissions() {
        if (PermissionUtils.hasLocationPermissions(requireActivity())) {
            downloadImage(article!!.urlToImage!!)
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    //====Method to share either text or URL.====
    private fun shareTextUrl(article: Article) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        share.putExtra(Intent.EXTRA_SUBJECT, article.title)
        share.putExtra(Intent.EXTRA_TEXT, article.title+"."+"  For more information: "+article.url)
        startActivity(Intent.createChooser(share, "Share link!"))
    }
    //=============================================




}