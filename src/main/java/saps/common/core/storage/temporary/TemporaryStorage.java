/* (C)2020 */
package saps.common.core.storage.temporary;

import saps.common.core.model.SapsImage;

public interface TemporaryStorage {

  boolean prepareToArchive(SapsImage task) throws Exception;

  boolean delete(SapsImage task) throws Exception;
}
