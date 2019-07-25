import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Signature;


/*
 *  LIST PROGRAMS
 *  This servlet will build the request to list all programs that an account is connected with.
 *  Documentation shows that the request will be:
 *  GET https://theseus-api.signalvine.com/v1/accounts/<account id>/programs
 *
 */


@WebServlet("/listParticipants")
public class ListParticipants extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private JSONObject decrypt(String token, String secret, String program) {
        JSONObject data = new JSONObject();
        data.put("keyword", "GET");
        data.put("token", token);
        data.put("secret", secret);
        data.put("urlEndPoint", "/v1/programs/" + program + "/participants");
        return data;
    }

    public ListParticipants() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        String token = request.getParameter("token");
//        String secret = request.getParameter("secret");
//        String accountID = request.getParameter("accountID");
//
//        JSONObject decryptedData;
//        // TODO: Build decrypter() that returns a JSONObject
//        decryptedData = decrypt(token, secret, accountID);
//
//
//        SignatureBuilder listParticipants = new SignatureBuilder(decryptedData.getString("keyword"),
//                decryptedData.getString("token"), decryptedData.getString("secret"),
//                decryptedData.getString("urlEndPoint"));

        SignatureBuilder listParticipants = new SignatureBuilder("GET", request.getParameter("token"),
                request.getParameter("secret"), "/v1/programs/" + request.getParameter("accountID") + "/participants");

        try {
            listParticipants.makeGetRequest();
        } finally {
            if (listParticipants.getStatus() == 200 || listParticipants.getStatus() == 202) {
                PrintWriter out = response.getWriter();
                out.print(listParticipants.successfulRequest());
            } else {
                PrintWriter out = response.getWriter();
                out.print(listParticipants.unsuccessfulRequest());
            }
        }
    }
}
