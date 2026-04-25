# DistribuciÃ³n Mayorista

Este documento define la estructura tÃ©cnica y lÃ³gica para una distribuidora de bebidas a gran escala, centrada en la eficiencia de la cadena de suministro, la optimizaciÃ³n de flota por capacidad de carga y la liquidaciÃ³n financiera de Ãºltima milla.

---

## ðŸ—ï¸ MÃ³dulo 1: GestiÃ³n de Inventario y Abastecimiento
Responsable del control de flujo de entrada desde plantas de producciÃ³n y la gestiÃ³n de existencias en centros de distribuciÃ³n.

### 1.1. CatalogaciÃ³n de Productos (SKU)
Cada referencia debe estar parametrizada para permitir cÃ¡lculos logÃ­sticos precisos:
* **SKU ID:** Identificador Ãºnico por marca y presentaciÃ³n.
* **PresentaciÃ³n:** Unidad, Six-pack, Caja, Barril o Estiba.
* **Peso LogÃ­stico:** Peso bruto total de la unidad de empaque (dato maestro para el MÃ³dulo 2).
* **GestiÃ³n de Vida Ãštil:** Control de fechas de vencimiento bajo metodologÃ­a FEFO (First Expired, First Out).

### 1.2. Ciclo de Vida del Stock
El sistema debe rastrear el estado de la mercancÃ­a dentro del Centro de DistribuciÃ³n:
* **RecepciÃ³n:** Ingreso validado contra manifiesto de fÃ¡brica.
* **Disponible:** Stock listo para la venta.
* **Comprometido:** Producto vinculado a pedidos en proceso.
* **En Picking:** MercancÃ­a en etapa de alistamiento.
* **Despachado:** Producto cargado y fuera de la bodega.
* **Excepciones de Inventario:**
    * âš ï¸ **AverÃ­a:** Producto daÃ±ado durante la manipulaciÃ³n.
    * âŒ **Vencido:** Stock que ha superado su fecha de consumo.

---

## ðŸšš MÃ³dulo 2: LogÃ­stica de Despacho y DistribuciÃ³n
MÃ³dulo encargado de la ejecuciÃ³n de entregas y la optimizaciÃ³n del uso de la flota vehicular.

### 2.1. GestiÃ³n de Capacidad de Flota
La flota se categoriza para maximizar la rentabilidad por viaje:
* **Camioneta Urbana:** Capacidad hasta 1.5 toneladas.
* **CamiÃ³n Sencillo:** Capacidad hasta 5 toneladas.
* **TractocamiÃ³n Regional:** Capacidad superior a 25 toneladas.

### 2.2. PlanificaciÃ³n de Rutas por Carga CrÃ­tica
El sistema genera rutas optimizadas siguiendo estas reglas:
1. **ConsolidaciÃ³n de Carga:** Los pedidos se agrupan en vehÃ­culos hasta alcanzar el **95% de su capacidad de peso**.
2. **SecuenciaciÃ³n de Paradas:** Orden logÃ­stico basado en geocodificaciÃ³n y ventanas de entrega del cliente.
3. **GestiÃ³n de Entregas (Novedades):**
    * **Entrega Exitosa:** Confirmada la entrega.
    * **Rechazo Parcial:** DevoluciÃ³n de producto por parte del cliente.
    * **No Entregado:** Fallo por local cerrado o restricciones de acceso.

---

## ðŸ’° MÃ³dulo 3: ConciliaciÃ³n Financiera y LiquidaciÃ³n Comercial
Analiza el resultado de la operaciÃ³n logÃ­stica para ejecutar cobros a clientes y pagos a aliados de transporte.

### 3.1. Modelos de Cobro y Recaudo
El sistema procesa diferentes modalidades de pago:
* **Pago Contra Entrega (COD):** ConciliaciÃ³n inmediata del dinero recaudado por el transportador.
* **Cartera Comercial:** GestiÃ³n de facturaciÃ³n a crÃ©dito para grandes superficies.

### 3.2. Matriz de LiquidaciÃ³n LogÃ­stica
La remuneraciÃ³n a la flota de distribuciÃ³n se calcula segÃºn la efectividad reportada en el MÃ³dulo 2:

| Resultado de la Entrega | % Pago LogÃ­stico | AcciÃ³n Financiera |
| :--- | :--- | :--- |
| **Entregado Completo** | 100% | Ingreso total validado. |
| **Rechazo Parcial** | 80% | GeneraciÃ³n automÃ¡tica de nota crÃ©dito. |
| **DevoluciÃ³n (Error Empresa)** | 0% | El costo del flete es asumido como pÃ©rdida operativa. |
| **Faltante de Inventario** | -100% | Descuento del valor comercial al transportador por pÃ©rdida. |



