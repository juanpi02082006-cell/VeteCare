package com.Vetecare.app.config;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.Vetecare.app.model.Especie;
import com.Vetecare.app.model.Mascota;
import com.Vetecare.app.model.Propietario;
import com.Vetecare.app.model.Rol;
import com.Vetecare.app.model.Sexo;
import com.Vetecare.app.model.Usuario;
import com.Vetecare.app.repository.MascotaRepository;
import com.Vetecare.app.repository.PropietarioRepository;
import com.Vetecare.app.repository.UsuarioRepository;

/**
 * Datos ficticios de demostración (Ley 1581 de 2012: no se usan datos reales).
 * Solo se cargan cuando la base de datos está vacía.
 * Todas las cuentas de prueba usan la contraseña Vetcare2026.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    public static final String CLAVE_DEMO = "Vetcare2026";

    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final MascotaRepository mascotaRepository;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(UsuarioRepository usuarioRepository, PropietarioRepository propietarioRepository,
            MascotaRepository mascotaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.propietarioRepository = propietarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Propietario carlos = propietario("Carlos Andrés Ruiz", "1098765432", "3001234567",
                "carlos.ruiz@correo.com", "Cra. 27 # 45-12, Bucaramanga");
        Propietario laura = propietario("Laura Patricia Gómez", "1095123456", "3109876543",
                "laura.gomez@correo.com", "Calle 56 # 30-21, Bucaramanga");
        Propietario jorge = propietario("Jorge Enrique Martínez", "91234567", "3157654321",
                "jorge.martinez@correo.com", "Av. Quebradaseca # 15-40, Bucaramanga");
        Propietario ana = propietario("Ana María Rodríguez", "1102345678", "3204567890",
                "ana.rodriguez@correo.com", "Cra. 33 # 52-08, Floridablanca");
        Propietario diego = propietario("Diego Alejandro Pérez", "1096789012", "3012345678",
                "diego.perez@correo.com", "Calle 105 # 22-14, Girón");
        propietarioRepository.saveAll(List.of(carlos, laura, jorge, ana, diego));

        mascotaRepository.saveAll(List.of(
                mascota("Max", Especie.PERRO, "Labrador Retriever", Sexo.MACHO, "2021-03-15", 28.5, carlos),
                mascota("Luna", Especie.GATO, "Siamés", Sexo.HEMBRA, "2022-07-02", 4.2, carlos),
                mascota("Rocky", Especie.PERRO, "Bulldog Francés", Sexo.MACHO, "2020-11-20", 12.8, laura),
                mascota("Mía", Especie.GATO, "Persa", Sexo.HEMBRA, "2019-05-10", 5.1, laura),
                mascota("Toby", Especie.PERRO, "Beagle", Sexo.MACHO, "2023-01-08", 11.3, jorge),
                mascota("Nala", Especie.PERRO, "Golden Retriever", Sexo.HEMBRA, "2018-09-25", 30.1, jorge),
                mascota("Simba", Especie.GATO, "Mestizo", Sexo.MACHO, "2024-02-14", 3.6, ana),
                mascota("Kira", Especie.PERRO, "Pastor Alemán", Sexo.HEMBRA, "2020-06-30", 32.4, ana),
                mascota("Coco", Especie.AVE, "Periquito australiano", Sexo.MACHO, "2023-08-12", 0.04, diego),
                mascota("Lola", Especie.PERRO, "Poodle", Sexo.HEMBRA, "2022-12-03", 6.7, diego)));

        usuarioRepository.saveAll(List.of(
                usuario("Administrador General", "admin@vetcare.com", Rol.ADMIN, null),
                usuario("Dra. Valentina Ortiz", "veterinario@vetcare.com", Rol.VETERINARIO, null),
                usuario("Sofía Herrera", "recepcion@vetcare.com", Rol.RECEPCIONISTA, null),
                usuario("Andrés Castillo", "inventario@vetcare.com", Rol.AUXILIAR, null),
                usuario("Carlos Andrés Ruiz", "carlos.ruiz@correo.com", Rol.PROPIETARIO, carlos)));
    }

    private Propietario propietario(String nombre, String documento, String telefono, String correo,
            String direccion) {
        Propietario p = new Propietario();
        p.setNombre(nombre);
        p.setDocumento(documento);
        p.setTelefono(telefono);
        p.setCorreo(correo);
        p.setDireccion(direccion);
        return p;
    }

    private Mascota mascota(String nombre, Especie especie, String raza, Sexo sexo, String nacimiento,
            double peso, Propietario propietario) {
        Mascota m = new Mascota();
        m.setNombre(nombre);
        m.setEspecie(especie);
        m.setRaza(raza);
        m.setSexo(sexo);
        m.setFechaNacimiento(LocalDate.parse(nacimiento));
        m.setPeso(peso);
        m.setPropietario(propietario);
        return m;
    }

    private Usuario usuario(String nombre, String correo, Rol rol, Propietario propietario) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setPassword(passwordEncoder.encode(CLAVE_DEMO));
        u.setRol(rol);
        u.setPropietario(propietario);
        return u;
    }
}
