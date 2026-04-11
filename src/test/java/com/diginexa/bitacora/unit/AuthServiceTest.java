package com.diginexa.bitacora.unit;

import com.diginexa.bitacora.config.JwtConfig;
import com.diginexa.bitacora.dtos.AuthSuccessResponse;
import com.diginexa.bitacora.dtos.LoginRequest;
import com.diginexa.bitacora.dtos.RegisterRequest;
import com.diginexa.bitacora.entities.Rol;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.services.AuthService;
import com.diginexa.bitacora.services.EmailService;
import com.diginexa.bitacora.services.JwtService;
import com.diginexa.bitacora.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.diginexa.bitacora.repositories.RolRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RolRepository rolRepository;
    @Mock private EmailService emailService;
    @Mock private JwtConfig jwtConfig;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private Rol rolEstudiante;
    private Rol rolTutor;
    private Rol rolCoordinador;
    private Usuario usuarioUnRol;
    private Usuario usuarioMultiRol;

    @BeforeEach
    void setUp() {
        rolEstudiante = new Rol(1L, "estudiante");
        rolTutor = new Rol(2L, "tutor");
        rolCoordinador = new Rol(3L, "coordinador");

        usuarioUnRol = buildUsuario(1, "estudiante@ueb.edu.ec", Set.of(rolEstudiante));
        usuarioMultiRol = buildUsuario(2, "tutor@ueb.edu.ec", Set.of(rolTutor, rolCoordinador));
    }

    // =========================================================
    // login()
    // =========================================================

    @Test
    void login_conUsuarioDeUnRol_retornaRolesComoListaDeUnElemento() {
        LoginRequest request = new LoginRequest();
        request.setUsuario("estudiante@ueb.edu.ec");
        request.setContrasena("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuarioUnRol);
        when(userService.save(any())).thenReturn(usuarioUnRol);
        when(jwtService.generateAccessToken(usuarioUnRol)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuarioUnRol)).thenReturn("refresh-token");
        when(jwtService.getJwtConfig()).thenReturn(jwtConfig);
        when(jwtConfig.getJwtExpiration()).thenReturn(3600L);

        AuthSuccessResponse response = authService.login(request);

        assertThat(response).isNotNull();
        UserResponse user = response.getUser();
        assertThat(user.getRoles()).isNotNull();
        assertThat(user.getRoles()).hasSize(1);
        assertThat(user.getRoles()).contains(1);
    }

    @Test
    void login_conUsuarioDeMultiplesRoles_retornaTodosLosRolesEnLaLista() {
        LoginRequest request = new LoginRequest();
        request.setUsuario("tutor@ueb.edu.ec");
        request.setContrasena("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuarioMultiRol);
        when(userService.save(any())).thenReturn(usuarioMultiRol);
        when(jwtService.generateAccessToken(usuarioMultiRol)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuarioMultiRol)).thenReturn("refresh-token");
        when(jwtService.getJwtConfig()).thenReturn(jwtConfig);
        when(jwtConfig.getJwtExpiration()).thenReturn(3600L);

        AuthSuccessResponse response = authService.login(request);

        assertThat(response).isNotNull();
        UserResponse user = response.getUser();
        assertThat(user.getRoles()).isNotNull();
        assertThat(user.getRoles()).hasSize(2);
        assertThat(user.getRoles()).containsExactlyInAnyOrder(2, 3);
    }

    @Test
    void login_retornaUsernameDerivadoDelCorreo() {
        LoginRequest request = new LoginRequest();
        request.setUsuario("estudiante@ueb.edu.ec");
        request.setContrasena("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuarioUnRol);
        when(userService.save(any())).thenReturn(usuarioUnRol);
        when(jwtService.generateAccessToken(usuarioUnRol)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuarioUnRol)).thenReturn("refresh-token");
        when(jwtService.getJwtConfig()).thenReturn(jwtConfig);
        when(jwtConfig.getJwtExpiration()).thenReturn(3600L);

        AuthSuccessResponse response = authService.login(request);

        assertThat(response.getUser().getUsername()).isEqualTo("estudiante");
    }

    // =========================================================
    // register()
    // =========================================================

    @Test
    void register_retornaRolesComoListaConElRolAsignado() {
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Nuevo Estudiante");
        request.setCorreo("nuevo@ueb.edu.ec");
        request.setContrasena("Passw0rd!");
        request.setRolId(1);

        when(rolRepository.findById(1)).thenReturn(Optional.of(rolEstudiante));
        when(userService.existsByCorreo(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userService.save(any())).thenReturn(usuarioUnRol);
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtService.getJwtConfig()).thenReturn(jwtConfig);
        when(jwtConfig.getJwtExpiration()).thenReturn(3600L);

        AuthSuccessResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getUser().getRoles()).isNotNull();
        assertThat(response.getUser().getRoles()).isNotEmpty();
        assertThat(response.getUser().getRoles()).contains(1);
    }

    // =========================================================
    // refreshToken()
    // =========================================================

    @Test
    void refreshToken_conUsuarioDeUnRol_retornaRolesComoLista() {
        String token = "valid-refresh-token";

        when(jwtService.extractUsername(token)).thenReturn("estudiante@ueb.edu.ec");
        when(userService.findByUsername("estudiante@ueb.edu.ec")).thenReturn(usuarioUnRol);
        when(jwtService.validateToken(token, usuarioUnRol)).thenReturn(true);
        when(jwtService.generateAccessToken(usuarioUnRol)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(usuarioUnRol)).thenReturn("new-refresh-token");
        when(jwtService.getJwtConfig()).thenReturn(jwtConfig);
        when(jwtConfig.getJwtExpiration()).thenReturn(3600L);

        AuthSuccessResponse response = authService.refreshToken(token);

        assertThat(response).isNotNull();
        assertThat(response.getUser().getRoles()).hasSize(1);
        assertThat(response.getUser().getRoles()).contains(1);
    }

    @Test
    void refreshToken_conUsuarioDeMultiplesRoles_retornaTodosLosRoles() {
        String token = "valid-refresh-token";

        when(jwtService.extractUsername(token)).thenReturn("tutor@ueb.edu.ec");
        when(userService.findByUsername("tutor@ueb.edu.ec")).thenReturn(usuarioMultiRol);
        when(jwtService.validateToken(token, usuarioMultiRol)).thenReturn(true);
        when(jwtService.generateAccessToken(usuarioMultiRol)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(usuarioMultiRol)).thenReturn("new-refresh-token");
        when(jwtService.getJwtConfig()).thenReturn(jwtConfig);
        when(jwtConfig.getJwtExpiration()).thenReturn(3600L);

        AuthSuccessResponse response = authService.refreshToken(token);

        assertThat(response).isNotNull();
        assertThat(response.getUser().getRoles()).hasSize(2);
        assertThat(response.getUser().getRoles()).containsExactlyInAnyOrder(2, 3);
    }

    // =========================================================
    // Helper
    // =========================================================

    private Usuario buildUsuario(Integer id, String correo, Set<Rol> roles) {
        Set<Rol> rolesCopy = new HashSet<>(roles);
        return Usuario.builder()
                .id(id)
                .nombre("Usuario Test")
                .correo(correo)
                .contrasena("hashed")
                .roles(rolesCopy)
                .activo(true)
                .requiereCambioClave(false)
                .build();
    }
}
