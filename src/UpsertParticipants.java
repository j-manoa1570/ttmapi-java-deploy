import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64DecoderStream;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

//import static java.nio.charset.StandardCharsets.UTF_8;


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

    private void setKey(String myKey)
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



    private JSONObject decrypt(String token, String secret, String program, String participantData) {

        try
        {
            participantDataAsBytes = participantData.getBytes();
            String encryptionKeyString = "98962DF4D1CCEEE8";
            encryptionKeyBytes = encryptionKeyString.getBytes();
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
            secretKey = new SecretKeySpec(encryptionKeyBytes, "DES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            decryptedMessageBytes = cipher.doFinal(participantDataAsBytes);
            decrypted = new String(decryptedMessageBytes);
        }
        catch (Exception e)
        {
            System.out.println("Error while decrypting: " + e.toString());
        }

        if (decrypted != null) {

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

    public UpsertParticipants() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String secret = request.getParameter("secret");
        String program = request.getParameter("programID");
        String participantData = request.getParameter("body");

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


                SignatureBuilder upsertParticipants = new SignatureBuilder(decryptedData.getString("keyword"),
                        decryptedData.getString("token"),
                        decryptedData.getString("secret"),
                        decryptedData.getString("urlEndPoint"),
                        decryptedData.getString("body"),
                        decryptedData.getString("programID"));


                try {
                    upsertParticipants.makePostRequest();
                } finally {
                    if (upsertParticipants.getStatus() == 200 || upsertParticipants.getStatus() == 202) {
                        PrintWriter out = response.getWriter();
                        out.print(upsertParticipants.unsuccessfulRequest());
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
