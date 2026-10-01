package top.stillmisty.qqgateway;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class QqWebhookVerifierTest {

  /** RFC 8032 §7.1 TEST 2 向量。 */
  private static final byte[] RFC_SEED =
      HexFormat.of().parseHex("4ccd089b28ff96da9db6c346ec114e0f5b8a319f35aba624da8cf6ed4fb8a6fb");

  private static final byte[] RFC_MESSAGE = HexFormat.of().parseHex("72");

  private static final String RFC_SIGNATURE =
      "92a009a9f0d4cab8720e820b5f642540a2b27b5416503f8fb3762223ebdb69da"
          + "085ac1e43e15996e458f3613d0f11d8c387b2eaeb4302aeeb00d291612bb0c00";

  @Test
  void matchesRfc8032TestVector() {
    QqWebhookVerifier verifier = QqWebhookVerifier.fromSeed(RFC_SEED);
    assertTrue(verifier.verify(RFC_SIGNATURE, RFC_MESSAGE));
  }

  @Test
  void rejectsTamperedMessageOrSignature() {
    QqWebhookVerifier verifier = QqWebhookVerifier.fromSeed(RFC_SEED);
    byte[] tamperedMessage = RFC_MESSAGE.clone();
    tamperedMessage[0] ^= 0x01;
    assertFalse(verifier.verify(RFC_SIGNATURE, tamperedMessage));
    String tamperedSignature = "00" + RFC_SIGNATURE.substring(2);
    assertFalse(verifier.verify(tamperedSignature, RFC_MESSAGE));
  }

  @Test
  void rejectsMalformedHexAndBlankSignature() {
    QqWebhookVerifier verifier = new QqWebhookVerifier("secret");
    assertFalse(verifier.verify("not-hex", RFC_MESSAGE));
    assertFalse(verifier.verify("  ", RFC_MESSAGE));
  }

  @Test
  void challengeSignatureIsVerifiableAndStable() {
    QqWebhookVerifier verifier = new QqWebhookVerifier("secret");
    String first = verifier.signChallenge("plain-token", "1725442341");
    String second = verifier.signChallenge("plain-token", "1725442341");
    assertEquals(first, second);
    assertTrue(verifier.verify(first, "plain-token1725442341".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void seedDerivationRepeatsSecretTo32Bytes() {
    byte[] shortSeed = QqWebhookVerifier.seedFromSecret("abc");
    assertEquals(32, shortSeed.length);
    assertEquals("abcabcabcabcabcabcabcabcabcabcab", new String(shortSeed, StandardCharsets.UTF_8));

    byte[] exact = new String(new char[32]).replace('\0', 'x').getBytes(StandardCharsets.UTF_8);
    assertArrayEquals(
        exact, QqWebhookVerifier.seedFromSecret(new String(exact, StandardCharsets.UTF_8)));
  }
}
