package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.coordinador.*;
import com.diginexa.bitacora.entities.Asignatura;
import com.diginexa.bitacora.entities.Rol;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.repositories.AsignaturaRepository;
import com.diginexa.bitacora.repositories.RolRepository;
import com.diginexa.bitacora.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CoordinadorService {

    private static final String CLAVE_POR_DEFECTO = "Unbosque";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // =============================================
    // GESTIÓN DE USUARIOS
    // =============================================

    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::toUsuarioDTO)
                .toList();
    }

    public UsuarioDTO obtenerUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        return toUsuarioDTO(usuario);
    }

    @Transactional
    public UsuarioDTO crearUsuario(CreateUsuarioRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        String claveAsignada = (request.getContrasena() != null && !request.getContrasena().isBlank())
                ? request.getContrasena()
                : CLAVE_POR_DEFECTO;

        Set<Rol> roles = resolverRoles(request.getRoles());

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .correo(request.getCorreo().toLowerCase().trim())
                .contrasena(passwordEncoder.encode(claveAsignada))
                .roles(roles)
                .activo(true)
                .requiereCambioClave(true)
                .build();

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Usuario creado por coordinador: id={}, correo={}", saved.getId(), saved.getCorreo());

        emailService.enviarBienvenida(saved.getCorreo(), saved.getNombre(), claveAsignada);

        return toUsuarioDTO(saved);
    }

    @Transactional
    public UsuarioDTO actualizarUsuario(Integer id, UpdateUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        if (request.getNombre() != null) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getCorreo() != null) {
            if (!usuario.getCorreo().equals(request.getCorreo()) && usuarioRepository.existsByCorreo(request.getCorreo())) {
                throw new IllegalArgumentException("El correo ya está registrado");
            }
            usuario.setCorreo(request.getCorreo().toLowerCase().trim());
        }
        if (request.getContrasena() != null && !request.getContrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            usuario.setRoles(resolverRoles(request.getRoles()));
        }
        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Usuario actualizado por coordinador: id={}", saved.getId());
        return toUsuarioDTO(saved);
    }

    @Transactional
    public ImportarUsuariosResponse importarUsuariosCsv(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV está vacío");
        }

        List<ImportarUsuariosResponse.ErrorFilaDTO> listaErrores = new ArrayList<>();
        int creados = 0;
        int totalFilas = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            boolean esCabecera = true;

            while ((linea = reader.readLine()) != null) {
                if (esCabecera) {
                    esCabecera = false;
                    continue;
                }
                if (linea.isBlank()) continue;

                totalFilas++;
                int filaActual = totalFilas + 1; // +1 por cabecera

                String[] campos = linea.split(",", -1);
                if (campos.length < 3) {
                    listaErrores.add(ImportarUsuariosResponse.ErrorFilaDTO.builder()
                            .fila(filaActual).correo("")
                            .error("Formato inválido: se esperan 3 columnas (nombre,correo,roles)")
                            .build());
                    continue;
                }

                String nombre = campos[0].trim();
                String correo = campos[1].trim().toLowerCase();
                String rolesCsv = campos[2].trim();

                if (nombre.isBlank() || correo.isBlank() || rolesCsv.isBlank()) {
                    listaErrores.add(ImportarUsuariosResponse.ErrorFilaDTO.builder()
                            .fila(filaActual).correo(correo).error("Campos obligatorios vacíos").build());
                    continue;
                }

                List<Integer> roles;
                try {
                    roles = Arrays.stream(rolesCsv.split(";"))
                            .map(String::trim)
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    listaErrores.add(ImportarUsuariosResponse.ErrorFilaDTO.builder()
                            .fila(filaActual).correo(correo)
                            .error("Roles inválidos: deben ser números separados por ';'").build());
                    continue;
                }

                try {
                    CreateUsuarioRequest req = new CreateUsuarioRequest();
                    req.setNombre(nombre);
                    req.setCorreo(correo);
                    req.setRoles(roles);
                    crearUsuario(req);
                    creados++;
                } catch (IllegalArgumentException e) {
                    listaErrores.add(ImportarUsuariosResponse.ErrorFilaDTO.builder()
                            .fila(filaActual).correo(correo).error(e.getMessage()).build());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo CSV", e);
        }

        log.info("Importación CSV completada: {} procesados, {} creados, {} errores", totalFilas, creados, listaErrores.size());
        return ImportarUsuariosResponse.builder()
                .totalProcesados(totalFilas)
                .creados(creados)
                .errores(listaErrores.size())
                .detalleErrores(listaErrores)
                .build();
    }

    @Transactional
    public void toggleUsuarioActivo(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        if (Boolean.TRUE.equals(usuario.getGraduado())) {
            throw new IllegalStateException("No se puede cambiar el estado de un usuario graduado");
        }
        usuario.setActivo(!usuario.getActivo());
        usuarioRepository.save(usuario);
        log.info("Usuario {} - activo: {}", id, usuario.getActivo());
    }

    @Transactional
    public void marcarUsuarioGraduado(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setGraduado(true);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        log.info("Usuario {} marcado como graduado", id);
    }

    @Transactional
    public void reactivarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setGraduado(false);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        log.info("Usuario {} reactivado (graduado revertido)", id);
    }

    // =============================================
    // GESTIÓN DE ASIGNATURAS
    // =============================================

    public List<AsignaturaDTO> listarAsignaturas() {
        return asignaturaRepository.findAll().stream()
                .map(this::toAsignaturaDTO)
                .toList();
    }

    public AsignaturaDTO obtenerAsignatura(Integer id) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asignatura no encontrada con id: " + id));
        return toAsignaturaDTO(asignatura);
    }

    @Transactional
    public AsignaturaDTO crearAsignatura(CreateAsignaturaRequest request) {
        if (asignaturaRepository.existsByCodigo(request.getCodigo())) {
            throw new IllegalArgumentException("El código de asignatura ya existe");
        }

        Asignatura asignatura = Asignatura.builder()
                .nombre(request.getNombre())
                .codigo(request.getCodigo().toUpperCase().trim())
                .descripcion(request.getDescripcion())
                .creditos(request.getCreditos())
                .semestre(request.getSemestre())
                .activa(true)
                .build();

        Asignatura saved = asignaturaRepository.save(asignatura);
        log.info("Asignatura creada: id={}, codigo={}", saved.getId(), saved.getCodigo());
        return toAsignaturaDTO(saved);
    }

    @Transactional
    public AsignaturaDTO actualizarAsignatura(Integer id, UpdateAsignaturaRequest request) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asignatura no encontrada con id: " + id));

        if (request.getNombre() != null) asignatura.setNombre(request.getNombre());
        if (request.getCodigo() != null) {
            if (!asignatura.getCodigo().equals(request.getCodigo()) && asignaturaRepository.existsByCodigo(request.getCodigo())) {
                throw new IllegalArgumentException("El código de asignatura ya existe");
            }
            asignatura.setCodigo(request.getCodigo().toUpperCase().trim());
        }
        if (request.getDescripcion() != null) asignatura.setDescripcion(request.getDescripcion());
        if (request.getCreditos() != null) asignatura.setCreditos(request.getCreditos());
        if (request.getSemestre() != null) asignatura.setSemestre(request.getSemestre());
        if (request.getActiva() != null) asignatura.setActiva(request.getActiva());

        Asignatura saved = asignaturaRepository.save(asignatura);
        log.info("Asignatura actualizada: id={}", saved.getId());
        return toAsignaturaDTO(saved);
    }

    @Transactional
    public void asignarEstudiantes(Integer asignaturaId, List<Integer> estudianteIds) {
        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new EntityNotFoundException("Asignatura no encontrada"));

        List<Usuario> estudiantes = usuarioRepository.findAllById(estudianteIds);
        asignatura.getEstudiantes().addAll(new HashSet<>(estudiantes));
        asignaturaRepository.save(asignatura);
        log.info("Estudiantes asignados a asignatura {}: {}", asignaturaId, estudianteIds);
    }

    @Transactional
    public void asignarTutores(Integer asignaturaId, List<Integer> tutorIds) {
        Asignatura asignatura = asignaturaRepository.findById(asignaturaId)
                .orElseThrow(() -> new EntityNotFoundException("Asignatura no encontrada"));

        List<Usuario> tutores = usuarioRepository.findAllById(tutorIds);
        asignatura.getTutores().addAll(new HashSet<>(tutores));
        asignaturaRepository.save(asignatura);
        log.info("Tutores asignados a asignatura {}: {}", asignaturaId, tutorIds);
    }

    // =============================================
    // HELPERS
    // =============================================

    private Set<Rol> resolverRoles(List<Integer> roleIds) {
        Set<Rol> roles = new HashSet<>();
        for (Integer roleId : roleIds) {
            Rol rol = rolRepository.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con id: " + roleId));
            roles.add(rol);
        }
        return roles;
    }

    private UsuarioDTO toUsuarioDTO(Usuario usuario) {
        List<Integer> roleIds = usuario.getRoles().stream()
                .sorted(Comparator.comparing(Rol::getId))
                .map(r -> Math.toIntExact(r.getId()))
                .toList();

        List<String> roleNames = usuario.getRoles().stream()
                .sorted(Comparator.comparing(Rol::getId))
                .map(Rol::getNombre)
                .toList();

        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .roles(roleIds)
                .rolesNombres(roleNames)
                .activo(usuario.getActivo())
                .graduado(usuario.getGraduado())
                .requiereCambioClave(usuario.getRequiereCambioClave())
                .ultimoAcceso(usuario.getUltimoAcceso())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }

    private AsignaturaDTO toAsignaturaDTO(Asignatura asignatura) {
        return AsignaturaDTO.builder()
                .id(asignatura.getId())
                .nombre(asignatura.getNombre())
                .codigo(asignatura.getCodigo())
                .descripcion(asignatura.getDescripcion())
                .creditos(asignatura.getCreditos())
                .semestre(asignatura.getSemestre())
                .activa(asignatura.getActiva())
                .totalEstudiantes(asignatura.getEstudiantes() != null ? asignatura.getEstudiantes().size() : 0)
                .totalTutores(asignatura.getTutores() != null ? asignatura.getTutores().size() : 0)
                .fechaCreacion(asignatura.getFechaCreacion())
                .build();
    }
}
