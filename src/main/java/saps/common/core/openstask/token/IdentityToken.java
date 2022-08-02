/* (C)2020 */
package saps.common.core.openstask.token;

public class IdentityToken {

  private final String accessId;
  private final String issuedAt;
  private final String expiresAt;

  public IdentityToken(String accessId, String issuedAt, String expiresAt) {
    this.accessId = accessId;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
  }

  public String getAccessId() {
    return accessId;
  }

  public String issuedAt() {
    return issuedAt;
  }

  public String expiresAt() {
    return expiresAt;
  }
}
