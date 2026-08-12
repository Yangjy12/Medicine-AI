package com.xinglin.video.service;

import com.xinglin.video.common.BusinessException;
import com.xinglin.video.vo.OssUploadVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class OssStorageService {
    private static final Logger log = LoggerFactory.getLogger(OssStorageService.class);
    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter HTTP_DATE = DateTimeFormatter.RFC_1123_DATE_TIME;
    private static final Set<String> VIDEO_EXTENSIONS = new HashSet<>(Arrays.asList(".mp4", ".m4v", ".mov", ".webm", ".avi", ".mkv"));
    private static final Set<String> COVER_EXTENSIONS = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".webp"));

    @Value("${xinglin.oss.enabled:false}")
    private boolean enabled;
    @Value("${xinglin.oss.endpoint:}")
    private String endpoint;
    @Value("${xinglin.oss.public-endpoint:}")
    private String publicEndpoint;
    @Value("${xinglin.oss.public-base-url:}")
    private String publicBaseUrl;
    @Value("${xinglin.oss.bucket:}")
    private String bucket;
    @Value("${xinglin.oss.access-key-id:}")
    private String accessKeyId;
    @Value("${xinglin.oss.access-key-secret:}")
    private String accessKeySecret;
    @Value("${xinglin.oss.video-dir:videos}")
    private String videoDir;
    @Value("${xinglin.oss.cover-dir:covers}")
    private String coverDir;
    @Value("${xinglin.oss.max-video-size-mb:1024}")
    private long maxVideoSizeMb;
    @Value("${xinglin.oss.max-cover-size-mb:10}")
    private long maxCoverSizeMb;

    public OssUploadVO uploadVideo(MultipartFile file, Long userId) {
        return upload(file, userId, videoDir, maxVideoSizeMb, VIDEO_EXTENSIONS, "video");
    }

    public OssUploadVO uploadCover(MultipartFile file, Long userId) {
        return upload(file, userId, coverDir, maxCoverSizeMb, COVER_EXTENSIONS, "cover");
    }

    public void deleteObject(String objectKey) {
        if (!enabled || !StringUtils.hasText(objectKey)) {
            return;
        }
        try {
            HttpURLConnection connection = openConnection(objectKey);
            String date = gmtNow();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Date", date);
            connection.setRequestProperty("Authorization", authorization("DELETE", "", date, objectKey));
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                log.warn("oss object delete failed objectKey={} status={} error={}", objectKey, status, readResponse(connection));
                return;
            }
            log.info("oss object deleted objectKey={}", objectKey);
        } catch (Exception ex) {
            log.warn("oss object delete failed objectKey={} message={}", objectKey, ex.getMessage());
        }
    }

    private OssUploadVO upload(MultipartFile file,
                               Long userId,
                               String rootDir,
                               long maxSizeMb,
                               Set<String> allowedExtensions,
                               String bizType) {
        validateEnabled();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }
        if (file.getSize() > maxSizeMb * 1024L * 1024L) {
            throw new BusinessException(400, "上传文件超过大小限制：" + maxSizeMb + "MB");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = resolveExtension(originalFilename);
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(400, "不支持的文件类型：" + extension);
        }
        String objectKey = buildObjectKey(rootDir, userId, extension);
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
        try {
            HttpURLConnection connection = openConnection(objectKey);
            String date = gmtNow();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(file.getSize());
            connection.setRequestProperty("Date", date);
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Authorization", authorization("PUT", contentType, date, objectKey));
            try (InputStream inputStream = file.getInputStream(); OutputStream outputStream = connection.getOutputStream()) {
                inputStream.transferTo(outputStream);
            }
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                log.warn("oss {} upload rejected userId={} objectKey={} status={} error={}",
                        bizType, userId, objectKey, status, readResponse(connection));
                throw new BusinessException(500, "文件上传失败，请检查OSS配置");
            }
            String url = buildPublicUrl(objectKey);
            log.info("oss {} uploaded userId={} objectKey={} size={} contentType={}",
                    bizType, userId, objectKey, file.getSize(), contentType);
            return new OssUploadVO(objectKey, url, originalFilename, contentType, file.getSize());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("oss {} upload failed userId={} filename={} message={}", bizType, userId, originalFilename, ex.getMessage());
            throw new BusinessException(500, "文件上传失败，请稍后重试");
        }
    }

    private void validateEnabled() {
        if (!enabled) {
            throw new BusinessException(503, "OSS上传未启用");
        }
        if (!StringUtils.hasText(endpoint)
                || !StringUtils.hasText(bucket)
                || !StringUtils.hasText(accessKeyId)
                || !StringUtils.hasText(accessKeySecret)) {
            throw new BusinessException(500, "OSS配置不完整");
        }
    }

    private String buildObjectKey(String rootDir, Long userId, String extension) {
        String cleanRoot = trimSlashes(StringUtils.hasText(rootDir) ? rootDir : "uploads");
        String date = DATE_PATH.format(LocalDate.now());
        String owner = userId == null ? "anonymous" : String.valueOf(userId);
        return cleanRoot + "/" + date + "/" + owner + "/" + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private String buildPublicUrl(String objectKey) {
        String encodedKey = Arrays.stream(objectKey.split("/"))
                .map(part -> URLEncoder.encode(part, StandardCharsets.UTF_8).replace("+", "%20"))
                .reduce((left, right) -> left + "/" + right)
                .orElse(objectKey);
        if (StringUtils.hasText(publicBaseUrl)) {
            return trimTrailingSlash(publicBaseUrl) + "/" + encodedKey;
        }
        String endpointForUrl = StringUtils.hasText(publicEndpoint) ? publicEndpoint : endpoint;
        return "https://" + bucket + "." + stripProtocol(endpointForUrl) + "/" + encodedKey;
    }

    private HttpURLConnection openConnection(String objectKey) throws Exception {
        String host = bucket + "." + stripProtocol(endpoint);
        URL url = new URL("https://" + host + "/" + encodeObjectKey(objectKey));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(60_000);
        connection.setRequestProperty("Host", host);
        return connection;
    }

    private String authorization(String method, String contentType, String date, String objectKey) throws Exception {
        String stringToSign = method + "\n\n" + contentType + "\n" + date + "\n/" + bucket + "/" + objectKey;
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(accessKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        String signature = Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
        return "OSS " + accessKeyId + ":" + signature;
    }

    private String gmtNow() {
        return HTTP_DATE.format(ZonedDateTime.now(ZoneOffset.UTC));
    }

    private String readResponse(HttpURLConnection connection) {
        try (InputStream stream = connection.getErrorStream() == null ? connection.getInputStream() : connection.getErrorStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (stream == null) {
                return "";
            }
            stream.transferTo(buffer);
            return buffer.toString(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "";
        }
    }

    private String resolveExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            throw new BusinessException(400, "文件缺少扩展名");
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }

    private String stripProtocol(String value) {
        return value.replace("https://", "").replace("http://", "").replaceAll("/+$", "");
    }

    private String encodeObjectKey(String objectKey) {
        return Arrays.stream(objectKey.split("/"))
                .map(part -> URLEncoder.encode(part, StandardCharsets.UTF_8).replace("+", "%20"))
                .reduce((left, right) -> left + "/" + right)
                .orElse(objectKey);
    }

    private String trimSlashes(String value) {
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String trimTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }
}
