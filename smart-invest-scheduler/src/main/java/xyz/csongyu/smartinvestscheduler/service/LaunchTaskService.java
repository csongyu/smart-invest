package xyz.csongyu.smartinvestscheduler.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LaunchTaskService {
    @Value("${spring-cloud-data-flow.url}")
    private String url;

    public int call(final String name, final List<String> properties) throws IOException {
        try (final CloseableHttpClient client = HttpClients.createDefault()) {
            final HttpPost httpPost = new HttpPost(this.url + "tasks/executions");
            final List<NameValuePair> parameters = new ArrayList<>();
            parameters.add(new BasicNameValuePair("name", name));
            parameters.add(new BasicNameValuePair("properties", String.join(",", properties)));
            httpPost.setEntity(new UrlEncodedFormEntity(parameters));
            try (final CloseableHttpResponse response = client.execute(httpPost)) {
                return response.getStatusLine().getStatusCode();
            }
        }
    }
}
