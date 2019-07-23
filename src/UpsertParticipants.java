import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;



/*
 *  LIST PROGRAMS
 *  This servlet will build the request to list all programs that an account is connected with.
 *  Documentation shows that the request will be:
 *  GET https://theseus-api.signalvine.com/v1/accounts/<account id>/programs
 *
 */
@WebServlet("/upsertParticipants")
public class UpsertParticipants extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private SecretKey secretKey;
    private static byte[] key;
    private String decrypted = "";
    private byte[] decryptedMessageBytes = null;
    private byte[] participantDataAsBytes = null;
    private byte[] encryptionKeyBytes = null;

    public UpsertParticipants() {
        super();
    }

    public byte[][] GenerateKeyAndIV(int keyLength, int ivLength, int iterations, byte[] salt, byte[] password, MessageDigest md) {

        int digestLength = md.getDigestLength();
        int requiredLength = (keyLength + ivLength + digestLength - 1) / digestLength * digestLength;
        byte[] generatedData = new byte[requiredLength];
        int generatedLength = 0;

        try {
            md.reset();

            // Repeat process until sufficient data has been generated
            while (generatedLength < keyLength + ivLength) {

                // Digest data (last digest if available, password data, salt if available)
                if (generatedLength > 0)
                    md.update(generatedData, generatedLength - digestLength, digestLength);
                md.update(password);
                if (salt != null)
                    md.update(salt, 0, 8);
                md.digest(generatedData, generatedLength, digestLength);

                // additional rounds
                for (int i = 1; i < iterations; i++) {
                    md.update(generatedData, generatedLength, digestLength);
                    md.digest(generatedData, generatedLength, digestLength);
                }

                generatedLength += digestLength;
            }

            // Copy key and IV into separate byte arrays
            byte[][] result = new byte[2][];
            result[0] = Arrays.copyOfRange(generatedData, 0, keyLength);
            if (ivLength > 0)
                result[1] = Arrays.copyOfRange(generatedData, keyLength, keyLength + ivLength);

            return result;

        } catch (DigestException e) {
            throw new RuntimeException(e);

        } finally {
            // Clean out temporary data
            Arrays.fill(generatedData, (byte)0);
        }
    }


    private JSONObject decrypt(String token, String secret, String program, String participantData) {

        try
        {
            String passphrase = "EaL9KpCG4KQ3zxda";

            byte[] cipherData = Base64.getDecoder().decode(participantData);
            byte[] saltData = Arrays.copyOfRange(cipherData, 8, 16);

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            final byte[][] keyAndIV = GenerateKeyAndIV(32, 16, 1, saltData, passphrase.getBytes(StandardCharsets.UTF_8), md5);
            SecretKeySpec key = new SecretKeySpec(keyAndIV[0], "AES");
            IvParameterSpec iv = new IvParameterSpec(keyAndIV[1]);

            byte[] encrypted = Arrays.copyOfRange(cipherData, 16, cipherData.length);
            Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCBC.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedData = aesCBC.doFinal(encrypted);
            decrypted = new String(decryptedData, StandardCharsets.UTF_8);
//            decrypted = decrypted.replace("[","");
//            decrypted = decrypted.replace("]", "");
            System.out.println("Decrypted Text: " + decrypted);
        }
        catch (Exception e)
        {
            System.out.println("Error while decrypting: " + e.toString());
        }

        if (decrypted.length() > 0) {

//            JSONArray decryptedData = new JSONArray(decrypted);
//            System.out.println("JSONArray: " + decryptedData);
            JSONObject data = new JSONObject();
            data.put("keyword", "POST");
            data.put("token", token);
            data.put("secret", secret);
            data.put("urlEndPoint", "/v2/programs/" + program + "/participants");
            data.put("programID", program);
//            data.put("body", decryptedData);
            data.put("body", decrypted);
            return data;
        } else {
            return null;
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String secret = request.getParameter("secret");
        String program = request.getParameter("programID");
//        String participantData = text;
        String participantData = request.getParameter("body");
        participantData = participantData.replace(" ", "+");
        JSONObject decryptedData;

        // TODO: Build full decrypter() that returns a JSONObject
        decryptedData = decrypt(token, secret, program, participantData);


        /*
         *  Make sure there is content in decryptedData first otherwise there will be tons of errors.
         */
        if (decryptedData != null) {

            /*
             *  Once JSON object is created, check for decryptedData has "body" property. If it has "body", create
             *  a new signature builder object and then try making the request. If it does not have the property,
             *  just create a new page with information.
             */
            if (decryptedData.has("body")) {


                SignatureBuilder upsertParticipants = new SignatureBuilder(
                        decryptedData.getString("keyword"),
                        decryptedData.getString("token"),
                        decryptedData.getString("secret"),
                        decryptedData.getString("urlEndPoint"),
                        decryptedData.getString("body"),
//                        decryptedData.getJSONArray("body"),
                        decryptedData.getString("programID"),
                        participantData);


                try {
                    upsertParticipants.makePostRequest();
                } finally {
                    if (upsertParticipants.getStatus() == 200 || upsertParticipants.getStatus() == 202) {
                        String fail = "<html><body><h1 align='center'>Your request was unsuccessful and returned HTTP Response code "
                                + upsertParticipants.getStatus() + ".</h1><h2>Token Used: " + upsertParticipants.getToken() + "</h2><h2>Secret Used: "
                                + upsertParticipants.getSecret() + "</h2><h2>Keyword Used: " + upsertParticipants.getKeyword() + "</h2><h2>Endpoint Used: "
                                + upsertParticipants.getUrlEndPoint();

                        if (upsertParticipants.getKeyword().equals("POST")) {
                            fail = fail + "</h2><h2>Body Param Used: " + upsertParticipants.getData() + "</h2><h2>Body Used: " + upsertParticipants.getBody() + "</h2><h2>Participant Data Used: " + upsertParticipants.getEverything();
                        }

                        fail = fail + "</h2><h2>Timestamp Used: " + upsertParticipants.getTimeStamp() + "</h2><h2>Signature Used: "
                                + upsertParticipants.getSignature() + "</h2><h2>Encrypted Signature Used: " + upsertParticipants.getEncryptedSignature() +
                                "</h2><h2>Authorization Used: " + upsertParticipants.getAuthorization() + "</h2><h2>Response received: "
                                + upsertParticipants.getResponseBody() + "</h2><h3>Decrypted Value: " + decrypted + "</h3></body></html>";
                        PrintWriter out = response.getWriter();
                        out.print(fail);
                    } else {
                        PrintWriter out = response.getWriter();
                        out.print(upsertParticipants.unsuccessfulRequest());
                    }
                }
            } else {

                /*
                 *  Output for when body is empty/not set due to decryption not working
                 */
                String fail = "<html><body><h1 align='center'>Your request was unsuccessful and has no \"body\" data.</h1>"
                        + "<h2>Token Used: " + decryptedData.getString("token") + "</h2><h2>Secret Used: "
                        + decryptedData.getString("secret") + "</h2><h2>Keyword Used: " + decryptedData.getString("keyword")
                        + "</h2><h2>Endpoint Used: " + decryptedData.getString("urlEndPoint")
                        + "</h2><h2>Timestamp Used: " + "</h2><h2>Body Used: Not Set</h2><h2>Timestamp Used: Not Set"
                        + "Not Set</h2><h2>Signature Used: Not Set</h2><h2>Encrypted Signature Used: Not Set</h2>"
                        + "<h2>Authorization Used: Not Set</h2><h2>Response received: Not Set</h2></body></html>";
                PrintWriter out = response.getWriter();
                out.print(fail);
            }
        } else {
            /*
             *  Output for when decryptData was not created properly
             */
            PrintWriter out = response.getWriter();
            out.print("<html><body><h1 align='center'>\"decryptedData\" was not created successfully...</h1><h2>decryptedData value: "
                    + decryptedData + "</h2><h2>decryptedMessageBytes value: " + decryptedMessageBytes + "</h2><h2>participantDataAsBytes value: "
                    + participantDataAsBytes +"</h2><h2>encryptionKeyBytes value: " + encryptionKeyBytes + "</h2><h2>secretKey value(as a string): "
                    + secretKey.toString() + "</h2></body></html>");
        }
    }
}
