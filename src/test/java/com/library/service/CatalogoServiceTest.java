package com.library.service;

import com.library.dto.request.LibroRequest;
import com.library.dto.response.LibroResponse;
import com.library.entity.*;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.*;
import com.library.service.impl.CatalogoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogoService - Tests unitarios")
class CatalogoServiceTest {

    @Mock private LibroRepository libroRepository;
    @Mock private AutorRepository autorRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private EjemplarRepository ejemplarRepository;
    @Mock private LibroDigitalRepository libroDigitalRepository;

    @InjectMocks
    private CatalogoService catalogoService;

    private Libro libroMock;
    private Autor autorMock;
    private Categoria categoriaMock;

    @BeforeEach
    void setUp() {
        autorMock = Autor.builder().id(1L).nombre("Gabriel").apellido("García Márquez")
                .activo(true).libros(List.of()).build();

        categoriaMock = Categoria.builder().id(1L).nombre("Novela").activa(true)
                .subcategorias(List.of()).libros(List.of()).build();

        libroMock = Libro.builder().id(1L).titulo("Cien años de soledad")
                .isbn("978-84-376-0494-7").activo(true)
                .autores(List.of(autorMock)).categorias(List.of(categoriaMock))
                .ejemplares(List.of()).librosDigitales(List.of()).reservas(List.of()).build();
    }

    @Test
    @DisplayName("obtenerLibro() - debe retornar libro existente")
    void obtenerLibro_ShouldReturnBook() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libroMock));
        when(ejemplarRepository.countByLibroIdAndEstadoAndActivoTrue(anyLong(), any())).thenReturn(2L);
        when(ejemplarRepository.findByLibroIdAndActivoTrue(anyLong())).thenReturn(List.of());
        when(libroDigitalRepository.findDisponibleByLibroId(anyLong())).thenReturn(Optional.empty());

        LibroResponse response = catalogoService.obtenerLibro(1L);

        assertThat(response).isNotNull();
        assertThat(response.getTitulo()).isEqualTo("Cien años de soledad");
        assertThat(response.getIsbn()).isEqualTo("978-84-376-0494-7");
    }

    @Test
    @DisplayName("obtenerLibro() - debe lanzar excepción si no existe")
    void obtenerLibro_ShouldThrowIfNotFound() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogoService.obtenerLibro(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("crearLibro() - debe fallar si ISBN ya existe")
    void crearLibro_ShouldFailIfIsbnExists() {
        LibroRequest req = new LibroRequest();
        req.setTitulo("Otro Libro");
        req.setIsbn("978-84-376-0494-7");
        req.setAutorIds(List.of(1L));
        req.setCategoriaIds(List.of(1L));

        when(libroRepository.existsByIsbn("978-84-376-0494-7")).thenReturn(true);

        assertThatThrownBy(() -> catalogoService.crearLibro(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ISBN");
    }

    @Test
    @DisplayName("crearLibro() - debe guardar libro correctamente")
    void crearLibro_ShouldSaveBook() {
        LibroRequest req = new LibroRequest();
        req.setTitulo("Nuevo Libro");
        req.setIsbn("978-0-00-000000-1");
        req.setAutorIds(List.of(1L));
        req.setCategoriaIds(List.of(1L));

        when(libroRepository.existsByIsbn(anyString())).thenReturn(false);
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autorMock));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaMock));
        when(libroRepository.save(any())).thenReturn(libroMock);
        when(ejemplarRepository.countByLibroIdAndEstadoAndActivoTrue(anyLong(), any())).thenReturn(0L);
        when(ejemplarRepository.findByLibroIdAndActivoTrue(anyLong())).thenReturn(List.of());
        when(libroDigitalRepository.findDisponibleByLibroId(anyLong())).thenReturn(Optional.empty());

        LibroResponse response = catalogoService.crearLibro(req);

        assertThat(response).isNotNull();
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    @DisplayName("eliminarLibro() - debe hacer soft delete")
    void eliminarLibro_ShouldSoftDelete() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libroMock));

        catalogoService.eliminarLibro(1L);

        verify(libroRepository).save(argThat(l -> !l.isActivo()));
    }
}
