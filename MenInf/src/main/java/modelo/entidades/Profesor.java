package modelo.entidades;

public class Profesor {
    private String nombre;
    private String correo;

    public Profesor() {}

    public Profesor(String nombre, String correo) {
        this.nombre = nombre;
        this.correo = correo;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
