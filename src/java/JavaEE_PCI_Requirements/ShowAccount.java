/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JavaEE_PCI_Requirements;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// DB resources
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.RequestDispatcher;
import org.apache.derby.jdbc.ClientDataSource;

/**
 *
 * @author ATG8
 */
public final class ShowAccount extends HttpServlet {

    // Variable
    private HttpSession session;
    // Database field data
    private int user_id;
    private String Cardholdername;
    private String CardType;
    private String ServiceCode;
    private String CardNumber;
    private Date expiredate;

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

        session = request.getSession(true);
        if (session.getAttribute("UMUCUserEmail") == null) {
            // Send back to login page 
            response.sendRedirect("login.jsp");
        } else {
            // Connect to the Database and pull the data
            getData();
            
            // Set the Attribute for viewing in the JSP
            request.setAttribute("Cardholdername", Cardholdername);
            request.setAttribute("CardType", CardType);
            request.setAttribute("ServiceCode", ServiceCode);
            request.setAttribute("CardNumber", CardNumber);
            request.setAttribute("expiredate", expiredate);
            
            RequestDispatcher dispatcher = request.getRequestDispatcher("account.jsp");
            dispatcher.forward(request, response);       
            
  
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
        processRequest(request, response);
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

    public void getData() {

        try {
            ClientDataSource ds = new ClientDataSource();
            ds.setDatabaseName("SDEV425");
            ds.setServerName("localhost");
            ds.setPortNumber(1527);
            ds.setUser("sdev425");
            ds.setPassword("Cisforc00kiethat'sgoodenoughforme!");
            ds.setDataSourceName("jdbc:derby");

            Connection conn = ds.getConnection();

            //Declare and set prepared statement
            String sql = "select user_id,Cardholdername, Cardtype,"
                    + "ServiceCode, CardNumber,expiredate"
                    + " from customeraccount  where user_id = ?";
            PreparedStatement prepStmt = conn.prepareStatement(sql);
            //Bind value
            prepStmt.setInt(1, (int) session.getAttribute("UMUCUserID"));
            
            ResultSet rs = prepStmt.executeQuery();
            // Assign values
            while (rs.next()) {
                user_id = rs.getInt(1);
                Cardholdername = rs.getString(2);
                CardType = rs.getString(3);
                ServiceCode = rs.getString(4);
                CardNumber = rs.getString(5);
                expiredate = rs.getDate(6);
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

}
