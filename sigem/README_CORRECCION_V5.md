# Correccion V5 - SIGEM DEMO

Se corrigio el `StackOverflowError` que ocurria durante el arranque del `DemoDataSeeder` por una recursividad en `hashCode()` entre `Usuario` y `Cliente`.

## Cambio

`Usuario` y `Cliente` ya no usan `@Data`, que generaba `equals/hashCode/toString` incluyendo relaciones JPA. Ahora usan `@Getter` y `@Setter`, y `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` con el `id` como unico campo incluido.

Esto evita la cadena:

`Usuario.hashCode() -> Cliente.hashCode() -> Usuario.hashCode() -> ...`

El resto del proyecto V4 se conserva, incluyendo los datos DEMO de ventas, alquileres y las cuentas documentadas.

## Verificacion

El entorno de trabajo disponible no tiene Maven instalado ni acceso para descargarlo, por lo que no fue posible ejecutar `mvn package` aqui. El ZIP contiene el proyecto fuente completo con la correccion aplicada.
