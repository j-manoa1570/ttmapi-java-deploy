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

    public ListPrograms() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        SignatureBuilder listPrograms = new SignatureBuilder("GET", request.getParameter("token"),request.getParameter("secret"),
                "/v1/accounts/" + request.getParameter("accountID") + "/programs");

        try {
            listPrograms.makeRequest();
        } finally {
            if (listPrograms.getStatus() == 200 || listPrograms.getStatus() == 202) {
                PrintWriter out = response.getWriter();
                out.print(listPrograms.successfulRequest());
            } else {
                PrintWriter out = response.getWriter();
                out.print(listPrograms.unsuccessfulRequest());
            }
        }
    }
}
