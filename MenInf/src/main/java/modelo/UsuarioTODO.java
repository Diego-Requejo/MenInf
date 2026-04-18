package modelo;

import java.util.HashMap;

public class UsuarioTODO {

    //#############################################################
    // TODO ESTE CODIGO SE DEBE CAMBIAR POR USO DE LA BASE DE DATOS
    //#############################################################
    private static HashMap<String, String> usuarios = new HashMap<>();

    public static boolean existeUsuario(String user) {
        return usuarios.containsKey(user);
    }

    public static void registrar(String user, String pass) {
        usuarios.put(user, pass);
    }

    public static boolean login(String user, String pass) {
        return usuarios.containsKey(user)
                && usuarios.get(user).equals(pass);
    }
}