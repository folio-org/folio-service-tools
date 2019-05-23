package org.folio.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.http.CaseInsensitiveHeaders;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.folio.okapi.common.XOkapiHeaders;

public class OkapiParams {

  private CaseInsensitiveHeaders headers;

  private String host;
  private int port;
  private String token;
  private String tenant;


  public OkapiParams(Map<String, String> headers) {
    Objects.requireNonNull(headers, "Headers map is null");

    if (headers.get(XOkapiHeaders.URL) == null) {
      throw new IllegalArgumentException(XOkapiHeaders.URL + " header is missing");
    }

    URL url;
    try {
      url = new URL(headers.get(XOkapiHeaders.URL));
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Okapi url header contains invalid value: " + headers.get(XOkapiHeaders.URL));
    }

    this.headers = new CaseInsensitiveHeaders();
    this.headers.addAll(headers);

    this.token = headers.get(XOkapiHeaders.TOKEN);
    this.tenant = headers.get(XOkapiHeaders.TENANT);

    this.host = url.getHost();
    this.port = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();
  }

  public CaseInsensitiveHeaders getHeaders() {
    // make a copy to avoid any modifications to contained headers
    CaseInsensitiveHeaders result = new CaseInsensitiveHeaders();
    result.addAll(headers);
    return result;
  }

  public Map<String, String> getHeadersAsMap() {
    Map<String, String> result = new HashMap<>(headers.size());

    for (Map.Entry<String, String> header : headers) {
      result.put(header.getKey(), header.getValue());
    }

    return result;
  }

  public String getToken() {
    return token;
  }

  public String getTenant() {
    return tenant;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("host", host)
      .append("port", port)
      .append("tenant", tenant)
      .append("token", token)
      .append("headers", headers)
      .build();
  }
}
