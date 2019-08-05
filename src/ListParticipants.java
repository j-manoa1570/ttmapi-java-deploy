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

    public ListParticipants() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        SignatureBuilder listParticipants = new SignatureBuilder("GET", request.getParameter("token"),
                request.getParameter("secret"), "/v1/programs/" + request.getParameter("accountID") + "/participants");

        try {
            listParticipants.makeRequest();
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
