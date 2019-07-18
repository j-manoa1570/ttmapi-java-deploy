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

    private JSONObject decrypt(String token, String secret, String account) {
        JSONObject data = new JSONObject();
        data.put("keyword", "GET");
        data.put("token", token);
        data.put("secret", secret);
        data.put("urlEndPoint", "/v1/programs/" + account + "/participants");
        return data;
    }

    public ProgramParticipants() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String secret = request.getParameter("secret");
        String accountID = request.getParameter("programID");

        JSONObject decryptedData;
        // TODO: Build decrypter() that returns a JSONObject
        decryptedData = decrypt(token, secret, accountID);


        SignatureBuilder programParticipants = new SignatureBuilder(decryptedData.getString("keyword"),
                decryptedData.getString("token"), decryptedData.getString("secret"),
                decryptedData.getString("urlEndPoint"));

        try {
            programParticipants.makeGetRequest();
        } finally {
            if (programParticipants.getStatus() == 200 || programParticipants.getStatus() == 202) {
                PrintWriter out = response.getWriter();
                out.print(programParticipants.successfulRequest());
            } else {
                PrintWriter out = response.getWriter();
                out.print(programParticipants.unsuccessfulRequest());
            }
        }
    }
}
