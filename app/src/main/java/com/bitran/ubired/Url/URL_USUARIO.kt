package com.eduardohm.ingenmtryx.dika.Util.URL


class URL_USUARIO {

    companion object {

        val Server = "https://ubi-red.000webhostapp.com"
        @JvmStatic
        fun GetUserValidaToken(): String {
            return Server + "/formularios/User/Data/GetUserValidaToken.php"
        }
        @JvmStatic
        fun InsertUser(): String {
            return Server + "/formularios/User/Data/InsertUser.php"
        }

        @JvmStatic
        fun GetUserPassword(): String {
            return Server + "/formularios/User/Data/GetUserPassword.php"
        }
        @JvmStatic
        fun GetFamilia(): String {
            return Server + "/formularios/User/Data/GetFamilia.php"
        }
        @JvmStatic
        fun InsertSolicitud(): String {
            return Server + "/formularios/User/Data/InsertSolicitud.php"
        }
        @JvmStatic
        fun GetSolicitud(): String {
            return Server + "/formularios/User/Data/GetSolicitud.php"
        }
        @JvmStatic
        fun InsertFamilia(): String {
            return Server + "/formularios/User/Data/InsertFamilia.php"
        }
        @JvmStatic
        fun GetUsuario(): String {
            return Server + "/formularios/User/Data/GetUsuario.php"
        }

        @JvmStatic
        fun DeleteUserFamilia(): String {
            return Server + "/formularios/User/Data/DeleteUserFamilia.php"
        }
        @JvmStatic
        fun DeleteUserSolicitud(): String {
            return Server + "/formularios/User/Data/DeleteUserSolicitud.php"
        }
        @JvmStatic
        fun DeleteTokenUser(): String {
            return Server + "/formularios/User/Data/DeleteTokenUser.php"
        }

    }
}
