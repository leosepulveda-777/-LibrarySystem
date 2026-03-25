package com.library.config;

import com.library.entity.*;
import com.library.enums.Role;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final LectorRepository lectorRepository;
    private final CategoriaRepository categoriaRepository;
    private final AutorRepository autorRepository;
    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initAdminUser();
        initBibliotecarioUser();
        initCategorias();
        initAutoresYLibros();
        log.info("=== Datos iniciales cargados correctamente ===");
        log.info("ADMIN    -> admin@library.com  / Admin123!");
        log.info("BIBLIO   -> biblio@library.com / Biblio123!");
        log.info("Swagger  -> http://localhost:8080/swagger-ui.html");
    }

    private void initAdminUser() {
        if (usuarioRepository.existsByEmail("admin@library.com")) return;
        Usuario admin = Usuario.builder()
                .nombre("Administrador").apellido("Sistema")
                .email("admin@library.com")
                .password(passwordEncoder.encode("Admin123!"))
                .telefono("3001234567").rol(Role.ADMIN).activo(true).build();
        usuarioRepository.save(admin);
        log.info("Usuario ADMIN creado");
    }

    private void initBibliotecarioUser() {
        if (usuarioRepository.existsByEmail("biblio@library.com")) return;
        Usuario biblio = Usuario.builder()
                .nombre("Bibliotecario").apellido("Principal")
                .email("biblio@library.com")
                .password(passwordEncoder.encode("Biblio123!"))
                .telefono("3009876543").rol(Role.BIBLIOTECARIO).activo(true).build();
        usuarioRepository.save(biblio);
        log.info("Usuario BIBLIOTECARIO creado");
    }

    private void initCategorias() {
        if (categoriaRepository.count() > 0) return;

        Categoria lit  = cat("Literatura",  "Obras literarias y ficción", null);
        Categoria cien = cat("Ciencia",      "Ciencias exactas y naturales", null);
        Categoria tech = cat("Tecnología",   "Informática y tecnología", null);
        Categoria hist = cat("Historia",     "Historia universal y local", null);

        cat("Novela",    "Novelas de ficción", lit);
        cat("Cuentos",   "Cuentos y relatos",  lit);
        cat("Poesía",    "Poesía y lírica",     lit);
        cat("Física",    "Física teórica",      cien);
        cat("Matemáticas","Matemáticas",        cien);
        cat("Biología",  "Ciencias de la vida", cien);
        cat("Programación","Lenguajes y paradigmas", tech);
        cat("Redes",     "Redes y comunicaciones", tech);
        cat("IA",        "Inteligencia Artificial", tech);
        cat("Historia Universal","Historia del mundo", hist);
        log.info("Categorías iniciales creadas");
    }

    private Categoria cat(String nombre, String desc, Categoria padre) {
        Categoria c = new Categoria();
        c.setNombre(nombre); c.setDescripcion(desc); c.setPadre(padre); c.setActiva(true);
        return categoriaRepository.save(c);
    }

    private void initAutoresYLibros() {
        if (autorRepository.count() > 0) return;

        Autor garcia  = autor("Gabriel",  "García Márquez", "Colombia",      "Premio Nobel de Literatura 1982");
        Autor borges  = autor("Jorge Luis","Borges",         "Argentina",     "Maestro del realismo mágico");
        Autor knuth   = autor("Donald",    "Knuth",          "Estados Unidos","Pionero de la computación");
        Autor hawking = autor("Stephen",   "Hawking",        "Reino Unido",   "Físico teórico y cosmólogo");

        Categoria novela = categoriaRepository.findAll().stream()
                .filter(c -> "Novela".equals(c.getNombre())).findFirst().orElse(null);
        Categoria prog = categoriaRepository.findAll().stream()
                .filter(c -> "Programación".equals(c.getNombre())).findFirst().orElse(null);
        Categoria fisica = categoriaRepository.findAll().stream()
                .filter(c -> "Física".equals(c.getNombre())).findFirst().orElse(null);

        libro("Cien años de soledad",       "978-8437604947",
              "La historia de la familia Buendía en Macondo.",
              1967, "Sudamericana", List.of(garcia),  novela != null ? List.of(novela)  : List.of(), 3);

        libro("El Aleph",                   "978-8420632575",
              "Cuentos fantásticos del maestro argentino.",
              1949, "Emecé",        List.of(borges),  novela != null ? List.of(novela)  : List.of(), 2);

        libro("The Art of Computer Programming","978-0201896831",
              "La obra de referencia definitiva sobre algoritmos.",
              1968, "Addison-Wesley",List.of(knuth),  prog   != null ? List.of(prog)    : List.of(), 2);

        libro("A Brief History of Time",    "978-0553346146",
              "Introducción a la cosmología y física teórica.",
              1988, "Bantam Books", List.of(hawking), fisica != null ? List.of(fisica)  : List.of(), 4);

        log.info("Autores y libros de muestra creados");
    }

    private Autor autor(String nombre, String apellido, String nac, String bio) {
        Autor a = new Autor();
        a.setNombre(nombre); a.setApellido(apellido);
        a.setNacionalidad(nac); a.setBiografia(bio); a.setActivo(true);
        return autorRepository.save(a);
    }

    private void libro(String titulo, String isbn, String sinopsis, int anio,
                       String editorial, List<Autor> autores, List<Categoria> cats, int numEjemplares) {
        Libro l = new Libro();
        l.setTitulo(titulo); l.setIsbn(isbn); l.setSinopsis(sinopsis);
        l.setAnioPublicacion(anio); l.setEditorial(editorial);
        l.setIdioma("Español"); l.setActivo(true);
        l.setAutores(autores); l.setCategorias(cats);
        l = libroRepository.save(l);

        for (int i = 1; i <= numEjemplares; i++) {
            Ejemplar e = new Ejemplar();
            e.setLibro(l);
            e.setCodigoInventario("INV-" + isbn.substring(0, Math.min(8, isbn.length())) + "-" + String.format("%02d", i));
            e.setUbicacionFisica("Estante A-" + i);
            e.setCondicion("BUENO");
            e.setFechaAdquisicion(LocalDateTime.now());
            e.setActivo(true);
            ejemplarRepository.save(e);
        }
    }
}
