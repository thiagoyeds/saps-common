package saps.common.core.openstask.swift;

import java.io.File;
import java.util.List;

public interface SwiftAPI {
    boolean createContainer(String containerName) throws Exception;

    boolean uploadObject(String containerName, File file, String pseudFolder) throws Exception;
    boolean deleteObject(String containerName, String filePath) throws Exception;
    boolean existsObject(String containerName, String basePath, String name) throws Exception;

    List<String> listObjects(String containerName, String dirPath) throws Exception;
    boolean deleteObjects(String containerName, String prefix) throws Exception;
    boolean downloadObjects(String containerName, String prefix) throws Exception;
}
