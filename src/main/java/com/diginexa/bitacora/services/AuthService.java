package com.diginexa.bitacora.services;

import com.diginexa.bitacora.config.JwtConfig;
import com.diginexa.bitacora.dtos.AuthSuccessResponse;
import com.diginexa.bitacora.dtos.CambiarClaveRequest;
import com.diginexa.bitacora.dtos.LoginRequest;
import com.diginexa.bitacora.dtos.RecuperarClaveRequest;
import com.diginexa.bitacora.dtos.RegisterRequest;
import com.diginexa.bitacora.dtos.UserResponse;
import com.diginexa.bitacora.entities.Rol;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.exceptions.domain.EmailAlreadyExistsException;
import com.diginexa.bitacora.exceptions.domain.InvalidEmailFormatException;
import com.diginexa.bitacora.exceptions.domain.RoleNotFoundException;
import com.diginexa.bitacora.exceptions.security.InvalidJwtTokenException;
import com.diginexa.bitacora.repositories.RolRepository;
import com.diginexa.bitacora.utils.EmailUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;
    private final EmailService emailService;

    @org.springframework.transaction.annotation.Transactional
    public AuthSuccessResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsuario(),
                            loginRequest.getContrasena()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            Usuario usuario = (Usuario) authentication.getPrincipal();

            // Registrar último acceso
            usuario.setUltimoAcceso(LocalDateTime.now());
            userService.save(usuario);
            String accessToken = jwtService.generateAccessToken(usuario);
            String refreshToken = jwtService.generateRefreshToken(usuario);

            Integer primaryRolId = usuario.getRoles().stream()
                    .min(Comparator.comparing(Rol::getId))
                    .map(r -> Math.toIntExact(r.getId()))
                    .orElse(0);

            UserResponse userResponse = new UserResponse(
                    usuario.getId(),
                    usuario.getCorreo(),
                    primaryRolId,
                    usuario.getNombre()
            );

            return new AuthSuccessResponse(
                    "Login exitoso",
                    accessToken,
                    refreshToken,
                    jwtService.getJwtConfig().getJwtExpiration(),
                    userResponse,
                    Boolean.TRUE.equals(usuario.getRequiereCambioClave())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    public AuthSuccessResponse register(RegisterRequest registerRequest) {
        // Validar formato de correo
        if (!EmailUtils.isValidEmail(registerRequest.getCorreo())) {
            throw new InvalidEmailFormatException("Formato de correo electrónico inválido");
        }

        // Verificar si el usuario ya existe
        if (userService.existsByCorreo(registerRequest.getCorreo())) {
            throw new EmailAlreadyExistsException("El correo electrónico ya está registrado");
        }

        // Validar que el rol existe
        Rol rol = rolRepository.findById(registerRequest.getRolId())
                .orElseThrow(() -> new RoleNotFoundException("Rol no encontrado"));

        // Crear nuevo usuario
        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        Usuario usuario = new Usuario();
        usuario.setNombre(registerRequest.getNombre());
        usuario.setCorreo(registerRequest.getCorreo().toLowerCase().trim());
        usuario.setContrasena(passwordEncoder.encode(registerRequest.getContrasena()));
        usuario.setRoles(roles);
        usuario.setActivo(true);

        Usuario savedUsuario = userService.save(usuario);

        // Generar tokens
        String accessToken = jwtService.generateAccessToken(savedUsuario);
        String refreshToken = jwtService.generateRefreshToken(savedUsuario);

        Integer primaryRolId = savedUsuario.getRoles().stream()
                .min(Comparator.comparing(Rol::getId))
                .map(r -> Math.toIntExact(r.getId()))
                .orElse(0);

        UserResponse userResponse = new UserResponse(
                savedUsuario.getId(),
                savedUsuario.getCorreo(),
                primaryRolId,
                savedUsuario.getNombre()
        );

        return new AuthSuccessResponse(
                "Usuario registrado exitosamente",
                accessToken,
                refreshToken,
                jwtService.getJwtConfig().getJwtExpiration(),
                userResponse,
                false
        );
    }

    @org.springframework.transaction.annotation.Transactional
    public void recuperarClave(RecuperarClaveRequest request) {
        String correo = request.getCorreo().toLowerCase().trim();

        // Si el correo no existe simplemente no hacemos nada (no revelar si existe o no)
        Usuario usuario;
        try {
            usuario = userService.findByCorreo(correo);
        } catch (UsernameNotFoundException e) {
            return;
        }

        String claveTemporal = generarClaveAleatoria();
        usuario.setContrasena(passwordEncoder.encode(claveTemporal));
        usuario.setRequiereCambioClave(true);
        userService.save(usuario);

        emailService.enviarBienvenida(usuario.getCorreo(), usuario.getNombre(), claveTemporal);
    }

    private String generarClaveAleatoria() {
        SecureRandom random = new SecureRandom();
        String minusculas = "abcdefghijklmnopqrstuvwxyz";
        String mayusculas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digitos    = "0123456789";
        String especiales = "@$!%*?&";

        List<Character> chars = new ArrayList<>();
        // Garantizar al menos uno de cada tipo (mínimo 8 total)
        chars.add(mayusculas.charAt(random.nextInt(mayusculas.length())));
        chars.add(minusculas.charAt(random.nextInt(minusculas.length())));
        chars.add(minusculas.charAt(random.nextInt(minusculas.length())));
        chars.add(digitos.charAt(random.nextInt(digitos.length())));
        chars.add(digitos.charAt(random.nextInt(digitos.length())));
        chars.add(especiales.charAt(random.nextInt(especiales.length())));
        chars.add(minusculas.charAt(random.nextInt(minusculas.length())));
        chars.add(mayusculas.charAt(random.nextInt(mayusculas.length())));

        Collections.shuffle(chars, random);
        StringBuilder sb = new StringBuilder();
        chars.forEach(sb::append);
        return sb.toString();
    }

    @org.springframework.transaction.annotation.Transactional
    public void cambiarClave(String username, CambiarClaveRequest request) {
        // Regex: mín 8 chars, 1 mayúscula, 1 minúscula, 1 número, 1 carácter especial
        String regexFortaleza = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_\\-])[A-Za-z\\d@$!%*?&.#_\\-]{8,}$";
        if (!request.getClaveNueva().matches(regexFortaleza)) {
            throw new IllegalArgumentException(
                "La contraseña debe tener mínimo 8 caracteres e incluir mayúsculas, minúsculas, números y un carácter especial (@$!%*?&.#_-)");
        }

        Usuario usuario = userService.findByUsername(username);

        if (!passwordEncoder.matches(request.getClaveActual(), usuario.getContrasena())) {
            throw new BadCredentialsException("La contraseña actual es incorrecta");
        }

        usuario.setContrasena(passwordEncoder.encode(request.getClaveNueva()));
        usuario.setRequiereCambioClave(false);
        userService.save(usuario);
    }

    public AuthSuccessResponse refreshToken(String refreshToken) {
        try {
            String username = jwtService.extractUsername(refreshToken);
            Usuario usuario = userService.findByUsername(username);

            if (!jwtService.validateToken(refreshToken, usuario)) {
                throw new InvalidJwtTokenException("Refresh token inválido o expirado");
            }

            // Generar nuevos tokens
            String newAccessToken = jwtService.generateAccessToken(usuario);
            String newRefreshToken = jwtService.generateRefreshToken(usuario);

            Integer primaryRolId = usuario.getRoles().stream()
                    .min(Comparator.comparing(Rol::getId))
                    .map(r -> Math.toIntExact(r.getId()))
                    .orElse(0);

            UserResponse userResponse = new UserResponse(
                    usuario.getId(),
                    usuario.getCorreo(),
                    primaryRolId,
                    usuario.getNombre()
            );

            return new AuthSuccessResponse(
                    "Token refrescado exitosamente",
                    newAccessToken,
                    newRefreshToken,
                    jwtService.getJwtConfig().getJwtExpiration(),
                    userResponse,
                    false
            );
        } catch (Exception e) {
            throw new InvalidJwtTokenException("Error al procesar el refresh token: " + e.getMessage());
        }
    }
}
