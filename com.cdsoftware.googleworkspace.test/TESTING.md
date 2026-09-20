# Guía de pruebas de com.cdsoftware.googleworkspace

Este proyecto es un fragmento OSGi de pruebas. Su `Fragment-Host` es
`com.cdsoftware.googleworkspace`, por lo que durante la ejecución comparte el
classpath del plugin principal y puede probar sus clases directamente.

## Herramientas incluidas

- JUnit 5: descubre y ejecuta los métodos anotados con `@Test`.
- AssertJ: expresa verificaciones legibles mediante `assertThat(...)`.
- Mockito: crea sustitutos de modelos iDempiere como `MRequest`, evitando que
  una prueba unitaria necesite consultar la base de datos.

## Estructura de una prueba

Las pruebas siguen el patrón Arrange–Act–Assert:

```java
@Test
void recognizesCommandAndNormalizesItsName() {
    // Arrange y Act: preparar la entrada y ejecutar la unidad.
    GoogleChatCommand command =
            new GoogleChatCommand("/SoLiCiTuDeS abiertas");

    // Assert: comprobar el comportamiento observable.
    assertThat(command.isCommand()).isTrue();
    assertThat(command.getCommand()).isEqualTo("solicitudes");
    assertThat(command.getArguments()).isEqualTo("abiertas");
}
```

Cada prueba debe cubrir una sola regla observable. Los nombres describen el
resultado esperado y no detalles internos de implementación.

## Cobertura inicial

| Clase de prueba | Unidad cubierta | Qué protege |
| --- | --- | --- |
| `GoogleChatCommandTest` | `GoogleChatCommand` | Reconocimiento, normalización y argumentos de comandos. |
| `GoogleChatCommandResultTest` | `GoogleChatCommandResult` | Exclusividad entre respuestas de texto y tarjetas. |
| `GoogleChatEventTest` | `GoogleChatEvent` | Contrato JSON de mensajes, botones y alta en espacios. |
| `RequestCardBuilderTest` | `RequestCardBuilder` | Detalle, valores vacíos y acción de actualizaciones. |
| `RequestsCardBuilderTest` | `RequestsCardBuilder` | Listado, estado vacío y acción para ver detalle. |
| `RequestUpdatesCardBuilderTest` | `RequestUpdatesCardBuilder` | Estado sin actualizaciones, sin acceso a base de datos. |

## Por qué se usa Mockito con MRequest

Una tarjeta solo necesita respuestas concretas de `MRequest`; no necesita
guardar ni consultar registros. El mock define únicamente esas respuestas:

```java
MRequest request = mock(MRequest.class);
when(request.getDocumentNo()).thenReturn("REQ-42");
when(request.getSummary()).thenReturn("Cannot approve invoice");
```

Así, si falla la prueba, la causa está en la construcción de la tarjeta y no
en la configuración de un cliente, una organización o una base de datos.

## Ejecución desde Eclipse

1. Importa el plugin principal y `com.cdsoftware.googleworkspace.test` en el
   mismo workspace que iDempiere 13.
2. Configura y activa la plataforma objetivo de iDempiere.
3. Abre una clase terminada en `Test.java`.
4. Usa **Run As > JUnit Test** para una clase o un método concreto.
5. Revisa la vista **JUnit**: verde indica éxito; rojo muestra la aserción y
   la línea que no cumplió el contrato.

Para ejecutar todas las pruebas, usa **Run As > JUnit Test** sobre la carpeta
`src` o sobre el proyecto de pruebas.

## Ejecución con Maven

Desde `com.cdsoftware.googleworkspace.test`:

```bash
mvn test
```

Tycho necesita que el artefacto local
`org.idempiere.p2.targetplatform:13.0.0-SNAPSHOT` esté previamente construido
e instalado. Si no existe, Maven falla antes de compilar las pruebas. Esa
falla pertenece a la resolución de la plataforma objetivo, no a JUnit ni a
las clases de prueba.

## Siguiente capa: pruebas de integración

No deben tratarse como pruebas unitarias las clases que dependen de `Query`,
`DB`, `MRole`, `Env`, `MSession` o de registros reales de iDempiere. Una fase
posterior debe ejecutarlas con runtime OSGi y una base de datos de pruebas para
validar:

- que `RequestService` filtra solicitudes abiertas y cerradas;
- que aplica cliente, registros activos y filtros de acceso;
- que `GoogleChatAuthorizationService` acepta solamente roles asignados con
  acceso de lectura a una ventana de `R_Request` y a la tabla;
- que `GoogleOIDCProvider` rechaza claims incorrectos y construye el
  `AuthenticatedUser` esperado;
- que `GoogleChatResource` transforma eventos completos en respuestas HTTP.

Estas pruebas deben usar datos dedicados, transacciones reversibles y no
depender de IDs de producción.
