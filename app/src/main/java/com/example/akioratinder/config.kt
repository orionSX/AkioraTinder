package com.example.akioratinder.config

object BackendConfig {

    private var _backendUrl: String = "https://orion-vuz-mobile.vercel.app/"
    
    val backendUrl: String
        get() = _backendUrl
    
    fun setBackendUrl(url: String) {
        _backendUrl = url.trimEnd('/')
    }
}