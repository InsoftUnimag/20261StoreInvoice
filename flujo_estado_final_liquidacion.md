# Flujo de Ejecución: Recibir Estado Final y Generar Liquidación de Transportista

Este documento detalla el paso a paso del flujo de negocio implementado en el **Sistema Financiero**, desde que se recibe la confirmación asíncrona de una entrega hasta la generación matemática de la liquidación que se le pagará al transportista.

El diseño sigue estrictamente los principios de la **Arquitectura Hexagonal**, el **Principio de Responsabilidad Única (SRP)** y las reglas de un modelo de dominio puro.

---

## 1. Gatillo del Evento (Capa de Infraestructura)
El flujo es completamente asíncrono y comienza impulsado por mensajes:
1. El **Módulo de Transporte** publica un evento JSON en el *topic* (cola) `estado.final.transporte` en RabbitMQ, notificando que una entrega finalizó (éxito o fracaso).
2. El componente `EstadoFinalEventConsumer` (Adaptador de Entrada) está subscrito a esta cola a través de *Spring Cloud Stream*.
3. Al llegar el mensaje, Spring valida y mapea el JSON automáticamente al objeto `ProcesarEstadoFinalCommand`.
4. El consumidor **delega inmediatamente** la orden al caso de uso `ProcesarEstadoFinalUseCase`, manteniendo el adaptador libre de reglas de negocio.

## 2. Invocación y Validación Básica (Capa de Aplicación)
El caso de uso `ProcesarEstadoFinalUseCase` toma el control y verifica la integridad mínima:
* Comprueba mediante código defensivo (`Objects.requireNonNull`) que los tres campos vitales estén presentes:
    * `idPedido`
    * `tasaEfectividad`
    * `idTransportista`

## 3. Idempotencia: Prevención de Duplicados (Aplicación y Persistencia)
Dado que en sistemas asíncronos un mensaje puede ser entregado más de una vez (*At-Least-Once delivery*), el sistema se protege:
1. Consulta a través del puerto `EventoRecibidoRepository` si ya existe un registro para ese `idPedido` con estado `PROCESADO`.
2. **Si ya existe:** El sistema asume que este evento es un duplicado o reintento de algo que ya se hizo. Ignora el mensaje silenciosamente y termina la ejecución con éxito para que el broker descarte el mensaje.
3. **Si no existe:** El sistema registra en la base de datos el inicio del proceso creando una entidad `EventoRecibido` con estado `PENDIENTE`.

## 4. Obtención del Precio del Pedido (Aplicación)
Para calcular cuánto pagarle al transportista, se necesita el valor monetario de la mercancía.
* El caso de uso usa el `PedidoRepository` (Puerto de Salida) para buscar el precio total asociado al `idPedido` en la tabla `pedidos` (que fue poblada previamente cuando Inventario creó el pedido).
* **Si el pedido no existe:** Se lanza `PedidoNotFoundException` y el estado del evento cambia a `ERROR`.
* **Si el precio es 0 o nulo:** Se lanza `LiquidacionException` y el evento cambia a `ERROR`. (Un precio inválido rompe la matemática).

## 5. Validación del Value Object (Capa de Dominio)
El valor numérico bruto de la "efectividad" se encapsula en un Value Object puro del dominio llamado `TasaEfectividad`.
* Al instanciarse, `TasaEfectividad` se autovalida: exige que su valor esté estrictamente entre `-100` y `100`.
* Si el Módulo de Transporte envía una tasa de `150`, la creación falla lanzando `InvalidTasaEfectividadException`, deteniendo el flujo y marcando el evento como `ERROR`.

## 6. Cálculo de la Liquidación (Capa de Dominio)
Con los datos seguros, la responsabilidad se delega a la entidad de dominio `LiquidacionTransportista` usando su método estático de comportamiento `calcularMonto(...)`:
1. Se calcula la tarifa base: `Precio Pedido * 10%`.
2. Se aplica la penalización o bonificación: Se multiplica la tarifa base por `(Tasa Efectividad / 100)`.
3. **Redondeo:** El resultado matemático estricto puede tener muchos decimales, así que se usa `RoundingMode.HALF_UP` (redondeo estándar comercial) sin decimales.
   * *Ejemplo: Precio = $105, Efectividad = 95%.*
   * *Tarifa base = $10.5*
   * *Cálculo = $10.5 * 0.95 = 9.975*
   * *Resultado = $10 (se redondea hacia arriba).*

## 7. Persistencia y Cierre del Flujo (Aplicación e Infraestructura)
1. **Guardar Liquidación:** Se instancia una nueva `LiquidacionTransportista` con el monto calculado, se le asigna el estado inicial `PENDIENTE` y se manda a guardar a través del `LiquidacionRepository`.
2. **Marcar Evento:** Al terminar, el modelo de dominio `EventoRecibido` ejecuta su método interno `marcarProcesado()`, el cual cambia su estado a `PROCESADO` y estampa la hora exacta (`fechaProcesado = LocalDateTime.now()`).
3. El caso de uso finaliza exitosamente, y Spring notifica a RabbitMQ que el mensaje fue consumido y puede ser borrado de la cola.

---
### Resumen de Manejo de Errores
Si ocurre cualquier excepción de dominio (precio 0, tasa inválida, pedido no encontrado):
* El caso de uso atrapa el error.
* Ejecuta `evento.marcarError()` cambiando el registro en base de datos a estado `ERROR`.
* El evento se guarda y luego la excepción se vuelve a lanzar.
* **Beneficio:** Queda el rastro en base de datos de por qué falló, y permite que procesos de *Dead Letter Queue (DLQ)* de RabbitMQ reintenten procesar el mensaje más adelante si fue un error transitorio.
