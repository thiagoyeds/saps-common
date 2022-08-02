/* (C)2020 */
package saps.common.core.storage.temporary;

public enum TemporaryStorageType {
  NFS("nfs"),
  SWIFT("swift");

  private final String type;

  TemporaryStorageType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}

