package org.folio.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.folio.okapi.common.XOkapiHeaders;

public class OkapiParams {

  private final Map<String, String> headers;

  private final String url;
  private final String host;
  private final int port;
  private final String token;
  private final String tenant;


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

    this.url = url.toString();
    this.headers = new CaseInsensitiveMap<>(headers);

    this.token = headers.get(XOkapiHeaders.TOKEN);
    this.tenant = headers.get(XOkapiHeaders.TENANT);

    this.host = url.getHost();
    this.port = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();
  }

  public Map<String, String> getHeaders() {
    // make a copy to avoid any modifications to contained headers
    return new CaseInsensitiveMap<>(headers);
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

  public String getUrl() {
    return url;
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
