package pe.edu.upeu.PharmaBackend.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.upeu.PharmaBackend.entity.Rol;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SeguridadJwtTest {
    @Test
    void firma_y_verifica_token_sin_incluir_contrasena() {
        String secreto = Base64.getEncoder().encodeToString(
                "clave-de-prueba-de-64-bytes-para-jwt-pharmasoft-seguro-local-2026"
                        .getBytes(StandardCharsets.UTF_8));
        JwtService servicio = new JwtService(secreto, 60);
        UserDetails usuario = User.withUsername("admin")
                .password("hash-no-debe-aparecer-en-el-token")
                .authorities("ROLE_ADMIN", "CATALOGO_LEER")
                .build();

        String token = servicio.generarToken(usuario);
        assertEquals("admin", servicio.extraerUsername(token));
        assertEquals(3600, servicio.getExpiracionSegundos());
        assertEquals(Set.of("ROLE_ADMIN", "CATALOGO_LEER"),
                Set.copyOf(servicio.leerClaims(token).get("authorities", List.class)));
        assertFalse(token.contains("hash-no-debe-aparecer"));
        assertThrows(JwtException.class,
                () -> servicio.extraerUsername(token.substring(0, token.length() - 4) + "AAAA"));
    }

    @Test
    void permisos_por_rol_separan_catalogo_clientes_y_reportes() {
        Rol almacen = new Rol();
        almacen.setNombre("ALMACENERO");
        Rol vendedor = new Rol();
        vendedor.setNombre("VENDEDOR");
        Rol admin = new Rol();
        admin.setNombre("ADMIN");

        assertEquals(List.of("CATALOGO_ESCRIBIR", "CATALOGO_LEER"),
                PermisosPorRol.permisos(List.of(almacen)));
        assertTrue(PermisosPorRol.permisos(List.of(vendedor)).contains("CLIENTES_LEER"));
        assertFalse(PermisosPorRol.permisos(List.of(vendedor)).contains("REPORTES_VER"));
        assertEquals(8, PermisosPorRol.permisos(List.of(admin)).size());
    }
}
