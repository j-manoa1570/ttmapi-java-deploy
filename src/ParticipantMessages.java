import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/*
 *  PARTICIPANT MESSAGES
 *  This servlet will build the request to list all programs that an participant is connected with.
 *  Documentation shows that the request will be:
 *  GET https://theseus-api.signalvine.com/v1/participants/<participant id>/messages
 *
 */


@WebServlet("/participantMessages")
public class ParticipantMessages extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public ParticipantMessages() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        SignatureBuilder participantMessages = new SignatureBuilder("GET", request.getParameter("token"),
                request.getParameter("secret"), "/v1/participants/" + request.getParameter("participantID") + "/messages");

        try {
            participantMessages.makeRequest();
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
