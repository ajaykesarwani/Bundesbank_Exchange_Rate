# Bundesbank Exchange Rates API Integration

## Quick Start

```bash
mvn clean install
mvn spring-boot:run
```

## Access : 
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

## API Examples

### 1. Get all currencies

```bash
http://localhost:8080/api/currencies
```

**Response:**
```json
["AUD","BGN","BRL","CAD","CHF","CNY","CZK","DKK","GBP","HKD","HUF","IDR","ILS","INR","ISK","JPY","KRW","MXN","MYR","NOK","NZD","PHP","PLN","RON","RUB","SEK","SGD","THB","TRY","USD","ZAR"]
```

---

### 2. Get rates for a specific date
```bash
http://localhost:8080/api/rates/2025-07-07
```

**Response:**
```json
{
  "date": "2025-07-07",
  "rates": {
    "CHF": 0.9354,
    "MXN": 21.9707,
    "ZAR": 20.8187,
    "INR": 100.7765,
    "THB": 38.216,
    "CNY": 8.4128,
    "AUD": 1.8043,
    "KRW": 1603.56,
    "ILS": 3.912,
    "JPY": 170.71,
    "PLN": 4.2493,
    "GBP": 0.8611,
    "IDR": 19060.17,
    "HUF": 399.7,
    "PHP": 66.449,
    "TRY": 46.9115,
    "HKD": 9.2062,
    "ISK": 142.4,
    "DKK": 7.4604,
    "USD": 1.1728,
    "MYR": 4.9668,
    "CAD": 1.6016,
    "BGN": 1.9558,
    "NOK": 11.8755,
    "RON": 5.0655,
    "SGD": 1.4998,
    "CZK": 24.625,
    "SEK": 11.1645,
    "NZD": 1.9552,
    "BRL": 6.3827
  }
}
```

---

### 3. Convert USD to EUR

```bash
http://localhost:8080/api/convert?currency=USD&amount=100&date=2025-07-07
```

**Response:**
```json
{
  "fromCurrency": "USD",
  "originalAmount": 100,
  "date": "2025-07-07",
  "convertedAmount": 85.266,
  "formattedResult": "100.00 USD on 2025-07-07 = 85.2660 EUR"
}
```

### 4. Get exchange rate for a specific currency on a specific date
```bash
http://localhost:8080/api/rates/2025-07-07/USD
```

**Response:**
```json
1.1728
```
### 5. Get All Exchange Rates
**Retrieve all exchange rates for all dates.**

```bash
http://localhost:8080/api/rates
```
**Response:**
```json
[
{
"date": "2025-07-07",
"rates": {
"USD": 1.2345,
"GBP": 0.8765,
...
}
},
...
]
```
---