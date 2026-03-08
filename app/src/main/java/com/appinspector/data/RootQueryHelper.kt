package com.appinspector.data

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wraps all su-based queries. Returns null for each query if root is unavailable.
 */
object RootQueryHelper {

    /** Returns true if root is available and granted */
    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.getShell().isRoot
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Runs [command] via su and returns list of output lines, or null if root unavailable / error.
     */
    suspend fun runCommand(command: String): List<String>? = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = Shell.cmd(command).exec()
            if (result.isSuccess) result.out else null
        } catch (e: Exception) {
            null
        }
    }

    /** su ls /data/app — returns package-like folder names */
    suspend fun lsDataApp(): List<String>? = runCommand("ls /data/app")

    /** su ls /system/app */
    suspend fun lsSystemApp(): List<String>? = runCommand("ls /system/app")

    /** su ls /system/priv-app */
    suspend fun lsSystemPrivApp(): List<String>? = runCommand("ls /system/priv-app")

    /** su ls /product/app */
    suspend fun lsProductApp(): List<String>? = runCommand("ls /product/app")

    /**
     * Parses /data/system/packages.xml and extracts package names.
     * Looks for lines like: <package name="com.example.app" ...>
     */
    suspend fun parsePackagesXml(): List<String>? {
        val lines = runCommand("grep 'package name=' /data/system/packages.xml") ?: return null
        val regex = Regex("""name="([^"]+)"""")
        return lines.mapNotNull { line ->
            regex.find(line)?.groupValues?.get(1)
        }
    }

    /** su pm list packages -a — returns list of "package:com.xxx" lines → stripped */
    suspend fun pmListPackagesAll(): List<String>? {
        val lines = runCommand("pm list packages -a") ?: return null
        return lines.mapNotNull { line ->
            if (line.startsWith("package:")) line.removePrefix("package:").trim()
            else null
        }
    }

    /**
     * Strips the version suffix from a /data/app directory entry.
     * E.g. "com.example.app-abc123==" → "com.example.app"
     */
    fun stripVersionSuffix(dirName: String): String {
        return dirName.replace(Regex("-[A-Za-z0-9+/=]+$"), "")
    }
}
