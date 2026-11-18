# Reglas de ProGuard/R8 para MPAZ PRO.
# Hoy la minificación está desactivada (isMinifyEnabled = false), así que estas reglas
# solo se aplican si se activa en el build de release.

# Room genera código para las entidades y el DAO: se conservan sus clases.
-keep class cl.mpaz.mpazpro.grupo_2.data.local.** { *; }
