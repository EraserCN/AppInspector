package com.appinspector.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import android.content.pm.PackageManager

class AppRepository(private val context: Context) {

    private val pm: PackageManager get() = context.packageManager
    private val mergeMutex = Mutex()

    /** Main entry: run all queries and return merged app map + rootAvailable flag */
    suspend fun queryAll(): Pair<Map<String, AppInfo>, Boolean> = withContext(Dispatchers.IO) {
        val resultMap = mutableMapOf<String, AppInfo>()

        suspend fun merge(pkg: String, method: QueryMethod) {
            mergeMutex.withLock {
                val existing = resultMap[pkg]
                if (existing != null) {
                    existing.discoveredBy.add(method)
                } else {
                    val info = tryGetAppInfo(pkg) ?: AppInfo(
                        packageName = pkg,
                        label = pkg,
                        icon = null,
                        isSystemApp = false,
                        discoveredBy = mutableSetOf(method)
                    )
                    info.discoveredBy.add(method)
                    resultMap[pkg] = info
                }
            }
        }

        coroutineScope {
            // ── PackageManager Direct ──────────────────────────────────────
            val j1 = async {
                runCatching {
                    val list = if (Build.VERSION.SDK_INT >= 33)
                        pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                    else @Suppress("DEPRECATION") pm.getInstalledPackages(0)
                    list.forEach { merge(it.packageName, QueryMethod.GET_INSTALLED_PACKAGES) }
                }
            }
            val j2 = async {
                runCatching {
                    val list = if (Build.VERSION.SDK_INT >= 33)
                        pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
                    else @Suppress("DEPRECATION") pm.getInstalledApplications(0)
                    list.forEach { merge(it.packageName, QueryMethod.GET_INSTALLED_APPLICATIONS) }
                }
            }
            val j3 = async {
                runCatching {
                    val flag = PackageManager.MATCH_UNINSTALLED_PACKAGES.toLong()
                    val list = if (Build.VERSION.SDK_INT >= 33)
                        pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(flag))
                    else @Suppress("DEPRECATION") pm.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES)
                    list.forEach { merge(it.packageName, QueryMethod.MATCH_UNINSTALLED) }
                }
            }
            listOf(j1, j2, j3).awaitAll()

            // Individual package query — uses packages already known
            val knownPkgs = mergeMutex.withLock { resultMap.keys.toList() }
            val j4 = async {
                knownPkgs.forEach { pkg ->
                    runCatching {
                        if (Build.VERSION.SDK_INT >= 33)
                            pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
                        else @Suppress("DEPRECATION") pm.getPackageInfo(pkg, 0)
                        merge(pkg, QueryMethod.INDIVIDUAL_PACKAGE_QUERY)
                    }
                }
            }
            j4.await()

            // ── Intent queries (all parallel) ─────────────────────────────
            suspend fun qi(intent: Intent, method: QueryMethod) {
                runCatching {
                    val flags = PackageManager.MATCH_ALL
                    val results = if (Build.VERSION.SDK_INT >= 33)
                        pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
                    else @Suppress("DEPRECATION") pm.queryIntentActivities(intent, flags)
                    results.forEach { ri -> ri.activityInfo?.packageName?.let { merge(it, method) } }
                }
            }

            suspend fun qs(intent: Intent, method: QueryMethod) {
                runCatching {
                    val flags = PackageManager.MATCH_ALL
                    val results = if (Build.VERSION.SDK_INT >= 33)
                        pm.queryIntentServices(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
                    else @Suppress("DEPRECATION") pm.queryIntentServices(intent, flags)
                    results.forEach { ri ->
                        val p = ri.serviceInfo?.packageName ?: ri.activityInfo?.packageName
                        p?.let { merge(it, method) }
                    }
                }
            }

            val intentJobs = listOf(
                // Launcher
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), QueryMethod.INTENT_LAUNCHER) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), QueryMethod.INTENT_HOME) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), QueryMethod.INTENT_TV_LAUNCHER) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_CAR_DOCK), QueryMethod.INTENT_CAR_DOCK) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_DESK_DOCK), QueryMethod.INTENT_DESK_DOCK) },
                // CATEGORY_APP_*
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_BROWSER), QueryMethod.INTENT_APP_BROWSER) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR), QueryMethod.INTENT_APP_CALCULATOR) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR), QueryMethod.INTENT_APP_CALENDAR) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_CAMERA"), QueryMethod.INTENT_APP_CAMERA) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CONTACTS), QueryMethod.INTENT_APP_CONTACTS) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_EMAIL), QueryMethod.INTENT_APP_EMAIL) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_FILES"), QueryMethod.INTENT_APP_FILES) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_GALLERY), QueryMethod.INTENT_APP_GALLERY) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MAPS), QueryMethod.INTENT_APP_MAPS) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_MESSAGING"), QueryMethod.INTENT_APP_MESSAGING) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC), QueryMethod.INTENT_APP_MUSIC) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_PHONE"), QueryMethod.INTENT_APP_PHONE) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_SETTINGS"), QueryMethod.INTENT_APP_SETTINGS) },
                async { qi(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_WEATHER), QueryMethod.INTENT_APP_WEATHER) },
                // Media
                async { qi(Intent(MediaStore.ACTION_IMAGE_CAPTURE), QueryMethod.INTENT_IMAGE_CAPTURE) },
                async { qi(Intent(MediaStore.ACTION_VIDEO_CAPTURE), QueryMethod.INTENT_VIDEO_CAPTURE) },
                async { qi(Intent(Intent.ACTION_VIEW).setType("image/*"), QueryMethod.INTENT_VIEW_IMAGE) },
                async { qi(Intent(Intent.ACTION_VIEW).setType("video/*"), QueryMethod.INTENT_VIEW_VIDEO) },
                async { qi(Intent(Intent.ACTION_VIEW).setType("audio/*"), QueryMethod.INTENT_VIEW_AUDIO) },
                async { qi(Intent(Intent.ACTION_VIEW).setType("application/pdf"), QueryMethod.INTENT_VIEW_PDF) },
                async { qi(Intent(Intent.ACTION_PICK).setType("image/*"), QueryMethod.INTENT_PICK_IMAGE) },
                async { qi(Intent(Intent.ACTION_GET_CONTENT).setType("*/*"), QueryMethod.INTENT_GET_CONTENT) },
                async { qi(Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*"), QueryMethod.INTENT_OPEN_DOCUMENT) },
                // Share
                async { qi(Intent(Intent.ACTION_SEND).setType("text/plain"), QueryMethod.INTENT_SEND_TEXT) },
                async { qi(Intent(Intent.ACTION_SEND).setType("image/*"), QueryMethod.INTENT_SEND_IMAGE) },
                async { qi(Intent(Intent.ACTION_SENDTO).setData(Uri.parse("mailto:")), QueryMethod.INTENT_SENDTO_MAILTO) },
                async { qi(Intent(Intent.ACTION_SENDTO).setData(Uri.parse("sms:")), QueryMethod.INTENT_SENDTO_SMS) },
                // Comm
                async { qi(Intent(Intent.ACTION_DIAL), QueryMethod.INTENT_DIAL) },
                async { qi(Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:0")), QueryMethod.INTENT_CALL) },
                async { qi(Intent(Intent.ACTION_VIEW).setType("vnd.android.cursor.item/contact"), QueryMethod.INTENT_VIEW_CONTACT) },
                // Browser/Search
                async { qi(Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com")), QueryMethod.INTENT_VIEW_URL) },
                async { qi(Intent(Intent.ACTION_WEB_SEARCH), QueryMethod.INTENT_WEB_SEARCH) },
                async { qi(Intent(Intent.ACTION_SEARCH), QueryMethod.INTENT_SEARCH) },
                // System/Assist
                async { qi(Intent(Intent.ACTION_ASSIST), QueryMethod.INTENT_ASSIST) },
                async { qi(Intent(Intent.ACTION_VOICE_COMMAND), QueryMethod.INTENT_VOICE_COMMAND) },
                async { qs(Intent("android.intent.action.TTS_SERVICE"), QueryMethod.INTENT_TTS) },
                async { qs(Intent("android.accessibilityservice.AccessibilityService"), QueryMethod.INTENT_ACCESSIBILITY) },
                async { qs(Intent("android.service.wallpaper.WallpaperService"), QueryMethod.INTENT_WALLPAPER) },
                async { qs(Intent("android.service.dreams.DreamService"), QueryMethod.INTENT_DREAM) },
                async { qs(Intent("android.printservice.PrintService"), QueryMethod.INTENT_PRINT_SERVICE) },
                async { qi(Intent("android.app.action.DEVICE_ADMIN_ENABLED"), QueryMethod.INTENT_DEVICE_ADMIN) },
                // Install
                async { qi(Intent(Intent.ACTION_INSTALL_PACKAGE), QueryMethod.INTENT_INSTALL_PACKAGE) },
                async { qi(Intent(Intent.ACTION_MANAGE_NETWORK_USAGE), QueryMethod.INTENT_MANAGE_STORAGE) },
                // Input Method
                async {
                    runCatching {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.inputMethodList.forEach { info ->
                            merge(info.serviceInfo.packageName, QueryMethod.INTENT_INPUT_METHOD)
                        }
                    }
                },
                // Sync adapters
                async { qs(Intent("android.content.SyncAdapter"), QueryMethod.INTENT_SYNC_ADAPTER) }
            )
            intentJobs.awaitAll()

            // ── Root queries ──────────────────────────────────────────────
            val rootAvailable = RootQueryHelper.isRootAvailable()
            if (rootAvailable) {
                val rootJobs = listOf(
                    async {
                        RootQueryHelper.lsDataApp()?.forEach { entry ->
                            val p = RootQueryHelper.stripVersionSuffix(entry.trim())
                            if (p.contains('.')) merge(p, QueryMethod.ROOT_LS_DATA_APP)
                        }
                    },
                    async {
                        RootQueryHelper.lsSystemApp()?.forEach { entry ->
                            val p = entry.trim()
                            if (p.contains('.')) merge(p, QueryMethod.ROOT_LS_SYSTEM_APP)
                        }
                    },
                    async {
                        RootQueryHelper.lsSystemPrivApp()?.forEach { entry ->
                            val p = entry.trim()
                            if (p.contains('.')) merge(p, QueryMethod.ROOT_LS_SYSTEM_PRIV_APP)
                        }
                    },
                    async {
                        RootQueryHelper.lsProductApp()?.forEach { entry ->
                            val p = entry.trim()
                            if (p.contains('.')) merge(p, QueryMethod.ROOT_LS_PRODUCT_APP)
                        }
                    },
                    async {
                        RootQueryHelper.parsePackagesXml()?.forEach { p ->
                            merge(p, QueryMethod.ROOT_PARSE_PACKAGES_XML)
                        }
                    },
                    async {
                        RootQueryHelper.pmListPackagesAll()?.forEach { p ->
                            merge(p, QueryMethod.ROOT_PM_LIST_PACKAGES)
                        }
                    }
                )
                rootJobs.awaitAll()
            }

            Pair(resultMap.toMap(), rootAvailable)
        }
    }

    private fun tryGetAppInfo(targetPackage: String): AppInfo? = runCatching {
        val ai = if (Build.VERSION.SDK_INT >= 33)
            pm.getApplicationInfo(targetPackage, PackageManager.ApplicationInfoFlags.of(0))
        else @Suppress("DEPRECATION") pm.getApplicationInfo(targetPackage, 0)
        val label = pm.getApplicationLabel(ai).toString()
        val icon = runCatching { pm.getApplicationIcon(targetPackage) }.getOrNull()
        val isSystem = (ai.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        
        val launchActivity = pm.getLaunchIntentForPackage(targetPackage)?.component?.className
        
        AppInfo(targetPackage, label, icon, isSystem, launchActivity)
    }.getOrNull()
}
