/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JavaEE_PCI_Requirements;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.derby.jdbc.ClientDataSource;

/**
 *
 * @author ATG8
 */
public final class Authenticate extends HttpServlet {

    // variables    
    private String username;
    private String pword;
    private Boolean isValid;
    private int user_id;
    private HttpSession session;
    Timestamp lastLog;
    Timestamp lastAttempt;
    int dbAttempts;
    int failure;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Authenticate</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Authenticate at " + request.getContextPath() + "</h1>");
            out.println("<h1>Results are " + username + "," + isValid + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get the post input 
        this.username = request.getParameter("emailAddress");
        this.pword = request.getParameter("pfield");
        this.isValid = validate(this.username, this.pword);
         response.setContentType("text/html;charset=UTF-8");
        // Set the session variable
        if (isValid) {
            // Create a session object if it is already not  created.
            session = request.getSession(true);
            session.setAttribute("UMUCUserEmail", username);         
            session.setAttribute("UMUCUserID", user_id);
           
            // Send to the Welcome JSP page              
            
            RequestDispatcher dispatcher = request.getRequestDispatcher("welcome.jsp");
            dispatcher.forward(request, response);

        } else {
            // Not a valid login
            // use case to explain why
            try{
                switch(failure){
                    //case 1: account expired at 90 days
                    case 1:
                        request.setAttribute("ErrorMessage", "Account has expired due to inactivity. "
                                    + " Please contact the administrator to reactivate.");
                        RequestDispatcher dispatcher1 = request.getRequestDispatcher("login.jsp");
                        dispatcher1.forward(request, response);
                        break;
                    //case 2: account locked due to excessive login attempts
                    case 2:
                        request.setAttribute("ErrorMessage", "Account locked due to excessive attempts.  Please"
                                + " try again later.");
                        RequestDispatcher dispatcher2 = request.getRequestDispatcher("login.jsp");
                        dispatcher2.forward(request, response);
                        break;
                    //case 3: username and/or password not correct
                    case 3:
                        request.setAttribute("ErrorMessage", "Invalid Username or Password. Try again or contact Jim.");
                        RequestDispatcher dispatcher3 = request.getRequestDispatcher("login.jsp");
                        dispatcher3.forward(request, response);
                }
            }catch(Exception caseErr){
                System.out.println("File IO exception " + caseErr.getMessage());
            }   
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    // Method to Authenticate
    public boolean validate(String name, String pass) {
        boolean status = false;
        int hitcnt=0;
        
        try {
            ClientDataSource ds = new ClientDataSource();
            ds.setDatabaseName("SDEV425");
            ds.setServerName("localhost");
            ds.setPortNumber(1527);
            ds.setUser("sdev425");
            ds.setPassword("Cisforc00kiethat'sgoodenoughforme!");
            ds.setDataSourceName("jdbc:derby");

            //Open connection    
            Connection conn = ds.getConnection();
            
            //Create prepared statement strings to gather numAttempts, lastLog, and lastAttempt
            String qAttempt = "select numAttempt from LoginInfo where user_id = ?";
            String qLastSuc = "select lastSuccess from LoginInfo where user_id = ?";
            String qLastAtt = "select lastAttempt from LoginInfo where user_id = ?";
            
            //Create prepared statements to update LoginInfo table
            String uAttempt = "update LoginInfo set numAttempt = ? where user_id = ?";
            String uLastSuc = "update LoginInfo set lastSuccess = '"
                    + new Timestamp(System.currentTimeMillis()) + "' where user_id = ?";
            String uLastAtt = "update LoginInfo set lastAttempt = '"
                    + new Timestamp(System.currentTimeMillis()) + "' where user_id = ?";
            
            //Declare and set initial prepared statement
            String sql1 = "select user_id from sdev_users where email = ?";
            PreparedStatement prepStmt1 = conn.prepareStatement(sql1);
            //Bind value
            prepStmt1.setString(1, this.username);
            
            ResultSet rs = prepStmt1.executeQuery();
            while (rs.next()) {
                user_id = rs.getInt(1);   
            }
            if (user_id > 0) {
                //check last attempted login, if past 90 days then error
                PreparedStatement prepQLastSuc = conn.prepareStatement(qLastSuc);
                prepQLastSuc.setInt(1, user_id);
                ResultSet rsLastSuc = prepQLastSuc.executeQuery();
                while (rsLastSuc.next()){
                    lastLog = rsLastSuc.getTimestamp(1);
                }
                // If last login is more than 90 days, 
                if((Timestamp.valueOf(LocalDateTime.now().minusDays(90)).after(lastLog))){
                    failure = 1;
                    return status;
                }else{
                    PreparedStatement prepQAttempt = conn.prepareStatement(qAttempt);
                    prepQAttempt.setInt(1, user_id);
                    // get and set attempts
                    ResultSet rsAttempt = prepQAttempt.executeQuery();
                    while (rsAttempt.next()){
                        dbAttempts = rsAttempt.getInt(1);
                    }
                    // get last attempt
                    PreparedStatement prepQLastAtt = conn.prepareStatement(qLastAtt);
                    prepQLastAtt.setInt(1, user_id);
                    ResultSet rsLastAtt = prepQLastAtt.executeQuery();
                    while (rsLastAtt.next()){
                        lastAttempt = rsLastAtt.getTimestamp(1);
                    }
                    
                    // if total attempts more than 6 and account still in lockout
                    if((dbAttempts > 5) && (Timestamp.valueOf(LocalDateTime.now().minusMinutes(30)).before(lastAttempt))){
                        failure = 2;
                        return status;
                    // if total attempts more than 6 and account no longer in lockout
                    }else if((dbAttempts > 5) && 
                            ((Timestamp.valueOf(LocalDateTime.now().minusMinutes(30)).after(lastAttempt)) ||
                                Timestamp.valueOf(LocalDateTime.now().minusMinutes(30)).equals(lastAttempt))){
                        // account no longer in lockout, reset number attempts
                        dbAttempts = 1;
                        PreparedStatement prepUAttempt = conn.prepareStatement(uAttempt);
                        prepUAttempt.setInt(1, dbAttempts);
                        prepUAttempt.setInt(2, user_id);
                        prepUAttempt.executeUpdate();
                        
                        // continue login attempt
                        //Declare and set secondary prepared statement
                        String sql2 = "select user_id from user_info where user_id = ? and password = ?";
                        PreparedStatement prepStmt2 = conn.prepareStatement(sql2);
                        //Bind userid
                        prepStmt2.setInt(1, user_id);

                        //Encrypt password before transmitting
                        String encryptedString;
                        String key = "Poopenheimer1234";
                        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
                        Cipher AesCipher = Cipher.getInstance("AES");
                        byte[] byteText = this.pword.getBytes();
                        Base64.Encoder encoder = Base64.getEncoder();
                        AesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
                        byte[] byteCipherText = AesCipher.doFinal(byteText);
                        encryptedString = encoder.encodeToString(byteCipherText);

                        //Bind encrypted password
                        prepStmt2.setString(2, encryptedString);

                        ResultSet rs2 = prepStmt2.executeQuery();
                        while (rs2.next()) {
                            hitcnt++;
                        }
                    // else account in good standing and less than 6 attempts, continue authenticate
                    }else{
                        //Declare and set secondary prepared statement
                        String sql2 = "select user_id from user_info where user_id = ? and password = ?";
                        PreparedStatement prepStmt2 = conn.prepareStatement(sql2);
                        //Bind userid
                        prepStmt2.setInt(1, user_id);

                        //Encrypt password before transmitting
                        String encryptedString;
                        String key = "Poopenheimer1234";
                        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
                        Cipher AesCipher = Cipher.getInstance("AES");
                        byte[] byteText = this.pword.getBytes();
                        Base64.Encoder encoder = Base64.getEncoder();
                        AesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
                        byte[] byteCipherText = AesCipher.doFinal(byteText);
                        encryptedString = encoder.encodeToString(byteCipherText);

                        //Bind encrypted password
                        prepStmt2.setString(2, encryptedString);

                        ResultSet rs2 = prepStmt2.executeQuery();
                        while (rs2.next()) {
                        hitcnt++;
                        }
                    // end if
                    }
                // else all checks complete, do nothing
                }
            // else account not valid, error 3
            }else{
                failure = 3;
            }
                    
            // Set to true if userid/password match
            if(hitcnt>0){
                status=true;
                PreparedStatement prepULastSuc = conn.prepareStatement(uLastSuc);
                prepULastSuc.setInt(1, user_id);
                prepULastSuc.executeUpdate();
                dbAttempts = 0;
                PreparedStatement prepUAttempt = conn.prepareStatement(uAttempt);
                prepUAttempt.setInt(1, dbAttempts);
                prepUAttempt.setInt(2, user_id);
                prepUAttempt.executeUpdate();
            // Else password incorrect
            }else{
                dbAttempts = ++dbAttempts;
                PreparedStatement prepUAttempt = conn.prepareStatement(uAttempt);
                prepUAttempt.setInt(1, dbAttempts);
                prepUAttempt.setInt(2, user_id);
                prepUAttempt.executeUpdate();
                PreparedStatement prepULastAtt = conn.prepareStatement(uLastAtt);
                prepULastAtt.setInt(1, user_id);
                prepULastAtt.executeUpdate();
                failure = 3;
            }

        } catch (Exception e) {
            System.out.println(e);
            failure = 1;
        }
        return status;
    }
}
