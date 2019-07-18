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


@WebServlet("/upsertParticipants")
public class UpsertParticipants extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private JSONObject decrypt(String token, String secret, String program) {
        JSONObject data = new JSONObject();
        data.put("keyword", "POST");
        data.put("token", token);
        data.put("secret", secret);
        data.put("urlEndPoint", "/v2/programs/" + program + "/participants");
        return data;
    }

    public UpsertParticipants() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        String secret = request.getParameter("secret");
        String program = request.getParameter("programID");

        JSONObject decryptedData;
        // TODO: Build full decrypter() that returns a JSONObject
        decryptedData = decrypt(token, secret, program);


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
