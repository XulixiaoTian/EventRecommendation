package external;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import entity.User;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

/** Class containing Google Api Id Token verification logic. */
public class IdVerification {

  private static final String CLIENT_ID = "84513336089-urdl44823416vtdas5bt4i2mqu17s6oc.apps.googleusercontent.com";

  public static Optional<User> verifyGoogleIdToken(String idTokenString) {
    GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
            JacksonFactory.getDefaultInstance())
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

    try {
      GoogleIdToken idToken = verifier.verify(idTokenString);
      if (idToken != null) {
        GoogleIdToken.Payload payload = idToken.getPayload();

        return Optional.of(User.builder()
            .setUserId(payload.getSubject())
            .setEmail(payload.getEmail())
            .setName((String) payload.get("name"))
            .setPictureUrl((String) payload.get("picture"))
            .setFirstName((String) payload.get("family_name"))
            .setLastName((String) payload.get("given_name")).build());

      } else {
        System.out.println("Invalid ID token: " + idTokenString);
      }
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }
}
