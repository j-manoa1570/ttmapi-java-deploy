import org.json.JSONObject;

import javax.crypto.Cipher;
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
    private static SecretKeySpec secretKey;
    private static byte[] key;

    private String decryptedData;

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
            setKey("98962DF4D1CCEEE8");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            decryptedData = new String(cipher.doFinal(Base64.getDecoder().decode(participantData)));
        }
        catch (Exception e)
        {
            System.out.println("Error while decrypting: " + e.toString());
        }

        JSONObject data = new JSONObject();
        data.put("keyword", "POST");
        data.put("token", token);
        data.put("secret", secret);
        data.put("urlEndPoint", "/v2/programs/" + program + "/participants");
        data.put("body", decryptedData);
        data.put("programID", program);
        return data;
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


        SignatureBuilder upsertParticipants = new SignatureBuilder(decryptedData.getString("keyword"),
                decryptedData.getString("token"), decryptedData.getString("secret"),
                decryptedData.getString("urlEndPoint"), decryptedData.getString("body"), decryptedData.getString("programID"));

        try {
            upsertParticipants.makeGetRequest();
        } finally {
            if (upsertParticipants.getStatus() == 200 || upsertParticipants.getStatus() == 202) {
                PrintWriter out = response.getWriter();
                out.print(upsertParticipants.successfulRequest());
            } else {
                PrintWriter out = response.getWriter();
                out.print(upsertParticipants.unsuccessfulRequest());
            }
        }
    }
}
