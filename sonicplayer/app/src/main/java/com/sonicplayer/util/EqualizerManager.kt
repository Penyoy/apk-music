package com.sonicplayer.util

import android.content.Context
import android.media.audiofx.Equalizer
import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer
import android.media.audiofx.LoudnessEnhancer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EqualizerManager(private val context: Context) {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    
    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled
    
    private val _bandLevels = MutableStateFlow<List<Float>>(emptyList())
    val bandLevels: StateFlow<List<Float>> = _bandLevels
    
    private val _presets = MutableStateFlow<List<String>>(emptyList())
    val presets: StateFlow<List<String>> = _presets
    
    private val _currentPreset = MutableStateFlow(-1)
    val currentPreset: StateFlow<Int> = _currentPreset
    
    private val _bassBoostStrength = MutableStateFlow(0)
    val bassBoostStrength: StateFlow<Int> = _bassBoostStrength
    
    private val _virtualizerStrength = MutableStateFlow(0)
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength
    
    private val _loudnessEnhancerStrength = MutableStateFlow(0)
    val loudnessEnhancerStrength: StateFlow<Int> = _loudnessEnhancerStrength
    
    private var audioSessionId: Int = 0
    
    fun initialize(audioSessionId: Int) {
        release()
        this.audioSessionId = audioSessionId
        
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _isEnabled.value
                
                val bandCount = numberOfBands.toInt()
                val levels = mutableListOf<Float>()
                for (i in 0 until bandCount) {
                    levels.add(getBandLevel(i.toShort()).toFloat())
                }
                _bandLevels.value = levels
                
                val presetList = mutableListOf<String>()
                for (i in 0 until numberOfPresets) {
                    presetList.add(getPresetName(i.toShort()).toString())
                }
                _presets.value = presetList
            }
            
            bassBoost = BassBoost(0, audioSessionId)
            virtualizer = Virtualizer(0, audioSessionId)
            loudnessEnhancer = LoudnessEnhancer(audioSessionId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        virtualizer?.enabled = enabled
        loudnessEnhancer?.enabled = enabled
    }
    
    fun setBandLevel(band: Int, level: Float) {
        try {
            equalizer?.setBandLevel(band.toShort(), level.toShort())
            val currentLevels = _bandLevels.value.toMutableList()
            if (band < currentLevels.size) {
                currentLevels[band] = level
                _bandLevels.value = currentLevels
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun usePreset(presetIndex: Int) {
        try {
            equalizer?.usePreset(presetIndex.toShort())
            _currentPreset.value = presetIndex
            
            // Update band levels
            val bandCount = equalizer?.numberOfBands?.toInt() ?: 0
            val levels = mutableListOf<Float>()
            for (i in 0 until bandCount) {
                levels.add(equalizer?.getBandLevel(i.toShort())?.toFloat() ?: 0f)
            }
            _bandLevels.value = levels
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun setBassBoost(strength: Int) {
        try {
            bassBoost?.setStrength(strength.toShort())
            _bassBoostStrength.value = strength
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun setVirtualizer(strength: Int) {
        try {
            virtualizer?.setStrength(strength.toShort())
            _virtualizerStrength.value = strength
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun setLoudnessEnhancer(gainmB: Int) {
        try {
            loudnessEnhancer?.setTargetGain(gainmB)
            _loudnessEnhancerStrength.value = gainmB
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getBandFrequencies(): List<Int> {
        val frequencies = mutableListOf<Int>()
        try {
            val bandCount = equalizer?.numberOfBands?.toInt() ?: 0
            for (i in 0 until bandCount) {
                val freqHz = equalizer?.getCenterFreq(i.toShort())?.div(1000) ?: 0
                frequencies.add(freqHz.toInt())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return frequencies
    }
    
    fun getBandLevelRange(): Pair<Int, Int> {
        return try {
            val range = equalizer?.bandLevelRange
            Pair((range?.get(0)?.toInt() ?: -1500), (range?.get(1)?.toInt() ?: 1500))
        } catch (e: Exception) {
            Pair(-1500, 1500)
        }
    }
    
    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        loudnessEnhancer?.release()
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }
    
    companion object {
        val DEFAULT_PRESETS = listOf(
            "Normal", "Classical", "Dance", "Flat", "Folk",
            "Heavy Metal", "Hip Hop", "Jazz", "Pop", "Rock",
            "Electronic", "Acoustic", "Vocal Booster", "Bass Booster", "Treble Booster"
        )
    }
}
