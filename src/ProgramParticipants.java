import org.json.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/*
 *  PROGRAM PARTICIPANTS
 *  This servlet will build the request to list all programs that an account is connected with.
 *  Documentation shows that the request will be:
 *  GET https://theseus-api.signalvine.com/v1/programs/<program id>/participants
 */

@WebServlet("/programParticipants")
public class ProgramParticipants extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public ProgramParticipants() {
        super();
    }

    /*
     *  DO GET
     *  INPUT: request and response parameters inherited from HttpServlet
     *  DESCRIPTION: Performs the received request. Takes in the parameters from GET, assigns them to variables that
     *  can be used in api, decrypts and builds JSON object that will be used for request to SV, makes request, and
     *  displays new page based on request status.
     *  OUTPUT: HTML page to display.
     *  NOTE: There is a lot of stuff commented out. This is intentional. Some of the commented out code could be used
     *  for future additional functions.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        SignatureBuilder programParticipants = new SignatureBuilder("GET",
                request.getParameter("token"), request.getParameter("secret"),
                "/v1/programs/" + request.getParameter("programID") + "/participants");

        try {
            programParticipants.makeRequest();
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
