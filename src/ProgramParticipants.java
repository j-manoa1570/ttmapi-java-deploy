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



@WebServlet("/programParticipants")
public class ProgramParticipants extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private JSONObject decrypt(String token, String secret, String program) {
        JSONObject data = new JSONObject();
        data.put("keyword", "GET");
        data.put("token", token);
        data.put("secret", secret);
        data.put("urlEndPoint", "/v1/programs/" + program + "/participants");
        return data;
    }

    public ProgramParticipants() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String secret = request.getParameter("secret");
        String programID = request.getParameter("programID");

        JSONObject decryptedData;
        // TODO: Build decrypter() that returns a JSONObject
        decryptedData = decrypt(token, secret, programID);


        SignatureBuilder programParticipants = new SignatureBuilder(decryptedData.getString("keyword"),
                decryptedData.getString("token"), decryptedData.getString("secret"),
                decryptedData.getString("urlEndPoint"));

        try {
            programParticipants.makeGetRequest();
        } finally {
            if (programParticipants.getStatus() == 200 || programParticipants.getStatus() == 202) {
                PrintWriter out = response.getWriter();
                out.print("<html><body><h1 align='center'>Your request was successful!</h1></body></html>");
            } else {
                PrintWriter out = response.getWriter();
                out.print("<html><body><h1 align='center'>Your request was unsuccessful and returned HTTP Response code "
                        + programParticipants.getStatus() + ".</h1><h2>Token Used: " + programParticipants.getToken() + "</h2><h2>Secret Used: "
                        + programParticipants.getSecret() + "</h2><h2>Keyword Used: " + programParticipants.getKeyword() + "</h2><h2>Endpoint Used: "
                        + programParticipants.getUrlEndPoint() + "</h2><h2>Timestamp Used: " + programParticipants.getTimeStamp() + "</h2><h2>Signature Used: "
                        + programParticipants.getSignature() + "</h2><h2>Encrypted Signature Used: " + programParticipants.getEncryptedSignature() +
                        "</h2><h2>Authorization Used: " + programParticipants.getAuthorization() + "</h2><h2>Response received: "
                        + programParticipants.getResponseBody() + "</h2></body></html>");
            }
        }
    }
}
