package com.novoda.downloadmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class DownloadBatchFactory {

    private DownloadBatchFactory() {
        // non instantiable factory class
    }

    static DownloadBatch newInstance(Batch batch,
                                     FileOperations fileOperations,
                                     DownloadsBatchPersistence downloadsBatchPersistence,
                                     DownloadsFilePersistence downloadsFilePersistence,
                                     CallbackThrottle callbackThrottle) {
        DownloadBatchTitle downloadBatchTitle = DownloadBatchTitleCreator.createFrom(batch);
        Map<DownloadFileId, NetworkAddressAndFilePath> networkAddressAndFileNameById = batch.networkAddressAndFileNameById();
        List<DownloadFile> downloadFiles = new ArrayList<>(networkAddressAndFileNameById.size());
        DownloadBatchId downloadBatchId = batch.getDownloadBatchId();
        long downloadedDateTimeInMillis = System.currentTimeMillis();

        for (Map.Entry<DownloadFileId, NetworkAddressAndFilePath> networkAddressAndFilePathByDownloadId : networkAddressAndFileNameById.entrySet()) {
            NetworkAddressAndFilePath networkAddressAndFilePath = networkAddressAndFilePathByDownloadId.getValue();

            InternalFileSize fileSize = InternalFileSizeCreator.unknownFileSize();

            String fileNetworkAddress = networkAddressAndFilePath.networkAddress();
            FileName fileNameFromNetworkAddress = FileNameExtractor.extractFrom(fileNetworkAddress);
            FilePath filePath = extractFilePathFrom(networkAddressAndFilePath, fileNameFromNetworkAddress);
            FileName fileName = filePath == null ? fileNameFromNetworkAddress : FileNameExtractor.extractFrom(filePath.path());

            DownloadFileId downloadFileId = networkAddressAndFilePathByDownloadId.getKey();
            InternalDownloadFileStatus downloadFileStatus = new LiteDownloadFileStatus(
                    downloadBatchId,
                    downloadFileId,
                    InternalDownloadFileStatus.Status.QUEUED,
                    fileSize,
                    filePath
            );

            FilePersistenceCreator filePersistenceCreator = fileOperations.filePersistenceCreator();
            FileDownloader fileDownloader = fileOperations.fileDownloader();
            FileSizeRequester fileSizeRequester = fileOperations.fileSizeRequester();

            FilePersistence filePersistence = filePersistenceCreator.create();
            DownloadFile downloadFile = new DownloadFile(
                    downloadBatchId,
                    downloadFileId,
                    fileNetworkAddress,
                    downloadFileStatus,
                    fileName,
                    filePath,
                    fileSize,
                    fileDownloader,
                    fileSizeRequester,
                    filePersistence,
                    downloadsFilePersistence
            );
            downloadFiles.add(downloadFile);
        }

        InternalDownloadBatchStatus liteDownloadBatchStatus = new LiteDownloadBatchStatus(
                downloadBatchId,
                downloadBatchTitle,
                downloadedDateTimeInMillis,
                DownloadBatchStatus.Status.QUEUED
        );

        return new DownloadBatch(
                liteDownloadBatchStatus,
                downloadFiles,
                new HashMap<>(),
                downloadsBatchPersistence,
                callbackThrottle
        );
    }

    private static FilePath extractFilePathFrom(NetworkAddressAndFilePath networkAddressAndFilePath, FileName fileNameFromNetworkAddress) {
        return networkAddressAndFilePath.filePath() == FilePathCreator.unknownFilePath()
                ? FilePathCreator.create(fileNameFromNetworkAddress.name()) : networkAddressAndFilePath.filePath();
    }
}
