package com.diginexa.bitacora.services;

import com.diginexa.bitacora.config.JwtConfig;
import com.diginexa.bitacora.dtos.AuthSuccessResponse;
import com.diginexa.bitacora.dtos.LoginRequest;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;

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
            String accessToken = jwtService.generateAccessToken(usuario);
            String refreshToken = jwtService.generateRefreshToken(usuario);

            UserResponse userResponse = new UserResponse(
                    usuario.getId(),
                    usuario.getCorreo(),
                    Math.toIntExact(usuario.getRol().getId()),
                    usuario.getNombre()
            );

            return new AuthSuccessResponse(
                    "Login exitoso",
                    accessToken,
                    refreshToken,
                    jwtService.getJwtConfig().getJwtExpiration(),
                    userResponse
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
        Usuario usuario = new Usuario();
        usuario.setNombre(registerRequest.getNombre());
        usuario.setCorreo(registerRequest.getCorreo().toLowerCase().trim());
        usuario.setContrasena(passwordEncoder.encode(registerRequest.getContrasena()));
        usuario.setRol(rol);

        Usuario savedUsuario = userService.save(usuario);

        // Generar tokens
        String accessToken = jwtService.generateAccessToken(savedUsuario);
        String refreshToken = jwtService.generateRefreshToken(savedUsuario);

        UserResponse userResponse = new UserResponse(
                savedUsuario.getId(),
                savedUsuario.getCorreo(),
                Math.toIntExact(savedUsuario.getRol().getId()),
                savedUsuario.getNombre()
        );

        return new AuthSuccessResponse(
                "Usuario registrado exitosamente",
                accessToken,
                refreshToken,
                jwtService.getJwtConfig().getJwtExpiration(),
                userResponse
        );
    }

    public AuthSuccessResponse refreshToken(String refreshToken) {
        try {
            String correo = jwtService.extractUsername(refreshToken);
            Usuario usuario = userService.findByCorreo(correo);

            if (!jwtService.validateToken(refreshToken, usuario)) {
                throw new InvalidJwtTokenException("Refresh token inválido o expirado");
            }

            // Generar nuevos tokens
            String newAccessToken = jwtService.generateAccessToken(usuario);
            String newRefreshToken = jwtService.generateRefreshToken(usuario);

            UserResponse userResponse = new UserResponse(
                    usuario.getId(),
                    usuario.getCorreo(),
                    Math.toIntExact(usuario.getRol().getId()),
                    usuario.getNombre()
            );

            return new AuthSuccessResponse(
                    "Token refrescado exitosamente",
                    newAccessToken,
                    newRefreshToken,
                    jwtService.getJwtConfig().getJwtExpiration(),
                    userResponse
            );
        } catch (Exception e) {
            throw new InvalidJwtTokenException("Error al procesar el refresh token: " + e.getMessage());
        }
    }
}