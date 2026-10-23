package cl.mpaz.mpazpro.nativo

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

// Recurso nativo de vibracion. cada ves que acerta vibra corto, si se equivoca doble.


class Vibracion(context: Context) {

    private val vibrador: Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION") (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        }

    fun correcto() {
        if (!vibrador.hasVibrator()) return
        vibrador.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun incorrecto() {
        if (!vibrador.hasVibrator()) return
        vibrador.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 120, 80, 120), -1))
    }
}