import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import sun.misc.BASE64Encoder;

public class MpesaIntegration {

    static class MpesaC2bCredential {
        static final String CONSUMER_KEY = "AqMIux0qLIIbZtGh5cBm3cqhtKfyZx22ScWW4A4utPPEturA";
        static final String CONSUMER_SECRET = "UUkorbR2oGPNWxj4NBlb7LHM6f8rRlxb0HZr0IqL0zAJxW1nFHkJ2V7NQsR41O4b";
        static final String API_URL = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";
    }

    static class LipanaMpesaPassword {
        static final String BUSINESS_SHORT_CODE = "174379";
        static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";

        public static String generatePassword() {
            try {
                String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String dataToEncode = BUSINESS_SHORT_CODE + PASSKEY + timestamp;
                BASE64Encoder encoder = new BASE64Encoder();
                String encodedPassword = encoder.encode(dataToEncode.getBytes("UTF-8"));
                return encodedPassword + "," + timestamp;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static String generateToken() {
        try {
            URL url = new URL(MpesaC2bCredential.API_URL);
            String credentials = MpesaC2bCredential.CONSUMER_KEY + ":" + MpesaC2bCredential.CONSUMER_SECRET;
            BASE64Encoder encoder = new BASE64Encoder();
            String basicAuth = "Basic " + encoder.encode(credentials.getBytes("UTF-8"));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", basicAuth);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                content.append(line);
            }

            in.close();
            conn.disconnect();

            // Manually extract "access_token" from the JSON response
            String response = content.toString();
            String token = extractValue(response, "access_token");
            System.out.println("Generated Token: " + token);
            return token;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void stkPush() {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter Phone Number: ");
            String phoneNumber = scanner.nextLine();

            System.out.print("Enter Amount: ");
            String amount = scanner.nextLine();

            String accessToken = generateToken();
            if (accessToken == null) {
                System.out.println("Failed to generate access token");
                return;
            }

            String[] passwordData = LipanaMpesaPassword.generatePassword().split(",");
            String password = passwordData[0];
            String timestamp = passwordData[1];

            URL url = new URL("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Construct JSON manually
            String payload = "{"
                    + "\"BusinessShortCode\":\"" + LipanaMpesaPassword.BUSINESS_SHORT_CODE + "\","
                    + "\"Password\":\"" + password + "\","
                    + "\"Timestamp\":\"" + timestamp + "\","
                    + "\"TransactionType\":\"CustomerPayBillOnline\","
                    + "\"Amount\":\"" + amount + "\","
                    + "\"PartyA\":\"" + phoneNumber + "\","
                    + "\"PartyB\":\"" + LipanaMpesaPassword.BUSINESS_SHORT_CODE + "\","
                    + "\"PhoneNumber\":\"" + phoneNumber + "\","
                    + "\"CallBackURL\":\"https://sandbox.safaricom.co.ke/mpesa/\","
                    + "\"AccountReference\":\"Beta Designs\","
                    + "\"TransactionDesc\":\"Development Charges\""
                    + "}";

            OutputStream os = conn.getOutputStream();
            os.write(payload.getBytes("UTF-8"));
            os.flush();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                content.append(line);
            }

            in.close();
            conn.disconnect();

            System.out.println("Response: " + content.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to extract values from a JSON string manually
    private static String extractValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int startIndex = json.indexOf(searchKey) + searchKey.length();
            int endIndex = json.indexOf("\"", startIndex);
            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose an option:");
        System.out.println("1. Generate Token");
        System.out.println("2. Perform STK Push");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                generateToken();
                break;
            case "2":
                stkPush();
                break;
            default:
                System.out.println("Invalid choice");
        }

        scanner.close();
    }
}
