

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

class dataPost {

//    /*
//     *  MAKE REQUEST
//     *  INPUT: urlEndPoint, keyword, authorization, timeStamp
//     *  DESCRIPTION: Creates a new request to be sent to SV API.
//     *  OUTPUT:
//     */
//    private static void makeRequest(String urlEndPoint, String keyword,
//                                    String authorization, String timeStamp, String outputObject) {
//        String url = "https://theseus-api.signalvine.com" + urlEndPoint;
//
//        try {
//            // Tries to create a new URL object
//            URL hostSite = new URL(url);
//            // Attempts url by first creating a url object (HttpURLConnection)
//            HttpURLConnection con = (HttpURLConnection) hostSite.openConnection();
//            try {
//
//                /**
//                 * Still not working I think it is the data format that is not working correctly in here.
//                 * make sure that the data being written is correct in either the authorization or the post body
//                 */
//
//                // Sets parameters (url type and header)
//                con.setRequestMethod(keyword);
//                con.setConnectTimeout(50000);
//                con.setReadTimeout(50000);
//                con.setDoOutput(true);
//                con.setRequestProperty("Content-Type", "application/json");
//                con.setRequestProperty("Authorization", authorization);
//                con.setRequestProperty("SignalVine-Date", timeStamp);
//
//                System.out.println("STEP 5: Built Request Statement");
//
//                /*
//                 * STEP 6 make connection
//                 * */
//                con.connect();
//
////                System.out.println(outputObject);
//                //Send data to the api
//                OutputStream out = con.getOutputStream();
//                byte[] input = outputObject.getBytes("utf-8");
//                out.write(input, 0, input.length);// here i sent the parameter
//                out.close();
//
//                // Prepares status variable and print it
//                int status = con.getResponseCode();
//                System.out.println("STEP 6: " + status);
//                if(status == con.HTTP_ACCEPTED || status == con.HTTP_OK) {
//                    // Read in response
//                    BufferedReader in = new BufferedReader(
//                            new InputStreamReader(con.getInputStream()));
//                    String inputLine;
//                    StringBuffer content = new StringBuffer();
//
//                    while ((inputLine = in.readLine()) != null) content.append(inputLine);
//                    in.close();
//                    System.out.println(content);
//                }
//
//
//            }
//            finally {
//                con.disconnect();
//            }
//
//
//        }
//        catch (MalformedURLException malformed) {
//            System.out.println(malformed);
//        }catch (IOException io) {
//            System.out.println(io);
//        }
//    }

//    /*
//     *  MAIN
//     *  Runs things and used for jar builds.
//     */
//    public static void main(String[] args) throws IOException {
//
//        if (args.length > 0) {
//            System.out.println("Received command line arguments!");
//            String secret = args[0];
//            String token = args[1];
//            System.out.println("Secret variable: " + secret);
//            System.out.println("Token variable: " + token);
//        } else {
//            //Enter data using BufferReader
//            BufferedReader reader =
//                    new BufferedReader(new InputStreamReader(System.in));
//
//            System.out.print("Your secret: ");
//            String secret = reader.readLine();
//            System.out.print("Your token: ");
//            String token = reader.readLine();
//            System.out.print("Your keyword: ");
//            String keyword = reader.readLine();
//            System.out.print("Your programID: ");
//            String programID = reader.readLine();
//            System.out.print("Your endpoint: ");
//            String urlEndPoint = reader.readLine();
//
//            SignatureBuilder signature;
//
//            if (keyword.equals("POST")) {
//                List<String> participants;
//                participants = new LinkedList<>();
//                System.out.print("Your fields: ");
//                String fields = reader.readLine();
//                String data;
//
//                do {
//                    System.out.print("Participant: ");
//                    data = reader.readLine();
//                    if (!data.equals(""))
//                        participants.add(data);
//                } while (!data.equals(""));
//                participants.add(0, fields);
//
//                System.out.println("STEP 1 Gather data: Gathered all necessary data");
//                signature = new SignatureBuilder(keyword, token, secret, urlEndPoint, participants.toString(), programID);
//            } else {
//                System.out.println("STEP 1 Gather data: Gathered all necessary data");
//                signature = new SignatureBuilder(keyword, token, secret, urlEndPoint, "", programID);
//            }
//
////        /*
////        * STEP 4 Create the Signature
////        * */
////        String signature = stringBuilderForPost(token,keyword,urlEndPoint,testStr,ts);
//            System.out.println("STEP 4 unencrypted:\n" + signature.getSignature());
//            System.out.println("STEP 4 encrypted: " + signature.getEncryptedSignature());
//            String tokenEncryptedSignature = "SignalVine " + token + ":" + signature.getEncryptedSignature();
////
////        /*
////        * STEP 5 Make request
////        * */
//            makeRequest(signature.getUrlEndPoint(), signature.getKeyword(), tokenEncryptedSignature, signature.getTimeStamp(),
//                    signature.getBody());
//        }
//    }



    // TODO: Each one of these functions needs to be its own servlet and have its own class
    // Participant messages is done too!
//    public void listPrograms(String something) {
//
//    }

//    public void programParticipants(String programID) {
//
//    }

    public void upsertParticipants(String programID) {

    }

//    public void listParticipants(String accountID) {
//
//    }

    public void particpantMessages(String participantID) {

    }
}
