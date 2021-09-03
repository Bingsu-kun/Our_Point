package com.webproject.ourpoint.configurations;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "es")
public class ElasticSearchConfig {

  private String url;

  private int port;

  @Bean
  public RestHighLevelClient restHighLevelClient() {
    return new RestHighLevelClient(RestClient.builder(new HttpHost(url, port, "http")));
  }

}
