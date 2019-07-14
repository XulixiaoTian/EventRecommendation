package server;

import static utils.ServerUtils.authenticateUser;

import entity.User;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mongodb.MongoDBConnection;

@WebServlet(name = "login")
public class Login extends HttpServlet {

  private final MongoDBConnection mongoDBConnection = new MongoDBConnection();

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Optional<User> user = authenticateUser(request);
    if (user.isPresent()) {
      System.out.println("Successful login user " + user.get().email());
      mongoDBConnection.upsertUser(user.get());
      response.setStatus(200);
      return;
    }

    System.out.println("No cookie found for the request.");
    response.setStatus(403);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setStatus(404);
  }


}
