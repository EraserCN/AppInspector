package com.appinspector.data

/**
 * All methods used to query installed apps.
 * Grouped by category for display purposes.
 */
enum class QueryMethod(
    val displayName: String,
    val category: Category,
    val description: String
) {
    // ── PackageManager Direct ──────────────────────────────────────────────
    GET_INSTALLED_PACKAGES(
        "getInstalledPackages", Category.PACKAGE_MANAGER,
        "PackageManager.getInstalledPackages(0)"
    ),
    GET_INSTALLED_APPLICATIONS(
        "getInstalledApplications", Category.PACKAGE_MANAGER,
        "PackageManager.getInstalledApplications(0)"
    ),
    MATCH_UNINSTALLED(
        "MATCH_UNINSTALLED_PACKAGES", Category.PACKAGE_MANAGER,
        "getInstalledPackages(MATCH_UNINSTALLED_PACKAGES) — 含卸载残留"
    ),
    INDIVIDUAL_PACKAGE_QUERY(
        "逐包getPackageInfo", Category.PACKAGE_MANAGER,
        "对已知所有包名逐一调用getPackageInfo()"
    ),

    // ── Intent: Launcher ──────────────────────────────────────────────────
    INTENT_LAUNCHER(
        "CATEGORY_LAUNCHER", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_LAUNCHER"
    ),
    INTENT_HOME(
        "CATEGORY_HOME", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_HOME (桌面Launcher)"
    ),
    INTENT_TV_LAUNCHER(
        "LEANBACK_LAUNCHER", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_LEANBACK_LAUNCHER (Android TV)"
    ),
    INTENT_CAR_DOCK(
        "CATEGORY_CAR_DOCK", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_CAR_DOCK"
    ),
    INTENT_DESK_DOCK(
        "CATEGORY_DESK_DOCK", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_DESK_DOCK"
    ),
    INTENT_APP_BROWSER(
        "CATEGORY_APP_BROWSER", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_BROWSER"
    ),
    INTENT_APP_CALCULATOR(
        "CATEGORY_APP_CALCULATOR", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_CALCULATOR"
    ),
    INTENT_APP_CALENDAR(
        "CATEGORY_APP_CALENDAR", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_CALENDAR"
    ),
    INTENT_APP_CAMERA(
        "CATEGORY_APP_CAMERA", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_CAMERA"
    ),
    INTENT_APP_CONTACTS(
        "CATEGORY_APP_CONTACTS", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_CONTACTS"
    ),
    INTENT_APP_EMAIL(
        "CATEGORY_APP_EMAIL", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_EMAIL"
    ),
    INTENT_APP_FILES(
        "CATEGORY_APP_FILES", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_FILES"
    ),
    INTENT_APP_GALLERY(
        "CATEGORY_APP_GALLERY", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_GALLERY"
    ),
    INTENT_APP_MAPS(
        "CATEGORY_APP_MAPS", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_MAPS"
    ),
    INTENT_APP_MESSAGING(
        "CATEGORY_APP_MESSAGING", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_MESSAGING"
    ),
    INTENT_APP_MUSIC(
        "CATEGORY_APP_MUSIC", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_MUSIC"
    ),
    INTENT_APP_PHONE(
        "CATEGORY_APP_PHONE", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_PHONE"
    ),
    INTENT_APP_SETTINGS(
        "CATEGORY_APP_SETTINGS", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_SETTINGS"
    ),
    INTENT_APP_WEATHER(
        "CATEGORY_APP_WEATHER", Category.INTENT_LAUNCHER,
        "ACTION_MAIN + CATEGORY_APP_WEATHER"
    ),

    // ── Intent: Media / Content ───────────────────────────────────────────
    INTENT_IMAGE_CAPTURE(
        "ACTION_IMAGE_CAPTURE", Category.INTENT_MEDIA,
        "MediaStore.ACTION_IMAGE_CAPTURE (相机)"
    ),
    INTENT_VIDEO_CAPTURE(
        "ACTION_VIDEO_CAPTURE", Category.INTENT_MEDIA,
        "MediaStore.ACTION_VIDEO_CAPTURE (录像)"
    ),
    INTENT_VIEW_IMAGE(
        "VIEW image/*", Category.INTENT_MEDIA,
        "ACTION_VIEW + type=image/*"
    ),
    INTENT_VIEW_VIDEO(
        "VIEW video/*", Category.INTENT_MEDIA,
        "ACTION_VIEW + type=video/*"
    ),
    INTENT_VIEW_AUDIO(
        "VIEW audio/*", Category.INTENT_MEDIA,
        "ACTION_VIEW + type=audio/*"
    ),
    INTENT_VIEW_PDF(
        "VIEW application/pdf", Category.INTENT_MEDIA,
        "ACTION_VIEW + type=application/pdf"
    ),
    INTENT_PICK_IMAGE(
        "PICK image/*", Category.INTENT_MEDIA,
        "ACTION_PICK + type=image/*"
    ),
    INTENT_GET_CONTENT(
        "GET_CONTENT */*", Category.INTENT_MEDIA,
        "ACTION_GET_CONTENT + type=*/*"
    ),
    INTENT_OPEN_DOCUMENT(
        "OPEN_DOCUMENT */*", Category.INTENT_MEDIA,
        "ACTION_OPEN_DOCUMENT + type=*/*"
    ),

    // ── Intent: Share / Send ─────────────────────────────────────────────
    INTENT_SEND_TEXT(
        "SEND text/plain", Category.INTENT_SHARE,
        "ACTION_SEND + type=text/plain"
    ),
    INTENT_SEND_IMAGE(
        "SEND image/*", Category.INTENT_SHARE,
        "ACTION_SEND + type=image/*"
    ),
    INTENT_SENDTO_MAILTO(
        "SENDTO mailto:", Category.INTENT_SHARE,
        "ACTION_SENDTO + data=mailto:"
    ),
    INTENT_SENDTO_SMS(
        "SENDTO sms:", Category.INTENT_SHARE,
        "ACTION_SENDTO + data=sms:"
    ),

    // ── Intent: Communication ─────────────────────────────────────────────
    INTENT_DIAL(
        "ACTION_DIAL", Category.INTENT_COMM,
        "ACTION_DIAL (拨号界面)"
    ),
    INTENT_CALL(
        "ACTION_CALL", Category.INTENT_COMM,
        "ACTION_CALL (直接拨打)"
    ),
    INTENT_VIEW_CONTACT(
        "VIEW contact", Category.INTENT_COMM,
        "ACTION_VIEW + type=vnd.android.cursor.item/contact"
    ),

    // ── Intent: Browser / Search ──────────────────────────────────────────
    INTENT_VIEW_URL(
        "VIEW http://", Category.INTENT_BROWSER,
        "ACTION_VIEW + data=http://example.com"
    ),
    INTENT_WEB_SEARCH(
        "ACTION_WEB_SEARCH", Category.INTENT_BROWSER,
        "Intent.ACTION_WEB_SEARCH"
    ),
    INTENT_SEARCH(
        "ACTION_SEARCH", Category.INTENT_BROWSER,
        "Intent.ACTION_SEARCH"
    ),

    // ── Intent: System / Accessibility ───────────────────────────────────
    INTENT_ASSIST(
        "ACTION_ASSIST", Category.INTENT_SYSTEM,
        "Intent.ACTION_ASSIST (语音助手)"
    ),
    INTENT_VOICE_COMMAND(
        "ACTION_VOICE_COMMAND", Category.INTENT_SYSTEM,
        "Intent.ACTION_VOICE_COMMAND"
    ),
    INTENT_TTS(
        "ACTION_TTS_SERVICE", Category.INTENT_SYSTEM,
        "TextToSpeech.Engine.ACTION_TTS_SERVICE"
    ),
    INTENT_ACCESSIBILITY(
        "AccessibilityService", Category.INTENT_SYSTEM,
        "PackageManager.queryAccessibilityServices()"
    ),
    INTENT_INPUT_METHOD(
        "InputMethod", Category.INTENT_SYSTEM,
        "InputMethodManager.getInputMethodList()"
    ),
    INTENT_SYNC_ADAPTER(
        "SyncAdapter", Category.INTENT_SYSTEM,
        "ContentResolver.getSyncAdapterTypes()"
    ),
    INTENT_DEVICE_ADMIN(
        "DeviceAdmin", Category.INTENT_SYSTEM,
        "ACTION_DEVICE_ADMIN_ENABLED receivers"
    ),
    INTENT_WALLPAPER(
        "WallpaperService", Category.INTENT_SYSTEM,
        "WallpaperManager / WallpaperService intent"
    ),
    INTENT_DREAM(
        "DreamService", Category.INTENT_SYSTEM,
        "ACTION_DREAM_SETTINGS / DreamService"
    ),
    INTENT_PRINT_SERVICE(
        "PrintService", Category.INTENT_SYSTEM,
        "android.printservice.PrintService"
    ),

    // ── Intent: Install / Storage ─────────────────────────────────────────
    INTENT_INSTALL_PACKAGE(
        "ACTION_INSTALL_PACKAGE", Category.INTENT_INSTALL,
        "Intent.ACTION_INSTALL_PACKAGE"
    ),
    INTENT_MANAGE_STORAGE(
        "ACTION_MANAGE_NETWORK_USAGE", Category.INTENT_INSTALL,
        "Intent.ACTION_MANAGE_NETWORK_USAGE"
    ),

    // ── Root ──────────────────────────────────────────────────────────────
    ROOT_LS_DATA_APP(
        "ls /data/app", Category.ROOT,
        "su -c \"ls /data/app\" 枚举用户安装APK目录"
    ),
    ROOT_LS_SYSTEM_APP(
        "ls /system/app", Category.ROOT,
        "su -c \"ls /system/app\" 枚举系统App目录"
    ),
    ROOT_LS_SYSTEM_PRIV_APP(
        "ls /system/priv-app", Category.ROOT,
        "su -c \"ls /system/priv-app\" 枚举特权App目录"
    ),
    ROOT_LS_PRODUCT_APP(
        "ls /product/app", Category.ROOT,
        "su -c \"ls /product/app\" 枚举product分区"
    ),
    ROOT_PARSE_PACKAGES_XML(
        "packages.xml", Category.ROOT,
        "su -c \"cat /data/system/packages.xml\" 解析系统包注册表"
    ),
    ROOT_PM_LIST_PACKAGES(
        "pm list packages -a", Category.ROOT,
        "su -c \"pm list packages -a\" 含隐藏/禁用包"
    );

    enum class Category(val displayName: String, val colorIndex: Int) {
        PACKAGE_MANAGER("PackageManager", 0),
        INTENT_LAUNCHER("Intent·启动器", 1),
        INTENT_MEDIA("Intent·媒体", 2),
        INTENT_SHARE("Intent·分享", 3),
        INTENT_COMM("Intent·通信", 4),
        INTENT_BROWSER("Intent·浏览器", 5),
        INTENT_SYSTEM("Intent·系统", 6),
        INTENT_INSTALL("Intent·安装", 7),
        ROOT("Root", 8)
    }
}
