import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/*
 *  LIST PROGRAMS
 *  This servlet will build the request to list all programs that an account is connected with.
 *  Documentation shows that the request will be:
 *  GET https://theseus-api.signalvine.com/v1/accounts/<account id>/programs
 *
 */



@WebServlet("/listPrograms")
public class ListPrograms extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private JSONObject decrypt(String token, String secret, String account) {
        JSONObject data = new JSONObject();
        data.put("keyword", "GET");
        data.put("token", token);
        data.put("secret", secret);
        data.put("urlEndPoint", "/v1/accounts/" + account + "/programs");
        return data;
    }

    public ListPrograms() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String secret = request.getParameter("secret");
        String account = request.getParameter("accountID");

        JSONObject decryptedData;
        // TODO: Build full decrypter() that returns a JSONObject
        decryptedData = decrypt(token, secret, account);


        SignatureBuilder listPrograms = new SignatureBuilder(decryptedData.getString("keyword"),
                decryptedData.getString("token"), decryptedData.getString("secret"),
                decryptedData.getString("urlEndPoint"));

        try {
            listPrograms.makeGetRequest();
        } finally {
            if (listPrograms.getStatus() == 200 || listPrograms.getStatus() == 202) {
                PrintWriter out = response.getWriter();
                out.print("<html><body><h1 align='center'>Your request was successful!</h1></body></html>");
            } else {
                PrintWriter out = response.getWriter();
                out.print("<html><body><h1 align='center'>Your request was unsuccessful and returned HTTP Response code "
                        + listPrograms.getStatus() + ".</h1><h2>Token Used: " + listPrograms.getToken() + "</h2><h2>Secret Used: "
                        + listPrograms.getSecret() + "</h2><h2>Keyword Used: " + listPrograms.getKeyword() + "</h2><h2>Endpoint Used: "
                        + listPrograms.getUrlEndPoint() + "</h2><h2>Timestamp Used: " + listPrograms.getTimeStamp() + "</h2><h2>Signature Used: "
                        + listPrograms.getSignature() + "</h2><h2>Encrypted Signature Used: " + listPrograms.getEncryptedSignature() +
                        "</h2><h2>Authorization Used: " + listPrograms.getAuthorization() + "</h2><h2>Response received: "
                        + listPrograms.getResponseBody() + "</h2></body></html>");
            }
        }
    }
}
