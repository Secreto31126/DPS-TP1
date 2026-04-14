- [x] Cómo usuario, quiero que el sistema me permita listar todas las monedas soportadas por la API para saber qué opciones tengo disponibles.

- [x] Cómo usuario, quiero que la respuesta de conversión incluya la marca de tiempo (timestamp) de cuándo fue obtenida esa cotización, para tener certeza de la actualidad del dato.

- [x] Cómo usuario, quiero poder obtener solo la cotización (tasa de cambio) entre dos monedas, sin realizar una conversión de un monto específico. e.g. (USD a EUR)

- [x] Cómo usuario, quiero que el sistema maneje y notifique errores de conexión o errores de la API de manera clara (ej. código de error 404, 500, etc.) en lugar de simplemente fallar.

- [x] Cómo usuario, quiero poder convertir desde el mismo monto y moneda a más de una moneda a la vez. e.g. (100 USD, [EUR, JPY]) para así tener visibilidad completa de la conversión de una moneda a múltiples.

- [x] Cómo usuario, quiero ver la cotización para una fecha pasada de una o más monedas por un cierto monto para poder visualizar el histórico de cotizaciones. e.g. (100 USD, [EUR, JPY], 2024-11-20)

- [x] Cómo usuario, quiero poder ver la cotización usada para cada moneda en la respuesta de mi solicitud de conversión, para así verificar y comparar las cotizaciones.

1. getAvailableCurrencies() [Requiere definir un modelo de respuesta]
2. se agrega el campo de rateDate [Trivial, solo modifica Rate]
3. getRate sobrecargado con una llamada de (Money from, Currency to) y (Currency from, Currency to)
4. Capa de conversion Unirest a CurrencyNotFound, ExternalServiceError, etc
5. getRate(..., List<Currencies> to)
6. getRate(..., ..., LocalDateTime rateDate)
7. Se modifica la respuesta de getRate
