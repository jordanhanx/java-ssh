package javasshdemo;

import java.io.IOException;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class AwsS3Service {

    private final S3Client s3Client;

    public AwsS3Service() {
        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_2)
                .build();
    }

    public void uploadObj(String bucket, String key, byte[] bytes) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.putObject(objectRequest, RequestBody.fromBytes(bytes));
    }

    public byte[] getObj(String bucket, String key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        ResponseInputStream<GetObjectResponse> obj = s3Client.getObject(getObjectRequest);
        return obj.readAllBytes();
    }

    public void deleteObj(String bucket, String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

}
