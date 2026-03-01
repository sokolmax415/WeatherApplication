package com.example.weatherapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.*
import android.content.pm.ResolveInfo
import androidx.core.net.toUri


class MainActivity : AppCompatActivity() {
    private val cityNames = listOf(
        "Москва",
        "Санкт-Петербург",
        "Новосибирск",
        "Екатеринбург",
        "Казань",
        "Нижний Новгород",
        "Краснодар",
        "Сочи",
        "Йошкар-Ола"
    )
    private val cityCodes = listOf(
        "moscow-4368",
        "saint-petersburg-4079",
        "novosibirsk-4690",
        "yekaterinburg-4517",
        "kazan-4364",
        "nizhny-novgorod-4355",
        "krasnodar-5136",
        "sochi-5233",
        "yoshkar-ola-11975"
    )
    private val periodNames = listOf(
        "Сейчас", "1 день", "Выходные", "3 дня",
        "Неделя", "10 дней", "2 недели", "Месяц", "Завтра"
    )
    private val periodValues = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val spinnerCity   = findViewById<Spinner>(R.id.spinnerCity)
        val spinnerPeriod = findViewById<Spinner>(R.id.spinnerPeriod)
        val btnOpen       = findViewById<Button>(R.id.btnOpenForecast)

        spinnerCity.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            cityNames
        )

        spinnerPeriod.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            periodNames
        )

        btnOpen.setOnClickListener {
            val citySlug = cityCodes[spinnerCity.selectedItemPosition]
            val periodDays = periodValues[spinnerPeriod.selectedItemPosition]
            val url = buildGismeteoUrl(citySlug, periodDays)
            openInBrowser(url)
        }
    }

    private fun buildGismeteoUrl(
        citySlug: String,
        periodDays: Int,
    ): String {
        val periodSegment = when (periodDays) {
            0 -> "now"
            2 -> "weekend"
            3    -> "3-days"
            4    -> "weekly"
            5   -> "10-days"
            6   -> "2-weeks"
            7  -> "month"
            8 -> "tomorrow"
            else -> "" // Это как заглушка для прогноза на 1 день(там нет после города ничего в запросе нет)
        }

        return "https://www.gismeteo.ru/weather-$citySlug/$periodSegment"
    }

    private fun openInBrowser(url: String) {
        val uri = url.toUri()

        val browserIntent = Intent(Intent.ACTION_VIEW, "https://".toUri())

        val resolveInfoList = packageManager.queryIntentActivities(
            browserIntent,
            PackageManager.MATCH_ALL
        )

        if (resolveInfoList.isEmpty()) {
            Toast.makeText(this, "Ошибка: браузер не найден", Toast.LENGTH_LONG).show()
            return
        }

        val targetIntents = resolveInfoList.map { info: ResolveInfo ->
            Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(info.activityInfo.packageName)
            }
        }

        val chooserIntent = Intent.createChooser(targetIntents.first(), "Открыть в браузере").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.drop(1).toTypedArray())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }


        startActivity(chooserIntent)

    }
}