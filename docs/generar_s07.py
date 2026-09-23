"""Produce el entregable S07 con capturas Android reales y límites declarados."""

from pathlib import Path
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak,
    Image, Preformatted, KeepTogether,
)

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
OUT = DOCS / "S07_ActividadAutonoma_Guillen_Mendoza.pdf"
CAP = DOCS / "capturas_s07"
INK = colors.HexColor("#143343")
TEAL = colors.HexColor("#0B6969")
PALE = colors.HexColor("#EEF5F5")

ss = getSampleStyleSheet()
ss.add(ParagraphStyle(name="TitleAS07", parent=ss["Title"], fontName="Helvetica-Bold", fontSize=18, leading=22, textColor=INK, spaceAfter=14))
ss.add(ParagraphStyle(name="HeadAS07", parent=ss["Heading2"], fontName="Helvetica-Bold", fontSize=11, leading=14, textColor=TEAL, spaceBefore=12, spaceAfter=6))
ss.add(ParagraphStyle(name="BodyAS07", parent=ss["BodyText"], fontName="Helvetica", fontSize=9.2, leading=13.2, spaceAfter=7))
ss.add(ParagraphStyle(name="SmallAS07", parent=ss["BodyText"], fontName="Helvetica", fontSize=7.6, leading=10.5, spaceAfter=4))
ss.add(ParagraphStyle(name="CodeAS07", fontName="Courier", fontSize=7.1, leading=9.4, backColor=PALE, borderPadding=7, spaceAfter=8))

def P(s, style="BodyAS07"):
    return Paragraph(s, ss[style])

def grid(rows, widths):
    t = Table([[P(escape(str(c)), "SmallAS07") for c in row] for row in rows], colWidths=widths, repeatRows=1)
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), PALE),
        ("GRID", (0, 0), (-1, -1), 0.35, colors.HexColor("#AAC5C7")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 5),
        ("RIGHTPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
    ]))
    return t

story = [
    P("PharmaMobile | Actividad autónoma S07", "TitleAS07"),
    P("Estudiante: Guillen Mendoza Alexander<br/>Docente: David Reyna<br/>Sesión: 7 - Endpoints, DTO y pruebas de conexión<br/>Fecha de ejecución: 22-23 de septiembre de 2026 (reloj del emulador en UTC)"),
    P("Repositorio: https://github.com/alexgm-A11/PharmaMobile_Practica<br/>Rama: feature/ktor-client<br/>Commit del código: 4fcfdcc9407e69dee9b65d0ea5e29d1cc0f64fd5"),
    P("1. Catálogo de endpoints", "HeadAS07"),
    P("Recurso: productos. API pública de práctica Platzi, versión v1. URL base: https://api.escuelajs.co/api/v1. Los cinco métodos se ejecutaron el 22/09/2026; para POST/PUT/DELETE se creó y eliminó un producto temporal. La interfaz de esta sesión consume GET; el CRUD visual corresponde a la sesión 8."),
    grid([
        ["Método y ruta", "Parámetros", "Respuesta comprobada", "Errores a controlar"],
        ["GET /products", "limit, offset (query)", "200, arreglo JSON", "400, 500"],
        ["GET /products/{id}", "id positivo (ruta)", "200, producto JSON", "400 observado para ID inexistente; 404 posible"],
        ["POST /products", "JSON: title, price, description, categoryId, images", "201, producto con id", "400, 500"],
        ["PUT /products/{id}", "id + JSON completo", "200, producto actualizado", "400, 404, 500 (observado con cuerpo parcial)"],
        ["DELETE /products/{id}", "id (ruta)", "200, true", "400, 404, 500"],
    ], [118, 145, 110, 132]),
    Spacer(1, 8),
    P("La API pública cambia con el tiempo. Se comprobó GET de lista e ID 28 (200), POST de un producto temporal (201), PUT completo (200) y DELETE del mismo ID (200, true). Una petición PUT parcial devolvió 500; no se presenta como éxito. Un ID inexistente respondió 400, no el 404 propuesto como ejemplo en la guía."),
    P("Fuentes técnicas: documentación oficial de la API (https://api.escuelajs.co/docs) y documentación de Ktor (https://ktor.io/docs/client-testing.html)."),
    PageBreak(),
    P("2. Diccionario de DTO", "TitleAS07"),
    P("Los tipos y valores por defecto coinciden con ProductoDto.kt. Un campo obligatorio no tiene valor por defecto. El producto remoto se convierte en ProductoCatalogo y se mantiene separado del Producto local porque la API no incluye stock."),
    P("ProductoDto", "HeadAS07"),
    grid([
        ["JSON", "Tipo Kotlin", "Obligatorio", "Por defecto", "Dominio"],
        ["id", "Int", "Sí", "-", "ProductoCatalogo.id"],
        ["title", "String", "Sí", "-", "ProductoCatalogo.nombre"],
        ["price", "Double", "Sí", "-", "ProductoCatalogo.precio"],
        ["description", "String", "No", '""', "ProductoCatalogo.descripcion"],
        ["images", "List<String>", "No", "emptyList()", "ProductoCatalogo.imagen (primera)"],
        ["category", "CategoriaDto?", "No", "null", "ProductoCatalogo.categoria (name)"],
    ], [90, 91, 78, 87, 159]),
    P("CategoriaDto", "HeadAS07"),
    grid([
        ["JSON", "Tipo Kotlin", "Obligatorio", "Por defecto", "Dominio"],
        ["id", "Int", "Sí", "-", "Sin equivalente; ID de API"],
        ["name", "String", "Sí", "-", "ProductoCatalogo.categoria"],
    ], [90, 91, 78, 87, 159]),
    P("Fragmento de JSON real de GET /products?limit=1&amp;offset=0", "HeadAS07"),
    Preformatted('''[
  {"id":28,"title":"Sleek Modern Leather Sofa","price":60,
   "description":"Enhance the elegance of your living space...",
   "images":["https://i.imgur.com/Qphac99.jpeg", "..."],
   "category":{"id":3,"name":"Love is light", "slug":"love-is-light"},
   "slug":"sleek-modern-leather-sofa"}
]''', ss["CodeAS07"]),
    P("El fragmento abrevia la descripción y el arreglo de imágenes; `slug` y las fechas sí llegaron en la respuesta real. La opción `ignoreUnknownKeys = true` permite ignorarlos sin alterar el DTO."),
    P("Código Kotlin de los DTO", "HeadAS07"),
    Preformatted('''@Serializable
data class CategoriaDto(val id: Int, val name: String)

@Serializable
data class ProductoDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String = "",
    val images: List<String> = emptyList(),
    val category: CategoriaDto? = null
)''', ss["CodeAS07"]),
]

records = [
    ("01 - Respuesta exitosa", "01-exito-android.png", "23/09/2026 03:51 UTC", "Abrir Catálogo REST con Internet y ejecutar GET /products?limit=10&amp;offset=0.", "HTTP 200 y lista renderizada.", "HTTP 200 en Logcat; se dibujaron nombres, precios, IDs y categorías.", "La carga se completa y el usuario ve los productos."),
    ("02 - Recurso inexistente", "02-inexistente-android.png", "23/09/2026 03:54 UTC", "Buscar ID 9999990 desde el campo de la pantalla.", "Error HTTP controlado; la guía usa 404 como ejemplo.", "La API respondió HTTP 400. La pantalla mostró 'Producto no encontrado o solicitud inválida (400)'.", "El usuario recibe un mensaje y la aplicación permanece abierta."),
    ("03 - Sin conexión", "03-sin-conexion-android.png", "23/09/2026 03:54 UTC", "Desactivar Wi-Fi y datos del emulador; tocar ACTUALIZAR; restaurar ambos después.", "Excepción de red capturada, sin cierre de app.", "La pantalla mostró 'Sin conexión o error de red. Verifique Internet e intente de nuevo.'.", "La lista se sustituye por un error legible con opción de reintentar."),
    ("04 - Tiempo de espera", "04-timeout-android.png", "23/09/2026 03:57 UTC", "Con Internet, tocar PROBAR TIMEOUT 1 MS.", "HttpRequestTimeoutException controlada y UI reactiva.", "La pantalla mostró 'HttpRequestTimeoutException controlada: la interfaz sigue respondiendo.'.", "El usuario no queda bloqueado; la prueba no modifica datos."),
    ("05 - Campo JSON desconocido", "05-json-desconocido-android.png", "23/09/2026 03:57 UTC", "Con Internet, tocar PROBAR CAMPO JSON DESCONOCIDO.", "Modo flexible continúa; modo estricto lanza SerializationException.", "Con el JSON real, el modo flexible leyó 1 producto y el estricto produjo SerializationException.", "La app continúa; el resultado de diagnóstico explica la diferencia."),
]
for title, filename, fecha, pasos, esperado, observado, conclusion in records:
    story += [PageBreak(), P(f"3. Bitácora | {title}", "TitleAS07"),
        P(f"<b>Fecha y plataforma:</b> {fecha}; emulador Android Pixel 6."),
        P(f"<b>Pasos:</b> {pasos}"), P(f"<b>Esperado:</b> {esperado}"),
        P(f"<b>Observado:</b> {observado}"), P(f"<b>Conclusión:</b> {conclusion}"),
        Image(str(CAP / filename), width=241, height=535),
        P(f"Captura real: docs/capturas_s07/{filename}", "SmallAS07")]

story += [
    PageBreak(),
    P("4. Evidencias, código y límites", "TitleAS07"),
    P("Registro Ktor obtenido de Logcat del emulador Android", "HeadAS07"),
    P("El cliente usa Logging con nivel ALL. A continuación se transcriben líneas representativas de una petición real; el cuerpo íntegro de la respuesta permanece consultable en Logcat. Este bloque es una transcripción, no una fotografía del panel Logcat."),
    Preformatted('''09-23 03:57:47.934 I System.out: REQUEST: https://api.escuelajs.co/api/v1/products?limit=1&offset=0
09-23 03:57:49.349 I System.out: RESPONSE: 200 OK
09-23 03:57:49.349 I System.out: BODY Content-Type: application/json; charset=utf-8
09-23 03:57:49.349 I System.out: BODY START
09-23 03:57:49.349 I System.out: [{"id":28,"title":"Sleek Modern Leather Sofa",...}]
09-23 03:57:49.349 I System.out: BODY END''', ss["CodeAS07"]),
    P("Verificación automatizada y estructura", "HeadAS07"),
    P("`ProductoApiTest` contiene cinco pruebas con MockEngine: 200, recurso inexistente, red ausente, timeout y JSON desconocido. `:shared:testAndroidHostTest`, `:androidApp:assembleDebug` y `:shared:compileKotlinIosSimulatorArm64` terminaron correctamente. `ProductoApi.kt` configura cliente Ktor; `ProductoDto.kt` declara los DTO; `ProductoCatalogoRepositorioRest.kt` convierte a dominio; `CatalogoViewModel.kt` maneja estado y mensajes; `CatalogoScreen.kt` dibuja la interfaz."),
    P("Evidencia iOS pendiente", "HeadAS07"),
    P("El código compartido compiló para iOS Simulator ARM64, pero esta máquina usa Windows y no puede ejecutar el simulador iOS. No se incluye una captura iOS inexistente. Debe abrirse iosApp en Xcode sobre macOS y capturarse la pantalla Catálogo REST conectada antes de marcar este punto como completo."),
    P("Repositorio y commit", "HeadAS07"),
    P("Repositorio: https://github.com/alexgm-A11/PharmaMobile_Practica<br/>Rama individual: feature/ktor-client<br/>Commit de la implementación documentada: https://github.com/alexgm-A11/PharmaMobile_Practica/commit/4fcfdcc9407e69dee9b65d0ea5e29d1cc0f64fd5<br/>README.md incluye la sección Conectividad REST."),
    P("Estado honesto de la lista de cotejo", "HeadAS07"),
    P("Completos: catálogo de cinco endpoints ejecutados; diccionario de ambos DTO; JSON y código; cinco escenarios con capturas Android; README y commit. Pendientes: captura iOS y fotografía específica del panel Logcat (se incluye la transcripción real)."),
]

def footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.HexColor("#557079"))
    canvas.drawString(44, 30, "PharmaMobile | S07 | Guillen Mendoza Alexander")
    canvas.drawRightString(A4[0] - 44, 30, f"Página {doc.page}")
    canvas.restoreState()

pdf = SimpleDocTemplate(str(OUT), pagesize=A4, leftMargin=44, rightMargin=44, topMargin=42, bottomMargin=44)
pdf.build(story, onFirstPage=footer, onLaterPages=footer)
print(OUT)
