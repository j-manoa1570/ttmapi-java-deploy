import org.json.JSONObject;
import sun.misc.IOUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/*
 *  UPSERT PARTICIPANTS
 *  This servlet will build the request to insert and update users into SV. Functionality is currently only insert users
 *  as update on their end does not appear to work.
 *  Documentation shows that the request will be:
 *  GET https://theseus-api.signalvine.com/v2/programs/<program id>/participants
 *
 */
@WebServlet("/upsertParticipants")
public class UpsertParticipants extends HttpServlet {

    // Local variables to maintain everything. This will probably be stripped down more in a final version as I don't
    // the three byte arrays are needed.
    private static final long serialVersionUID = 1L;
    private String decrypted = "";
    private byte[] decryptedMessageBytes = null;
    private byte[] participantDataAsBytes = null;
    private byte[] encryptionKeyBytes = null;


    /*
     *  BASIC CONSTRUCTOR
     *  Creates a new upsert item inheriting all of the methods and variables from HttpServlet Class
     */
    public UpsertParticipants() {
        super();
    }

    /*
     *  GENERATE KEY AND IV
     *  DESCRIPTION: Black magic
     */
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

    /*
     *  DECRYPT
     *  INPUT: Token, secret, program, participantData
     *  DESCRIPTION: Decrypt participantData and created a JSON object with all data.
     *  OUTPUT: JSON object
     */
    private JSONObject decrypt(String token, String secret, String program, String participantData) {

        // TODO: Since Encryption and Decryption uses the same key, turn this into true async encrypt

        // This is essentially black magic as I don't really know how it does what it does. All I know is that it does
        // and for now that is enough.
        try
        {
            // Passphrase that will be used to decrypt.
            String passphrase = "EaL9KpCG4KQ3zxda";

            // Get byte data from cipher text
            byte[] cipherData = Base64.getDecoder().decode(participantData);
            byte[] saltData = Arrays.copyOfRange(cipherData, 8, 16);

            // Black magic
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            final byte[][] keyAndIV = GenerateKeyAndIV(32, 16, 1, saltData, passphrase.getBytes(StandardCharsets.UTF_8), md5);
            SecretKeySpec key = new SecretKeySpec(keyAndIV[0], "AES");
            IvParameterSpec iv = new IvParameterSpec(keyAndIV[1]);

            // More black magic
            byte[] encrypted = Arrays.copyOfRange(cipherData, 16, cipherData.length);
            Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCBC.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedData = aesCBC.doFinal(encrypted);
            decrypted = new String(decryptedData, StandardCharsets.UTF_8);

        }
        catch (Exception e)
        {
            System.out.println("Error while decrypting: " + e.toString());
        }

        // Assuming the decryption was successful so the length will be greater than 0, build a JSON object to return
        // otherwise return null.
        if (decrypted.length() > 0) {

            JSONObject data = new JSONObject();
            data.put("keyword", "POST");
            data.put("token", token);
            data.put("secret", secret);
            data.put("urlEndPoint", "/v2/programs/" + program + "/participants");
            data.put("programID", program);
            data.put("body", decrypted);
            return data;
        } else {
            return null;
        }
    }

    private String extractPostRequestBody(HttpServletRequest request) throws IOException {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
        return "";
    }

    /*
     *  DO GET
     *  INPUT: request and response parameters inherited from HttpServlet
     *  DESCRIPTION: Performs the received request. Takes in the parameters from GET, assigns them to variables that
     *  can be used in api, decrypts and builds JSON object that will be used for request to SV, makes request, and
     *  displays new page based on request status.
     *  OUTPUT: HTML page to display.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String secret = request.getParameter("secret");
        String program = request.getParameter("programID");
        String participantData = request.getParameter("body");

        // Since http requests interpret "+" as " ", this has to be fixed prior to decrypting data so it can decrypt data
        participantData = participantData.replace(" ", "+");

        // Initialize JSON object
        JSONObject decryptedData;

        // function call returns a JSON object that is assigned to decryptedData.
        decryptedData = decrypt(token, secret, program, participantData);


        // Make sure there is content in decryptedData first otherwise there will be tons of errors.
        if (decryptedData != null) {

            // Once JSON object is created, check for decryptedData has "body" property. If it has "body", create
            // a new signature builder object and then try making the request. If it does not have the property,
            // just create a new page with information.
            if (decryptedData.has("body")) {

                // Creates a new SignatureBuilder object that prepares the data for a POST request
                SignatureBuilder upsertParticipants = new SignatureBuilder(
                        decryptedData.getString("keyword"),
                        decryptedData.getString("token"),
                        decryptedData.getString("secret"),
                        decryptedData.getString("urlEndPoint"),
                        decryptedData.getString("body"),
                        decryptedData.getString("programID"),
                        participantData);

                // Attempt a POST request
                try {
                    upsertParticipants.makeRequest();
                } finally {
                    // Successful request will respond with a page that says successful
                    if (upsertParticipants.getStatus() == 200 || upsertParticipants.getStatus() == 202) {
                        PrintWriter out = response.getWriter();
                        out.print(upsertParticipants.successfulRequest());
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        // Unsuccessful request will respond with a page that says unsuccessful and a bunch of data to
                        // understand what it was doing and what values it was working with
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
                }
            } else {

                // Output webpage for when body is empty/not set due to decryption not working
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
            // Output webpage for when decryptData was not created properly
            PrintWriter out = response.getWriter();
            out.print("<html><body><h1 align='center'>\"decryptedData\" was not created successfully...</h1><h2>decryptedData value: "
                    + decryptedData + "</h2><h2>decryptedMessageBytes value: " + decryptedMessageBytes + "</h2><h2>participantData value: "
                    + participantData + "</h2><h2>Decrypted value is: " + decrypted + "</h2><h2>participantDataAsBytes value: "
                    + participantDataAsBytes +"</h2><h2>encryptionKeyBytes value: "
                    + encryptionKeyBytes + "</h2></body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("Received POST request");
        System.out.println("Is there anybody out there? ");

        String data = extractPostRequestBody(request);

        System.out.println("data: " + data);

        JSONObject requestBody = new JSONObject(data);

        String token = requestBody.getString("token");
        String secret = requestBody.getString("secret");
        String program = requestBody.getString("programID");
        String participantData = requestBody.getString("body");

        System.out.println("Got Header data:");
        System.out.println("token = " + token);
        System.out.println("program = " + program);
        System.out.println("secret = " + secret);
        System.out.println("participantData = " + participantData);


        // Since http requests interpret "+" as " ", this has to be fixed prior to decrypting data so it can decrypt data
        participantData = participantData.replace(" ", "+");

        // Initialize JSON object
        JSONObject decryptedData;

        // function call returns a JSON object that is assigned to decryptedData.
        decryptedData = decrypt(token, secret, program, participantData);

        // Creates a new SignatureBuilder object that prepares the data for a POST request
        SignatureBuilder upsertParticipants = new SignatureBuilder(
                decryptedData.getString("keyword"),
                decryptedData.getString("token"),
                decryptedData.getString("secret"),
                decryptedData.getString("urlEndPoint"),
                decryptedData.getString("body"),
                decryptedData.getString("programID"),
                participantData);

        // Attempt a POST request
        try {
            upsertParticipants.makeRequest();
        } finally {
            // Successful request will respond with a page that says successful
            if (upsertParticipants.getStatus() == 200 || upsertParticipants.getStatus() == 202) {
                PrintWriter out = response.getWriter();
                out.print(upsertParticipants.successfulRequest());
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                // Unsuccessful request will respond with a page that says unsuccessful and a bunch of data to
                // understand what it was doing and what values it was working with
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
        }
    }
}
