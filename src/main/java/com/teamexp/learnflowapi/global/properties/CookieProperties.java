package com.teamexp.learnflowapi.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app.cookie") // Application.yml 파일에서 appl.cookie로 시작하는 설정을 매핑
@Component
@Getter
@Setter
public class CookieProperties {

    private final Refresh refresh = new Refresh();

    @Getter
    @Setter
    public static class Refresh {
        private String name;
        private int maxAge;
        private boolean httpOnly;
        private boolean secure;
        private String sameSite;
        private String path;
    }
}
