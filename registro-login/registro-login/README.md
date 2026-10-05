# Registro y Login con Spring Boot + Spring Security

Aplicación web para registrar usuarios, iniciar sesión y consultar la lista de usuarios registrados.

## Tecnologías

- Java 25 (LTS)
- Spring Boot 3.5.x
- Spring Security 6
- Spring Data JPA
- Thymeleaf
- MySQL (XAMPP)

## Requisitos

- JDK 25 o superior
- Maven Wrapper incluido

## Ejecutar en desarrollo

```bash
./mvnw spring-boot:run
```

La aplicación queda disponible en:

- http://localhost:8080

La aplicación usa MySQL en todos los perfiles. Inicia MySQL desde el panel de XAMPP y crea primero la base:

```sql
CREATE DATABASE universidad CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Por defecto se conecta a `jdbc:mysql://localhost:3306/universidad` como `root` sin contraseña, que es la configuración usual de XAMPP. Si tu MySQL usa otros datos, configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` en el `.env` local. Hibernate crea o actualiza la tabla `users`; no se usa Flyway ni se crean tablas de seguimiento.

## Perfil de producción

Para ejecutar con el perfil `prod`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

No guardes credenciales en el archivo de propiedades. Define `DB_USERNAME` y `DB_PASSWORD`
como variables de entorno (o en un archivo `.env` local no versionado) antes de iniciar.
El perfil `prod` también acepta `DB_URL`; por defecto usa la URL local de XAMPP y `root` sin contraseña.
También puedes definir `APP_SEED_TEST_PASSWORD` para crear las cuentas de prueba y
`APP_ADMIN_REGISTRATION_CODE` para permitir el registro de administradores desde el formulario.
Ejemplo de nombres (reemplaza los valores por secretos propios):

```dotenv
DB_USERNAME=usuario_mysql
DB_PASSWORD=contraseña_mysql
APP_SEED_TEST_PASSWORD=contraseña_de_prueba_segura
APP_ADMIN_REGISTRATION_CODE=codigo_de_registro_admin
```

La base `universidad` debe existir; el usuario MySQL debe tener permisos para crear y alterar
tablas. Al iniciar, Hibernate crea `users` si no existe y actualiza su esquema si es necesario.
La aplicación no necesita tablas adicionales para controlar migraciones o el sembrado de Faker.

Para crear las cuentas de prueba, configura `APP_SEED_TEST_PASSWORD` en el entorno antes de iniciar. Debe tener entre 8 y 72 caracteres; se guarda con BCrypt y nunca se imprime. El perfil `prod` mostrará solo el correo y rol de las cuentas: `prueba.usuario@universidad.test` (`USER`) y `prueba.admin@universidad.test` (`ADMIN`). La cuenta `ADMIN` puede listar, editar y eliminar usuarios; no puede eliminarse a sí misma ni al último administrador.

Al iniciar con el perfil `prod`, se agregan 20 usuarios ficticios con Faker solo cuando `users` está vacía. Esta comprobación usa `UserRepository.count()`; si ya hay al menos un usuario, el sembrador omite el lote para evitar duplicarlo, sin guardar una marca en otra tabla. Para generar el lote, inicia la aplicación cuando la tabla esté vacía. Las contraseñas de estos usuarios aleatorios se generan al azar y no se conservan en texto claro; usa las cuentas de prueba para iniciar sesión.

## Funcionalidades principales

- Registro de usuarios
- Validación de email y contraseña
- Encriptación con BCrypt
- Login con Spring Security
- Listado de usuarios autenticados
- Vistas Thymeleaf con estilo propio

## Usuarios de prueba

Puedes crear usuarios desde la interfaz de registro. La contraseña se almacena cifrada con BCrypt.

## Verificación

Las pruebas también usan MySQL, pero en una base separada. Créala antes de ejecutar la suite:

```sql
CREATE DATABASE universidad_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

```bash
./mvnw test
```

---

Proyecto desarrollado como taller académico de Spring Boot, Security y JPA.
