import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * 按 QQ Webhook 的种子派生规则对事件体做 Ed25519 签名（与 QqWebhookVerifier 完全一致）。
 *
 * <p>用法（JDK 单文件运行）：{@code java tools/e2e/SignEvent.java <clientSecret> <bodyFile>}
 */
public final class SignEvent {

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("用法: SignEvent <clientSecret> <bodyFile>");
      System.exit(2);
    }
    byte[] seed = args[0].getBytes(StandardCharsets.UTF_8);
    if (seed.length == 0) {
      throw new IllegalArgumentException("clientSecret 不能为空");
    }
    while (seed.length < 32) {
      byte[] doubled = Arrays.copyOf(seed, seed.length * 2);
      System.arraycopy(seed, 0, doubled, seed.length, seed.length);
      seed = doubled;
    }
    seed = Arrays.copyOf(seed, 32);

    PrivateKey privateKey =
        KeyFactory.getInstance("Ed25519")
            .generatePrivate(new EdECPrivateKeySpec(NamedParameterSpec.ED25519, seed));
    Signature signature = Signature.getInstance("Ed25519");
    signature.initSign(privateKey);
    signature.update(Files.readAllBytes(Path.of(args[1])));
    System.out.println(HexFormat.of().formatHex(signature.sign()));
  }
}
