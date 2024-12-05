# Mpesa Integration in Java

This project implements an integration with the Mpesa API to perform STK Push transactions and generate access tokens using Java. It utilizes `HttpURLConnection` for making HTTP requests and `Base64` for encoding credentials and passwords.

---

## Features

- Generate an Mpesa Access Token.
- Perform STK Push transactions for customer payments.
- Dynamic input for phone number and transaction amount.
- Encodes Mpesa password dynamically based on the timestamp.

---

## Prerequisites

Before running this project, ensure the following are set up:

1. **Java Development Kit (JDK):** Version 8 or later.
2. **Mpesa API Credentials:**
   - Consumer Key
   - Consumer Secret
   - Business Short Code
   - Passkey
3. **Libraries:**
   - `org.json` for JSON parsing. Add it as a dependency via Maven or download the JAR file from [JSON.org](https://github.com/stleary/JSON-java).

---

## How to Use

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/mpesa-java-integration.git
cd mpesa-java-integration
