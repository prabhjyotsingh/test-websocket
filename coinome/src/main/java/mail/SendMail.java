package mail;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class SendMail {

    public enum SmtpTransport {
        SMTP,
        SMTPS
    }

    public void sendMail(String message) {
        Properties prop = null;
        try {
            prop = loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean useSmtpAuth = false;
        useSmtpAuth = true;
        String port = prop.getProperty("smtp.port", "25");
        String host = prop.getProperty("smtp.host", "netiqmta.netiq.com");

        Properties smtpProps;
        boolean useSsl = Boolean.parseBoolean(prop.getProperty("smtp.useSsl",
                "false"));
        if (useSsl) {
            smtpProps = getTlsSmtpProperties(host, port, useSmtpAuth, useSsl);
        } else {
            smtpProps = getSmtpProperties(host, port, useSmtpAuth, useSsl);
        }

        Session session;
        session = Session.getInstance(smtpProps);
        session.setDebug(true);
        debugSession(session);

        MimeMessage msg = null;
        try {
            msg = getMessage(message, session);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SmtpTransport st;
        if (useSsl) {
            st = SmtpTransport.SMTPS;
        } else {
            st = SmtpTransport.SMTP;
        }


        try {
            send(session, prop, msg, st, useSmtpAuth);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Properties getSmtpProperties(String host, String port,
                                               boolean useSmtpAuth, boolean useSsl) {
        Properties props = new Properties();
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.debug", "true");
        if (useSmtpAuth) {
            props.put("mail.smtp.auth", "true");
        }
        if (useSsl) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.starttls.enable", "true");
        }
        return props;
    }

    public static Properties getTlsSmtpProperties(String host, String port,
                                                  boolean useSmtpAuth, boolean useSsl) {
        Properties tlsProps = new Properties();

        // If you intend to use SSL/TLS, you will want to set the transport to
        // "smtps" and use the mail.smtps.xxx properties.
        tlsProps.put("mail.smtps.port", port);
        tlsProps.put("mail.smtps.host", host);
        tlsProps.put("mail.smtps.debug", "true");
        if (useSmtpAuth) {
            tlsProps.put("mail.smtps.auth", "true");
        }
        if (useSsl) {
            tlsProps.put("mail.smtps.ssl.enable", "true");
            tlsProps.put("mail.smtps.starttls.enable", "true");
        }
        return tlsProps;
    }

    public static Properties loadProperties() throws Exception {
        Properties prop = new Properties();
        prop.load(new FileInputStream("test/test.properties"));
        return prop;
    }

    public static void send(Session session, Properties props, MimeMessage msg,
                            SmtpTransport st, boolean useSmtpAuth) throws Exception {
        String host = props.getProperty("smtp.host");
        Transport transport;
        if (st.equals(SmtpTransport.SMTPS)) {
            transport = session.getTransport("smtps");
            System.out.println("Using smtps transport");
        } else {
            transport = session.getTransport("smtp");
            System.out.println("Using smtp transport");
        }

        final String user = props.getProperty("smtp.user");
        final String password = props.getProperty("smtp.pass");
        if (password == null) {
            System.out.println("We didn't get a password!");
        }

        try {
            if (useSmtpAuth) {
                System.out.println("Connecting to " + host + " as " + user);
                transport.connect(host, user, password);
            } else {
                transport.connect();
            }
            transport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Message sent");
        } finally {
            transport.close();
            System.out.println("Connection to " + host + " closed");
        }
    }

    private static void debugSession(Session session) {
        Properties sessionProps = session.getProperties();
        sessionProps.list(System.out);
    }

    private static Session getAuthenticatedSession(Properties smtpProps,
                                                   String pass) throws Exception {
        Properties prop = loadProperties();
        final String username = prop.getProperty("smtp.user");
        final String password = pass;

        System.out.println("Using session authenticator with user: " + username);
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        return Session.getInstance(smtpProps, auth);
    }

    private static MimeMessage getMessage(String text, Session session)
            throws Exception {
        MimeMessage msg = new MimeMessage(session);

        Properties prop = loadProperties();
        String subject = prop.getProperty("smtp.subject", "Test subject");
        String fromEmail = prop.getProperty("smtp.from");
        String recipients = prop.getProperty("smtp.to");
        List<String> recipientList;
        recipientList = Arrays.asList(recipients.split(","));
        for (String to : recipientList) {
            msg.addRecipients(RecipientType.TO, to);
        }

        msg.setFrom(new InternetAddress(fromEmail, false));
        msg.setSubject(subject, "UTF-8");

        MimeMultipart mp = new MimeMultipart();
        MimeBodyPart partText = new MimeBodyPart();
        partText.setText(text);
        mp.addBodyPart(partText);
        msg.setContent(mp);
        return msg;
    }
}
