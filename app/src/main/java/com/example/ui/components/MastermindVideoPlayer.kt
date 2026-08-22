package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AppIcons
import com.example.util.TimeUtils

object VideoUtils {
    fun extractYouTubeId(url: String): String? {
        if (url.isBlank()) return null
        val trimmed = url.trim()

        // 11-char alphanumeric ID directly
        if (trimmed.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
            return trimmed
        }

        // youtu.be/ID
        val youtuBeRegex = Regex("youtu\\.be/([a-zA-Z0-9_-]{11})")
        youtuBeRegex.find(trimmed)?.groupValues?.get(1)?.let { return it }

        // youtube.com/watch?v=ID
        val watchRegex = Regex("[?&]v=([a-zA-Z0-9_-]{11})")
        watchRegex.find(trimmed)?.groupValues?.get(1)?.let { return it }

        // youtube.com/embed/ID
        val embedRegex = Regex("youtube\\.com/embed/([a-zA-Z0-9_-]{11})")
        embedRegex.find(trimmed)?.groupValues?.get(1)?.let { return it }

        // youtube.com/shorts/ID
        val shortsRegex = Regex("youtube\\.com/shorts/([a-zA-Z0-9_-]{11})")
        shortsRegex.find(trimmed)?.groupValues?.get(1)?.let { return it }

        return null
    }

    fun isYouTubeUrl(url: String): Boolean {
        return extractYouTubeId(url) != null || url.contains("youtube.com", ignoreCase = true) || url.contains("youtu.be", ignoreCase = true)
    }
}

@Composable
fun MastermindVideoPlayer(
    videoUrl: String,
    startSeconds: Int = 0,
    endSeconds: Int = 0,
    autostart: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (videoUrl.isBlank()) return

    val isYouTube = remember(videoUrl) { VideoUtils.isYouTubeUrl(videoUrl) }
    val youtubeId = remember(videoUrl) { VideoUtils.extractYouTubeId(videoUrl) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Info Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isYouTube) Color(0xFFEF4444) else Color(0xFF38BDF8)
                    ) {
                        Text(
                            text = if (isYouTube) "YOUTUBE" else "VIDEO",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (startSeconds > 0 || endSeconds > 0) {
                        Text(
                            text = "Clip: ${TimeUtils.parseSecondsToTimeString(startSeconds)} → ${if (endSeconds > 0) TimeUtils.parseSecondsToTimeString(endSeconds) else "End"}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (autostart) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "⚡ Autostart",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Player Viewport (16:9 aspect ratio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (isYouTube && youtubeId != null) {
                    YouTubeIframePlayer(
                        youtubeId = youtubeId,
                        startSeconds = startSeconds,
                        endSeconds = endSeconds,
                        autostart = autostart
                    )
                } else {
                    LocalOrDirectVideoPlayer(
                        videoUrl = videoUrl,
                        startSeconds = startSeconds,
                        endSeconds = endSeconds,
                        autostart = autostart
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun YouTubeIframePlayer(
    youtubeId: String,
    startSeconds: Int,
    endSeconds: Int,
    autostart: Boolean
) {
    val endParam = if (endSeconds > startSeconds) "&end=$endSeconds" else ""
    val autoPlayParam = if (autostart) "1" else "0"

    val htmlContent = remember(youtubeId, startSeconds, endSeconds, autostart) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; background: #000; overflow: hidden; }
                html, body { width: 100%; height: 100%; }
                iframe { width: 100%; height: 100%; border: 0; }
            </style>
        </head>
        <body>
            <iframe 
                id="ytplayer"
                type="text/html"
                src="https://www.youtube-nocookie.com/embed/$youtubeId?enablejsapi=1&autoplay=$autoPlayParam&start=$startSeconds$endParam&playsinline=1&rel=0&modestbranding=1"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen>
            </iframe>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = !autostart
                    cacheMode = WebSettings.LOAD_DEFAULT
                    allowContentAccess = true
                }
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://www.youtube-nocookie.com", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://www.youtube-nocookie.com", htmlContent, "text/html", "UTF-8", null)
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun LocalOrDirectVideoPlayer(
    videoUrl: String,
    startSeconds: Int,
    endSeconds: Int,
    autostart: Boolean
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(autostart) }
    var currentPositionMs by remember { mutableIntStateOf(startSeconds * 1000) }
    var durationMs by remember { mutableIntStateOf(0) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val handler = remember { Handler(Looper.getMainLooper()) }

    DisposableEffect(videoUrl) {
        val checkRunnable = object : Runnable {
            override fun run() {
                videoViewRef?.let { vv ->
                    if (vv.isPlaying) {
                        val pos = vv.currentPosition
                        currentPositionMs = pos
                        if (endSeconds > 0 && pos >= endSeconds * 1000) {
                            vv.pause()
                            vv.seekTo(startSeconds * 1000)
                            isPlaying = false
                        }
                    }
                }
                handler.postDelayed(this, 500)
            }
        }
        handler.post(checkRunnable)

        onDispose {
            handler.removeCallbacks(checkRunnable)
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    try {
                        setVideoURI(Uri.parse(videoUrl))
                    } catch (e: Exception) {
                        errorMessage = "Unable to load video: ${e.localizedMessage}"
                    }

                    setOnPreparedListener { mp ->
                        mediaPlayerRef = mp
                        durationMs = duration
                        if (startSeconds > 0) {
                            seekTo(startSeconds * 1000)
                        }
                        if (autostart) {
                            start()
                            isPlaying = true
                        }
                    }

                    setOnCompletionListener {
                        isPlaying = false
                        if (startSeconds > 0) {
                            seekTo(startSeconds * 1000)
                        }
                    }

                    setOnErrorListener { _, _, _ ->
                        errorMessage = "Playback error occurred"
                        true
                    }

                    videoViewRef = this
                }
            },
            update = { vv ->
                videoViewRef = vv
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top external open link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(videoUrl), "video/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        AppIcons.OpenInNew,
                        contentDescription = "Open in external video player",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (errorMessage != null) {
                Surface(
                    color = Color.Red.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Bottom Player Control Bar
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                if (vv.isPlaying) {
                                    vv.pause()
                                    isPlaying = false
                                } else {
                                    vv.start()
                                    isPlaying = true
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = if (isPlaying) "⏸" else "▶",
                            color = Color(0xFF38BDF8),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${TimeUtils.parseSecondsToTimeString(currentPositionMs / 1000)} / ${TimeUtils.parseSecondsToTimeString(durationMs / 1000)}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Reset to clip start
                    if (startSeconds > 0) {
                        TextButton(
                            onClick = {
                                videoViewRef?.seekTo(startSeconds * 1000)
                                currentPositionMs = startSeconds * 1000
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("↺ Start", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
