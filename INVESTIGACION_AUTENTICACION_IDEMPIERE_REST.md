# Investigación del mecanismo de autenticación de iDempiere REST

## Alcance

Este informe analiza el mecanismo real de autenticación de `idempiere-rest` utilizado por el plugin `com.cdsoftware.googleworkspace`.

La investigación se realizó exclusivamente sobre el código fuente local de iDempiere 13, `idempiere-rest` y `com.cdsoftware.googleworkspace`. No se modificó código fuente ni configuración.

## 1. Causa del HTTP 401

**Clase:** `com.trekglobal.idempiere.rest.api.v1.auth.filter.RequestFilter`  
**Método:** `filter(ContainerRequestContext)`  
**Archivo:** `plugins/idempiere-rest/com.trekglobal.idempiere.rest.api/src/com/trekglobal/idempiere/rest/api/v1/auth/filter/RequestFilter.java`

La condición que genera directamente el HTTP 401 cuando no se envía un token es:

```java
String authHeaderVal = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

if (authHeaderVal != null && authHeaderVal.startsWith("Bearer")) {
    // Validación del token...
} else {
    requestContext.abortWith(
        Response.status(Response.Status.UNAUTHORIZED).build()
    );
}
```

`POST v1/google/chat` no coincide con ninguna de las excepciones públicas definidas en el filtro. Por tanto, una petición sin un encabezado `Authorization` que comience por `Bearer` llega al `else` y es abortada con HTTP 401.

El rechazo ocurre antes de ejecutar `GoogleChatResource.chat()`. No lo produce Jetty, el servlet ni la configuración de la ventana **Rest Resource**.

## 2. Flujo de la petición

```text
POST /api/v1/google/chat
        ↓
Jetty / contenedor web OSGi
Web-ContextPath: api
        ↓
org.glassfish.jersey.servlet.ServletContainer
Servlet mapping: /*
        ↓
ApplicationV1.getClasses()
        ↓
Descubrimiento OSGi de ResourceExtension
        ↓
GoogleWorkspaceResourceExtension.getResourceClasses()
        ↓
Jersey identifica GoogleChatResource.chat()
        ↓
RequestFilter.filter()
@Provider
@Priority(Priorities.AUTHORIZATION)
        ↓
¿Es una ruta pública hardcodeada? → No
        ↓
¿Es health o una URL prefirmada? → No
        ↓
¿Authorization comienza con Bearer? → No
        ↓
requestContext.abortWith(401)
        ↓
GoogleChatResource.chat() no se ejecuta
```

El contexto web `/api` se declara mediante:

```text
Web-ContextPath: api
```

en:

```text
plugins/idempiere-rest/com.trekglobal.idempiere.rest.api/META-INF/MANIFEST.MF
```

El archivo `WEB-INF/web.xml` registra:

- `org.glassfish.jersey.servlet.ServletContainer` como servlet.
- `com.trekglobal.idempiere.rest.api.v1.ApplicationV1` como aplicación JAX-RS.
- `/*` como mapping del servlet.

## 3. Por qué `/auth/tokens` funciona sin JWT

`POST /api/v1/auth/tokens` no es público mediante `@PermitAll` ni por una configuración de Rest Resource.

Es una excepción explícita dentro de `RequestFilter.filter()`:

```java
HttpMethod.POST.equals(requestContext.getMethod())
&& requestContext.getUriInfo().getPath()
                 .endsWith("v1/auth/tokens")
```

Cuando la condición coincide, el filtro ejecuta `return` antes de consultar el encabezado `Authorization`.

El recurso correspondiente se declara en:

```text
plugins/idempiere-rest/com.trekglobal.idempiere.rest.api/src/com/trekglobal/idempiere/rest/api/v1/auth/AuthService.java
```

mediante `@POST` y `@Path("auth/tokens")`.

Las excepciones sin JWT encontradas en `RequestFilter` son:

- Todas las peticiones `OPTIONS`.
- `POST v1/auth/tokens`.
- `GET v1/auth/jwk`.
- `POST v1/auth/refresh`.
- `POST v1/auth/logout`.
- Los tres endpoints de recuperación de contraseña.
- `POST v1/webhooks/{un-solo-segmento}`.
- `GET v1/health`, sujeto opcionalmente a `REST_HEALTH_MONITORING_KEY`.
- URLs prefirmadas reconocidas por `PresignedURL`.

Estas excepciones son comparaciones directas de método y ruta dentro del filtro. No son anotaciones declarativas.

## 4. Configuración de `ApplicationV1`

`ApplicationV1` extiende `javax.ws.rs.core.Application` y registra explícitamente en `getClasses()`:

- Recursos REST.
- `JacksonFeature`.
- `RequestFilter`.
- `RequestSetLanguageFilter`.
- `ResponseFilter`.

El mecanismo global de autenticación queda registrado mediante:

```java
classes.add(RequestFilter.class);
```

No se observa package scanning para registrar este mecanismo.

### Descubrimiento de `ResourceExtension`

Las extensiones se descubren como servicios OSGi:

```java
IServicesHolder<ResourceExtension> list =
    Service.locator().list(ResourceExtension.class);
```

Después, sus recursos se incorporan a Jersey:

```java
classes.addAll(service.getResourceClasses());
```

El plugin publica `GoogleWorkspaceResourceExtension` mediante OSGi Declarative Services en:

```text
plugins/com.cdsoftware.googleworkspace/com.cdsoftware.googleworkspace/OSGI-INF/GoogleWorkspaceResourceExtension.xml
```

La interfaz `ResourceExtension` solo expone:

```java
Set<Class<?>> getResourceClasses();
```

No contiene métodos para:

- Declarar un recurso público.
- Excluir una ruta del filtro.
- Registrar políticas de autenticación.
- Añadir una excepción a `RequestFilter`.

## 5. Procesamiento de `Authorization: Bearer`

El flujo dentro de `RequestFilter` es:

1. Obtiene el encabezado `Authorization`.
2. Exige que su valor comience por `Bearer`.
3. Divide el valor por espacios y exige que exista un token.
4. Invoca `validate(token, requestContext)`.
5. Exige que el contexto contenga `AD_User_ID` y `AD_Role_ID` para rutas que no sean `v1/auth/*`.
6. Aplica posteriormente el control de acceso de Rest Resource.

### JWT propio de iDempiere

Si el token no corresponde a un servicio OIDC registrado, se valida como JWT propio de iDempiere mediante:

- Algoritmo `HMAC512`.
- Secreto obtenido mediante `TokenUtils.getTokenSecret()`.
- Issuer obtenido mediante `TokenUtils.getTokenIssuer()`.

También se comprueban, entre otros datos:

- Cliente activo.
- Usuario activo.
- Rol activo.
- Organización y almacén.
- Sesión existente y no cerrada.
- Resultado de `Login.validateLogin()`.

### Token OIDC externo

Antes de validar el JWT propio, el filtro ejecuta:

```java
MOIDCService service = MOIDCService.findMatchingOIDCService(token);
```

`MOIDCService` extrae `iss`, `aud` o `client_id` y busca una configuración activa en `REST_OIDCService`.

Si encuentra una configuración, valida:

- Firma mediante las claves JWKS del proveedor.
- Issuer.
- Audience o `client_id`.
- Expiración.
- Scope opcional.

Después usa un servicio OSGi `IOIDCProvider` para convertir la identidad externa en un `AuthenticatedUser` de iDempiere con:

- Tenant o cliente.
- Usuario.
- Rol.
- Organización.
- Sesión, cuando corresponda.

Por tanto, el soporte OIDC no convierte al endpoint en público. Es otra forma de autenticarse ante la API como una identidad vinculada a iDempiere.

## 6. Rest Resource y `REST_RESOURCE_ACCESS_CONTROL`

La ventana **Rest Resource** y `REST_RESOURCE_ACCESS_CONTROL` controlan autorización, no autenticación.

El orden real es:

```text
Bearer válido
    ↓
Usuario y rol cargados en Env
    ↓
REST_RESOURCE_ACCESS_CONTROL
    ↓
Permiso del rol sobre ruta y método HTTP
```

### Autenticación

Responde a la pregunta:

> ¿Quién eres?

La realiza `RequestFilter` validando el Bearer y estableciendo el usuario, cliente, rol y organización en el contexto de iDempiere.

### Autorización

Responde a la pregunta:

> ¿Puedes acceder a este recurso y utilizar este método HTTP?

La realizan `MRestResourceAccess` y `MRestResource` después de validar la identidad.

`REST_RESOURCE_ACCESS_CONTROL` se consulta mediante:

```java
MSysConfig.getBooleanValue("REST_RESOURCE_ACCESS_CONTROL", true);
```

Los registros Rest Resource contienen patrones regex de rutas. `MRestResource.getMatchResources(path)` busca coincidencias y `MRestResourceAccess.hasAccess()` comprueba el acceso del rol al recurso y método HTTP.

Si no existe ningún Rest Resource coincidente, el método devuelve acceso permitido:

```java
if (resources == null || resources.length == 0)
    return true;
```

Esto explica por qué una ventana Rest Resource vacía no impide que los endpoints personalizados funcionen cuando se proporciona un JWT válido.

En consecuencia:

- Desactivar `REST_RESOURCE_ACCESS_CONTROL` no hace público el endpoint.
- Crear un registro Rest Resource tampoco elimina la exigencia del Bearer.
- Una petición sin Bearer es rechazada antes de evaluar Rest Resource.

## 7. Mecanismos públicos o de exclusión encontrados

No se encontró en el código de `idempiere-rest`:

- Uso de `@PermitAll`.
- Uso de `@RolesAllowed`.
- Name binding para aplicar selectivamente `RequestFilter`.
- Un `DynamicFeature` de seguridad.
- Una anotación propia para endpoints públicos.
- Un SysConfig para registrar rutas públicas.
- Una extensión OSGi para ampliar la lista de excepciones de autenticación.
- Una capacidad de `ResourceExtension` para omitir autenticación.
- Tests o plugins que implementen un `ResourceExtension` público.

El mecanismo actual para omitir el JWT dentro de `ApplicationV1` es la lista de condiciones escrita directamente en `RequestFilter.filter()`.

### Filtro adicional dentro del plugin

Registrar otro `ContainerRequestFilter` para validar Google no resuelve por sí solo el problema.

Aunque el filtro propio valide correctamente el token de Google, el `RequestFilter` global seguirá ejecutándose y exigirá un Bearer aceptable para su propia lógica. No existe una propiedad en el contexto que permita marcar la petición como ya autenticada y omitir el filtro existente.

## 8. Webhook público nativo

`idempiere-rest` incluye un precedente específico para callbacks externos:

```text
POST /api/v1/webhooks/{endpointKey}
```

`RequestFilter` lo excluye mediante `isInboundWebhookPath()`. La función solamente acepta un segmento después de `v1/webhooks/`.

El recurso se define en:

```text
plugins/idempiere-rest/com.trekglobal.idempiere.rest.api/src/com/trekglobal/idempiere/rest/api/v1/resource/WebhookInboundResource.java
```

Su JavaDoc indica que utiliza firmas HMAC de Standard Webhooks en lugar de JWT.

Después de omitir el filtro JWT, `WebhookInboundHandler` aplica su propia seguridad:

- Endpoint configurado y activo.
- SysConfig `REST_WEBHOOK_INBOUND_ENABLED`.
- Allowlist de direcciones IP.
- Firma HMAC-SHA256 opcional.
- Deduplicación mediante `webhook-id`.
- Ejecución de un proceso de iDempiere configurado.

Sin embargo, la verificación está acoplada a Standard Webhooks HMAC. No se encontró una extensión para sustituirla por OIDC de Google ni para delegar la petición a `GoogleChatResource`.

## 9. Posibilidad de hacer público `GoogleChatResource`

Con el código actual, `POST /api/v1/google/chat` no puede hacerse público mediante:

- `ResourceExtension`.
- Una anotación como `@PermitAll`.
- Un registro Rest Resource.
- `REST_RESOURCE_ACCESS_CONTROL`.
- Un filtro adicional aislado en el plugin.

La petición solo puede pasar si ocurre alguna de estas condiciones:

1. Presenta un Bearer aceptado por `RequestFilter`.
2. Se modifica el mecanismo que determina las rutas exentas.
3. El endpoint se aloja fuera de la instancia de `ApplicationV1`.

### Uso directo de OIDC de Google

Existe una alternativa sin JWT propio de iDempiere: implementar o configurar un `IOIDCProvider` capaz de aceptar el Bearer OIDC enviado por Google.

Esta opción requiere que:

- Google envíe el token en `Authorization: Bearer`.
- El token tenga claims compatibles con la detección de `MOIDCService`.
- Exista una configuración `REST_OIDCService` que coincida con issuer y audience.
- La identidad se traduzca a un usuario, cliente, rol y organización de iDempiere.

No sería un endpoint anónimo. Google quedaría autenticado mediante la infraestructura OIDC de `idempiere-rest`.

Tampoco restringe automáticamente ese token a `v1/google/chat`. La restricción tendría que implementarse mediante scopes OIDC y/o Rest Resource.

`MOIDCService` soporta opcionalmente validar que uno de los scopes sea exactamente igual al path solicitado.

## 10. Evaluación de alternativas arquitectónicas

### A. Excluir `v1/google/chat` del filtro existente

Es técnicamente posible y replica el mecanismo utilizado para `/auth/tokens` y `/webhooks/{endpointKey}`.

La desventaja es que requiere modificar `idempiere-rest` y añadir otra ruta hardcodeada a `RequestFilter`.

### B. Crear un `ContainerRequestFilter` propio para Google

No es suficiente de manera aislada. El filtro global existente continuaría exigiendo su propio Bearer.

Esta alternativa solo sería viable si también se modifica `RequestFilter` para reconocer una autenticación previa o para ignorar recursos identificados mediante una anotación o servicio extensible.

### C. Registrar otra aplicación JAX-RS

Permitiría separar completamente la autenticación de Google del filtro JWT global.

Requeriría un servlet mapping o contexto diferente. Mantener exactamente `/api/v1/google/chat` puede generar conflicto con el mapping `/*` que actualmente pertenece al `ServletContainer` de `ApplicationV1`.

### D. Crear un servlet o endpoint separado

También separaría correctamente la validación OIDC de Google de la seguridad global de `idempiere-rest`.

Es una arquitectura viable si puede utilizarse una ruta o contexto distinto y el plugin establece explícitamente el contexto de iDempiere necesario para procesar el evento.

### E. Utilizar un mecanismo extensible existente

El mecanismo existente más cercano es `IOIDCProvider`:

- Está diseñado para validar tokens OIDC externos.
- Se registra como servicio OSGi.
- Convierte una identidad externa en un usuario y rol de iDempiere.
- Puede utilizar scopes para restringir paths.

Su idoneidad depende de las características exactas del JWT enviado por Google Chat y de si se desea tratar a Google como una identidad autenticada de iDempiere.

El webhook nativo también constituye un patrón oficial para callbacks externos, pero su autenticación actual usa HMAC y no ofrece una extensión OIDC.

## Conclusión

La causa inmediata del HTTP 401 es `RequestFilter.filter()`, que exige un encabezado `Authorization: Bearer` para toda ruta que no esté incluida explícitamente en su lista interna de excepciones.

`ResourceExtension` solamente incorpora nuevas clases de recursos a Jersey. No amplía ni configura el mecanismo de autenticación.

La ventana Rest Resource opera después de autenticar al usuario y controla autorización por rol, ruta y método. Por eso una ventana vacía no permite solicitudes anónimas ni evita el HTTP 401.

La seguridad pública de `idempiere-rest` está implementada mediante una lista de rutas hardcodeadas en `RequestFilter`. Actualmente no existe un contrato limpio para que un plugin declare que uno de sus `ResourceExtension` utiliza otro mecanismo de autenticación.

Para Google Chat, las opciones con mejor fundamento en el código inspeccionado son:

1. Integrar el JWT de Google mediante la extensión OIDC existente, si sus claims y el modelo de usuario/rol son compatibles.
2. Extender el contrato de seguridad de `idempiere-rest` para permitir autenticadores o rutas públicas registradas por OSGi.
3. Alojar el callback de Google en un servlet o aplicación separada que valide OIDC de forma independiente.

No se recomienda implementar una alternativa definitiva hasta confirmar el formato exacto del token que Google Chat envía al endpoint y si puede modelarse correctamente mediante `MOIDCService` e `IOIDCProvider`.
