package pe.edu.upeu.pharmamobile.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.cinterop.addressOf
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/** Keychain genérico, restringido a este dispositivo y servicio. */
@OptIn(ExperimentalForeignApi::class)
class AlmacenSeguroIos : AlmacenSeguro {
    override fun guardarToken(token: String) {
        require(token.isNotBlank())
        borrarToken()
        conConsulta { consulta ->
            val bytes = token.encodeToByteArray()
            bytes.usePinned { fijados ->
                val datos = CFDataCreate(null, fijados.addressOf(0).reinterpret(), bytes.size.toLong())
                    ?: error("No se pudo codificar el token")
                try {
                    CFDictionaryAddValue(consulta, kSecValueData, datos)
                    CFDictionaryAddValue(consulta, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
                    check(SecItemAdd(consulta, null) == errSecSuccess) { "Keychain no guardó el token" }
                } finally {
                    CFRelease(datos)
                }
            }
        }
    }

    override fun leerToken(): String? = conConsulta { consulta ->
        CFDictionaryAddValue(consulta, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(consulta, kSecMatchLimit, kSecMatchLimitOne)
        memScoped {
            val resultado = alloc<COpaquePointerVar>()
            when (SecItemCopyMatching(consulta, resultado.ptr)) {
                errSecItemNotFound -> null
                errSecSuccess -> {
                    val datos: CFDataRef = resultado.value?.reinterpret()
                        ?: error("Keychain devolvió datos vacíos")
                    try {
                        val longitud = CFDataGetLength(datos).toInt()
                        CFDataGetBytePtr(datos)?.readBytes(longitud)?.decodeToString()
                    } finally {
                        CFRelease(datos)
                    }
                }
                else -> error("No se pudo leer Keychain")
            }
        }
    }

    override fun borrarToken() {
        conConsulta { consulta ->
            val estado = SecItemDelete(consulta)
            check(estado == errSecSuccess || estado == errSecItemNotFound) {
                "No se pudo borrar el token de Keychain"
            }
        }
    }

    private inline fun <T> conConsulta(operacion: (CFMutableDictionaryRef?) -> T): T {
        val servicio = CFStringCreateWithCString(null, "pe.edu.upeu.pharmamobile.sesion", kCFStringEncodingUTF8)
        val cuenta = CFStringCreateWithCString(null, "jwt", kCFStringEncodingUTF8)
        val consulta = CFDictionaryCreateMutable(null, 0, null, null)
        try {
            CFDictionaryAddValue(consulta, kSecClass, kSecClassGenericPassword)
            CFDictionaryAddValue(consulta, kSecAttrService, servicio)
            CFDictionaryAddValue(consulta, kSecAttrAccount, cuenta)
            return operacion(consulta)
        } finally {
            CFRelease(consulta)
            CFRelease(servicio)
            CFRelease(cuenta)
        }
    }
}
