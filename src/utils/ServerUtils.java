package utils;

import static external.IdVerification.verifyGoogleIdToken;

import entity.User;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class ServerUtils {

  private static final String ID_TOKEN_PARAMETER = "idToken";

  public static void writeJSONObject(HttpServletResponse response, JSONObject obj) {
    try {
      response.setContentType("application/json");
      response.addHeader("Access-Control-Allow-Origin", "*");

      PrintWriter out = response.getWriter();

      out.print(obj);
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void writeJSONArray(HttpServletResponse response, JSONArray array) {
    try {
      response.setContentType("application/json");
      response.addHeader("Access-Control-Allow-Origin", "*");

      PrintWriter out = response.getWriter();

      out.print(array);
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static JSONObject readJsonObject(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();
    try {
      BufferedReader reader = request.getReader();
      String line = null;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      reader.close();
      return new JSONObject(sb.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Optional<User> authenticateUser(HttpServletRequest request) {
    String idToken = findCookie(request.getCookies(), ID_TOKEN_PARAMETER);

    if (idToken != null) {
      return verifyGoogleIdToken(idToken);
    }
    return Optional.empty();
  }

  @Nullable
  private static String findCookie(Cookie[] cookies, String name) {
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(name)) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
