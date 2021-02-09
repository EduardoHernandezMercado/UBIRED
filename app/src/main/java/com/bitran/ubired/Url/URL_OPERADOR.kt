package com.eduardohm.ingenmtryx.dika.Util.URL

class URL_OPERADOR {

        companion object {

            val Server = "https://dika-x.000webhostapp.com/php-getting-started"
            @JvmStatic
            fun GetUserValidaToken(): String {
                return Server + "/formularios/Opera/Data/GetUserValidaToken.php"
            }
            @JvmStatic
            fun InsertOpera(): String {
                return Server + "/formularios/Opera/Data/InsertOpera.php"
            }

            @JvmStatic
            fun GetUserPassword(): String {
                return Server + "/formularios/Opera/Data/GetUserPassword.php"
            }

    }
}