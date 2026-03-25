package com.library.service.impl;

import com.library.dto.request.*;
import com.library.dto.response.*;
import com.library.entity.*;
import com.library.enums.EstadoEjemplar;
import com.library.exception.*;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de catálogo: libros, autores, categorías, ejemplares y digitales
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CatalogoService {

    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final EjemplarRepository ejemplarRepository;
    private final LibroDigitalRepository libroDigitalRepository;

    // ============ CATEGORÍAS ============

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findByPadreIsNullAndActivaTrue()
                .stream().map(this::toCategoriaResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtenerCategoria(Long id) {
        return toCategoriaResponse(findCategoria(id));
    }

    public CategoriaResponse crearCategoria(CategoriaRequest req) {
        if (categoriaRepository.existsByNombre(req.getNombre())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + req.getNombre());
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(req.getNombre());
        categoria.setDescripcion(req.getDescripcion());
        if (req.getPadreId() != null) {
            categoria.setPadre(findCategoria(req.getPadreId()));
        }
        return toCategoriaResponse(categoriaRepository.save(categoria));
    }

    public CategoriaResponse actualizarCategoria(Long id, CategoriaRequest req) {
        Categoria categoria = findCategoria(id);
        categoria.setNombre(req.getNombre());
        categoria.setDescripcion(req.getDescripcion());
        if (req.getPadreId() != null) {
            categoria.setPadre(findCategoria(req.getPadreId()));
        } else {
            categoria.setPadre(null);
        }
        return toCategoriaResponse(categoriaRepository.save(categoria));
    }

    public void eliminarCategoria(Long id) {
        Categoria categoria = findCategoria(id);
        categoria.setActiva(false);
        categoriaRepository.save(categoria);
    }

    // ============ AUTORES ============

    @Transactional(readOnly = true)
    public Page<AutorResponse> listarAutores(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("apellido").ascending());
        return autorRepository.findBySearchTerm(search, pageable).map(this::toAutorResponse);
    }

    @Transactional(readOnly = true)
    public AutorResponse obtenerAutor(Long id) {
        return toAutorResponse(findAutor(id));
    }

    public AutorResponse crearAutor(AutorRequest req) {
        Autor autor = new Autor();
        autor.setNombre(req.getNombre());
        autor.setApellido(req.getApellido());
        autor.setBiografia(req.getBiografia());
        autor.setNacionalidad(req.getNacionalidad());
        if (req.getFechaNacimiento() != null) autor.setFechaNacimiento(LocalDate.parse(req.getFechaNacimiento()));
        if (req.getFechaFallecimiento() != null) autor.setFechaFallecimiento(LocalDate.parse(req.getFechaFallecimiento()));
        return toAutorResponse(autorRepository.save(autor));
    }

    public AutorResponse actualizarAutor(Long id, AutorRequest req) {
        Autor autor = findAutor(id);
        autor.setNombre(req.getNombre());
        autor.setApellido(req.getApellido());
        autor.setBiografia(req.getBiografia());
        autor.setNacionalidad(req.getNacionalidad());
        if (req.getFechaNacimiento() != null) autor.setFechaNacimiento(LocalDate.parse(req.getFechaNacimiento()));
        if (req.getFechaFallecimiento() != null) autor.setFechaFallecimiento(LocalDate.parse(req.getFechaFallecimiento()));
        return toAutorResponse(autorRepository.save(autor));
    }

    public void eliminarAutor(Long id) {
        Autor autor = findAutor(id);
        autor.setActivo(false);
        autorRepository.save(autor);
    }

    // ============ LIBROS ============

    @Transactional(readOnly = true)
    public Page<LibroResponse> buscarLibros(String titulo, String isbn, Long autorId,
                                             Long categoriaId, String editorial, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("titulo").ascending());
        return libroRepository.busquedaAvanzada(titulo, isbn, autorId, categoriaId, editorial, pageable)
                .map(this::toLibroResponse);
    }

    @Transactional(readOnly = true)
    public LibroResponse obtenerLibro(Long id) {
        return toLibroResponse(findLibro(id));
    }

    public LibroResponse crearLibro(LibroRequest req) {
        if (libroRepository.existsByIsbn(req.getIsbn())) {
            throw new DuplicateResourceException("Ya existe un libro con ISBN: " + req.getIsbn());
        }
        Libro libro = new Libro();
        mapLibroFields(libro, req);
        return toLibroResponse(libroRepository.save(libro));
    }

    public LibroResponse actualizarLibro(Long id, LibroRequest req) {
        Libro libro = findLibro(id);
        if (!libro.getIsbn().equals(req.getIsbn()) && libroRepository.existsByIsbn(req.getIsbn())) {
            throw new DuplicateResourceException("Ya existe un libro con ISBN: " + req.getIsbn());
        }
        mapLibroFields(libro, req);
        return toLibroResponse(libroRepository.save(libro));
    }

    public void eliminarLibro(Long id) {
        Libro libro = findLibro(id);
        libro.setActivo(false);
        libroRepository.save(libro);
    }

    private void mapLibroFields(Libro libro, LibroRequest req) {
        libro.setTitulo(req.getTitulo());
        libro.setIsbn(req.getIsbn());
        libro.setSinopsis(req.getSinopsis());
        libro.setAnioPublicacion(req.getAnioPublicacion());
        libro.setEditorial(req.getEditorial());
        libro.setIdioma(req.getIdioma());
        libro.setNumeroPaginas(req.getNumeroPaginas());
        libro.setImagenPortada(req.getImagenPortada());

        List<Autor> autores = req.getAutorIds().stream()
                .map(this::findAutor).collect(Collectors.toList());
        libro.setAutores(autores);

        List<Categoria> categorias = req.getCategoriaIds().stream()
                .map(this::findCategoria).collect(Collectors.toList());
        libro.setCategorias(categorias);
    }

    // ============ EJEMPLARES ============

    @Transactional(readOnly = true)
    public List<EjemplarResponse> listarEjemplares(Long libroId) {
        return ejemplarRepository.findByLibroIdAndActivoTrue(libroId)
                .stream().map(this::toEjemplarResponse).collect(Collectors.toList());
    }

    public EjemplarResponse crearEjemplar(EjemplarRequest req) {
        if (ejemplarRepository.existsByCodigoInventario(req.getCodigoInventario())) {
            throw new DuplicateResourceException("Código de inventario ya existe: " + req.getCodigoInventario());
        }
        Libro libro = findLibro(req.getLibroId());
        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setLibro(libro);
        ejemplar.setCodigoInventario(req.getCodigoInventario());
        ejemplar.setUbicacionFisica(req.getUbicacionFisica());
        ejemplar.setCondicion(req.getCondicion());
        ejemplar.setFechaAdquisicion(LocalDateTime.now());
        return toEjemplarResponse(ejemplarRepository.save(ejemplar));
    }

    public EjemplarResponse actualizarEstadoEjemplar(Long id, String estado) {
        Ejemplar ejemplar = ejemplarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejemplar", id));
        ejemplar.setEstado(EstadoEjemplar.valueOf(estado));
        return toEjemplarResponse(ejemplarRepository.save(ejemplar));
    }

    public void eliminarEjemplar(Long id) {
        Ejemplar ejemplar = ejemplarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejemplar", id));
        ejemplar.setActivo(false);
        ejemplarRepository.save(ejemplar);
    }

    // ============ LIBROS DIGITALES ============

    @Transactional(readOnly = true)
    public List<LibroDigitalResponse> listarLibrosDigitales(Long libroId) {
        return libroDigitalRepository.findByLibroIdAndActivoTrue(libroId)
                .stream().map(this::toLibroDigitalResponse).collect(Collectors.toList());
    }

    public LibroDigitalResponse crearLibroDigital(LibroDigitalRequest req) {
        Libro libro = findLibro(req.getLibroId());
        LibroDigital ld = new LibroDigital();
        ld.setLibro(libro);
        ld.setFormato(req.getFormato());
        ld.setUrlArchivo(req.getUrlArchivo());
        ld.setTamanioMb(req.getTamanioMb());
        ld.setMaxPrestamosSimultaneos(req.getMaxPrestamosSimultaneos() != null ? req.getMaxPrestamosSimultaneos() : 5);
        return toLibroDigitalResponse(libroDigitalRepository.save(ld));
    }

    public void eliminarLibroDigital(Long id) {
        LibroDigital ld = libroDigitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LibroDigital", id));
        ld.setActivo(false);
        libroDigitalRepository.save(ld);
    }

    // ============ HELPERS (find) ============

    public Libro findLibro(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", id));
    }

    public Autor findAutor(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor", id));
    }

    public Categoria findCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }

    // ============ MAPPERS ============

    public LibroResponse toLibroResponse(Libro l) {
        long disponibles = ejemplarRepository.countByLibroIdAndEstadoAndActivoTrue(l.getId(), EstadoEjemplar.DISPONIBLE);
        long total = ejemplarRepository.findByLibroIdAndActivoTrue(l.getId()).size();
        boolean hayDigital = libroDigitalRepository.findDisponibleByLibroId(l.getId()).isPresent();

        return LibroResponse.builder()
                .id(l.getId()).titulo(l.getTitulo()).isbn(l.getIsbn())
                .sinopsis(l.getSinopsis()).anioPublicacion(l.getAnioPublicacion())
                .editorial(l.getEditorial()).idioma(l.getIdioma())
                .numeroPaginas(l.getNumeroPaginas()).imagenPortada(l.getImagenPortada())
                .activo(l.isActivo()).createdAt(l.getCreatedAt())
                .autores(l.getAutores().stream().map(this::toAutorResponse).collect(Collectors.toList()))
                .categorias(l.getCategorias().stream().map(this::toCategoriaResponse).collect(Collectors.toList()))
                .totalEjemplares((int) total).ejemplaresDisponibles((int) disponibles)
                .disponibleDigital(hayDigital)
                .build();
    }

    public AutorResponse toAutorResponse(Autor a) {
        return AutorResponse.builder()
                .id(a.getId()).nombre(a.getNombre()).apellido(a.getApellido())
                .biografia(a.getBiografia()).nacionalidad(a.getNacionalidad())
                .fechaNacimiento(a.getFechaNacimiento()).fechaFallecimiento(a.getFechaFallecimiento())
                .activo(a.isActivo()).build();
    }

    public CategoriaResponse toCategoriaResponse(Categoria c) {
        return CategoriaResponse.builder()
                .id(c.getId()).nombre(c.getNombre()).descripcion(c.getDescripcion())
                .padreId(c.getPadre() != null ? c.getPadre().getId() : null)
                .nombrePadre(c.getPadre() != null ? c.getPadre().getNombre() : null)
                .activa(c.isActiva())
                .subcategorias(c.getSubcategorias().stream()
                        .filter(Categoria::isActiva)
                        .map(this::toCategoriaResponse).collect(Collectors.toList()))
                .build();
    }

    public EjemplarResponse toEjemplarResponse(Ejemplar e) {
        return EjemplarResponse.builder()
                .id(e.getId()).libroId(e.getLibro().getId())
                .tituloLibro(e.getLibro().getTitulo())
                .codigoInventario(e.getCodigoInventario())
                .estado(e.getEstado()).ubicacionFisica(e.getUbicacionFisica())
                .condicion(e.getCondicion()).fechaAdquisicion(e.getFechaAdquisicion())
                .activo(e.isActivo()).build();
    }

    public LibroDigitalResponse toLibroDigitalResponse(LibroDigital ld) {
        return LibroDigitalResponse.builder()
                .id(ld.getId()).libroId(ld.getLibro().getId())
                .tituloLibro(ld.getLibro().getTitulo())
                .formato(ld.getFormato()).urlArchivo(ld.getUrlArchivo())
                .tamanioMb(ld.getTamanioMb())
                .maxPrestamosSimultaneos(ld.getMaxPrestamosSimultaneos())
                .prestamosActivos(ld.getPrestamosActivos())
                .disponible(ld.getPrestamosActivos() < ld.getMaxPrestamosSimultaneos())
                .activo(ld.isActivo()).createdAt(ld.getCreatedAt()).build();
    }
}
