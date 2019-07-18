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
 *  This servlet will build the request to list all programs that an participant is connected with.
 *  Documentation shows that the request will be:
 *  GET https://theseus-api.signalvine.com/v1/participants/<participant id>/programs
 *
 */


@WebServlet("/participantMessages")
public class ParticipantMessages extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private JSONObject decrypt(String token, String secret, String participant) {
        JSONObject data = new JSONObject();
        data.put("keyword", "GET");
        data.put("token", token);
        data.put("secret", secret);
        data.put("urlEndPoint", "/v1/participants/" + participant + "/messages");
        return data;
    }

    public ParticipantMessages() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String secret = request.getParameter("secret");
        String participantID = request.getParameter("participantID");

        JSONObject decryptedData;
        // TODO: Build decrypter() that returns a JSONObject
        decryptedData = decrypt(token, secret, participantID);


        SignatureBuilder participantMessages = new SignatureBuilder(decryptedData.getString("keyword"),
                decryptedData.getString("token"), decryptedData.getString("secret"),
                decryptedData.getString("urlEndPoint"));

        try {
            participantMessages.makeGetRequest();
        } finally {
            if (participantMessages.getStatus() == 200 || participantMessages.getStatus() == 202) {
                PrintWriter out = response.getWriter();
                out.print(participantMessages.successfulRequest());
            } else {
                PrintWriter out = response.getWriter();
                out.print(participantMessages.unsuccessfulRequest());
            }
        }
    }
}
