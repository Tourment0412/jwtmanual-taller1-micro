package com.uniquindio.archmicroserv.jwtgeneratortaller1.acceptance.config;

import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.Usuario;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.model.enums.Rol;
import com.uniquindio.archmicroserv.jwtgeneratortaller1.repositories.UsuarioRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Configuración para crear datos de prueba necesarios en las pruebas de integración.
 * 
 * Esta clase se encarga de "sembrar" (seed) datos de prueba en la base de datos
 * antes de que se ejecuten las pruebas de Cucumber. Específicamente, crea un
 * usuario administrador con credenciales conocidas que pueden ser utilizadas
 * por los escenarios de prueba.
 * 
 * Funcionalidades principales:
 * - Crea un usuario administrador con credenciales fijas y conocidas
 * - Evita duplicados verificando si el usuario ya existe
 * - Se ejecuta automáticamente al iniciar las pruebas
 * - Proporciona datos consistentes para todos los escenarios
 * 
 * Datos del usuario administrador:
 * - Usuario: "admin"
 * - Email: "admin@example.com"
 * - Teléfono: "3000000000"
 * - Contraseña: "admin123"
 * - Rol: ADMIN
 * 
 * @author Sistema de Pruebas
 * @version 1.0
 * @since 2024
 */
@TestConfiguration
public class AdminSeedConfig {

    /**
     * Crea un bean que se encarga de sembrar el usuario administrador en la base de datos.
     * 
     * Este método utiliza un patrón especial de Spring donde retorna un Object anónimo
     * con un método @PostConstruct. Esto permite:
     * - Ejecutar código después de que Spring inicialice todos los beans
     * - Acceder a repositorios ya configurados
     * - Garantizar que el usuario admin esté disponible antes de las pruebas
     * 
     * El método verifica si ya existe un usuario con ID "admin" y solo lo crea
     * si no existe, evitando duplicados y errores en ejecuciones posteriores.
     * 
     * @param usuarioRepo Repositorio de usuarios (inyectado por Spring)
     * @return Object anónimo con método @PostConstruct para inicialización
     */
    @Bean
    public Object seedAdminUser(UsuarioRepo usuarioRepo) {
        return new Object() {
            /**
             * Método que se ejecuta después de la inicialización de Spring.
             * 
             * Este método se ejecuta automáticamente cuando Spring termina de
             * inicializar todos los beans. En este punto:
             * - La base de datos ya está configurada y disponible
             * - Los repositorios están listos para usar
             * - La aplicación está completamente inicializada
             * 
             * El proceso de creación del usuario admin:
             * 1. Busca si ya existe un usuario con ID "admin"
             * 2. Si no existe, crea uno nuevo con datos predefinidos
             * 3. Si ya existe, no hace nada (evita duplicados)
             * 4. Guarda el usuario en la base de datos
             */
            @PostConstruct
            public void init() {
                // Buscar usuario admin existente, si no existe, crear uno nuevo
                usuarioRepo.findById("admin").orElseGet(() -> {
                    // Crear nuevo usuario administrador
                    Usuario u = new Usuario();
                    u.setUsuario("admin");                    // ID único del usuario
                    u.setCorreo("admin@example.com");         // Email de contacto
                    u.setNumeroTelefono("3000000000");        // Teléfono colombiano válido
                    u.setClave("admin123");                   // Contraseña conocida para pruebas
                    u.setRol(Rol.getRolByName("ADMIN"));      // Rol de administrador
                    
                    // Guardar en la base de datos y retornar
                    return usuarioRepo.save(u);
                });
            }
        };
    }
}


