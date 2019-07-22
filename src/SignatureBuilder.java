import org.json.JSONObject;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SignatureBuilder {

    private String keyword;
    private String token;
    private String secret;
    private String urlEndPoint;
    private String body;
    private String timeStamp;
    private String programID;
    private String requestType;
    private String everything;
    private String signature;
    private String encryptedSignature;
    private String authorization;
    private int status = 9000;
    private String responseBody;
    private String data;
    // Arrays that are used when formatting strings
//    private String[] badbody = new String[] {"[","]",", "," ","/_"};
//    private String[] goodbody = new String[] {"","","\n",",", " "};

    private String[] badbody = new String[] {", ","][","[","]"};
    private String[] goodbody = new String[] {",","\n","",""};

    /*
     *  Constructor for building a GET signature
     */
    SignatureBuilder(String keyword, String token, String secret, String urlEndPoint) {
        initializeEverything(keyword, token, secret, urlEndPoint, "");
    }

    /*
     *  Constructor for building a POST signature
     */
    SignatureBuilder(String keyword, String token, String secret, String urlEndPoint, String body, String programID, String data) {
        this.programID = programID;
        this.data = data;
        initializeEverything(keyword, token, secret, urlEndPoint, body);
    }

    private void initializeEverything(String keyword, String token, String secret, String urlEndPoint, String body) {
        this.keyword = keyword;
        this.token = token;
        this.secret = secret;
        this.urlEndPoint = urlEndPoint;
        setTimeStamp();
        if (keyword.equals("POST")) {
            this.body = bodyFormatter(body);
            this.everything = this.body;
            this.body = strStripper(this.body);
            this.body = JSONBuilder();
        } else {
            this.body = "";
        }
        this.signature = sigBuilder();
        this.encryptedSignature = encrypter();
        this.authorization = "SignalVine " + token + ":" + encryptedSignature;
    }

    public String getKeyword() { return keyword; }
    public String getToken() { return token; }
    public String getSecret() { return secret; }
    public String getUrlEndPoint() { return urlEndPoint; }
    public String getBody() { return body; }
    public String getProgramID() { return programID; }
    public String getTimeStamp() { return timeStamp; }
    public String getSignature() { return signature; }
    public String getEncryptedSignature() { return encryptedSignature; }
    public String getAuthorization() { return authorization; }
    public String getResponseBody() {return responseBody; }
    public int getStatus() { return status; }
    public String getEverything() { return everything; }
    public String getData() { return data; }

    public void setKeyword(String keyword) { this.keyword = keyword; }
    public void setToken(String token) { this.token = token; }
    public void setSecret(String secret) { this.secret = secret; }
    public void setUrlEndPoint(String urlEndPoint) { this.urlEndPoint = urlEndPoint; }
    public void setBody(String body) { this.body = body; }
    public void setProgramID(String programID) { this.programID = programID; }
    public void setSignature() { this.signature = sigBuilder(); }
    public void setEncryptedSignature() { this.encryptedSignature = encrypter(); }
    public void setAuthorization() { this.authorization = "SignalVine " + token + ":" + encryptedSignature; }
    public void setStatus(int status) { this.status = status; }

    /*
     *  SET TIME STAMP
     *  Sets time stamp to the current time formatted as ISO-8601 as required by SV.
     *  INPUT: None
     *  DESCRIPTION: Uses java.time.* to get the local timestamp and then formats into SV
     *  format.
     *  OUTPUT: None but sets object timestamp variable.
     */
    public void setTimeStamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SSS");
        this.timeStamp = formatter.format(LocalDateTime.now());
        this.timeStamp = this.timeStamp.replace(" ", "T");
        this.timeStamp = this.timeStamp + "Z";
    }

    /*
     *  STRING BUILDER
     *  INPUT: token, keyword, urlEndPoint, body, timeStamp.
     *  Method builds the string that will be the unencrypted signature. A
     *  "\n" is added between each field and is required.
     *  Builds the string in the following lowercase form:
     *  token
     *  keyword
     *  urlEndPoint
     *  body
     *  timeStamp
     *
     *  EXAMPLE:
     *  1468574625
     *  post
     *  /v2/programs/91e54ce0-56b9-4582-8078-4963cf01f649/participants
     *  {"options":{"mode":"row","new":"add","existing":"ignore","absent":"ignore"},"program":"91e54ce0-56b9-4582-8078-4963cf01f649","participants":"phone,first_name,school,last_name_student,first_name_student,last_name\n(805)345-5161,the,xavier's school for gifted children,wolverine,the,wolverine"}
     *  2019-06-28t02:25:05.029z
     *
     *  OUTPUT: signature
     *  This is a string that is ready to be encrypted.
     */
    private String sigBuilder() {
        return token.toLowerCase() + "\n" + keyword.toLowerCase() +
                "\n" + urlEndPoint.toLowerCase() + "\n" +
                body.toLowerCase() + "\n" + timeStamp.toLowerCase();
    }

    /*
     *  ENCRYPTER
     *  INPUT: signature, secret
     *  DESCRIPTION: Using SHA256, the signature is encrypted with the secret
     *  that is associated with a valid user (me) and encodes the new
     *  encrypted signature as base 64.
     *  OUTPUT: encryptedSignature
     */
    private String encrypter() {
        String encryptedSignature = "";
        try
        {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            Base64.Encoder encoder = Base64.getEncoder();
            encryptedSignature = encoder.encodeToString(sha256_HMAC.doFinal(signature.getBytes()));
        }
        catch (Exception error) {
            System.out.println("ERROR: Could not encrypt message!!!");
        }
        return encryptedSignature;
    }

    /*
     *  STRING STRIPPER
     *  INPUT: toStrip, badSubStr (array of bad substrings), goodSubStr (array of good substrings)
     *  DESCRIPTION: Feed it a string and two arrays(bad substrings and good substrings) and it loops
     *  through and removes the bad one and replaces it with the good one. NOTE: arrays must be of
     *  same length.
     *  OUTPUT: A cleaned up string.
     * */
    private String strStripper (String toStrip) {

        for (int i = 0; i < badbody.length; i++) toStrip = toStrip.replace(badbody[i], goodbody[i]);

        return toStrip;
    }

    /*
     *  JSON BUILDER
     *  INPUT: None
     *  DESCRIPTION: Creates a JSON object to use for passing a properly formatted body to send to SV. Creates
     *  a JSONObject for first building the options. Another JSONObject is create to store the JSON body that
     *  will be sent. This is designed this way since the options object is a sub JSON object to the JSON body.
     *  OUTPUT: A string that is formatted and can be sent to SV with no problems.
     */
    private String JSONBuilder() {
        String heading = body.substring(0,body.indexOf("\n"));
        heading = heading.replace(",", "\",\"");
        heading = "[\"" + heading + "\"]";

        JSONObject options = new JSONObject();
        options.put("new", "ignore");
        options.put("mode", "row");
        options.put("absent", "ignore");

        JSONObject apiObject = new JSONObject();
        apiObject.put("program", programID);
        apiObject.put("options", options);
        apiObject.put("participants", body);
        System.out.println("STEP 2 JSONObject: " + apiObject);

        String testStr = apiObject.toString();
        testStr = testStr.replace("\"absent\":\"ignore\"", "\"absent\":\"ignore\",\"existing\":" + heading);
        System.out.println("STEP 3 JSON as a string:" + testStr);

        return testStr;
    }

    /*
     *  BODY FORMATTER
     *  INPUT: JSON formatted string
     *  DESCRIPTION: Takes a JSON string, splits up the field types and field values into separate lists, and
     *               returns a string version of the two lists.
     *  OUTPUT: String
     */
    private String bodyFormatter(String bodyParam) {
        String body = "{" + bodyParam + "}";

        JSONObject bodyData = new JSONObject(body);
        Iterator keys = bodyData.keys();
        List<String> fieldTypes = new LinkedList<String>();
        List<String> fieldValues = new LinkedList<String>();

        while (keys.hasNext()) {
            String dynamicKey = (String) keys.next();
            if (!dynamicKey.equals("_id") && !dynamicKey.equals("_createdDate") && !dynamicKey.equals("_updatedDate")) {
                fieldTypes.add(dynamicKey);
                fieldValues.add(bodyData.getString(dynamicKey));
            }
        }
        return new String(fieldTypes.toString() + fieldValues.toString());
    }

    /*
     *  MAKE GET REQUEST
     *  INPUT:
     *  DESCRIPTION:
     *  OUTPUT:
     */
    public void makeGetRequest() {

        String url = "https://theseus-api.signalvine.com" + urlEndPoint;

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            try {
            // optional default is GET
                //con.setRequestMethod(keyword);
                con.setConnectTimeout(50000);
                con.setReadTimeout(50000);
//                con.setDoOutput(true);
                con.setRequestProperty("Authorization", authorization);
                con.setRequestProperty("SignalVine-Date", timeStamp);

                status = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + status);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();
                System.out.println(response);
                responseBody = response.toString();
            }
            finally {
                con.disconnect();
            }
        } catch (MalformedURLException malformed) {
            System.out.println(malformed);
        } catch (IOException io) {
            System.out.println(io);
        }
    }

    /*
     *  MAKE REQUEST
     *  INPUT: urlEndPoint, keyword, authorization, timeStamp
     *  DESCRIPTION: Creates a new request to be sent to SV API.
     *  OUTPUT:
     */
    public void makePostRequest()  {
        String url = "https://theseus-api.signalvine.com" + urlEndPoint;

        try {
            // Tries to create a new URL object
            URL hostSite = new URL(url);
            // Attempts url by first creating a url object (HttpURLConnection)
            HttpURLConnection con = (HttpURLConnection) hostSite.openConnection();
            try {

                /**
                 * Still not working I think it is the data format that is not working correctly in here.
                 * make sure that the data being written is correct in either the authorization or the post body
                 */

                // Sets parameters (url type and header)
                con.setRequestMethod(keyword);
                con.setConnectTimeout(50000);
                con.setReadTimeout(50000);
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", authorization);
                con.setRequestProperty("SignalVine-Date", timeStamp);

                System.out.println("STEP 5: Built Request Statement");

                /*
                 * STEP 6 make connection
                 * */
                con.connect();

//                System.out.println(outputObject);
                //Send data to the api
                OutputStream out = con.getOutputStream();
                byte[] input = body.getBytes("utf-8");
                out.write(input, 0, input.length);// here i sent the parameter
                out.close();

                // Prepares status variable and print it
                status = con.getResponseCode();
                System.out.println("STEP 6: " + status);
                if(status == con.HTTP_ACCEPTED || status == con.HTTP_OK) {
                    // Read in response
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) content.append(inputLine);
                    in.close();
                    System.out.println(content);
                    responseBody = content.toString();
                }
            }
            finally {
                con.disconnect();
            }


        }
        catch (MalformedURLException malformed) {
            System.out.println(malformed);
        }catch (IOException io) {
            System.out.println(io);
        }
    }

    /*
     *  SUCCESSFUL REQUEST
     *  INPUT: None
     *  DESCRIPTION: Returns success string
     *  OUTPUT: Success string
     */
    public String successfulRequest() {
        return "<html><body><h1 align='center'>Your request was successful!</h1></body></html>";
    }

    /*
     *  UNSUCCESSFUL REQUEST
     *  INPUT: None
     *  DESCRIPTION: Outputs a string for an unsuccessful request
     *  OUTPUT: String
     */
    public String unsuccessfulRequest() {
        String fail = "<html><body><h1 align='center'>Your request was unsuccessful and returned HTTP Response code "
                + getStatus() + ".</h1><h2>Token Used: " + getToken() + "</h2><h2>Secret Used: "
                + getSecret() + "</h2><h2>Keyword Used: " + getKeyword() + "</h2><h2>Endpoint Used: "
                + getUrlEndPoint();

        if (keyword.equals("POST")) {
            fail = fail + "</h2><h2>Body Param Used: " + getData() + "</h2><h2>Body Used: " + getBody() + "</h2><h2>Participant Data Used: " + getEverything();
        }

        fail = fail + "</h2><h2>Timestamp Used: " + getTimeStamp() + "</h2><h2>Signature Used: "
                + getSignature() + "</h2><h2>Encrypted Signature Used: " + getEncryptedSignature() +
                "</h2><h2>Authorization Used: " + getAuthorization() + "</h2><h2>Response received: "
                + getResponseBody() + "</h2></body></html>";

        return fail;
    }


}
