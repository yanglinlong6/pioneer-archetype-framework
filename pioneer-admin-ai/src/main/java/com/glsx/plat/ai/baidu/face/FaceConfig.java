package com.glsx.plat.ai.baidu.face;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@RefreshScope
public class FaceConfig {

    @Value("${baidu.appId:}")
    private String appId;

    @Value("${baidu.apiKey:}")
    private String apiKey;

    @Value("${baidu.secretKey:}")
    private String secretKey;

    @Value("${baidu.face.matchScore:80}")
    private Integer faceMatchScore;

}
