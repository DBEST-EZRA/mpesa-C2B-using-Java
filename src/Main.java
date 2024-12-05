import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

import org.json.JSONObject;

public class MpesaIntegration {

    static class MpesaC2bCredential {
        static final String CONSUMER_KEY = "add yours";
        static final String CONSUMER_SECRET = "add yours";
        static final String API_URL = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";
    }

    static class LipanaMpesaPassword {
        static final String BUSINESS_SHORT_CODE = "174379";
        static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";

        public static String generatePassword() {
            try {
                String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String dataToEncode = BUSINESS_SHORT_CODE + PASSKEY + timestamp;
                String encodedPassword = Base64.getEncoder().encodeToString(dataToEncode.getBytes(StandardCharsets.UTF_8));
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
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", basicAuth);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            JSONObject response = new JSONObject(content.toString());
            String accessToken = response.getString("access_token");
            System.out.println("Generated Token: " + accessToken);
            return accessToken;

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

            JSONObject payload = new JSONObject();
            payload.put("BusinessShortCode", LipanaMpesaPassword.BUSINESS_SHORT_CODE);
            payload.put("Password", password);
            payload.put("Timestamp", timestamp);
            payload.put("TransactionType", "CustomerPayBillOnline");
            payload.put("Amount", amount);
            payload.put("PartyA", phoneNumber);
            payload.put("PartyB", LipanaMpesaPassword.BUSINESS_SHORT_CODE);
            payload.put("PhoneNumber", phoneNumber);
            payload.put("CallBackURL", "https://sandbox.safaricom.co.ke/mpesa/");
            payload.put("AccountReference", "Beta Designs");
            payload.put("TransactionDesc", "Development Charges");

            OutputStream os = conn.getOutputStream();
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            System.out.println("Response: " + content.toString());

        } catch (Exception e) {
            e.printStackTrace();
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
