package com.jpmorgan.cakeshop.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;

import java.io.File;
import java.util.Arrays;

import static com.jpmorgan.cakeshop.util.ProcessUtils.ensureFileIsExecutable;

public class DownloadUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadUtils.class);

    public static String getDefaultGethToolsUrl() {
        switch (ProcessUtils.getPlatformDirectory()) {
            case "mac":
                return "https://gethstore.blob.core.windows.net/builds/geth-alltools-darwin-amd64-1.8.27-4bcc0a37.tar.gz";
            case "linux":
                return "https://gethstore.blob.core.windows.net/builds/geth-alltools-linux-amd64-1.8.27-4bcc0a37.tar.gz";
            case "windows":
            default:
                throw new RuntimeException("Your OS is not currently supported");
        }
    }

    public static String getDefaultQuorumReleaseUrl() {
        switch (ProcessUtils.getPlatformDirectory()) {
            case "mac":
                return "https://bintray.com/quorumengineering/quorum/download_file?file_path=v2.3.0%2Fgeth_v2.3.0_darwin_amd64.tar.gz";
            case "linux":
                return "https://bintray.com/quorumengineering/quorum/download_file?file_path=v2.3.0%2Fgeth_v2.3.0_linux_amd64.tar.gz";
            case "windows":
            default:
                throw new RuntimeException("Your OS is not currently supported");
        }
    }

    public static String getDefaultIstanbulToolsUrl() {
        switch (ProcessUtils.getPlatformDirectory()) {
            case "mac":
                return "https://dl.bintray.com/quorumengineering/istanbul-tools/istanbul-tools_v1.0.1_darwin_amd64.tar.gz";
            case "linux":
                return "https://dl.bintray.com/quorumengineering/istanbul-tools/istanbul-tools_v1.0.1_linux_amd64.tar.gz";
            case "windows":
            default:
                throw new RuntimeException("Your OS is not currently supported");
        }
    }


    public static ResponseExtractor<Void> createTarResponseExtractor(String destinationPath, String filename) {
        return response -> {
            LOG.info("Attemping to extract {} from download archive: {}", filename, destinationPath);
            try (final TarArchiveInputStream debInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(response.getBody()))) {
                TarArchiveEntry entry;
                while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
                    // geth puts everything in a folder, check only the filename at the end
                    if (entry.getName().endsWith(filename)) {
                        LOG.info("Found binary: {}. Copying...", entry.getName());
                        FileUtils.copyInputStreamToFile(debInputStream, new File(destinationPath));
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not extract binary from tar archive", e);
            }
            LOG.info("Making '{}' executable", filename);
            ensureFileIsExecutable(destinationPath);
            LOG.info("Done extracting {}", filename);
            return null;
        };
    }

    // Accept header may be needed for some downloads
    public static RequestCallback getOctetStreamRequestCallback() {
        return request -> request.getHeaders()
            .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
    }
}
