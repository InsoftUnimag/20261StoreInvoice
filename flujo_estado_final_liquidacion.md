# Flujo de EjecuciÃ³n: Recibir Estado Final y Generar LiquidaciÃ³n de Transportista

Este documento detalla el paso a paso del flujo de negocio implementado en el **Sistema Financiero**, desde que se recibe la confirmaciÃ³n asÃ­ncrona de una entrega hasta la generaciÃ³n matemÃ¡tica de la liquidaciÃ³n que se le pagarÃ¡ al transportista.

El diseÃ±o sigue estrictamente los principios de la **Arquitectura Hexagonal**, el **Principio de Responsabilidad Ãšnica (SRP)** y las reglas de un modelo de dominio puro.

---

## 1. Gatillo del Evento (Capa de Infraestructura)
El flujo es completamente asÃ­ncrono y comienza impulsado por mensajes:
1. El **MÃ³dulo de Transporte** publica un evento JSON en el *topic* (cola) `estado.final.transporte` en RabbitMQ, notificando que una entrega finalizÃ³ (Ã©xito o fracaso).
2. El componente `EstadoFinalEventConsumer` (Adaptador de Entrada) estÃ¡ subscrito a esta cola a travÃ©s de *Spring Cloud Stream*.
3. Al llegar el mensaje, Spring valida y mapea el JSON automÃ¡ticamente al objeto `ProcesarEstadoFinalCommand`.
4. El consumidor **delega inmediatamente** la orden al caso de uso `ProcesarEstadoFinalUseCase`, manteniendo el adaptador libre de reglas de negocio.

## 2. InvocaciÃ³n y ValidaciÃ³n BÃ¡sica (Capa de AplicaciÃ³n)
El caso de uso `ProcesarEstadoFinalUseCase` toma el control y verifica la integridad mÃ­nima:
* Comprueba mediante cÃ³digo defensivo (`Objects.requireNonNull`) que los tres campos vitales estÃ©n presentes:
    * `idPedido`
    * `tasaEfectividad`
    * `idTransportista`

## 3. Idempotencia: PrevenciÃ³n de Duplicados (AplicaciÃ³n y Persistencia)
Dado que en sistemas asÃ­ncronos un mensaje puede ser entregado mÃ¡s de una vez (*At-Least-Once delivery*), el sistema se protege:
1. Consulta a travÃ©s del puerto `EventoRecibidoRepository` si ya existe un registro para ese `idPedido` con estado `PROCESADO`.
2. **Si ya existe:** El sistema asume que este evento es un duplicado o reintento de algo que ya se hizo. Ignora el mensaje silenciosamente y termina la ejecuciÃ³n con Ã©xito para que el broker descarte el mensaje.
3. **Si no existe:** El sistema registra en la base de datos el inicio del proceso creando una entidad `EventoRecibido` con estado `PENDIENTE`.

## 4. ObtenciÃ³n del Precio del Pedido (AplicaciÃ³n)
Para calcular cuÃ¡nto pagarle al transportista, se necesita el valor monetario de la mercancÃ­a.
* El caso de uso usa el `PedidoRepository` (Puerto de Salida) para buscar el precio total asociado al `idPedido` en la tabla `pedidos` (que fue poblada previamente cuando Inventario creÃ³ el pedido).
* **Si el pedido no existe:** Se lanza `PedidoNotFoundException` y el estado del evento cambia a `ERROR`.
* **Si el precio es 0 o nulo:** Se lanza `LiquidacionException` y el evento cambia a `ERROR`. (Un precio invÃ¡lido rompe la matemÃ¡tica).

## 5. ValidaciÃ³n del Value Object (Capa de Dominio)
El valor numÃ©rico bruto de la "efectividad" se encapsula en un Value Object puro del dominio llamado `TasaEfectividad`.
* Al instanciarse, `TasaEfectividad` se autovalida: exige que su valor estÃ© estrictamente entre `-100` y `100`.
* Si el MÃ³dulo de Transporte envÃ­a una tasa de `150`, la creaciÃ³n falla lanzando `InvalidTasaEfectividadException`, deteniendo el flujo y marcando el evento como `ERROR`.

## 6. CÃ¡lculo de la LiquidaciÃ³n (Capa de Dominio)
Con los datos seguros, la responsabilidad se delega a la entidad de dominio `LiquidacionTransportista` usando su mÃ©todo estÃ¡tico de comportamiento `calcularMonto(...)`:
1. Se calcula la tarifa base: `Precio Pedido * 10%`.
2. Se aplica la penalizaciÃ³n o bonificaciÃ³n: Se multiplica la tarifa base por `(Tasa Efectividad / 100)`.
3. **Redondeo:** El resultado matemÃ¡tico estricto puede tener muchos decimales, asÃ­ que se usa `RoundingMode.HALF_UP` (redondeo estÃ¡ndar comercial) sin decimales.
   * *Ejemplo: Precio = $105, Efectividad = 95%.*
   * *Tarifa base = $10.5*
   * *CÃ¡lculo = $10.5 * 0.95 = 9.975*
   * *Resultado = $10 (se redondea hacia arriba).*

## 7. Persistencia y Cierre del Flujo (AplicaciÃ³n e Infraestructura)
1. **Guardar LiquidaciÃ³n:** Se instancia una nueva `LiquidacionTransportista` con el monto calculado, se le asigna el estado inicial `PENDIENTE` y se manda a guardar a travÃ©s del `LiquidacionRepository`.
2. **Marcar Evento:** Al terminar, el modelo de dominio `EventoRecibido` ejecuta su mÃ©todo interno `marcarProcesado()`, el cual cambia su estado a `PROCESADO` y estampa la hora exacta (`fechaProcesado = LocalDateTime.now()`).
3. El caso de uso finaliza exitosamente, y Spring notifica a RabbitMQ que el mensaje fue consumido y puede ser borrado de la cola.

---
### Resumen de Manejo de Errores
Si ocurre cualquier excepciÃ³n de dominio (precio 0, tasa invÃ¡lida, pedido no encontrado):
* El caso de uso atrapa el error.
* Ejecuta `evento.marcarError()` cambiando el registro en base de datos a estado `ERROR`.
* El evento se guarda y luego la excepciÃ³n se vuelve a lanzar.
* **Beneficio:** Queda el rastro en base de datos de por quÃ© fallÃ³, y permite que procesos de *Dead Letter Queue (DLQ)* de RabbitMQ reintenten procesar el mensaje mÃ¡s adelante si fue un error transitorio.

