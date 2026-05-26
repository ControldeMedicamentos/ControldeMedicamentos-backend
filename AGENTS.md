# Control de Medicamentos - Proyecto SISMED/SIGA

## Contexto

Sistema de gestión de medicamentos y vacunas para establecimiento de salud en La Libertad, Perú. Debe integrar:
1. Historia clínica (Office Forms) → registra atenciones y medicamentos consumidos
2. Inventario SISMED (Excel oficial) → descuento automático de stock al consumir medicamentos
3. Reporte mensual → genera movimiento SISMED que coincida con conteo físico

**Estándar**: Formato oficial Ministerio de Salud Perú (SISMED/SIGA)

## Archivos Críticos

- `HISTORIA CLÍNICA1.pdf` - Ejemplo de historia clínica capturada (estructura del formulario)
- `Formato_Movimiento_de_Vacunas.xlsx` - Excel oficial SISMED (pendiente recepción)
- `memory/proyecto_medicamentos.md` - Documentación del contexto

## Reglas del Proyecto

### Base de Datos
- Modelar con integridad referencial: pacientes → atenciones → medicamentos consumidos
- Campo `codigo_sismed` en medicamentos debe mapear exactamente a MPRODUCTO del Excel
- Auditoría: rastrear quién y cuándo consumió cada medicamento

### Consumo de Medicamentos
- Cada medicamento debe tener campo `cantidad_consumida` y `codigo_sismed`
- El descuento debe ser automático (transaccional, no manual)
- Validar stock disponible ANTES de permitir consumo
- Registrar motivo de consumo (SIS, intersanidad, defunción, exonerado, etc.)

### Reporte SISMED (Cierre de Mes)
- Debe generar exactamente los mismos campos que Excel: saldo inicial, ingresos, consumos por tipo, devoluciones, vencidos, merma, distribución, transferencia, stock final
- Validación: `Stock Final = Saldo Inicial + Ingresos - Consumos - Devoluciones - Vencidos - Merma`
- Exportar en formato compatible para importar al sistema central DIRESA

## Medicamentos Por Preparar

Según usuario: "sobre los medicamentos vamos realizandoll de acuerdo al excel ya mas adelante me estarán pasando ello"
→ Esperar recepción del Excel antes de codificar maestros de medicamentos. Ir preparando estructura de BD.

## Prioridad Técnica

1. Esperar Excel SISMED (estructura de tablas, códigos de medicamentos)
2. Diseñar BD (ER) que refleje estructura SISMED + historia clínica
3. Implementar APIs de consumo de medicamentos con validación de stock
4. Implementar generador de reporte mensual

## Tone

Directo, técnico. Sin charla. Las reglas del usuario global (AGENTS.md global) se aplican.
