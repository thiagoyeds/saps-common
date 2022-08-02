/* (C)2020 */
package saps.common.core.storage.permanent;

public enum PermanentStorageType {
  NFS("nfs"),
  SWIFT("swift");

  private final String type;

  PermanentStorageType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
