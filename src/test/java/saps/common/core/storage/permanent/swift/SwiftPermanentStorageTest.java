/* (C)2020 */
package saps.common.core.storage.permanent.swift;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.StartsWith;
import org.testng.Assert;
import saps.common.core.model.SapsImage;
import saps.common.core.model.enums.ImageTaskState;
import saps.common.core.openstask.swift.SwiftAPIClient;
import saps.common.core.storage.exceptions.InvalidPropertyException;
import saps.common.core.storage.exceptions.TaskNotFoundException;
import saps.common.core.storage.permanent.AccessLink;
import saps.common.core.storage.permanent.PermanentStorage;
import saps.common.core.storage.permanent.PermanentStorageConstants;
import saps.common.utils.SapsPropertiesConstants;

public class SwiftPermanentStorageTest {

  private SapsImage task01, task02, task03;
  private List<String> filesTask01, filesTask02;

  private final class ArchiverConfigFilePath {
    private final class Fail {
      private static final String NORMAL_MODE =
          "src/test/resources/config/archiver/normal-mode.failconf";
      private static final String DEBUG_MODE =
          "src/test/resources/config/archiver/debug-mode.failconf";
    }

    private final class Success {
      private static final String NORMAL_MODE =
          "src/test/resources/config/archiver/normal-mode.conf";
      private static final String DEBUG_MODE = "src/test/resources/config/archiver/debug-mode.conf";
    }
  }

  private static final String MOCK_SWIFT_PERMANENT_STORAGE_FOLDER_PREFIX = "archiver";
  private final String MOCK_SWIFT_FOLDER_PREFIX = "archiver";
  private final String MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS = "trash";
  private final String MOCK_CONTAINER_NAME = "saps-test";

  private final class Dirs {
    private static final String Task01 = "1";
    private static final String Task02 = "2";
    private static final String Task03 = "3";
  }

  private final class LocalFiles {
    private final class Task01 {
      private static final String INPUTDOWNLOADING_FILE = Dirs.Task01 + "/inputdownloading/file.ip";
      private static final String PREPROCESSING_FILE = Dirs.Task01 + "/preprocessing/file.pp";
      private static final String PROCESSING_FILE = Dirs.Task01 + "/processing/file.p";
    }

    private final class Task02 {
      private static final String INPUTDOWNLOADING_FILE = Dirs.Task02 + "inputdownloading/file.ip";
    }
  }

  private static class PermanentStorageFiles {
    private static class Task03 {
      private static final String INPUTDOWNLOADING_FILE_PATH = "inputdownloading/file.ip";
      private static final String PREPROCESSING_FILE_PATH = "preprocessing/file.pp";
      private static final String PROCESSING_FILE_PATH = "processing/file.p";

      private static List<String> InputdownloadingFilesPath;
      private static List<String> PreprocessingFilesPath;
      private static List<String> ProcessingFilesPath;
    }
  }

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    String PATTERN_TASK03_FILE_PATH = "%s" + File.separator + "%s" + File.separator + "%s";
    PermanentStorageFiles.Task03.InputdownloadingFilesPath = new ArrayList<String>();
    PermanentStorageFiles.Task03.InputdownloadingFilesPath.add(
        String.format(
            PATTERN_TASK03_FILE_PATH,
            MOCK_SWIFT_PERMANENT_STORAGE_FOLDER_PREFIX,
            Dirs.Task03,
            PermanentStorageFiles.Task03.INPUTDOWNLOADING_FILE_PATH));

    PermanentStorageFiles.Task03.PreprocessingFilesPath = new ArrayList<String>();
    PermanentStorageFiles.Task03.PreprocessingFilesPath.add(
        String.format(
            PATTERN_TASK03_FILE_PATH,
            MOCK_SWIFT_PERMANENT_STORAGE_FOLDER_PREFIX,
            Dirs.Task03,
            PermanentStorageFiles.Task03.PREPROCESSING_FILE_PATH));

    PermanentStorageFiles.Task03.ProcessingFilesPath = new ArrayList<String>();
    PermanentStorageFiles.Task03.ProcessingFilesPath.add(
        String.format(
            PATTERN_TASK03_FILE_PATH,
            MOCK_SWIFT_PERMANENT_STORAGE_FOLDER_PREFIX,
            Dirs.Task03,
            PermanentStorageFiles.Task03.PROCESSING_FILE_PATH));

    task01 =
        new SapsImage(
            "1",
            "",
            "",
            new Date(),
            ImageTaskState.FINISHED,
            SapsImage.NONE_ARREBOL_JOB_ID,
            SapsImage.NONE_FEDERATION_MEMBER,
            0,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            new Timestamp(1),
            new Timestamp(1),
            "",
            "");
    task02 =
        new SapsImage(
            "2",
            "",
            "",
            new Date(),
            ImageTaskState.FAILED,
            SapsImage.NONE_ARREBOL_JOB_ID,
            SapsImage.NONE_FEDERATION_MEMBER,
            0,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            new Timestamp(1),
            new Timestamp(1),
            "",
            "");
    task03 =
        new SapsImage(
            "3",
            "",
            "",
            new Date(),
            ImageTaskState.ARCHIVED,
            SapsImage.NONE_ARREBOL_JOB_ID,
            SapsImage.NONE_FEDERATION_MEMBER,
            0,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            new Timestamp(1),
            new Timestamp(1),
            "",
            "");

    filesTask01 = new ArrayList<String>();
    filesTask01.add(MOCK_SWIFT_FOLDER_PREFIX + "/" + LocalFiles.Task01.INPUTDOWNLOADING_FILE);
    filesTask01.add(MOCK_SWIFT_FOLDER_PREFIX + "/" + LocalFiles.Task01.PREPROCESSING_FILE);
    filesTask01.add(MOCK_SWIFT_FOLDER_PREFIX + "/" + LocalFiles.Task01.PROCESSING_FILE);

    filesTask02 = new ArrayList<String>();
    filesTask02.add(
        MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS
            + "/"
            + LocalFiles.Task02.INPUTDOWNLOADING_FILE);
  }

  private Properties createDefaultPropertiesWithoutArchiverInDebugMode() throws IOException {
    Properties properties = new Properties();
    try (FileInputStream input = new FileInputStream(ArchiverConfigFilePath.Success.NORMAL_MODE)) {
      properties.load(input);
      return properties;
    }
  }

  private Properties createFailurePropertiesWithoutArchiverInDebugMode() throws IOException {
    Properties properties = new Properties();
    try (FileInputStream input = new FileInputStream(ArchiverConfigFilePath.Fail.NORMAL_MODE)) {
      properties.load(input);
      return properties;
    }
  }

  private Properties createDefaultPropertiesWithArchiverInDebugMode() throws IOException {
    Properties properties = new Properties();
    try (FileInputStream input = new FileInputStream(ArchiverConfigFilePath.Success.DEBUG_MODE)) {
      properties.load(input);
      return properties;
    }
  }

  private Properties createFailurePropertiesWithArchiverInDebugMode() throws IOException {
    Properties properties = new Properties();
    try (FileInputStream input = new FileInputStream(ArchiverConfigFilePath.Fail.DEBUG_MODE)) {
      properties.load(input);
      return properties;
    }
  }

  @Test(expected = InvalidPropertyException.class)
  public void failureTestToBuildArchiveBecausePropertiesIsNull() throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    new SwiftPermanentStorage(null, swiftAPIClient);
  }

  @Test(expected = InvalidPropertyException.class)
  public void failureTestToBuildArchiveWithoutDebugMode() throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createFailurePropertiesWithoutArchiverInDebugMode();
    new SwiftPermanentStorage(properties, swiftAPIClient);
  }

  @Test(expected = InvalidPropertyException.class)
  public void failureTestToBuildArchiveInDebugMode() throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createFailurePropertiesWithArchiverInDebugMode();
    new SwiftPermanentStorage(properties, swiftAPIClient);
  }

  @Test
  public void testToArchiveSuccessfulTaskWithoutArchiverInDebugMode() throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithoutArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.doAnswer(
            (i) -> {
              return null;
            })
        .when(swiftAPIClient)
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    boolean archiveTask01 = permanentStorage.archive(task01);

    Mockito.verify(swiftAPIClient, Mockito.times(0))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS)));
    Mockito.verify(swiftAPIClient, Mockito.times(3))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    Assert.assertEquals(archiveTask01, true);
  }

  @Test
  public void failureTestWhenTryingToArchiveSuccessfulTaskWithoutArchiverInDebugMode()
      throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithoutArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.doAnswer(
            (i) -> {
              throw new Exception();
            })
        .when(swiftAPIClient)
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    boolean archiveTask01 = permanentStorage.archive(task01);

    Mockito.verify(swiftAPIClient, Mockito.times(0))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS)));
    Mockito.verify(swiftAPIClient, Mockito.times(2))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    Assert.assertEquals(archiveTask01, false);
  }

  @Test
  public void testToArchiveFailureTaskWithoutArchiverInDebugMode() throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithoutArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.doAnswer(
            (i) -> {
              return null;
            })
        .when(swiftAPIClient)
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    boolean archiveTask02 = permanentStorage.archive(task02);

    Mockito.verify(swiftAPIClient, Mockito.times(0))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS)));
    Mockito.verify(swiftAPIClient, Mockito.times(1))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    Assert.assertEquals(archiveTask02, false);
  }

  @Test
  public void failureTestWhenTryingToArchiveFailureTaskWithoutArchiverInDebugMode()
      throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithoutArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.doAnswer(
            (i) -> {
              throw new Exception();
            })
        .when(swiftAPIClient)
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    boolean archiveTask02 = permanentStorage.archive(task02);

    Mockito.verify(swiftAPIClient, Mockito.times(0))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS)));
    Mockito.verify(swiftAPIClient, Mockito.times(2))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    Assert.assertEquals(archiveTask02, false);
  }

  @Test
  public void testToArchiveSuccessfulTaskWithArchiverInDebugMode() throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.doAnswer(
            (i) -> {
              return null;
            })
        .when(swiftAPIClient)
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    boolean archiveTask01 = permanentStorage.archive(task01);

    Mockito.verify(swiftAPIClient, Mockito.times(0))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS)));
    Mockito.verify(swiftAPIClient, Mockito.times(3))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    Assert.assertEquals(archiveTask01, true);
  }

  @Test
  public void failureTestWhenTryingToArchiveSuccessfulTaskWithArchiverInDebugMode()
      throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.doAnswer(
            (i) -> {
              throw new Exception();
            })
        .when(swiftAPIClient)
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    boolean archiveTask01 = permanentStorage.archive(task01);

    Mockito.verify(swiftAPIClient, Mockito.times(0))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS)));
    Mockito.verify(swiftAPIClient, Mockito.times(2))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    Assert.assertEquals(archiveTask01, false);
  }

  @Test
  public void testToArchiveFailureTaskWithArchiverInDebugMode() throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.doAnswer(
            (i) -> {
              return null;
            })
        .when(swiftAPIClient)
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS)));

    boolean archiveTask02 = permanentStorage.archive(task02);

    Mockito.verify(swiftAPIClient, Mockito.times(1))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS)));
    Mockito.verify(swiftAPIClient, Mockito.times(0))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    Assert.assertEquals(archiveTask02, false);
  }

  @Test
  public void failureTestWhenTryingToArchiveFailureTaskWithArchiverInDebugMode() throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.doAnswer(
            (i) -> {
              throw new Exception();
            })
        .when(swiftAPIClient)
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS)));

    boolean archiveTask02 = permanentStorage.archive(task02);

    Mockito.verify(swiftAPIClient, Mockito.times(2))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS)));
    Mockito.verify(swiftAPIClient, Mockito.times(0))
        .uploadObject(
            Mockito.anyString(),
            Mockito.any(File.class),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX)));

    Assert.assertEquals(archiveTask02, false);
  }

  @Test
  public void testToDeleteSuccessfulTaskWithoutArchiverInDebugMode() throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithoutArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.when(
            swiftAPIClient.listObjects(
                Mockito.eq(MOCK_CONTAINER_NAME),
                Mockito.eq(MOCK_SWIFT_FOLDER_PREFIX + "/" + Dirs.Task01)))
        .thenReturn(filesTask01);
    Mockito.doAnswer(
            (i) -> {
              return null;
            })
        .when(swiftAPIClient)
        .deleteObject(
            Mockito.eq(MOCK_CONTAINER_NAME),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX + "/" + Dirs.Task01)));

    boolean deleteTask01 = permanentStorage.delete(task01);

    Assert.assertEquals(deleteTask01, true);
    Mockito.verify(swiftAPIClient, Mockito.times(1))
        .listObjects(MOCK_CONTAINER_NAME, MOCK_SWIFT_FOLDER_PREFIX + "/" + Dirs.Task01);
    Mockito.verify(swiftAPIClient, Mockito.times(1))
        .deleteObject(
            MOCK_CONTAINER_NAME,
            MOCK_SWIFT_FOLDER_PREFIX + "/" + LocalFiles.Task01.INPUTDOWNLOADING_FILE);
    Mockito.verify(swiftAPIClient, Mockito.times(1))
        .deleteObject(
            MOCK_CONTAINER_NAME,
            MOCK_SWIFT_FOLDER_PREFIX + "/" + LocalFiles.Task01.PREPROCESSING_FILE);
    Mockito.verify(swiftAPIClient, Mockito.times(1))
        .deleteObject(
            MOCK_CONTAINER_NAME,
            MOCK_SWIFT_FOLDER_PREFIX + "/" + LocalFiles.Task01.PROCESSING_FILE);
  }

  @Test
  public void testToDeleteFailureTaskWithoutArchiverInDebugMode() throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithoutArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.when(
            swiftAPIClient.listObjects(
                Mockito.eq(MOCK_CONTAINER_NAME),
                Mockito.eq(MOCK_SWIFT_FOLDER_PREFIX + "/" + Dirs.Task02)))
        .thenReturn(filesTask02);
    Mockito.doAnswer(
            (i) -> {
              return null;
            })
        .when(swiftAPIClient)
        .deleteObject(
            Mockito.eq(MOCK_CONTAINER_NAME),
            Mockito.argThat(new StartsWith(MOCK_SWIFT_FOLDER_PREFIX + "/" + Dirs.Task02)));

    boolean deleteTask02 = permanentStorage.delete(task02);

    Assert.assertEquals(deleteTask02, true);
    Mockito.verify(swiftAPIClient, Mockito.times(1))
        .listObjects(MOCK_CONTAINER_NAME, MOCK_SWIFT_FOLDER_PREFIX + "/" + Dirs.Task02);
    Mockito.verify(swiftAPIClient, Mockito.times(1))
        .deleteObject(
            MOCK_CONTAINER_NAME,
            MOCK_SWIFT_FOLDER_PREFIX_DEBUG_FAILED_TASKS
                + "/"
                + LocalFiles.Task02.INPUTDOWNLOADING_FILE);
  }

  @Test
  public void
      testToDeleteSuccessfulTaskButListObjectsWithPrefixMethodReturnsEmptyListWithoutArchiverInDebugMode()
          throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithoutArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.when(
            swiftAPIClient.listObjects(
                Mockito.eq(MOCK_CONTAINER_NAME),
                Mockito.eq(MOCK_SWIFT_FOLDER_PREFIX + "/" + Dirs.Task01)))
        .thenReturn(new ArrayList<String>());
    Mockito.doAnswer(
            (i) -> {
              return null;
            })
        .when(swiftAPIClient)
        .deleteObject(Mockito.anyString(), Mockito.anyString());

    boolean deleteTask01 = permanentStorage.delete(task01);

    Assert.assertEquals(deleteTask01, true);
    Mockito.verify(swiftAPIClient, Mockito.times(1))
        .listObjects(MOCK_CONTAINER_NAME, MOCK_SWIFT_FOLDER_PREFIX + "/" + Dirs.Task01);
    Mockito.verify(swiftAPIClient, Mockito.times(0))
        .deleteObject(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  public void
      testToDeleteFailureTaskButListObjectsWithPrefixMethodReturnsEmptyListWithoutArchiverInDebugMode()
          throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithoutArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.when(
            swiftAPIClient.listObjects(
                Mockito.eq(MOCK_CONTAINER_NAME),
                Mockito.eq(MOCK_SWIFT_FOLDER_PREFIX + "/" + Dirs.Task02)))
        .thenReturn(new ArrayList<String>());
    Mockito.doAnswer(
            (i) -> {
              return null;
            })
        .when(swiftAPIClient)
        .deleteObject(Mockito.anyString(), Mockito.anyString());

    boolean deleteTask02 = permanentStorage.delete(task02);

    Assert.assertEquals(deleteTask02, true);
    Mockito.verify(swiftAPIClient, Mockito.times(1))
        .listObjects(MOCK_CONTAINER_NAME, MOCK_SWIFT_FOLDER_PREFIX + "/" + Dirs.Task02);
    Mockito.verify(swiftAPIClient, Mockito.times(0))
        .deleteObject(Mockito.anyString(), Mockito.anyString());
  }

  @Test(expected = TaskNotFoundException.class)
  public void testToGenerateTaskLinksButTaskIdNotFound() throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithoutArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.when(
            swiftAPIClient.existsObject(
                Mockito.eq(MOCK_CONTAINER_NAME),
                Mockito.eq(MOCK_SWIFT_PERMANENT_STORAGE_FOLDER_PREFIX),
                Mockito.eq(task03.getTaskId())))
        .thenReturn(false);

    permanentStorage.generateAccessLinks(task03);
  }

  @Test
  public void testToGenerateTaskLinks() throws Exception {
    SwiftAPIClient swiftAPIClient = mock(SwiftAPIClient.class);
    Properties properties = createDefaultPropertiesWithoutArchiverInDebugMode();
    PermanentStorage permanentStorage = new SwiftPermanentStorage(properties, swiftAPIClient);

    Mockito.when(
            swiftAPIClient.existsObject(
                Mockito.eq(MOCK_CONTAINER_NAME),
                Mockito.eq(MOCK_SWIFT_PERMANENT_STORAGE_FOLDER_PREFIX),
                Mockito.eq(task03.getTaskId())))
        .thenReturn(true);

    String taskId03 = task03.getTaskId();
    String dirInput =
        MOCK_SWIFT_PERMANENT_STORAGE_FOLDER_PREFIX
            + File.separator
            + taskId03
            + File.separator
            + PermanentStorageConstants.INPUTDOWNLOADING_DIR;
    String dirPrep =
        MOCK_SWIFT_PERMANENT_STORAGE_FOLDER_PREFIX
            + File.separator
            + taskId03
            + File.separator
            + PermanentStorageConstants.PREPROCESSING_DIR;
    String dirProc =
        MOCK_SWIFT_PERMANENT_STORAGE_FOLDER_PREFIX
            + File.separator
            + taskId03
            + File.separator
            + PermanentStorageConstants.PROCESSING_DIR;

    Mockito.when(swiftAPIClient.listObjects(Mockito.eq(MOCK_CONTAINER_NAME), Mockito.eq(dirInput)))
        .thenReturn(PermanentStorageFiles.Task03.InputdownloadingFilesPath);
    Mockito.when(swiftAPIClient.listObjects(Mockito.eq(MOCK_CONTAINER_NAME), Mockito.eq(dirPrep)))
        .thenReturn(PermanentStorageFiles.Task03.PreprocessingFilesPath);
    Mockito.when(swiftAPIClient.listObjects(Mockito.eq(MOCK_CONTAINER_NAME), Mockito.eq(dirProc)))
        .thenReturn(PermanentStorageFiles.Task03.ProcessingFilesPath);

    List<AccessLink> accessLinks = permanentStorage.generateAccessLinks(task03);
    Assert.assertTrue(assertLinks(taskId03, accessLinks, properties));
  }

  private boolean assertLinks(String taskId, List<AccessLink> accessLinks, Properties properties) {
    String containerName =
        properties.getProperty(SapsPropertiesConstants.Openstack.ObjectStoreService.CONTAINER_NAME);
    String objectStoreServiceApiUrl =
        properties.getProperty(SapsPropertiesConstants.Openstack.ObjectStoreService.API_URL);
    for (AccessLink al : accessLinks) {
      String name = al.getName();
      String link = al.getUrl();
      if (!link.contains(containerName)
          || !link.contains(objectStoreServiceApiUrl)
          || !link.contains(taskId)
          || !link.contains("temp_url_expires")
          || !link.contains("temp_url_sig")
          || !link.contains("filename")
          || !link.contains(name)) return false;
    }
    return true;
  }
}
