package xyz.csongyu.smartinvesttask.configuration;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfiguration {
    @Value("${httpclient.connection-pool.default-max-per-route}")
    private int defaultMaxPerRoute;

    @Value("${httpclient.connection-pool.max-total}")
    private int maxTotal;

    @Value("${httpclient.connect-timeout}")
    private int connectTimeout;

    @Value("${httpclient.socket-timeout}")
    private int socketTimeout;

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(this.defaultMaxPerRoute);
        connectionManager.setMaxTotal(this.maxTotal);
        final RequestConfig requestConfig =
            RequestConfig.custom().setConnectTimeout(this.connectTimeout).setSocketTimeout(this.socketTimeout).build();
        return HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig)
            .build();
    }
}
