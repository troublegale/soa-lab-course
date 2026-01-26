package itmo.ivank.caller.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
public class RestClientConfig {

    @Bean
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return xmlMapper;
    }

    @Bean
    public RestTemplate restTemplate(XmlMapper xmlMapper) throws Exception {
        // Create SSL context that trusts all certificates
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                .build();

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE
        );

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add message converters
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        MappingJackson2XmlHttpMessageConverter xmlConverter = new MappingJackson2XmlHttpMessageConverter(xmlMapper);
        restTemplate.setMessageConverters(Arrays.asList(stringConverter, xmlConverter));
        
        return restTemplate;
    }

    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate(XmlMapper xmlMapper) throws Exception {
        // Create SSL context that trusts all certificates
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                .build();

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE
        );

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add message converters
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        MappingJackson2XmlHttpMessageConverter xmlConverter = new MappingJackson2XmlHttpMessageConverter(xmlMapper);
        restTemplate.setMessageConverters(Arrays.asList(stringConverter, xmlConverter));
        
        return restTemplate;
    }
}
