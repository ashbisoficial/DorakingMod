# Doraking (Forge 1.20.1)

Mod patrocinador: dorakines, puesto y dueno comerciante.

- Sin recetas de crafteo. Sin generacion en el mundo.
- Todo se obtiene del inventario creativo (pestana "Doraking") o comerciando con el Dueno.
- Dorakin de Anko: Regeneracion. Dorakin de Manjar: Velocidad.
- Puesto Doraking: bloque decorativo con el letrero del logo.
- Dueno de Doraking: aparece SOLO con su huevo de aparicion; invulnerable; vende por esmeraldas
  (precios en entity/DuenoEntity.java, metodo updateTrades).

## Compilar
Requiere JDK 17. Copia esta carpeta sobre el MDK oficial de Forge 1.20.1-47.3.0
(para tener gradlew), o instala Gradle 8.1+ y ejecuta:

    gradlew build

El .jar queda en build/libs/doraking-1.0.0.jar -> ponlo en la carpeta mods.
