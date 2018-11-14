import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class RestAssuredGetBooking {

    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {

        RestAssuredGetBooking http = new RestAssuredGetBooking();

        System.out.println("\nTesting - Send Http POST request");
        http.sendPost();

    }

    // HTTP POST request
    private void sendPost() throws Exception {

        String url = "https://booking.uz.gov.ua/train_search/";

        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpPost post = new HttpPost(url);

            // add header
            post.setHeader("User-Agent", USER_AGENT);

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("from", "2210700"));
            urlParameters.add(new BasicNameValuePair("to", "2218000"));
            urlParameters.add(new BasicNameValuePair("date", "2018-12-24"));
            urlParameters.add(new BasicNameValuePair("time", "00:00"));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                break;
            }

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            if (!result.toString().contains("По заданому Вами напрямку місць немає")){
                generateAndSendEmail(result);
            }
        } finally {
            client.close();
        }
    }

    private void generateAndSendEmail(StringBuffer content) throws AddressException, MessagingException {
        Properties mailServerProperties;
        Session getMailSession;
        MimeMessage generateMailMessage;

        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");

        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(getMailSession);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress("dmytro.royenko@gmail.com"));
        generateMailMessage.setSubject("Message from booking.uz.gov.ua");
        String emailBody = content.toString();
        generateMailMessage.setContent(emailBody, "text/html");

        Transport transport = getMailSession.getTransport("smtp");

        // Enter your correct gmail UserID and Password
        // if you have 2FA enabled then provide App Specific Password
        transport.connect("smtp.gmail.com", "*******", "******");
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();
    }

}
