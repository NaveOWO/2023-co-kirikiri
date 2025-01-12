package co.kirikiri.infra;

import co.kirikiri.exception.ServerException;
import co.kirikiri.service.FileService;
import co.kirikiri.service.dto.FileInformation;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Service
public class AmazonS3FileService implements FileService {

    private static final String ROOT_DIRECTORY_PROPERTY = "cloud.aws.s3.root-directory";
    private static final String SUB_DIRECTORY_PROPERTY = "cloud.aws.s3.sub-directory";
    private static final String BUCKET_PROPERTY = "cloud.aws.s3.bucket";
    private static final String EXPIRATION_PROPERTY = "cloud.aws.s3.url-expiration";
    private static final String DIRECTORY_SEPARATOR = "/";

    private final AmazonS3 amazonS3;
    private final Environment environment;

    public AmazonS3FileService(final AmazonS3 amazonS3, final Environment environment) {
        this.amazonS3 = amazonS3;
        this.environment = environment;
    }

    @Override
    public void save(final String path, final FileInformation fileInformation) {
        final String key = makeKey(path);
        final ObjectMetadata objectMetadata = makeObjectMetadata(fileInformation);
        putObjectToS3(key, fileInformation.inputStream(), objectMetadata);
    }

    private String makeKey(final String path) {
        return findProperty(ROOT_DIRECTORY_PROPERTY) + DIRECTORY_SEPARATOR
                + findProperty(SUB_DIRECTORY_PROPERTY) + path;
    }

    private String findProperty(final String property) {
        return environment.getProperty(property);
    }

    private ObjectMetadata makeObjectMetadata(final FileInformation fileInformation) {
        final ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(fileInformation.size());
        objectMetadata.setContentType(fileInformation.contentType());
        return objectMetadata;
    }

    private void putObjectToS3(final String key, final InputStream inputStream, final ObjectMetadata objectMetadata) {
        try {
            amazonS3.putObject(getBucketName(), key, inputStream, objectMetadata);
        } catch (final SdkClientException sdkClientException) {
            throw new ServerException(sdkClientException.getMessage());
        }
    }

    private String getBucketName() {
        return findProperty(BUCKET_PROPERTY);
    }

    @Override
    public URL generateUrl(final String path, final HttpMethod httpMethod) {
        final String key = makeKey(path);
        final Date expiration = createExpiration(Long.parseLong(findProperty(EXPIRATION_PROPERTY)));
        final GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(getBucketName(), key)
                        .withMethod(com.amazonaws.HttpMethod.valueOf(httpMethod.name()))
                        .withExpiration(expiration);
        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
    }

    private Date createExpiration(final Long validity) {
        final long now = new Date().getTime();
        return new Date(now + validity);
    }
}
