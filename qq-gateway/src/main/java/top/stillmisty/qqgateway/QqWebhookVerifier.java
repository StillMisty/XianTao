package top.stillmisty.qqgateway;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * Webhook 回调 Ed25519 签名工具。
 *
 * <p>密钥由 AppSecret 派生：将密钥字节重复拼接至不少于 32 字节后截取前 32 字节作为 Ed25519 种子（与官方文档的 Go 示例一致）。Ed25519
 * 签名是确定性的，因此「校验」等价于用同一密钥重算签名后做常量时间比较， 不需要单独导出公钥。
 *
 * <p>本类线程安全。
 */
public final class QqWebhookVerifier {

  private static final int ED25519_SEED_SIZE = 32;

  private final PrivateKey privateKey;

  public QqWebhookVerifier(String clientSecret) {
    this(seedFromSecret(clientSecret));
  }

  /** 直接从 Ed25519 种子构造（测试用，校验算法的 RFC 8032 一致性）。 */
  static QqWebhookVerifier fromSeed(byte[] seed) {
    return new QqWebhookVerifier(seed.clone());
  }

  private QqWebhookVerifier(byte[] seed) {
    this.privateKey = derivePrivateKey(seed);
  }

  /**
   * 校验回调请求签名。
   *
   * @param signatureHex 请求头 {@code X-Signature-Ed25519} 的十六进制值
   * @param rawBody 原始请求体字节（必须与平台发送的完全一致）
   * @return 签名是否有效
   */
  public boolean verify(String signatureHex, byte[] rawBody) {
    if (signatureHex.isBlank()) {
      return false;
    }
    byte[] provided;
    try {
      provided = HexFormat.of().parseHex(signatureHex.trim());
    } catch (IllegalArgumentException e) {
      return false;
    }
    return MessageDigest.isEqual(sign(rawBody), provided);
  }

  /** 对任意字节串签名并返回十六进制（测试与 op 13 应答共用）。 */
  String signHex(byte[] message) {
    return HexFormat.of().formatHex(sign(message));
  }

  /**
   * 生成回调地址验证（op 13）的签名响应。
   *
   * @param plainToken 平台下发的一次性 token
   * @param eventTs 平台下发的事件时间戳（字符串形式）
   * @return 十六进制签名值
   */
  public String signChallenge(String plainToken, String eventTs) {
    return signHex((plainToken + eventTs).getBytes(StandardCharsets.UTF_8));
  }

  private byte[] sign(byte[] message) {
    try {
      Signature signature = Signature.getInstance("Ed25519");
      signature.initSign(privateKey);
      signature.update(message);
      return signature.sign();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Ed25519 签名失败", e);
    }
  }

  private static PrivateKey derivePrivateKey(byte[] seed) {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
      return keyFactory.generatePrivate(new EdECPrivateKeySpec(NamedParameterSpec.ED25519, seed));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("初始化 Ed25519 密钥失败", e);
    }
  }

  /** 官方文档的种子派生：重复拼接至 >= 32 字节后取前 32 字节。 */
  static byte[] seedFromSecret(String clientSecret) {
    byte[] seed = clientSecret.getBytes(StandardCharsets.UTF_8);
    if (seed.length == 0) {
      throw new IllegalArgumentException("clientSecret 不能为空");
    }
    while (seed.length < ED25519_SEED_SIZE) {
      seed = Arrays.copyOf(seed, seed.length * 2);
      System.arraycopy(seed, 0, seed, seed.length / 2, seed.length / 2);
    }
    return Arrays.copyOf(seed, ED25519_SEED_SIZE);
  }
}
