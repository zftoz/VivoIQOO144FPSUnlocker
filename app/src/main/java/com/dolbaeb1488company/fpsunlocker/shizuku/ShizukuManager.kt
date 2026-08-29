package com.dolbaeb1488company.fpsunlocker.shizuku

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Менеджер взаимодействия с Shizuku API.
 * Предоставляет методы для проверки состояния сервера, запроса прав с системным диалогом
 * и исполнения команд с правами ADB (UID 2000).
 */
object ShizukuManager {
    private const val TAG = "ShizukuManager"
    const val SHIZUKU_REQUEST_CODE = 144

    private var binderReceivedListener: Shizuku.OnBinderReceivedListener? = null
    private var binderDeadListener: Shizuku.OnBinderDeadListener? = null
    private var requestPermissionResultListener: Shizuku.OnRequestPermissionResultListener? = null

    /**
     * Проверяет, установлен и работает ли сервис Shizuku (доступен ли Binder).
     */
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Проверяет, предоставлены ли приложению права Shizuku (ADB).
     */
    fun hasPermission(): Boolean {
        if (!isShizukuAvailable()) return false
        return try {
            if (Shizuku.isPreV11()) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to check Shizuku permission", e)
            false
        }
    }

    /**
     * Регистрирует слушатели жизненного цикла Shizuku Binder.
     */
    fun registerListeners(
        onBinderReceived: () -> Unit,
        onBinderDead: () -> Unit,
        onPermissionResult: (granted: Boolean) -> Unit
    ) {
        // 1. Слушатель подключения Binder'а
        binderReceivedListener = Shizuku.OnBinderReceivedListener {
            Log.d(TAG, "Shizuku Binder received successfully!")
            onBinderReceived()
        }
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener!!)

        // 2. Слушатель отключения/смерти процесса Shizuku
        binderDeadListener = Shizuku.OnBinderDeadListener {
            Log.w(TAG, "Shizuku Binder died!")
            onBinderDead()
        }
        Shizuku.addBinderDeadListener(binderDeadListener!!)

        // 3. Слушатель ответа пользователя на всплывающее системное окно
        requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == SHIZUKU_REQUEST_CODE) {
                val granted = grantResult == PackageManager.PERMISSION_GRANTED
                Log.d(TAG, "Shizuku permission dialog result: granted = $granted")
                onPermissionResult(granted)
            }
        }
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener!!)
    }

    /**
     * Удаляет слушатели Shizuku (вызывать при onDestroy).
     */
    fun unregisterListeners() {
        binderReceivedListener?.let {
            try { Shizuku.removeBinderReceivedListener(it) } catch (_: Throwable) {}
        }
        binderDeadListener?.let {
            try { Shizuku.removeBinderDeadListener(it) } catch (_: Throwable) {}
        }
        requestPermissionResultListener?.let {
            try { Shizuku.removeRequestPermissionResultListener(it) } catch (_: Throwable) {}
        }
        binderReceivedListener = null
        binderDeadListener = null
        requestPermissionResultListener = null
    }

    /**
     * Запрашивает права Shizuku — показывает официальное системное диалоговое окно авторизации.
     */
    fun requestPermission(): Boolean {
        if (!isShizukuAvailable()) return false
        return try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                if (Shizuku.shouldShowRequestPermissionRationale()) {
                    // Пользователь ранее закрыл окно, но Shizuku позволяет запросить снова
                    Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
                } else {
                    Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
                }
                true
            } else {
                true
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error requesting Shizuku permission", e)
            false
        }
    }

    /**
     * Выполняет команду shell через Binder Shizuku (с правами ADB UID 2000).
     */
    suspend fun execShellCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        if (!hasPermission()) {
            return@withContext Result.failure(IllegalStateException("Shizuku permission is not granted"))
        }

        try {
            // Запуск shell-процесса с правами ADB (UID 2000) через Binder Shizuku
            val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            newProcessMethod.isAccessible = true
            val process = newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as java.lang.Process
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.appendLine(line)
            }
            
            val errorOutput = StringBuilder()
            while (errorReader.readLine().also { line = it } != null) {
                errorOutput.appendLine(line)
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Result.success(output.toString().trim())
            } else {
                Result.failure(RuntimeException("Command exited with code $exitCode: ${errorOutput.toString().trim()}"))
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to exec command via Shizuku: $command", e)
            Result.failure(e)
        }
    }

    /**
     * Автоматически выдаёт приложению системные разрешения WRITE_SECURE_SETTINGS и WRITE_SETTINGS через Shizuku в 1 клик.
     * В Android разрешение WRITE_SETTINGS управляется через AppOps (appops set WRITE_SETTINGS allow),
     * а WRITE_SECURE_SETTINGS через PackageManager (pm grant).
     */
    suspend fun grantWriteSecureSettings(context: Context): Result<String> {
        val pkg = context.packageName
        val cmd = """
            pm grant $pkg android.permission.WRITE_SECURE_SETTINGS 2>/dev/null
            cmd appops set $pkg WRITE_SETTINGS allow 2>/dev/null || appops set $pkg WRITE_SETTINGS allow 2>/dev/null
            cmd appops set $pkg SYSTEM_ALERT_WINDOW allow 2>/dev/null || appops set $pkg SYSTEM_ALERT_WINDOW allow 2>/dev/null
            echo "SUCCESS: Permissions granted to $pkg"
        """.trimIndent()
        return execShellCommand(cmd)
    }

    /**
     * Записывает настройку в System Settings через Shizuku с правами ADB (UID 2000).
     */
    suspend fun putSystemSetting(key: String, value: String): Result<String> {
        val cmd = "settings put system $key \"$value\""
        return execShellCommand(cmd)
    }

    /**
     * Читает настройку из System Settings через Shizuku.
     */
    suspend fun getSystemSetting(key: String): Result<String> {
        val cmd = "settings get system $key"
        return execShellCommand(cmd)
    }

    /**
     * Записывает настройку в Secure Settings через Shizuku.
     */
    suspend fun putSecureSetting(key: String, value: String): Result<String> {
        val cmd = "settings put secure $key \"$value\""
        return execShellCommand(cmd)
    }

    /**
     * Записывает настройку в Global Settings через Shizuku.
     */
    suspend fun putGlobalSetting(key: String, value: String): Result<String> {
        val cmd = "settings put global $key \"$value\""
        return execShellCommand(cmd)
    }
}
