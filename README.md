# Doraking (Forge 1.20.1)

Mod patrocinador: dorakines, puesto y dueno comerciante.

- Sin recetas de crafteo. Sin generacion en el mundo.
- Todo se obtiene del inventario creativo (pestana "Doraking") o comerciando con el Dueno.
- Sabores: Anko (Regeneracion), Manjar (Velocidad), Pastelera (Absorcion), Nutella (Salto).
- Puesto Doraking: bloque decorativo con el letrero del logo.
- Dueno de Doraking: aparece SOLO con su huevo de aparicion; invulnerable; vende por esmeraldas
  (precios en entity/DuenoEntity.java, metodo updateTrades).

## Compilar
Requiere JDK 17. Copia esta carpeta sobre el MDK oficial de Forge 1.20.1-47.3.0
(para tener gradlew), o instala Gradle 8.1+ y ejecuta:

    gradlew build

El .jar queda en build/libs/doraking-1.0.0.jar -> ponlo en la carpeta mods.

## Versiones
- 1.0.0: dorakines (anko, manjar), puesto y dueno basicos.
- 1.1.0: puesto de 3 bloques con toldo y decoracion; dueno con modelo 3D propio.
- 1.2.0: sabores pastelera y nutella.

Regla: cada modificacion sube la version en `build.gradle`; el .jar y el Release se llaman doraking-<version>.
